import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;


public class BookBuyerAgent extends Agent{
	
	//Title of the book to buy
	private String targetBookTitle;
	//List of known seller agents
	private AID[] sellerAgents;
	
	
	protected void setup() {
		System.out.println("Hello! Buyer-Agent " + getAID().getName() + " is ready.");
		
		//Get the title of the book as a start-up argument
		Object[] args = getArguments();
		if(args != null && args.length>0) {
			targetBookTitle = (String) args[0];
			System.out.println("Trying to buy " + targetBookTitle);
			
			//Add ticker behaviour to schedule a request to seller agents every minute
			addBehaviour(new TickerBehaviour(this, 10000) {
				protected void onTick() {
					//Update list of seller agents
					DFAgentDescription template = new DFAgentDescription();
					ServiceDescription sd = new ServiceDescription();
					sd.setType("book-selling");
					template.addServices(sd);
					try {
						DFAgentDescription[] result = DFService.search(myAgent, template);
						sellerAgents = new AID[result.length];
						for (int i=0; i<result.length; i++) {
							sellerAgents[i] = result[i].getName();
						}
					} catch (FIPAException fe) {
						fe.printStackTrace();
					}
					
					myAgent.addBehaviour(new RequestPerformer());
				}
			});
			
		} else {
			System.out.println("No book title specified");
			doDelete();
		}
	}
	
	protected void takeDown() {
		System.out.println("Agent " + getAID().getName() + " terminating");
	}
	
	/**
	 * Inner class RequestPerformer. This is the behaviour used by Book-buyer agents
	 * to request seller agents the target book.
	 */
	private class RequestPerformer extends Behaviour {
		private AID bestSeller; //agent providing best offer
		private int bestPrice; //best offered price
		private int repliesCnt; //total of received replies
		private MessageTemplate mt; //template to receive replies
		private int step = 0;
		
		public void action() {
			switch(step) {
			case 0:
				System.out.println("Agent: " + getAID().getName() + " - at case 0");
				//send the CFP (call for proposal) to all sellers
				ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
				for (int i=0; i<sellerAgents.length; i++) {
					cfp.addReceiver(sellerAgents[i]);
				}
				cfp.setContent(targetBookTitle);
				cfp.setConversationId("book-trade");
				cfp.setReplyWith("cfp"+System.currentTimeMillis()); //unique value
				myAgent.send(cfp);
				//Prepare the template for the proposals
				mt = MessageTemplate.and(
						MessageTemplate.MatchConversationId("book-trade"), 
						MessageTemplate.MatchInReplyTo(cfp.getReplyWith())
						);
				step = 1;
				break;
				
			case 1:
				System.out.println("Agent: " + getAID().getName() + " - at case 1");
				//Receive all proposals and refusals from sellers
				ACLMessage reply = myAgent.receive(mt);
				if(reply != null) {
					//Reply received
					if(reply.getPerformative() == ACLMessage.PROPOSE) {
						int price = Integer.parseInt(reply.getContent());
						if (bestSeller == null || bestPrice > price) {
							//this is currently the best offer - accept
							bestPrice = price;
							bestSeller = reply.getSender();
						}
					}
					repliesCnt++;
					if(repliesCnt >= sellerAgents.length) {
						step = 2;
					}
				} else {
					block();
				}
				break;
				
			case 2:
				System.out.println("Agent: " + getAID().getName() + " - at case 2");
				//send purchase order to the seller that provided the best price
				ACLMessage order = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
				order.addReceiver(bestSeller);
				order.setContent(targetBookTitle);
				order.setConversationId("book-trade");
				order.setReplyWith("order"+System.currentTimeMillis());
				myAgent.send(order);
				System.out.println("Agent: " + getAID().getName() + " - placed order for" + targetBookTitle);
				
				//Again set message template to get the reply for our purchase order (confirmation basically)
				mt = MessageTemplate.and(
						MessageTemplate.MatchConversationId("book-trade"), 
						MessageTemplate.MatchInReplyTo(order.getReplyWith())
						);
				step = 3;
				break;
				
			case 3:
				System.out.println("Agent: " + getAID().getName() + " - at case 3");
				//receive the purchase order reply
				reply = myAgent.receive(mt);
				if (reply != null) {
					//purchase order reply received!
					System.out.println("Agent: " + getAID().getName() + " - recevied reply for order placement");
					if(reply.getPerformative() == ACLMessage.INFORM) { //only perform if the reply is INFORM as opposed to FAILURE
						//purchase successful - we can terminate
						System.out.println(getAID().getName() + " " +targetBookTitle+ " successfuly purchased for:");
						System.out.println("Price: "+bestPrice);
						myAgent.doDelete();
					}
					step = 4;
				} else {
					block();
				}
				break;
			}
		}

		@Override
		public boolean done() {
			return ((step==2 && bestSeller == null) || step == 4);
		}
	} //end of inner class RequestPerformer

}
