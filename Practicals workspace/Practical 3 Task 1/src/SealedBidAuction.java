import java.util.Hashtable;

import jade.core.Agent;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;

public class SealedBidAuction {

	public static void main(String[] args) {
		// setup the JADE environemnt
		Profile myProfile = new ProfileImpl();
		Runtime myRuntime = Runtime.instance();
		ContainerController myContainer = myRuntime.createMainContainer(myProfile);
		
		Hashtable itemsToSell = new Hashtable();
		itemsToSell.put("Processor", 1);
		itemsToSell.put("Monitor", 2);
		itemsToSell.put("GPU", 3);
		itemsToSell.put("Keyboard", 4);
		itemsToSell.put("Motherboard", 5);
		
		Hashtable bidFor1 = new Hashtable();
		bidFor1.put("Processor", 200);
		bidFor1.put("Monitor", 105);
		bidFor1.put("GPU", 300);
		bidFor1.put("Keyboard", 500);
		
		Hashtable bidFor2 = new Hashtable();
		bidFor2.put("Monitor", 100);
		bidFor2.put("GPU", 400);
		bidFor2.put("Keyboard", 600);
		
		Object[] auctioneerArgs = new Object[1];
		auctioneerArgs[0] = itemsToSell;
		
		Object[] bidderArgs1 = new Object[1];
		bidderArgs1[0] = bidFor1;
		
		Object[] bidderArgs2 = new Object[1];
		bidderArgs2[0] = bidFor2;
		

		try {
			AgentController rma = myContainer.createNewAgent("rma", "jade.tools.rma.rma", null);
			rma.start();

			AgentController myAuctioneer = myContainer.createNewAgent("Mr Auctioneer", Auctioneer.class.getCanonicalName(),
					auctioneerArgs);
			myAuctioneer.start();
			
			AgentController myBidder1 = myContainer.createNewAgent("Mr Bidder 1", Bidder.class.getCanonicalName(),
					bidderArgs1);
			myBidder1.start();
			
			AgentController myBidder2 = myContainer.createNewAgent("Mr Bidder 2", Bidder.class.getCanonicalName(),
					bidderArgs2);
			myBidder2.start();

		} catch (Exception e) {
			System.out.println("Exception starting agent: " + e.toString());
		}
		
		

	}

}
