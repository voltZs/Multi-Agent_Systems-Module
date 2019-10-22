package mas.coursework;

import java.util.ArrayList;

import jade.content.ContentElement;
import jade.content.lang.Codec;
import jade.content.lang.Codec.CodecException;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.Ontology;
import jade.content.onto.OntologyException;
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
import mas.coursework.ManufacturerAgent.EndDay;
import mas.coursework.ManufacturerAgent.Manufacture;
import mas.coursework.ManufacturerAgent.OrderComponents;
import mas.coursework.ManufacturerAgent.OrderConfirmationListener;
import mas.coursework.ManufacturerAgent.PhoneOrdersListener;
import mas.coursework.ManufacturerAgent.QueryStocks;
import mas.coursework.ManufacturerAgent.StockResponseListener;
import mas.coursework_ontology.SupplyChainOntology;
import mas.coursework_ontology.elements.CalendarNotification;

public class SupplierAgent extends Agent{
	
	private Codec codec = new SLCodec();
	private Ontology ontology = SupplyChainOntology.getInstance();
	
	private AID tickerAgent;
	private AID manufacturerAgent;
	
	protected void setup(){
		getContentManager().registerLanguage(codec);
		getContentManager().registerOntology(ontology);
		String[] args = (String[])this.getArguments();
		
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
						boolean done = notif.isDone();
						boolean terminate = notif.getTerminate();
						if(newDay){
//							add cyclic behaviours
							ArrayList<CyclicBehaviour> cyclicBehaviours = new ArrayList<>();
							CyclicBehaviour stockCheckingListener = new StockCheckingListener(myAgent);
							addBehaviour(stockCheckingListener);
							CyclicBehaviour orderProcessingListener = new OrderProcessingListener(myAgent);
							addBehaviour(orderProcessingListener);
							cyclicBehaviours.add(stockCheckingListener);
							cyclicBehaviours.add(orderProcessingListener);
							addBehaviour(new EndDayListener(myAgent, cyclicBehaviours));
						} 
						else if(terminate){
//							not a new day and done is true -> simulation must be over
							System.out.println("Deleting agent: " + getAID().getLocalName());
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
