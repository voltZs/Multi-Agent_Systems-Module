package mas.coursework;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import jade.content.Concept;
import jade.content.ContentElement;
import jade.content.lang.Codec;
import jade.content.lang.Codec.CodecException;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.Ontology;
import jade.content.onto.OntologyException;
import jade.content.onto.basic.Action;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.SequentialBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import mas.coursework.TickerAgent.ManageDaysBehaviour;
import mas.coursework_ontology.SupplyChainOntology;
import mas.coursework_ontology.elements.Battery;
import mas.coursework_ontology.elements.CalendarNotification;
import mas.coursework_ontology.elements.Component;
import mas.coursework_ontology.elements.HasInStock;
import mas.coursework_ontology.elements.Item;
import mas.coursework_ontology.elements.OrderPair;
import mas.coursework_ontology.elements.Phone;
import mas.coursework_ontology.elements.Screen;
import mas.coursework_ontology.elements.SellComponents;
import mas.coursework_ontology.elements.SellPhones;

public class ManufacturerAgent extends Agent {

	private Codec codec = new SLCodec();
	private Ontology ontology = SupplyChainOntology.getInstance();

	private AID tickerAgent;
	private ArrayList<AID> suppliers;
	private ArrayList<AID> customers;
	
	private int componentQueriesToday;
	private int componentOrdersToday;
	
	private ProfitBrain profitBrain = new ProfitBrain();
	private Warehouse warehouse;
	private PhoneOrdersManager phoneOrdersMngr = new PhoneOrdersManager();
	
	private int evaluatedAttr;
	private int run;

	protected void setup() {
		getContentManager().registerLanguage(codec);
		getContentManager().registerOntology(ontology);
		Object[] args = this.getArguments();
		warehouse = new Warehouse((int) args[0]);
		evaluatedAttr = (int) args[1];
		run = (int) args[2];
		
		DFAgentDescription dfad = new DFAgentDescription();
		dfad.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setType("manufacturer-agent");
		sd.setName(getLocalName() + "-manufacturer-agent");
		dfad.addServices(sd);
		try {
			DFService.register(this, dfad);
		} catch (FIPAException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
//		Screen someScreen = new Screen();
//		someScreen.setIdentifier("7\"");
//		someScreen.setType("screen");
//		Battery someBattery = new Battery();
//		someBattery.setIdentifier("2000mAh");
//		someBattery.setType("battery");
//		warehouse.addToWarehouse(someScreen);
//		System.out.println(warehouse.toString());
//		warehouse.incrementNewDay();
//		warehouse.addToWarehouse(someBattery);
//		System.out.println(warehouse.toString());
		

		addBehaviour(new CalendarListenerBehaviour(this));

	}

	public class CalendarListenerBehaviour extends CyclicBehaviour {

		public CalendarListenerBehaviour(Agent a) {
			super(a);
		}

		@Override
		public void action() {
			// Listen to new day message
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
			
			ACLMessage message = receive(mt);
			// if message received,
			if (message != null) {
				try {
					ContentElement ce = null;
					ce = getContentManager().extractContent(message);
					if (ce instanceof CalendarNotification) {
						CalendarNotification notif = (CalendarNotification) ce;
						boolean newDay = notif.isNewDay();
						boolean terminate = notif.getTerminate();
						if (newDay && !terminate) {
							// set tickerAgent
							// we nest this in here because only tickerAgents
							// send calendarNotifications with newDay set true
							if (tickerAgent == null) {
								tickerAgent = message.getSender();
							}
							// set up customer agentss
							customers = new ArrayList<>();
							DFAgentDescription dfd = new DFAgentDescription();
							ServiceDescription sd = new ServiceDescription();
							sd.setType("customer-agent");
							dfd.addServices(sd);
							try {
								DFAgentDescription[] customerAgents = DFService.search(myAgent, dfd);
								for (DFAgentDescription ad : customerAgents) {
									customers.add(ad.getName());
								}
							} catch (FIPAException e) {
								e.printStackTrace();
							}

							SequentialBehaviour dailyTasks = new SequentialBehaviour();
							dailyTasks.addSubBehaviour(new PhoneOrdersListener(myAgent));
							dailyTasks.addSubBehaviour(new QueryStocks(myAgent));
							dailyTasks.addSubBehaviour(new StockResponseListener(myAgent));
							dailyTasks.addSubBehaviour(new RespondToPhoneOrders(myAgent));
							dailyTasks.addSubBehaviour(new OrderComponents(myAgent));
							dailyTasks.addSubBehaviour(new OrderConfirmationListener(myAgent));
							dailyTasks.addSubBehaviour(new AssembleAndShip(myAgent));
							dailyTasks.addSubBehaviour(new EndDay(myAgent));
							myAgent.addBehaviour(dailyTasks);
						} else {
							// not a new day and terminate is true -> simulation must
							// be over
							System.out.println("Deleting agent: " + getAID().getLocalName());
							SimulationLogger.appendLine(evaluatedAttr, run, profitBrain.getAccumulatedProfit());
							try {
								DFService.deregister(myAgent);
							} catch (FIPAException e) {
								e.printStackTrace();
							}
							myAgent.doDelete();
						}
					}
				} catch (CodecException ce) {
					ce.printStackTrace();
				} catch (OntologyException oe) {
					oe.printStackTrace();
				}
			} else {
				block();
			}
		}
	}

	public class PhoneOrdersListener extends Behaviour {
		private int orderCount = 0;

		public PhoneOrdersListener(Agent a) {
			super(a);
		}

		@Override
		public void action() {
			// listen to phone orders from customers, end when all customers
			// have sent out an order

			// This behaviour should only respond to REQUEST messages
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
			ACLMessage msg = receive(mt);
			if (msg != null) {
				try {
					ContentElement ce = null;
					// System.out.println(msg.getContent());
					// Let JADE convert from String to Java objects
					// Output will be a ContentElements
					ce = getContentManager().extractContent(msg);
					if (ce instanceof Action) {
						Concept action = ((Action) ce).getAction();
						if (action instanceof SellPhones) {
							SellPhones order = (SellPhones) action;
							phoneOrdersMngr.phoneOrderReceived(order);
							System.out.println("Received phone order");
//							System.out.println(phoneOrdersMngr.orderToString(order));
							orderCount++;
						}
					}
				} catch (CodecException ce) {
					ce.printStackTrace();
				} catch (OntologyException oe) {
					oe.printStackTrace();
				}

				System.out.println("----------------");
			} else {
				block();
			}

		}

		@Override
		public int onEnd() {
//			System.out.println("PENDING ORDERS -----------");
//			System.out.println(phoneOrdersMngr.ordersToString());
			return super.onEnd();
		}

		@Override
		public boolean done() {
			return orderCount == customers.size();
		}
	}

	public class QueryStocks extends OneShotBehaviour {

		public QueryStocks(Agent a) {
			super(a);
		}

		@Override
		public void action() {
			// find all suppliers
			suppliers = new ArrayList<>();
			DFAgentDescription dfd = new DFAgentDescription();
			ServiceDescription sd = new ServiceDescription();
			sd.setType("supplier-agent");
			dfd.addServices(sd);
			try {
				DFAgentDescription[] foundAgents = DFService.search(myAgent, dfd);
				for (DFAgentDescription ad : foundAgents) {
					suppliers.add(ad.getName());
				}
			} catch (FIPAException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			for (AID supplier : suppliers) {
				ACLMessage msg = new ACLMessage(ACLMessage.QUERY_IF);
				msg.setLanguage(codec.getName());
				msg.setOntology(ontology.getName());	
				HasInStock hasInStock = new HasInStock();	
				msg.addReceiver(supplier);
				
//				Change this based on what needs queried
				for(SellPhones phoneOrder : phoneOrdersMngr.getNewOrders()){
					for (Component comp : phoneOrdersMngr.getPhoneOrderComponents(phoneOrder)){
						hasInStock.setItem((Item)comp);
						hasInStock.setOwner(supplier); // these are arbitrary but need to be filled in as predicates don't allow optional slots?
						try {
							getContentManager().fillContent(msg, hasInStock);
						} catch(CodecException conExc){
							conExc.printStackTrace();
						} catch (OntologyException ontExc) {
							ontExc.printStackTrace();
						}
						send(msg);
						componentQueriesToday++;
					}
				}
			}
		}
	}

	public class StockResponseListener extends Behaviour {
		int receivedStockResponses = 0;

		public StockResponseListener(Agent a) {
			super(a);
//			supplierComponentsDaily.clear();
		}

		@Override
		public void action() {
			//This behaviour should only respond to REQUEST messages
			MessageTemplate mt = MessageTemplate.or(MessageTemplate.MatchPerformative(ACLMessage.CONFIRM),
													MessageTemplate.MatchPerformative(ACLMessage.DISCONFIRM)); 
			ACLMessage msg = receive(mt);
			if(msg != null){
				if(msg.getPerformative() == ACLMessage.CONFIRM){
					try {
						ContentElement ce = null;
						// Let JADE convert from String to Java objects
						// Output will be a ContentElements
						ce = getContentManager().extractContent(msg);
						if(ce instanceof HasInStock) {
							HasInStock hasInStock = (HasInStock) ce;
							Component queriedItem = (Component) hasInStock.getItem();
							if (queriedItem != null) {
								profitBrain.updateComponentMarket(hasInStock);
							}
						}	
					} catch (CodecException coExc) {
						coExc.printStackTrace();
					} catch (OntologyException onExc) {
						onExc.printStackTrace();
					}
				} else {
//					LOGIC FOR HANDLING ITEMS THAT HAVE BEEN DISCONFIRMED TO BE IN STOCK
				}
				receivedStockResponses++;
			} else {
				block();
			}
		}

		@Override
		public void reset() {
			super.reset();
			receivedStockResponses = 0;
			componentQueriesToday = 0;
		}
		
		@Override
		public int onEnd() {
			reset();
//			System.out.println(profitBrain.compMarketToString());
			return super.onEnd();
		}

		@Override
		public boolean done() {
			return receivedStockResponses == componentQueriesToday;
		}
	}
	
	public class RespondToPhoneOrders extends OneShotBehaviour {

		public RespondToPhoneOrders(Agent a) {
			super(a);
		}

		@Override
		public void action() {
//			DECIDE ON PLAN FOR THE DAY 
			ArrayList<SellPhones> acceptedOrders = profitBrain.plan(phoneOrdersMngr, warehouse);
			
			for(SellPhones orderedToday : phoneOrdersMngr.getNewOrders()){
				ACLMessage msg = null; 
				if(acceptedOrders.indexOf(orderedToday) == -1){
//					if the order was not accepted
					msg = new ACLMessage(ACLMessage.REFUSE);					
				} else {
					msg = new ACLMessage(ACLMessage.AGREE);
//					also after decision made add successful orders to pendingphoneOrders
				}
				msg.addReceiver(orderedToday.getBuyer());
				send(msg);
			}
			
//			And  empty todays orders as we are done with them
			phoneOrdersMngr.clearTodaysOrders();
		}
	}

	public class OrderComponents extends OneShotBehaviour {
		
		public OrderComponents(Agent a) {
			super(a);
		}
		
		@Override
		public void action() {
			componentOrdersToday = 0;
			
//			DECIDE WHAT TO ORDER
			ArrayList<SellComponents> todaysOrders = profitBrain.componentsToOrder();
//			System.out.println("Submitted "+ todaysOrders.size() +" component orders");
			
			for(SellComponents order : todaysOrders){
				AID supplier = order.getSeller();
				order.setBuyer(myAgent.getAID());
				ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
				msg.setLanguage(codec.getName());
				msg.setOntology(ontology.getName());
				msg.addReceiver(supplier);
				
				Action request = new Action();
				request.setAction(order);
				request.setActor(supplier); 
				try {
					getContentManager().fillContent(msg, request);
				} catch (CodecException conExc) {
					conExc.printStackTrace();
				} catch (OntologyException ontExc) {
					ontExc.printStackTrace();
				}
				send(msg);
				componentOrdersToday++;
			}	
		}
	}

	public class OrderConfirmationListener extends Behaviour {
		int receivedOrderResponses = 0;

		public OrderConfirmationListener(Agent a) {
			super(a);
		}

		@Override
		public void action() {
			//This behaviour should only respond to REQUEST messages
			MessageTemplate mt = MessageTemplate.or(MessageTemplate.MatchPerformative(ACLMessage.AGREE),
													MessageTemplate.MatchPerformative(ACLMessage.REFUSE)); 
			ACLMessage msg = receive(mt);
			if(msg != null){
				if(msg.getPerformative() == ACLMessage.AGREE){
					try {
						ContentElement ce = null;
						// Let JADE convert from String to Java objects
						// Output will be a ContentElements
						ce = getContentManager().extractContent(msg);
						if (ce instanceof Action) {
							Concept action = ((Action) ce).getAction();
							if(action instanceof SellComponents){
								SellComponents confirmedOrder = (SellComponents) action;
//								System.out.println("Order confirmation received.");
								warehouse.receiveComponents(confirmedOrder);
							}
						}
					} catch (CodecException coExc) {
						coExc.printStackTrace();
					} catch (OntologyException onExc) {
						onExc.printStackTrace();
					}
				} else {
//					LOGIC FOR HANDLING ITEMS THAT HAVE BEEN DISCONFIRMED TO BE IN STOCK
				}
				receivedOrderResponses++;
			} else {
				
				System.out.println("BBlocking orderconfirmationlistener");
				block();
			}
		}

		@Override
		public void reset() {
			super.reset();
			receivedOrderResponses = 0;
			componentOrdersToday = 0;
		}
		
		@Override
		public int onEnd() {
			reset();
			return super.onEnd();
		}

		@Override
		public boolean done() {
			// TODO Auto-generated method stub
			return receivedOrderResponses == componentOrdersToday ;
		}

	}
	
	public class AssembleAndShip extends OneShotBehaviour{
		
		public AssembleAndShip(Agent a) {
			super(a);
		}

		@Override
		public void action() {
			
			HashMap<SellPhones, Integer> toBeAssembled = profitBrain.levelsToAssemble();
//			System.out.println("Today assembling: ");
			for(SellPhones phoneOrder : toBeAssembled.keySet()){
				phoneOrdersMngr.assemble(phoneOrder, toBeAssembled.get(phoneOrder));
//				System.out.println("\tamount: "+ toBeAssembled.get(phoneOrder) + " for " + phoneOrder);
			}
			
			ArrayList<SellPhones> toBeShipped = profitBrain.ordersToShip();
//			System.out.println("Today shipping: " + toBeShipped.size() + " phone orders");
			for(SellPhones shippedOrder : toBeShipped){
				warehouse.ship(shippedOrder);
				phoneOrdersMngr.shipOrder(shippedOrder);
			}
		}
	}

	public class EndDay extends OneShotBehaviour {

		public EndDay(Agent a) {
			super(a);
		}

		@Override
		public void action() {
//			System.out.println(warehouse.toString());
			profitBrain.todaysProfit(warehouse, phoneOrdersMngr);
			
			ArrayList<Double> dailyProfits = profitBrain.getDailyProfits();
//			System.out.println(dailyProfits);
			System.out.println("Total profits so far: "+ profitBrain.getAccumulatedProfit());
//			Increment values for the next day
			warehouse.incrementNewDay();
			phoneOrdersMngr.incrementNewDay();
			profitBrain.incrementNewDay();
			
			ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
			msg.addReceiver(tickerAgent);
			// send a message to each supplier as well
			for (AID supplier : suppliers) {
				msg.addReceiver(supplier);
			}
			msg.setLanguage(codec.getName());
			msg.setOntology(ontology.getName());
			// creating the message content
			CalendarNotification dayNotif = new CalendarNotification();
			dayNotif.setDone(true);
			dayNotif.setNewDay(false);
			try {
				// Let JADE convert from Java objects to string
				getContentManager().fillContent(msg, dayNotif);
				send(msg);
			} catch (CodecException ce) {
				ce.printStackTrace();
			} catch (OntologyException oe) {
				oe.printStackTrace();
			}
			System.out.println(myAgent.getAID().getLocalName() + " ending day!");
		}
	}
}
