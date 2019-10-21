package mas.coursework;

import java.util.HashMap;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
public class SupplyChainSimulation {

	public static void main(String[] args) {
		Profile myProfile = new ProfileImpl();
		Runtime myRuntime = Runtime.instance();
		try{
			ContainerController myContainer = myRuntime.createMainContainer(myProfile);	
			AgentController rma = myContainer.createNewAgent("rma", "jade.tools.rma.rma", null);
			rma.start();
			
			AgentController tickerAgent = myContainer.createNewAgent("ticker", TickerAgent.class.getCanonicalName(), null);
			tickerAgent.start();
			
			AgentController manufacturerAgent = myContainer.createNewAgent("manufacturer", ManufacturerAgent.class.getCanonicalName(), null);
			manufacturerAgent.start();
		}
		catch(Exception e){
			System.out.println("Exception starting agent: " + e.toString());
		}
	}

}
