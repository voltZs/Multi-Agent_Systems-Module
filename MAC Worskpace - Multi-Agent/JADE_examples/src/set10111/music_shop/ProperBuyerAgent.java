package set10111.music_shop;

import java.util.ArrayList;
import java.util.List;

import jade.content.lang.Codec;
import jade.content.lang.Codec.CodecException;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.Ontology;
import jade.content.onto.OntologyException;
import jade.content.onto.basic.Action;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.SequentialBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import set10111.music_shop_ontology.ECommerceOntology;
import set10111.music_shop_ontology.elements.*;

public class ProperBuyerAgent extends Agent {
	private Codec codec = new SLCodec();
	private Ontology ontology = ECommerceOntology.getInstance();
	private AID sellerAID;
	protected void setup(){
		getContentManager().registerLanguage(codec);
		getContentManager().registerOntology(ontology);
		String[] args = (String[])this.getArguments();

		// give seller time to register in yellow pages
		doWait(15000);
		DFAgentDescription template = new DFAgentDescription();
		ServiceDescription sd = new ServiceDescription();
		sd.setType("music-shop-seller");
		template.addServices(sd);
		try {
			sellerAID = DFService.search(this, template)[0].getName();
		} catch (FIPAException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		SequentialBehaviour purchaseProcess = new SequentialBehaviour();
		purchaseProcess.addSubBehaviour(new QueryCDBehaviour(this));
		purchaseProcess.addSubBehaviour(new ListenQueryReply(this));
		purchaseProcess.addSubBehaviour(new RequestCDBehaviour(this));
		addBehaviour(purchaseProcess);
	}
	
	private class QueryCDBehaviour extends OneShotBehaviour{
		public QueryCDBehaviour(Agent a) {
			super(a);
		}
		private boolean finished = false;
		
		@Override
		public void action() {
			// TODO Auto-generated method stub
			// Prepare the Query-IF message
			ACLMessage msg = new ACLMessage(ACLMessage.QUERY_IF);
			msg.addReceiver(sellerAID); // sellerAID is the AID of the Seller
										// agent
			msg.setLanguage(codec.getName());
			msg.setOntology(ontology.getName());
			// Prepare the content.
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
			Owns owns = new Owns();
			owns.setOwner(sellerAID);
			owns.setItem(cd);
			try {
				// Let JADE convert from Java objects to string
				getContentManager().fillContent(msg, owns);
				send(msg);
			} catch (CodecException ce) {
				ce.printStackTrace();
			} catch (OntologyException oe) {
				oe.printStackTrace();
			}
			finished = true;
		}
	}
	
	private class ListenQueryReply extends Behaviour{
		boolean CDInStock = false;
		
		public ListenQueryReply(Agent a ){
			super(a);
		}
		
		@Override
		public void action() {
			MessageTemplate mt = MessageTemplate.or(
					MessageTemplate.MatchPerformative(ACLMessage.CONFIRM), 
					MessageTemplate.MatchPerformative(ACLMessage.DISCONFIRM));
			ACLMessage msg = myAgent.receive(mt);
			if (msg!= null){
				if(msg.getPerformative() == ACLMessage.CONFIRM){
					CDInStock = true;
				}
			} else{
				block();
			}
		}

		@Override
		public boolean done() {
			// TODO Auto-generated method stub
			return CDInStock;
		}
		
	}
	
	private class RequestCDBehaviour extends OneShotBehaviour{
		public RequestCDBehaviour(Agent a) {
			super(a);
		}

		@Override
		public void action() {
			// Prepare the action request message
			ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
			msg.addReceiver(sellerAID); // sellerAID is the AID of the Seller
										// agent
			msg.setLanguage(codec.getName());
			msg.setOntology(ontology.getName());
			// Prepare the content.
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
			Sell order = new Sell();
			order.setBuyer(myAgent.getAID());
			order.setItem(cd);

			// IMPORTANT: According to FIPA, we need to create a wrapper Action
			// object
			// with the action and the AID of the agent
			// we are requesting to perform the action
			// you will get an exception if you try to send the sell action
			// directly
			// not inside the wrapper!!!
			Action request = new Action();
			request.setAction(order);
			request.setActor(sellerAID); // the agent that you request to
											// perform the action
			try {
				// Let JADE convert from Java objects to string
				getContentManager().fillContent(msg, request); // send the
																// wrapper
																// object
				send(msg);
			} catch (CodecException ce) {
				ce.printStackTrace();
			} catch (OntologyException oe) {
				oe.printStackTrace();
			}
		}
	}
}
