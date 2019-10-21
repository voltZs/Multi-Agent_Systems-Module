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

public class ManufacturerAgent extends Agent {
	
	private Codec codec = new SLCodec();
	private Ontology ontology = SupplyChainOntology.getInstance();
	
	private AID tickerAgent;
	private ArrayList<AID> suppliers;
	
	protected void setup(){
		getContentManager().registerLanguage(codec);
		getContentManager().registerOntology(ontology);
		String[] args = (String[])this.getArguments();
		
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
	
	public class CalendarListenerBehaviour extends CyclicBehaviour{
		
		public CalendarListenerBehaviour(Agent a){
			super(a);
		}
		
		@Override
		public void action() {
//			Listen to new day message
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM); 
			ACLMessage message = receive(mt);
//			if message received, 
			if(message != null){
				try {
					ContentElement ce = null;
					ce = getContentManager().extractContent(message);
					if (ce instanceof CalendarNotification) {
						CalendarNotification notif = (CalendarNotification) ce;
						boolean newDay = notif.isNewDay();
						boolean done = notif.isDone();
						if(newDay){
//							set tickerAgent
//							we nest this in here because only tickerAgents send calendarNotifications with newDay set true
							if(tickerAgent == null){
								tickerAgent = message.getSender();
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
						} else if(done){
//							not a new day and done is true -> simulation must be over
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
	
	public class PhoneOrdersListener extends Behaviour{
		public PhoneOrdersListener(Agent a){
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
	
	public class QueryStocks extends OneShotBehaviour{
		
		public QueryStocks(Agent a){
			super(a);
		}

		@Override
		public void action() {
			// find all suppliers 
			DFAgentDescription dfd = new DFAgentDescription();
			ServiceDescription sd = new ServiceDescription();
			sd.setType("supplier");
			dfd.addServices(sd);
			try {
				DFAgentDescription[] supplierAgents = DFService.search(myAgent, dfd);
				for(DFAgentDescription ad : supplierAgents){
					suppliers.add(ad.getName());
				}
			} catch (FIPAException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			// send queries to all suppliers
		}
	}
	
	public class StockResponseListener extends Behaviour{
		
		public StockResponseListener(Agent a){
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
	
	public class OrderComponents extends OneShotBehaviour{
		
		public OrderComponents(Agent a){
			super(a);
		}

		@Override
		public void action() {
			// TODO Auto-generated method stub
			
		}
	}
	
	public class OrderConfirmationListener extends Behaviour{
		
		public OrderConfirmationListener(Agent a){
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
	
	public class Manufacture extends OneShotBehaviour{
		
		public Manufacture(Agent a){
			super(a);
		}
		
		@Override
		public void action() {
			// TODO Auto-generated method stub
			
		}
		
	}
	
	public class EndDay extends OneShotBehaviour{
		
		public EndDay (Agent a){
			super(a);
		}
		
		@Override
		public void action() {
			ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
			msg.addReceiver(tickerAgent);
			//send a message to each supplier as well
//			for(AID supplier : suppliers) {
//				msg.addReceiver(supplier);
//			}
			msg.setLanguage(codec.getName());
			msg.setOntology(ontology.getName());
//			creating the message content
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
