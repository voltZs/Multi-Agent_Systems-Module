package mas.coursework;

import java.util.ArrayList;
import java.util.HashMap;

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
import mas.coursework_ontology.elements.CalendarNotification;
import mas.coursework_ontology.elements.Component;
import mas.coursework_ontology.elements.HasInStock;
import mas.coursework_ontology.elements.Item;
import mas.coursework_ontology.elements.Phone;
import mas.coursework_ontology.elements.SellPhones;

public class ManufacturerAgent extends Agent {

	private Codec codec = new SLCodec();
	private Ontology ontology = SupplyChainOntology.getInstance();

	private AID tickerAgent;
	private ArrayList<AID> suppliers;
	private ArrayList<AID> customers;
	
	private int componentQueriesToday;
	private int componentOrdersToday;
	
	private HashMap<String, HashMap> supplierComponentsDaily = new HashMap<>();

	protected void setup() {
		getContentManager().registerLanguage(codec);
		getContentManager().registerOntology(ontology);
		String[] args = (String[]) this.getArguments();

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
						boolean done = notif.isDone();
						if (newDay) {
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
							dailyTasks.addSubBehaviour(new OrderComponents(myAgent));
							dailyTasks.addSubBehaviour(new OrderConfirmationListener(myAgent));
							dailyTasks.addSubBehaviour(new Manufacture(myAgent));
							dailyTasks.addSubBehaviour(new EndDay(myAgent));
							myAgent.addBehaviour(dailyTasks);
						} else if (done) {
							// not a new day and done is true -> simulation must
							// be over
							System.out.println("Deleting agent: " + getAID().getLocalName());
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
							Phone phone = order.getPhone();

							// logic for accepting or rejecting order
							// logic for accepting or rejecting order
							// logic for accepting or rejecting order
							// logic for accepting or rejecting order
							// logic for accepting or rejecting order
							// logic for accepting or rejecting order
							// and based on that AGREE or REFUSE

							ACLMessage reply = msg.createReply();
							reply.setPerformative(ACLMessage.AGREE);
							myAgent.send(reply);
							orderCount++;
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

		@Override
		public boolean done() {
			return orderCount == customers.size();
		}
	}

	// LOGIC FOR FIGURING OUT WHAT NEEDS TO BE QUERIED AND ORDERED
	// COMPONENT-WISE

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
			
			System.out.println("I have this many suppliers: " + suppliers.size());
			for(AID supplier : suppliers){
				System.out.print(supplier.getLocalName()+", ");
				System.out.println();
			}
			
			for (AID supplier : suppliers) {
				ACLMessage msg = new ACLMessage(ACLMessage.QUERY_IF);
				msg.setLanguage(codec.getName());
				msg.setOntology(ontology.getName());	
				HasInStock hasInStock = new HasInStock();	
				msg.addReceiver(supplier);
				
//				Change this based on what needs queried
				Component component = new Component();
				component.setType("screen");
				component.setIdentifier("5\"");
				hasInStock.setItem((Item)component);
				hasInStock.setOwner(supplier); // these are arbitrary but need to be filled in as predicates don't allow optional slots
				hasInStock.setPrice(0);
				
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

	public class StockResponseListener extends Behaviour {
		int receivedStockResponses = 0;

		public StockResponseListener(Agent a) {
			super(a);
			supplierComponentsDaily.clear();
		}

		@Override
		public void action() {
			//This behaviour should only respond to REQUEST messages
			MessageTemplate mt = MessageTemplate.or(MessageTemplate.MatchPerformative(ACLMessage.CONFIRM),
													MessageTemplate.MatchPerformative(ACLMessage.DISCONFIRM)); 
			ACLMessage msg = receive(mt);
			if(msg != null){
				System.out.println("STOCKRESPONSE: " + msg.getContent());
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
	//							LOGIC FOR HANDLING ITEMS THAT HAVE BEEN CONFIRMED TO BE IN STOCK
								System.out.println(" queried item: " + queriedItem.getType() + " identif: " + queriedItem.getIdentifier() + "Sold for : " + hasInStock.getPrice());
								if(supplierComponentsDaily.get(queriedItem.getType()) != null &)
									THIS IS A SHIT APPROACH AHHHHHHHHHH
								HashMap<String, ArrayList> innerHashmap = new HashMap<>();
								ArrayList<HasInStock> availableStock
								innerHashmap.put(queriedItem.getIdentifier(), );
								supplierComponentsDaily.put(queriedItem.getType(), innerHashmap);
							}
						}	
					} catch (CodecException coExc) {
						coExc.printStackTrace();
					} catch (OntologyException onExc) {
						onExc.printStackTrace();
					}
				} else {
//					LOGIC FOR HANDLING ITEMS THAT HAVE BEEN DISCONFIRMED TO BE IN STOCK
					System.out.println(" queried item not sold");
				}
				receivedStockResponses++;
				System.out.println("StockResponses received: " + receivedStockResponses);
				System.out.println("componentQueriesToday: " + componentQueriesToday);
			} else {
				System.out.println("Blocking StockresponnseListener");
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
			return super.onEnd();
		}

		@Override
		public boolean done() {
			return receivedStockResponses == componentQueriesToday;
		}
	}
	
	

	public class OrderComponents extends OneShotBehaviour {
		
		public OrderComponents(Agent a) {
			super(a);
		}
		
		@Override
		public void action() {
			componentOrdersToday = 0;
			
//			create orders here based on the previously queried components
			
			System.out.println("I have this many suppliers: " + suppliers.size());
			for(AID supplier : suppliers){
				System.out.print(supplier.getLocalName()+", ");
				System.out.println();
			}
			
			for (AID supplier : suppliers) {
				ACLMessage msg = new ACLMessage(ACLMessage.QUERY_IF);
				msg.setLanguage(codec.getName());
				msg.setOntology(ontology.getName());	
				HasInStock hasInStock = new HasInStock();	
				msg.addReceiver(supplier);
				
//				Change this based on what needs queried
				Component component = new Component();
				component.setType("screen");
				component.setIdentifier("5\"");
				hasInStock.setItem((Item)component);
				hasInStock.setOwner(supplier); // these are arbitrary but need to be filled in as predicates don't allow optional slots
				hasInStock.setPrice(0);
				
				try {
					getContentManager().fillContent(msg, hasInStock);
				} catch(CodecException conExc){
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

		public OrderConfirmationListener(Agent a) {
			super(a);
		}

		@Override
		public void action() {
			// TODO Auto-generated method stub

		}

		@Override
		public boolean done() {
			// TODO Auto-generated method stub
			return true;
		}

	}

	public class Manufacture extends OneShotBehaviour {

		public Manufacture(Agent a) {
			super(a);
		}

		@Override
		public void action() {
			// TODO Auto-generated method stub

		}

	}

	public class EndDay extends OneShotBehaviour {

		public EndDay(Agent a) {
			super(a);
		}

		@Override
		public void action() {
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
