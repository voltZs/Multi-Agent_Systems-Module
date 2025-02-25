package set10111.simulation;

import java.util.ArrayList;
import java.util.HashMap;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.SequentialBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class BuyerAgent extends Agent {
	private ArrayList<AID> sellers = new ArrayList<>();
	private ArrayList<String>  booksToBuy = new ArrayList<>();
	private HashMap<String,ArrayList<Offer>> currentOffers = new HashMap<>();
	private AID tickerAgent;
	private int numQueriesSent;
	private int numProposalReplies;
	private int totalSpent = 0;
	@Override
	protected void setup() {
		//add this agent to the yellow pages
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setType("buyer");
		sd.setName(getLocalName() + "-buyer-agent");
		dfd.addServices(sd);
		try{
			DFService.register(this, dfd);
		}
		catch(FIPAException e){
			e.printStackTrace();
		}
		//add books to buy
		booksToBuy.add("Java for Dummies");
		booksToBuy.add("JADE: the Inside Story");
		booksToBuy.add("Multi-Agent Systems for Everybody");
		
		addBehaviour(new TickerWaiter(this));
	}


	@Override
	protected void takeDown() {
		//Deregister from the yellow pages
		try{
			DFService.deregister(this);
		}
		catch(FIPAException e){
			e.printStackTrace();
		}
	}

	public class TickerWaiter extends CyclicBehaviour {

		//behaviour to wait for a new day
		public TickerWaiter(Agent a) {
			super(a);
		}

		@Override
		public void action() {
			MessageTemplate mt = MessageTemplate.or(MessageTemplate.MatchContent("new day"),
					MessageTemplate.MatchContent("terminate"));
			ACLMessage msg = myAgent.receive(mt); 
			if(msg != null) {
				if(tickerAgent == null) {
					tickerAgent = msg.getSender();
				}
				if(msg.getContent().equals("new day")) {
					//spawn new sequential behaviour for day's activities
					SequentialBehaviour dailyActivity = new SequentialBehaviour();
					//sub-behaviours will execute in the order they are added
					if(booksToBuy.size() > 0){
						dailyActivity.addSubBehaviour(new FindSellers(myAgent));
						dailyActivity.addSubBehaviour(new SendEnquiries(myAgent));
						dailyActivity.addSubBehaviour(new CollectOffers(myAgent));
						dailyActivity.addSubBehaviour(new ProcessOffers(myAgent));
						dailyActivity.addSubBehaviour(new ListenToOrderConfs(myAgent));
					}
					dailyActivity.addSubBehaviour(new EndDay(myAgent));
					myAgent.addBehaviour(dailyActivity);
				}
				else {
					//termination message to end simulation
					System.out.println("Total spent for books: £" + totalSpent);
					myAgent.doDelete();
				}
			}
			else{
				block();
			}
		}

	}

	public class FindSellers extends OneShotBehaviour {

		public FindSellers(Agent a) {
			super(a);
		}

		@Override
		public void action() {
			DFAgentDescription sellerTemplate = new DFAgentDescription();
			ServiceDescription sd = new ServiceDescription();
			sd.setType("seller");
			sellerTemplate.addServices(sd);
			try{
				sellers.clear();
				DFAgentDescription[] agentsType1  = DFService.search(myAgent,sellerTemplate); 
				for(int i=0; i<agentsType1.length; i++){
					sellers.add(agentsType1[i].getName()); // this is the AID
				}
			}
			catch(FIPAException e) {
				e.printStackTrace();
			}

		}

	}

	public class SendEnquiries extends OneShotBehaviour {

		public SendEnquiries(Agent a) {
			super(a);
		}

		@Override
		public void action() {
			//send out a call for proposals for each book
			numQueriesSent = 0;
			for(String bookTitle : booksToBuy) {
				ACLMessage enquiry = new ACLMessage(ACLMessage.CFP);
				enquiry.setContent(bookTitle);
				enquiry.setConversationId(bookTitle);
				for(AID seller : sellers) {
					enquiry.addReceiver(seller);
					numQueriesSent++;
				}
				myAgent.send(enquiry);
				
			}

		}
	}

	public class CollectOffers extends Behaviour {
		private int numRepliesReceived = 0;
		
		public CollectOffers(Agent a) {
			super(a);
			currentOffers.clear();
		}

		
		@Override
		public void action() {
			boolean received = false;
			for(String bookTitle : booksToBuy) {
				MessageTemplate mt = MessageTemplate.MatchConversationId(bookTitle);
				ACLMessage msg = myAgent.receive(mt);
				if(msg != null) {
					received = true;
					numRepliesReceived++;
					if(msg.getPerformative() == ACLMessage.PROPOSE) {
						//we have an offer
						//the first offer for a book today
						if(!currentOffers.containsKey(bookTitle)) {
							ArrayList<Offer> offers = new ArrayList<>();
							offers.add(new Offer(msg.getSender(),
									Integer.parseInt(msg.getContent())));
							currentOffers.put(bookTitle, offers);
						}
						//subsequent offers
						else {
							ArrayList<Offer> offers = currentOffers.get(bookTitle);
							offers.add(new Offer(msg.getSender(),
									Integer.parseInt(msg.getContent())));
						}
							
					}

				}
			}
			if(!received) {
				block();
			}
		}

		

		@Override
		public boolean done() {
			return numRepliesReceived == numQueriesSent;
		}

		@Override
		public int onEnd() {
			//print the offers
			for(String book : booksToBuy) {
				if(currentOffers.containsKey(book)) {
					ArrayList<Offer> offers = currentOffers.get(book);
					for(Offer o : offers) {
						System.out.println(book + "," + o.getSeller().getLocalName() + "," + o.getPrice());
					}
				}
				else {
					System.out.println("No offers for " + book);
				}
			}
			return 0;
		}

	}
	
	public class ProcessOffers extends OneShotBehaviour{	
		
		private int numOfReplies = 0;
		
		public ProcessOffers(Agent a){
			super(a);
		}
		
		@Override
		public void action() {
			numProposalReplies = 0;
			
			for (String bookTitle : currentOffers.keySet()){
				boolean wanted = false;
				for(String book: booksToBuy){
					if (book.equals(bookTitle)){
						//this is a book I want and it's been offered to me
						wanted = true;
						break;
					}
				}
				//pick smallest price from offers
				ArrayList<Offer> offersForBook = currentOffers.get(bookTitle);
				int lowestPrice = offersForBook.get(0).getPrice(); //init at first element
				AID bestSeller = offersForBook.get(0).getSeller();  //init at first element
				for(Offer offer : offersForBook){
					int currPrice = offer.getPrice();
					if(currPrice < lowestPrice){
						lowestPrice = currPrice;
						bestSeller = offer.getSeller();
					}
				}
				
				//randomise whether we actually want to buy in the round/today
				if(Math.random()>0.4){
					wanted = false;
				}
				
				ACLMessage msg;
				if(wanted){
					msg = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
//					booksToBuy.remove(booksToBuy.indexOf(bookTitle));
					numProposalReplies++;
					System.out.println("Accepting proposal for : " + bookTitle);
				} else {
					msg = new ACLMessage(ACLMessage.REJECT_PROPOSAL);
					System.out.println("Rejecting proposal for : " + bookTitle);
				}
				try{
					msg.setConversationId(bookTitle);
					msg.addReceiver(bestSeller);
					myAgent.send(msg);
				} catch(Exception e){
					e.printStackTrace();;
				}
			}
		}
	}
	
	public class ListenToOrderConfs extends Behaviour{
		private int numReceived = 0;
		
		public ListenToOrderConfs(Agent a){
			super(a);
		}

		public void action() {
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
			ACLMessage msg = myAgent.receive(mt);
			//need to wrap in this if statement, otherwise the behaviour blocks when the numProposalReplies is 0 and never returns true (0 == 0)
			if(numProposalReplies>0){
				if(msg !=null){
					for(String book:booksToBuy){
						if(book.equals(msg.getConversationId())){
							int price = Integer.parseInt(msg.getContent());
							totalSpent += price;
							booksToBuy.remove(book);
							numReceived++;
							break;
						}
					}
				} else {
					block();
				}	
			}
		}

		@Override
		public boolean done() {
			return numReceived == numProposalReplies;
		}
		
	}
	
	
	
	public class EndDay extends OneShotBehaviour {
		
		public EndDay(Agent a) {
			super(a);
		}

		@Override
		public void action() {
			System.out.println("I'm here");
			ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
			msg.addReceiver(tickerAgent);
			msg.setContent("done");
			myAgent.send(msg);
			//send a message to each seller that we have finished
			ACLMessage sellerDone = new ACLMessage(ACLMessage.INFORM);
			sellerDone.setContent("done");
			for(AID seller : sellers) {
				sellerDone.addReceiver(seller);
			}
			myAgent.send(sellerDone);
		}
		
	}

}





