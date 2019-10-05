import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.TickerBehaviour;

public class SimpleAgent extends Agent {

	Behaviour tenSeconds;
	long t0 = System.currentTimeMillis();
	//create random time value between 1 and 2 mins (60k and 120k milis)
	long randTime = (long) (Math.random() * 60000 + 60000);
	
	protected void setup() {
		System.out.println(randTime);
		System.out.println("Hello! Agent " + getAID().getName() + " is ready to rumble.");
		tenSeconds = new TickerBehaviour(this, 10000) {
			long elapsed;
			protected void onTick() {
				elapsed = System.currentTimeMillis()-t0;
				if(elapsed > randTime) {
					//delete agent if time limit reached
					System.out.println("I'm done here - " + getAID().getName());
					myAgent.doDelete(); //Delete this agent
				} else {
					System.out.println("It's been 10 seconds and my name is: " +  getAID().getName());
				}
			}
		};
		addBehaviour(tenSeconds);
	}
}
