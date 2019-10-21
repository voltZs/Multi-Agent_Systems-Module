package mas.coursework;

import jade.content.ContentElement;
import jade.content.lang.Codec;
import jade.content.lang.Codec.CodecException;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.Ontology;
import jade.content.onto.OntologyException;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.SequentialBehaviour;
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
						boolean done = notif.isDone();
						if(newDay){
//							set tickerAgent
//							we nest this in here because only tickerAgents send calendarNotifications with newDay set true
							if(tickerAgent == null){
								tickerAgent = message.getSender();
							}
//							add cyclic behaviours
//							add end day listener
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
}
