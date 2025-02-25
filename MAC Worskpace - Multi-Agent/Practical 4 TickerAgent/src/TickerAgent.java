import java.util.ArrayList; import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription; import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage; import jade.lang.acl.MessageTemplate;

public class TickerAgent extends Agent{
	protected void setup(){
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setType("ticker -agent");
		sd.setName(getLocalName() + "-ticker-agent");
		dfd.addServices(sd);
		try{
			DFService.register(this, dfd);
		} catch (FIPAException e){
			e.printStackTrace();
		}
		
		doWait(10000);
		addBehaviour(new SynchAgentsBehaviour(this));
	}
	
	protected void takeDown(){
		try {
			DFService.deregister(this);
		} catch (FIPAException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public class SynchAgentsBehaviour extends Behaviour{
		
		private int step = 0; //where we are in the behaviour
		private int numFinReceived = 0; //number of finished message from other agents
		private ArrayList<AID> simulationAgents = new ArrayList<>();
		
		public SynchAgentsBehaviour(Agent a){
			super(a);
		}
		
		@Override
		public void action() {
			// TODO Auto-generated method stub
			switch(step){
			case 0:
				//find all agents using directory service
				//here we have two types of agent
				//"simulation -agent" and "simulation -agent 2" 
				DFAgentDescription template1 = new DFAgentDescription(); 
				ServiceDescription sd = new ServiceDescription(); 
				sd.setType("simulation-agent"); 
				template1.addServices(sd);
				DFAgentDescription template2 = new DFAgentDescription(); 
				ServiceDescription sd2 = new ServiceDescription();
				sd2.setType("simulation-agent2");
				template2.addServices(sd2);
				
				try{
					simulationAgents.clear();
					//search for agents of type "simulation-agent"
					DFAgentDescription[] agentsType1 = DFService.search(myAgent, template1);
					for(DFAgentDescription agent : agentsType1){
						simulationAgents.add(agent.getName()); //this is the AID
						System.out.println(agent.getName());
					}
					//search for agents of type "simulation-agent2"
					DFAgentDescription[] agentsType2 = DFService.search(myAgent, template2);
					for(DFAgentDescription agent : agentsType2){
						simulationAgents.add(agent.getName()); //this is the AID
						System.out.println(agent.getName());
					}
				} catch(FIPAException e){
					e.printStackTrace();
				}
				
				//send a new day message
				ACLMessage tick = new ACLMessage(ACLMessage.INFORM);
				tick.setContent("new day");
				for (AID id : simulationAgents){
					tick.addReceiver(id);
				}
				myAgent.send(tick);
				step++;
				break;
			case 1:
				//wait to receive a "done" message from all agents
				MessageTemplate mt = MessageTemplate.MatchContent("done");
				ACLMessage msg = myAgent.receive(mt);
				if (msg != null) {
					numFinReceived++;
					if (numFinReceived >= simulationAgents.size()) {
						step++;
					}
				} else {
					block();
				}
				break;
			}
		}

		@Override
		public boolean done() {
			// TODO Auto-generated method stub
			return step==2;
		}
		
		public void reset () {
			step = 0;
			numFinReceived = 0;
		}
		
		public int onEnd () { 
			System.out.println("End of day"); 
			reset();
			myAgent.addBehaviour(this); 
			return 0;
		}
		
	}
}


