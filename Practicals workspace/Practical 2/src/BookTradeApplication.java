import jade.core.Agent;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;

public class BookTradeApplication {

	public static void main(String[] args) {
		// setup the JADE env
		Profile myProfile = new ProfileImpl();
		Runtime myRuntime = Runtime.instance();
		ContainerController myContainer = myRuntime.createMainContainer(myProfile);
		
		
		try {
			AgentController rma = myContainer.createNewAgent("rma", "jade.tools.rma.rma", null);
			rma.start();
			
			AgentController myAgent1 = myContainer.createNewAgent(
					"sellerA", BookSellerAgent.class.getCanonicalName(), null);
			myAgent1.start();
			AgentController myAgent2 = myContainer.createNewAgent(
					"sellerB", BookSellerAgent.class.getCanonicalName(), null);
			myAgent2.start();
			AgentController myAgent3 = myContainer.createNewAgent(
					"sellerC", BookSellerAgent.class.getCanonicalName(), null);
			myAgent3.start();
			AgentController myAgent4 = myContainer.createNewAgent(
					"sellerD", BookSellerAgent.class.getCanonicalName(), null);
			myAgent4.start();
			AgentController myAgent5 = myContainer.createNewAgent(
					"sellerE", BookSellerAgent.class.getCanonicalName(), null);
			myAgent5.start();
			
		} catch(Exception e) {
			System.out.println("Exception starting agent: " + e.toString());
		}
		

	}

}
