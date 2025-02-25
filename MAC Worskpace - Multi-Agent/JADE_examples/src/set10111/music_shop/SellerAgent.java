package set10111.music_shop;

import java.util.ArrayList;
import java.util.HashMap;

import jade.content.Concept;
import jade.content.ContentElement;
import jade.content.lang.Codec;
import jade.content.lang.Codec.CodecException;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.Ontology;
import jade.content.onto.OntologyException;
import jade.content.onto.basic.Action;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import set10111.music_shop_ontology.ECommerceOntology;
import set10111.music_shop_ontology.elements.*;


public class SellerAgent extends Agent {
	private Codec codec = new SLCodec();
	private Ontology ontology = ECommerceOntology.getInstance();
	//stock list, with serial number as the key
	private HashMap<Integer,Item> itemsForSale = new HashMap<>(); //catalogue of things they sell
	private HashMap<Integer,Integer> stockLevels = new HashMap<>(); // stock levels of things
	
	protected void setup(){
		getContentManager().registerLanguage(codec);
		getContentManager().registerOntology(ontology);
		
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setType("music-shop-seller");
		sd.setName(getLocalName() + "-music-shop-seller");
		dfd.addServices(sd);
		try{
			DFService.register(this, dfd);
		} catch (FIPAException e){
			e.printStackTrace();
		}
		
		CD cd = new CD();
		cd.setName("Synchronicity");
		cd.setSerialNumber(123);
		ArrayList<Track> tracks = new ArrayList<Track>();
		Track t = new Track();
		t.setName("Every breath you take");
		t.setDuration(230);
		tracks.add(t);
		t = new Track();
		t.setName("King of pain");
		t.setDuration(500);
		tracks.add(t);
		cd.setTracks(tracks);
		itemsForSale.put(cd.getSerialNumber(),cd);
		stockLevels.put(cd.getSerialNumber(), (int) Math.floor(Math.random()*15));
		
		addBehaviour(new QueryBehaviour());
		addBehaviour(new SellBehaviour());
		
	}

	private class QueryBehaviour extends CyclicBehaviour{
		@Override
		public void action() {
			//This behaviour should only respond to QUERY_IF messages
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.QUERY_IF); 
			ACLMessage msg = receive(mt);
			if(msg != null){
				try {
					ContentElement ce = null;
					System.out.println(msg.getContent()); //print out the message content in SL

					// Let JADE convert from String to Java objects
					// Output will be a ContentElement
					ce = getContentManager().extractContent(msg);
					if (ce instanceof Owns) {
						Owns owns = (Owns) ce;
						Item it = owns.getItem();
						// Extract the CD name and print it to demonstrate use of the ontology
						CD cd = (CD)it;
						System.out.println("The CD name is " + cd.getName());
						int stockAmount = stockLevels.get(cd.getSerialNumber());
						//check if seller has it in stock
						if(itemsForSale.containsKey(cd.getSerialNumber()) && stockAmount >0) {
							System.out.println("I have the CD in stock - " + stockAmount + " copies!");
							ACLMessage reply = msg.createReply();
							reply.setPerformative(ACLMessage.CONFIRM);
							myAgent.send(reply);
						}
						else {
							System.out.println("CD not in catalogue or out of stock");
							ACLMessage reply = msg.createReply();
							reply.setPerformative(ACLMessage.DISCONFIRM);
							myAgent.send(reply);
						}
					}
				}

				catch (CodecException ce) {
					ce.printStackTrace();
				}
				catch (OntologyException oe) {
					oe.printStackTrace();
				}

			}
			else{
				block();
			}
		}
		
	}
	
	@Override
	protected void takeDown() {
		// TODO Auto-generated method stub
		try {
			DFService.deregister(this);
		} catch (FIPAException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		super.takeDown();
	}
	
	private class SellBehaviour extends CyclicBehaviour{
		@Override
		public void action() {
			//This behaviour should only respond to REQUEST messages
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST); 
			ACLMessage msg = receive(mt);
			if(msg != null){
				try {
					ContentElement ce = null;
					System.out.println(msg.getContent()); //print out the message content in SL

					// Let JADE convert from String to Java objects
					// Output will be a ContentElement
					ce = getContentManager().extractContent(msg);
					if(ce instanceof Action) {
						Concept action = ((Action)ce).getAction();
						if (action instanceof Sell) {
							Sell order = (Sell)action;
							Item it = order.getItem();
							// Extract the CD name and print it to demonstrate use of the ontology
							if(it instanceof CD){
								CD cd = (CD)it;
								ACLMessage reply = msg.createReply();
								//check if seller has it in stock
								//to sell something an item most be in the catalogue and also in stock
								int stockAmount = stockLevels.get(cd.getSerialNumber());
								if(itemsForSale.containsKey(cd.getSerialNumber()) && stockAmount >0) {
									System.out.println("Selling CD " + cd.getName());
									reply.setPerformative(ACLMessage.AGREE);
									stockAmount--;
									stockLevels.put(cd.getSerialNumber(), stockAmount);
									myAgent.send(reply);
								}
								else {
									System.out.println("You tried to order something out of stock!!!! Check first!");
									reply.setPerformative(ACLMessage.REFUSE);
									myAgent.send(reply);
								}

							}
						}

					}
				}

				catch (CodecException ce) {
					ce.printStackTrace();
				}
				catch (OntologyException oe) {
					oe.printStackTrace();
				}

			}
			else{
				block();
			}
		}

	}

}
