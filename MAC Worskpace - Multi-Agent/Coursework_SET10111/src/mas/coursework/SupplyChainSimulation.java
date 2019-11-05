package mas.coursework;

import java.util.ArrayList;
import java.util.HashMap;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import mas.coursework_ontology.elements.Component;
import mas.coursework_ontology.elements.Screen;
import mas.coursework_ontology.elements.SellPhones;
public class SupplyChainSimulation {
	
	private static int numOfCustomers = 2;
	
	public static HashMap<Component, Integer> warehouse;

	public static void main(String[] args) {
		
		Profile myProfile = new ProfileImpl();
		Runtime myRuntime = Runtime.instance();
		try{
			
			ContainerController myContainer = myRuntime.createMainContainer(myProfile);	
			AgentController rma = myContainer.createNewAgent("rma", "jade.tools.rma.rma", null);
			rma.start();
			
			AgentController manufacturerAgent = myContainer.createNewAgent("manufacturer", ManufacturerAgent.class.getCanonicalName(), null);
			manufacturerAgent.start();
			
			Object[] agentArguments1 = new Object[1];
//			set type (int) for agent 1 or 2 based on what catalogue they should adopt
			agentArguments1[0] = 1;
			AgentController supplierAgent1 = myContainer.createNewAgent("supplier1", SupplierAgent.class.getCanonicalName(), agentArguments1);
			supplierAgent1.start();
			
			Object[] agentArguments2 = new Object[1];
//			set type (int) for agent 1 or 2 based on what catalogue they should adopt
			agentArguments2[0] = 2;
			AgentController supplierAgent2 = myContainer.createNewAgent("supplier2", SupplierAgent.class.getCanonicalName(), agentArguments2);
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
	
	public static void addScreen(Component comp){
		ArrayList<String> keys = new ArrayList<>();
		for(Component key : warehouse.keySet()){
			keys.add(key.getIdentifier());
		}
		
		if(warehouse.containsKey(comp)){
			int temp = warehouse.get(comp);
			temp++;
			warehouse.put(comp, temp);
		} else {
		warehouse.put(comp, 1);
		}
	}

}
