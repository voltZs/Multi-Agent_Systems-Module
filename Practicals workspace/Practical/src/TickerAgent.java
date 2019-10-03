import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.TickerBehaviour;

public class TickerAgent extends Agent {
	 //Get the current time - this will be the time that the agent was launched at
	 long t0 = System.currentTimeMillis();
	 Behaviour loop;
	 protected void setup(){
		 loop = new TickerBehaviour( this, 300 ){
			 long elapsed;
			 protected void onTick(){
				 elapsed = System.currentTimeMillis()-t0;
				 if(elapsed <= 10000) {
					//Print elapsed time since launch
					 System.out.println( elapsed + ": " +myAgent.getLocalName());
				 } else {
					System.out.println("Minute's up, bye!");
					myAgent.doDelete(); //Delete this agent
				 }
				 
			 }
		 };
		 addBehaviour( loop );
	 }
}