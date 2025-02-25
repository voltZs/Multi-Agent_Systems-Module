package mas.coursework;

import java.util.ArrayList;

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
import mas.coursework_ontology.SupplyChainOntology;
import mas.coursework_ontology.elements.CalendarNotification;
import mas.coursework_ontology.elements.SellPhones;

public class CustomerAgent extends Agent{
	
	private Codec codec = new SLCodec();
	private Ontology ontology = SupplyChainOntology.getInstance();
	
	private AID tickerAgent;
	private AID manufacturerAgent;
	PhoneOrderGenerator generator;
	
	protected void setup(){
		
		getContentManager().registerLanguage(codec);
		getContentManager().registerOntology(ontology);
		Object[] args = this.getArguments();
		generator = new PhoneOrderGenerator((int) args[0]);
		
		DFAgentDescription dfad = new DFAgentDescription();
		dfad.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setType("customer-agent");
		sd.setName(getLocalName() + "-customer-agent");
		dfad.addServices(sd);
		try {
			DFService.register(this, dfad);
		} catch (FIPAException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		addBehaviour(new CalendarListenerBehaviour(this));
	}
	
	public class CalendarListenerBehaviour extends CyclicBehaviour{
		public CalendarListenerBehaviour(Agent a){
			super(a);
		}
		
		@Override
		public void action() {
	//		Listen to new day message
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM); 
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
						if(newDay & !terminate){
//							set tickerAgent
//							we nest this in here because only tickerAgents send calendarNotifications with newDay set true
							if(tickerAgent == null){
								tickerAgent = message.getSender();
							}
							
							SequentialBehaviour makeOrder = new SequentialBehaviour(myAgent);
							makeOrder.addSubBehaviour(new GenerateOrder(myAgent));
							makeOrder.addSubBehaviour(new OrderResponseListener(myAgent));
							addBehaviour(makeOrder);
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
	
	public class GenerateOrder extends OneShotBehaviour{
		
		public GenerateOrder(Agent a){
			super(a);
		}

		@Override
		public void action() {
//			create order and submit to manufacturer agent
			DFAgentDescription dfd = new DFAgentDescription();
			ServiceDescription sd = new ServiceDescription();
			sd.setType("manufacturer-agent");
			dfd.addServices(sd);
			try {
				DFAgentDescription[] manufacturerAgents = DFService.search(myAgent, dfd);
				manufacturerAgent = manufacturerAgents[0].getName();
			} catch (FIPAException e) {
				e.printStackTrace();
			}
			
			// Prepare the action request message
			ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
			msg.addReceiver(manufacturerAgent);
			msg.setLanguage(codec.getName());
			msg.setOntology(ontology.getName());
			SellPhones order = generator.getOrder();
			order.setBuyer(getAID());
			Action request = new Action();
			request.setAction(order);
			request.setActor(manufacturerAgent); 
			try {
				// Let JADE convert from Java objects to string
				getContentManager().fillContent(msg, request); 
				send(msg);
			} catch (CodecException ce) {
				ce.printStackTrace();
			} catch (OntologyException oe) {
				oe.printStackTrace();
			}
		}
	}
	
	public class OrderResponseListener extends Behaviour{
		
		private boolean responseReceived = false;
		
		public OrderResponseListener(Agent a){
			super(a);
		}

		@Override
		public void action() {
//			listen to order confirmation, if received, send dayDone message
			MessageTemplate template = MessageTemplate.or(
					MessageTemplate.MatchPerformative(ACLMessage.AGREE),
					MessageTemplate.MatchPerformative(ACLMessage.REFUSE));
			ACLMessage msg = receive(template);
			if(msg != null){
//				order was accepted/refused, we can end day
				ACLMessage endDayMsg = new ACLMessage(ACLMessage.INFORM);
				endDayMsg.addReceiver(tickerAgent);
				endDayMsg.setLanguage(codec.getName());
				endDayMsg.setOntology(ontology.getName());
//				creating the message content
				CalendarNotification dayNotif = new CalendarNotification();
				dayNotif.setDone(true);
				dayNotif.setNewDay(false);
				try {
					// Let JADE convert from Java objects to string
					getContentManager().fillContent(endDayMsg, dayNotif);
					send(endDayMsg);
				} catch (CodecException ce) {
					ce.printStackTrace();
				} catch (OntologyException oe) {
					oe.printStackTrace();
				}
				responseReceived = true;
			} else {
				block();
			}
		}

		@Override
		public boolean done() {
			return responseReceived;
		}
	}
}
