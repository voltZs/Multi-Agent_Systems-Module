import java.io.File;
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
	private boolean auctionStarted = false;
	// The GUI by means of which the user can add books in the catalogue
	private AuctioneerGui myGui;

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
		
		bidders = new ArrayList<AID>();
		// Create and show GUI
		myGui = new AuctioneerGui(this);
		myGui.showGui();

		System.out.println(getAID().getName() + " is live.");

		File csvPath = new File("auctioneerItemsTask1.csv");
		ArrayList<String[]> csv = CSVReader.readCSV(csvPath.getAbsolutePath());
		System.out.println(csvPath.getAbsolutePath());
		catalogue = new Hashtable();
		for(String[] line : csv) {
//			System.out.println(line[0] + " - " + line[1]);
			catalogue.put(line[0], line[1]);
		}
		
		long t0 = System.currentTimeMillis();
		addBehaviour(new SimpleBehaviour() {
			long elapsed;
			boolean timeOut = false;
			public void action() {
				elapsed = System.currentTimeMillis()-t0;
				// give time for bidder registration
				if(auctionStarted) {
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
//					Not adding a block() here otherwise the agent doesn't execute the first part of the if statement
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
	
	public void startAuction(){
		auctionStarted = true;
	}
	
	private class AuctionProcess extends SimpleBehaviour {
		
		private AID highestBidder;
		private int highestPrice;
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
				highestPrice = 0;
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
				
// 				while # of replies < # bidders - making sure we get a reply from everyone
				while(repliesCnt < bidders.size()){
					//listen to replies with PROPOSE/REFUSE
					ACLMessage reply = myAgent.receive(mt);
					if (reply != null) {
						System.out.println("Received reply from " + reply.getSender().getName());
						if(reply.getPerformative() == ACLMessage.PROPOSE){
							int bidderPrice = Integer.parseInt(reply.getContent());
							System.out.println("\t offering: " + bidderPrice);
							//with each reply replace the highest bidder if bid is higher
							if(bidderPrice > highestPrice) {
								highestPrice = bidderPrice;
								highestBidder = reply.getSender();
							} 
						} else {
							System.out.println("\t no offer");
						}
						repliesCnt++;
					} 
				}
				System.out.println("-----");
				
				//send message to highest bidder ACCEPT_PROPOSAL
				if(highestPrice != 0){
					ACLMessage acc = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
					acc.addReceiver(highestBidder);
					acc.setContent(String.valueOf(catalogue.get(itemDescription)));
					acc.setConversationId("auctioned-item-"+itemDescription);
					acc.setReplyWith("acc"+System.currentTimeMillis());
					myAgent.send(acc);
					System.out.println(itemDescription + " sold to " + highestBidder.getName());
				} else {
					System.out.println(itemDescription + " not sold");
				}
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
