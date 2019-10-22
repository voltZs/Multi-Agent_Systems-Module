package mas.coursework;

import java.util.HashMap;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import mas.coursework_ontology.elements.SellPhones;
public class SupplyChainSimulation {
	
	private static int numOfCustomers = 2;

	public static void main(String[] args) {
		Profile myProfile = new ProfileImpl();
		Runtime myRuntime = Runtime.instance();
		try{
//			PhoneOrderGenerator phoneGenerator = new PhoneOrderGenerator();
//			SellPhones order = phoneGenerator.getOrder();
//			System.out.println(order);
			
			ContainerController myContainer = myRuntime.createMainContainer(myProfile);	
			AgentController rma = myContainer.createNewAgent("rma", "jade.tools.rma.rma", null);
			rma.start();
			
			AgentController manufacturerAgent = myContainer.createNewAgent("manufacturer", ManufacturerAgent.class.getCanonicalName(), null);
			manufacturerAgent.start();
			
			AgentController supplierAgent1 = myContainer.createNewAgent("supplier1", SupplierAgent.class.getCanonicalName(), null);
			supplierAgent1.start();
			
			AgentController supplierAgent2 = myContainer.createNewAgent("supplier2", SupplierAgent.class.getCanonicalName(), null);
			supplierAgent2.start();
			
			for(int i=0; i < numOfCustomers; i++){
				AgentController customerAgent = myContainer.createNewAgent("customer"+ (i+1), CustomerAgent.class.getCanonicalName(), null);
				customerAgent.start();
			}
//			
			AgentController tickerAgent = myContainer.createNewAgent("ticker", TickerAgent.class.getCanonicalName(), null);
			tickerAgent.start();
		}
		catch(Exception e){
			System.out.println("Exception starting agent: " + e.toString());
		}
	}

}
