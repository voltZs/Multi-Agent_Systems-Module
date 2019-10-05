import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.core.Runtime;

public class Application {

	public static void main(String[] args) {
		// setup the JADE env
		Profile myProfile = new ProfileImpl();
		Runtime myRuntime = Runtime.instance();
		ContainerController myContainer = myRuntime.createMainContainer(myProfile);
		try {
			//Start the agent controller, which itself is an agent (rma)
			AgentController rma = myContainer.createNewAgent("rma", "jade.tools.rma.rma", null);
		rma.start();
		
		//Start our own SimpleAgent, Zsolt
		AgentController myAgent = myContainer.createNewAgent("Zsolt", TickerAgent.class.getCanonicalName(), null);
		myAgent.start();
		
		} catch(Exception e) {
			System.out.println("Exception starting agent: " + e.toString());
		};

	}

}
