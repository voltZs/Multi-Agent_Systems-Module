package mas.coursework;

import java.util.ArrayList;
import java.util.HashMap;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;
import mas.coursework_ontology.elements.Component;
import mas.coursework_ontology.elements.Screen;
import mas.coursework_ontology.elements.SellPhones;
public class SupplyChainSimulation {
	
	private static int numberOfNodes = 25; //this is the number of simulation configurations that will be created
	private static int simulationReplicas = 30; //this is the number of times each simulation will run with one configuration so an average can be drawn

	private static int evaluatedAttr;
	
	private static int numOfCustomers = 14;
	private static int warehouseCost = 5;
	private static int penaltyRange = 50;

	public static void main(String[] args) {
		Profile myProfile = new ProfileImpl();
		Runtime myRuntime = Runtime.instance();
		
		ContainerController myContainer = myRuntime.createMainContainer(myProfile);	
		AgentController rma;
		try {
			rma = myContainer.createNewAgent("rma", "jade.tools.rma.rma", null);
			rma.start();
		} catch (StaleProxyException e1) {
			e1.printStackTrace();
		}
		
//		set bellow to 'customers' or 'wh_penalty' or 'order_penalty' based on what is evaluated
//		SimulationLogger.appendHeader("customers");
		for(int i = 5; i< numberOfNodes; i++){
			evaluatedAttr = numOfCustomers;
			createAgents(myContainer, 0);
			numOfCustomers += 4;
		}
	}
	
	public static void createAgents(ContainerController myContainer, int run){
		try{
			Object[] agentArguments0 = new Object[3];
			agentArguments0[0] = warehouseCost;
			agentArguments0[1] = evaluatedAttr;
			agentArguments0[2] = run;	
			AgentController manufacturerAgent = myContainer.createNewAgent("manufacturer-run"+run, ManufacturerAgent.class.getCanonicalName(), agentArguments0);
			manufacturerAgent.start();
			
			Object[] agentArguments1 = new Object[1];
//			set type (int) for agent 1 or 2 based on what catalogue they should adopt
			agentArguments1[0] = 1;
			AgentController supplierAgent1 = myContainer.createNewAgent("supplier1-run"+run, SupplierAgent.class.getCanonicalName(), agentArguments1);
			supplierAgent1.start();
			
			Object[] agentArguments2 = new Object[1];
//			set type (int) for agent 1 or 2 based on what catalogue they should adopt
			agentArguments2[0] = 2;
			AgentController supplierAgent2 = myContainer.createNewAgent("supplier2-run"+run, SupplierAgent.class.getCanonicalName(), agentArguments2);
			supplierAgent2.start();
			
			for(int i=0; i < numOfCustomers; i++){
				Object[] agentArguments3 = new Object[1];
				agentArguments3[0] = penaltyRange;
				AgentController customerAgent = myContainer.createNewAgent("customer"+(i+1)+"-run"+run, CustomerAgent.class.getCanonicalName(), agentArguments3);
				customerAgent.start();
			}
//			
			AgentController tickerAgent = myContainer.createNewAgent("ticker-run"+run, TickerAgent.class.getCanonicalName(), null);
			tickerAgent.start();	
			while(true){
				if(run >= simulationReplicas){
					break;
				}
				try{
					tickerAgent.getState();
				} catch(StaleProxyException e){
					System.out.println("Ticker agent was deleted, run " + (run+1) + " ending.");
					System.out.println("====================================================================================");
					System.out.println("====================================================================================");
					System.out.println("====================================================================================");
					run++;
					if(run<simulationReplicas){
						createAgents(myContainer, run);
						break;
					}
				}
			}
		}
		catch(Exception e){
			System.out.println("Exception starting agent: " + e.toString());
		}
	}
}
