import java.util.Date;
import java.util.Hashtable;
import java.util.Set;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

public class Auctioneer extends Agent {
	Hashtable catalogue;
	Behaviour auction;
	AID[] bidders;

	protected void setup() {
		
		//Register the agent to DF (Yellow pages) service 
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setType("auction-running");
		sd.setName("JADE-auction-running");
		dfd.addServices(sd);
		try {
			DFService.register(this, dfd);
		} catch (FIPAException fe) {
			fe.printStackTrace();
		}
		
		Object[] args = getArguments();
		// Create the catalogue
		catalogue = (Hashtable) args[0];
		
		System.out.println(getAID().getName() + " is live.");
		Set<String> keys = catalogue.keySet();
		for(String key : keys) {
			System.out.println(getAID().getName() + " I sell: " + key + " with ID: " + catalogue.get(key));
		}
		
		auction = new SimpleBehaviour() {
			boolean auctionComplete = false;
			public void action() {
				System.out.println(getAID().getName() + " starting auction now!");
				
				//...
				
				System.out.println(getAID().getName() + " auction ended!");
				auctionComplete = true;
			}

			@Override
			public boolean done() {
				// TODO Auto-generated method stub
				return auctionComplete;
			}
		};
		
		long t0 = System.currentTimeMillis();
		addBehaviour(new SimpleBehaviour() {
			long elapsed;
			boolean timeOut = false;
			public void action() {
				elapsed = System.currentTimeMillis()-t0;
				// give time for bidder registration
				if(elapsed > 10000) {
					//time out, end of registration, start auction
					myAgent.addBehaviour(auction);
					timeOut = true;
				} else {
					// listen for registration calls here
				}
			}

			public boolean done() {
				return timeOut;
			}
		});
		
	}

	protected void takeDown() {

	}
	

}
