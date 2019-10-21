package mas.coursework_ontology.elements;

import java.util.List;

import jade.content.AgentAction;
import jade.content.onto.annotations.Slot;
import jade.core.AID;

public class Sell implements AgentAction{
	private AID buyer;
	private List<OrderPair> orderedItems;
	
	@Slot(mandatory = true)
	public AID getBuyer() {
		return buyer;
	}
	public void setBuyer(AID buyer) {
		this.buyer = buyer;
	}
	
	@Slot(mandatory = true)
	public List<OrderPair> getOrderItems() {
		return orderedItems;
	}
	public void setOrderItems(List<OrderPair> orderItems) {
		this.orderedItems = orderItems;
	}
}
