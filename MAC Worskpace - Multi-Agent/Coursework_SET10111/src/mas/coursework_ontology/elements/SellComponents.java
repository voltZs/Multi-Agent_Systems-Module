package mas.coursework_ontology.elements;

import java.util.List;

import jade.content.AgentAction;
import jade.content.onto.annotations.Slot;
import jade.core.AID;

public class SellComponents implements AgentAction{
	private AID buyer;
	private AID seller;
	private List<OrderPair> orderPairs;
	private int deliveryTime;
	private int orderID;

	@Slot(mandatory = true)
	public AID getBuyer() {
		return buyer;
	}
	public void setBuyer(AID buyer) {
		this.buyer = buyer;
	}
	
	public AID getSeller() {
		return seller;
	}
	public void setSeller(AID seller) {
		this.seller = seller;
	}
	
	@Slot(mandatory = true)
	public List<OrderPair> getOrderPairs() {
		return orderPairs;
	}
	public void setOrderPairs(List<OrderPair> orderPairs) {
		this.orderPairs = orderPairs;
	}
	
	public int getOrderID() {
		return orderID;
	}
	public void setOrderID(int orderID) {
		this.orderID = orderID;
	}
	
	public int getDeliveryTime() {
		return deliveryTime;
	}
	public void setDeliveryTime(int deliveryTime) {
		this.deliveryTime = deliveryTime;
	}
}
