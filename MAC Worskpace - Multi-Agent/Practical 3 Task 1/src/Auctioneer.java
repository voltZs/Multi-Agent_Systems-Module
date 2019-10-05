import java.util.ArrayList;
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
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;


public class Auctioneer extends Agent {
	private Hashtable catalogue;
	private ArrayList<AID> bidders;

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
		
		bidders = new ArrayList<AID>();
		
		System.out.println(getAID().getName() + " is live.");
		Set<String> keys = catalogue.keySet();
		for(String key : keys) {
			System.out.println(getAID().getName() + " I sell: " + key + " with ID: " + catalogue.get(key));
		}
		
		long t0 = System.currentTimeMillis();
		addBehaviour(new SimpleBehaviour() {
			long elapsed;
			boolean timeOut = false;
			public void action() {
				elapsed = System.currentTimeMillis()-t0;
				// give time for bidder registration
				if(elapsed > 10000) {
					//time out, end of registration, start auction
					System.out.println("Bidders registered: ");
					for (AID bidder : bidders){
						System.out.println("\t" + bidder.getName());
					}
					myAgent.addBehaviour(new AuctionProcess());
					timeOut = true;
				} else {
					// listen for registration calls here
					MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
					ACLMessage msg = myAgent.receive(mt); //pass in the message template to make sure we ignore messages that are not calls for proposal
//					System.out.println("Agent: " + getAID().getName() + " - checking for proposals.");
					if (msg != null) {
						// Received a message, process it 
//						System.out.println("Agent: " + getAID().getName() + " - received message");
						String bidderAID = msg.getContent();
						AID bidder = msg.getSender();
						//bidders pass in their aid as msg content if they want to register for auction
						if(bidder.getName().equals(msg.getContent())) {
							bidders.add(bidder);
							System.out.println("Adding bidder " + bidder.getName() + " to my list");
						} else {
							System.out.println("Agent: " + bidder.getName() + " sent inform message to auctioneer that is not their name");
						}
					}
//					Not adding a block() here otherwise the agent doesn't execute the first part of the if statement after a minute 
//					(unless it receives a registration call at that time)
				}
			}

			public boolean done() {
				return timeOut;
			}
		});
		
	}

	protected void takeDown() {

	}
	
	private class AuctionProcess extends SimpleBehaviour {
		
		private AID highestBidder;
		private int hightesPrice;
		private int repliesCnt;
		private MessageTemplate mt;
		boolean auctionComplete = false;
		
		public void action() {
			System.out.println(getAID().getName() + " starting auction now! \n ------------------------------");
			
			//for each item in catalogue
			Set<String> keys = catalogue.keySet();
			for(String itemDescription : keys) {
				//reset all attributes for auctioning
				highestBidder = null;
				hightesPrice = 0;
				repliesCnt = 0; //total of received replies
				mt = null; //template to receive replies
				
				// send CFP to all bidders
				ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
				for (int i=0; i<bidders.size(); i++) {
					cfp.addReceiver(bidders.get(i));
				}
				cfp.setContent(itemDescription);
				cfp.setConversationId("auction-run-for-"+itemDescription);
				cfp.setReplyWith("cfp"+System.currentTimeMillis()); //unique value
				myAgent.send(cfp);
				//Prepare the template for the proposals
				mt = MessageTemplate.and(
						MessageTemplate.MatchConversationId("auction-run-for-"+itemDescription), 
						MessageTemplate.MatchInReplyTo(cfp.getReplyWith())
						);
				
				
				// while # of replies < # bidders - making sure we get a reply from everyone
//				while(){
//					
//				}
					//listen to replies with PROPOSE/REFUSE
					//with each reply replace the highest bidder if bid is higher
				//send message to highest bidder ACCEPT_PROPOSAL
			}
					
			System.out.println("--------------------------------- \n" + getAID().getName() + " auction ended! ");
			auctionComplete = true;
		}

		@Override
		public boolean done() {
			// TODO Auto-generated method stub
			return auctionComplete;
		}
		
	}
	

}
