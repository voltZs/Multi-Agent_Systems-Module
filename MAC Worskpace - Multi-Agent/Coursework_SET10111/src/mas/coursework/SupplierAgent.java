package mas.coursework;

import java.util.ArrayList;

import jade.content.Concept;
import jade.content.ContentElement;
import jade.content.Predicate;
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
import jade.core.behaviours.SequentialBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import mas.coursework_ontology.SupplyChainOntology;
import mas.coursework_ontology.elements.*;

public class SupplierAgent extends Agent{
	
	private Codec codec = new SLCodec();
	private Ontology ontology = SupplyChainOntology.getInstance();
	
	private AID tickerAgent;
	private AID manufacturerAgent;
	private int agentType;
	private int deliveryTime;
	SupplierCatalogue catalogue;
	
	protected void setup(){
//		get arguments passed in when creating agent
		Object[] args = this.getArguments();
		agentType = (int) args[0];
		deliveryTime = (int) ((Math.pow(agentType,2))*(-1));
		doWait(500*agentType);
		catalogue = new SupplierCatalogue(agentType);
		getContentManager().registerLanguage(codec);
		getContentManager().registerOntology(ontology);
		
		DFAgentDescription dfad = new DFAgentDescription();
		dfad.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setType("supplier-agent");
		sd.setName(getLocalName() + "-supplier-agent");
		dfad.addServices(sd);
		try {
			DFService.register(this, dfad);
		} catch (FIPAException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		addBehaviour(new FindAgents(this));
		addBehaviour(new CalendarListenerBehaviour(this));
	}
	
	public class FindAgents extends Behaviour{
		
		public FindAgents(Agent a){
			super(a);
		}
		
		@Override
		public void action() {
			DFAgentDescription dfd1 = new DFAgentDescription();
			DFAgentDescription dfd2 = new DFAgentDescription();
			ServiceDescription sd1 = new ServiceDescription();
			ServiceDescription sd2 = new ServiceDescription();
			sd1.setType("ticker-agent");
			sd2.setType("manufacturer-agent");
			dfd1.addServices(sd1);
			dfd2.addServices(sd2);
			try {
				tickerAgent = DFService.search(myAgent, dfd1)[0].getName();
				manufacturerAgent = DFService.search(myAgent, dfd2)[0].getName();
			} catch (FIPAException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		@Override
		public boolean done() {
			return tickerAgent != null && manufacturerAgent != null;
		}
		
	}
	
	public class CalendarListenerBehaviour extends CyclicBehaviour{
		public CalendarListenerBehaviour(Agent a){
			super(a);
		}
		
		@Override
		public void action() {
	//		Listen to new day message
			MessageTemplate mt = MessageTemplate.and(
					MessageTemplate.MatchPerformative(ACLMessage.INFORM),
					MessageTemplate.MatchSender(tickerAgent)); 
			ACLMessage message = receive(mt);
	//		if message received, 
			if(message != null){
				try {
					ContentElement ce = null;
					ce = getContentManager().extractContent(message);
					if (ce instanceof CalendarNotification) {
						CalendarNotification notif = (CalendarNotification) ce;
						boolean newDay = notif.isNewDay();
						boolean terminate = notif.getTerminate();
						if(newDay && !terminate){
//							add cyclic behaviours
							ArrayList<CyclicBehaviour> cyclicBehaviours = new ArrayList<>();
							CyclicBehaviour stockCheckingListener = new StockCheckingListener(myAgent);
							addBehaviour(stockCheckingListener);
							CyclicBehaviour orderProcessingListener = new OrderProcessingListener(myAgent);
							addBehaviour(orderProcessingListener);
							cyclicBehaviours.add(stockCheckingListener);
							cyclicBehaviours.add(orderProcessingListener);
							addBehaviour(new EndDayListener(myAgent, cyclicBehaviours));
						} else{
//							not a new day and done is true -> simulation must be over
							System.out.println("Deleting agent: " + getAID().getLocalName());
							try {
								DFService.deregister(myAgent);
							} catch (FIPAException e) {
								e.printStackTrace();
							}
							myAgent.doDelete();
						}
					}
				}
				catch (CodecException ce) {
					ce.printStackTrace();
				}
				catch (OntologyException oe) {
					oe.printStackTrace();
				}
			}
			else{
				block();
			}
		}
	}
	
	public class StockCheckingListener extends CyclicBehaviour{
		public StockCheckingListener(Agent a) {
			super(a);
		}

		@Override
		public void action() {
//			listen to queries about stock - HasInStock
//			if in stock - reply CONFIRM
//			otherwise DISCONFIRM
//			listen to phone orders from customers, end when all customers have sent out an order

			//This behaviour should only respond to REQUEST messages
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.QUERY_IF); 
			ACLMessage msg = receive(mt);
			if(msg != null){
				try {
					ContentElement ce = null;
					// Let JADE convert from String to Java objects
					// Output will be a ContentElements
					ce = getContentManager().extractContent(msg);
					if(ce instanceof HasInStock) {
						HasInStock hasInStock = (HasInStock) ce;
						Component queriedItem = (Component) hasInStock.getItem();
						ACLMessage reply = msg.createReply();
						
						if (queriedItem != null){
							int result = catalogue.checkIfSold(queriedItem.getType(), queriedItem.getIdentifier());
							if(result != 0){
								reply.setPerformative(ACLMessage.CONFIRM);
								HasInStock confirmation = hasInStock;
//								set self as owner so the receiver knows which seller this is about directly from the content field 
								confirmation.setOwner(getAID());
								confirmation.setPrice(result);
								confirmation.setDeliveryTime(deliveryTime);
								try {
									 // Let JADE convert from Java objects to string
									 getContentManager().fillContent(reply, confirmation);
								}catch (CodecException cExc) {
									cExc.printStackTrace();
								} catch (OntologyException oExc) {
									oExc.printStackTrace();
								} 
							} else {
//								this item is not in catalogue
								reply.setPerformative(ACLMessage.DISCONFIRM);
							}
						} else {
//							also send disconfirm message when the queried item didn't parse as a Component
							reply.setPerformative(ACLMessage.DISCONFIRM);
						}
						send(reply);
					}	
				} catch (CodecException coExc) {
					coExc.printStackTrace();
				} catch (OntologyException onExc) {
					onExc.printStackTrace();
				}
			} else {
				block();
			}
		}
	}
	
	public class OrderProcessingListener extends CyclicBehaviour{

		public OrderProcessingListener(Agent a) {
			super(a);
		}

		@Override
		public void action() {
//			listen for component orders
//			when received, check if component is in stock and ACCEPT / REFUSE

			//This behaviour should only respond to REQUEST messages
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST); 
			ACLMessage msg = receive(mt);
			if(msg != null){
				try {
					ContentElement ce = null;
					// Let JADE convert from String to Java objects
					// Output will be a ContentElements
					ce = getContentManager().extractContent(msg);
					if (ce instanceof Action) {
						Concept action = ((Action) ce).getAction();
						if(action instanceof SellComponents){
							ACLMessage reply = msg.createReply();
							reply.setPerformative(ACLMessage.AGREE);
							SellComponents order = (SellComponents) action;
							order.setDeliveryTime(deliveryTime);
							ArrayList<OrderPair> orderList = (ArrayList<OrderPair>) order.getOrderPairs();
							boolean allInStock = true;
							for (OrderPair pair : orderList){
								Component orderedComponent = (Component) pair.getOrderedItem();
								if(catalogue.checkIfSold(orderedComponent.getType(), orderedComponent.getIdentifier()) <= 0){
									allInStock = false;
									reply.setPerformative(ACLMessage.REFUSE);
									break;
								}
							}
							if(allInStock){
								Action request = new Action();
								request.setAction(order);
								request.setActor(getAID()); 
								try {
									// Let JADE convert from Java objects to string
									getContentManager().fillContent(reply, request); 
								} catch (CodecException cExc) {
									cExc.printStackTrace();
								} catch (OntologyException oExc) {
									oExc.printStackTrace();
								}
							}
							send(reply);
						}
					}	
				} catch (CodecException coExc) {
					coExc.printStackTrace();
				} catch (OntologyException onExc) {
					onExc.printStackTrace();
				}
			} else {
				block();
			}
		}
	}
	
	public class EndDayListener extends CyclicBehaviour{
		
		ArrayList<CyclicBehaviour> toBeRemoved;
		
		public EndDayListener(Agent a, ArrayList<CyclicBehaviour> toDelete) {
			super(a);
			this.toBeRemoved = toDelete;
		}

		@Override
		public void action() {
//			listen to done message from manufacturer
			MessageTemplate mt = MessageTemplate.and(
					MessageTemplate.MatchPerformative(ACLMessage.INFORM),
					MessageTemplate.MatchSender(manufacturerAgent)); 
			ACLMessage message = receive(mt);
			if(message != null){
				try {
					ContentElement ce = null;
					ce = getContentManager().extractContent(message);
					if (ce instanceof CalendarNotification) {
						CalendarNotification notif = (CalendarNotification) ce;
						boolean done = notif.isDone();
						if(done){
//							if received done, send done message to tickerAgent
							ACLMessage endDayMsg = new ACLMessage(ACLMessage.INFORM);
							endDayMsg.addReceiver(tickerAgent);
							endDayMsg.setLanguage(codec.getName());
							endDayMsg.setOntology(ontology.getName());
//							creating the message content
							CalendarNotification dayNotif = new CalendarNotification();
							dayNotif.setDone(true);
							dayNotif.setNewDay(false);
							try {
								// Let JADE convert from Java objects to string
								getContentManager().fillContent(endDayMsg, dayNotif);
								send(endDayMsg);
							} catch (CodecException ce1) {
								ce1.printStackTrace();
							} catch (OntologyException oe) {
								oe.printStackTrace();
							}

//							remove cyclicBehaviours including this one
							for(CyclicBehaviour behaviour : toBeRemoved){
								myAgent.removeBehaviour(behaviour);
							}
							
							System.out.println(myAgent.getAID().getLocalName() + " ending day!");
							myAgent.removeBehaviour(this);
						}
					}
				}
				catch (CodecException ce) {
					ce.printStackTrace();
				}
				catch (OntologyException oe) {
					oe.printStackTrace();
				}
			} else {
				block();
			}
		}
	}
	
}
