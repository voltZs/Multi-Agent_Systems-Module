package mas.coursework;

import java.util.ArrayList;

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
import mas.coursework_ontology.SupplyChainOntology;
import mas.coursework_ontology.elements.CalendarNotification;

public class TickerAgent extends Agent{
	private Codec codec = new SLCodec();
	private Ontology ontology = SupplyChainOntology.getInstance();
	
	private int dayCount = 0;
	private int dayMAX = 100;
	private ArrayList<AID> simulationAgents = new ArrayList<>();
	
	protected void setup(){
		getContentManager().registerLanguage(codec);
		getContentManager().registerOntology(ontology);
		String[] args = (String[])this.getArguments();
		
		DFAgentDescription dfad = new DFAgentDescription();
		dfad.setName(getAID());
		System.out.println(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setType("ticker-agent");
		sd.setName(getLocalName() + "-ticker-agent");
		dfad.addServices(sd);
		try {
			DFService.register(this, dfad);
		} catch (FIPAException e) {
			e.printStackTrace();
		}
		
		//wait 4 seconds so other agents can discover the tickerAgent
		doWait(4000);
		addBehaviour(new ManageDaysBehaviour(this));
	}
	
	public class ManageDaysBehaviour extends Behaviour{
		
		private int step = 0;
		private int receivedDoneMessages = 0;
		
		public ManageDaysBehaviour(Agent a){
			super(a);
		}
		
		@Override
		public void action() {
			switch(step){
			
			case 0:
				//find all agents thru yellow pages
				DFAgentDescription dfd1 = new DFAgentDescription();
				ServiceDescription sd1 = new ServiceDescription();
				sd1.setType("supplier-agent");
				dfd1.addServices(sd1);
				
				DFAgentDescription dfd2 = new DFAgentDescription();
				ServiceDescription sd2 = new ServiceDescription();
				sd2.setType("manufacturer-agent");
				dfd2.addServices(sd2);
				
				DFAgentDescription dfd3 = new DFAgentDescription();
				ServiceDescription sd3 = new ServiceDescription();
				sd3.setType("customer-agent");
				dfd3.addServices(sd3);
				
				try {
					DFAgentDescription[] foundAgents = DFService.search(myAgent, dfd1);
					for(DFAgentDescription ad : foundAgents){
						simulationAgents.add(ad.getName());
					}
					foundAgents = DFService.search(myAgent, dfd2);
					for(DFAgentDescription ad : foundAgents){
						simulationAgents.add(ad.getName());
					}
					foundAgents = DFService.search(myAgent, dfd3);
					for(DFAgentDescription ad : foundAgents){
						simulationAgents.add(ad.getName());
					}
				} catch (FIPAException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
				for (AID agent : simulationAgents){
					msg.addReceiver(agent);
				}
				msg.setLanguage(codec.getName());
				msg.setOntology(ontology.getName());
				
//				creating the message content
				CalendarNotification dayNotif = new CalendarNotification();
				dayNotif.setNewDay(true);

				System.out.println("--------------------------------------------------------------------------------");
				if(dayCount == dayMAX){
					dayNotif.setTerminate(true);
					System.out.println(getAID().getLocalName()+": Simulation has ended! Lasted: " + (dayCount) + " days.");	
					dayCount++;
				} else {
					dayNotif.setTerminate(false);
//					send new day message to all simulation agents
					System.out.println(getAID().getLocalName()+": It's a new day everyone: " + (dayCount+1));
					step++;
				}
				System.out.println("Broadcasting to " + simulationAgents.size() + " agents");
				System.out.println("--------------------------------------------------------------------------------");
				try {
//					Let JADE convert from Java objects to string
					getContentManager().fillContent(msg, dayNotif);
					send(msg);
				} catch (CodecException ce) {
					ce.printStackTrace();
				} catch (OntologyException oe) {
					oe.printStackTrace();
				}
				break;
				
			case 1:
				//listening to done messages from simulation agents
				MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM); 
				ACLMessage message = receive(mt);
				if(message != null){
					try {
						ContentElement ce = null;
						ce = getContentManager().extractContent(message);
						if (ce instanceof CalendarNotification) {
							CalendarNotification notif = (CalendarNotification) ce;
							boolean done = notif.isDone();
							if(done){
								receivedDoneMessages++;
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
				if(receivedDoneMessages == simulationAgents.size()){
					dayCount++;
					reset();
				}
				break;
			}
		}

		@Override
		public boolean done() {
			return dayCount == dayMAX+1;
		}

		@Override
		public int onEnd() {
			System.out.println("Deleting agent: " + getAID().getLocalName());
			try {
				DFService.deregister(myAgent);
			} catch (FIPAException e) {
				e.printStackTrace();
			}
			myAgent.doDelete();
//			SupplyChainSimulation.main(null);
			return super.onEnd();
		}

		@Override
		public void reset() {
			super.reset();
			step = 0;
			receivedDoneMessages = 0;
			simulationAgents.clear();
		}
		
	}

}