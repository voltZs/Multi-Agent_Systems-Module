import java.util.Hashtable;
import java.util.Set;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class Bidder extends Agent {
	Hashtable toBuyList;
	// The GUI by means of which the user can add books in the catalogue
	private BidderGui myGui;
			
	protected void setup() {
		
		Object[] args = getArguments();
		// Create the catalogue
		if(args.length >0){
			toBuyList = (Hashtable) args[0];
		} else {
			toBuyList = new Hashtable();
		}
		// Create and show GUI
		myGui = new BidderGui(this);
		myGui.showGui();
		
	
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
		
		addBehaviour(new OfferListener());

		System.out.println(getAID().getName() + " is live.");
		Set<String> keys = toBuyList.keySet();
		for(String key : keys) {
			System.out.println(getAID().getName() + "I want to buy: " + key + " for: " + toBuyList.get(key));
		}
	}

	protected void takeDown() {

	}
	
	private class OfferListener extends CyclicBehaviour{
		@Override
		public void action() {
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
			ACLMessage msg = myAgent.receive(mt);
			if (msg != null) {
				
				String auctionItem = msg.getContent();
				System.out.println(getAID().getName() + " received auction offer for "+ auctionItem);
				ACLMessage reply = msg.createReply();
				Integer offerAmount = (Integer) toBuyList.get(auctionItem);
				if(offerAmount != null) {
					//we have this item in my list - create proposal
					reply.setPerformative(ACLMessage.PROPOSE);
					reply.setContent(String.valueOf(offerAmount.intValue()));
				} else {
					//This item is not in my list
					reply.setPerformative(ACLMessage.REFUSE);
					reply.setContent("not-interested");
				}
				myAgent.send(reply);
			} else {
				block();
			}
		}
	}
	
	/**
	 This is invoked by the GUI when the user adds a new book for sale
	 */
	public void updateToBuyList(final String item, final int price) {
		addBehaviour(new OneShotBehaviour() {
			public void action() {
				toBuyList.put(item, new Integer(price));
			}
		});
	}


}
