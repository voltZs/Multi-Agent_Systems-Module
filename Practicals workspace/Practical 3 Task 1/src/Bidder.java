import java.util.Hashtable;
import java.util.Set;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;

public class Bidder extends Agent {
	Hashtable toBuyList;
	
	protected void setup() {
		
		Object[] args = getArguments();
		// Create the catalogue
		toBuyList = (Hashtable) args[0];
		
	
		//find auctioneer and register to him
		addBehaviour(new WakerBehaviour(this, 3000) {
			public void onWake() {
				AID auctioneer = null;
				DFAgentDescription template = new DFAgentDescription();
				ServiceDescription sd = new ServiceDescription();
				sd.setType("auction-running");
				template.addServices(sd);
				try {
					DFAgentDescription[] result = DFService.search(myAgent, template);
					System.out.println(result.length);
					if (result.length != 0) {
						auctioneer = result[0].getName();
					}
				} catch (FIPAException fe) {
					fe.printStackTrace();
				}
				// send message to auctioneer to register to him
				if (auctioneer != null) {
					System.out.println(getAID().getName() + " registering to auctioneer: " + auctioneer.getName());
					ACLMessage reg = new ACLMessage(ACLMessage.INFORM);
					reg.addReceiver(auctioneer);
					reg.setContent(getAID().getName());
					reg.setConversationId("bidder-registration");
					myAgent.send(reg);
				} else {
					System.out.println(getAID().getName() + " No auctioneer found");
				}
			}
		});

		System.out.println(getAID().getName() + " is live.");
		Set<String> keys = toBuyList.keySet();
		for(String key : keys) {
			System.out.println(getAID().getName() + "I want to buy: " + key + " for: " + toBuyList.get(key));
		}
	}

	protected void takeDown() {

	}

}
