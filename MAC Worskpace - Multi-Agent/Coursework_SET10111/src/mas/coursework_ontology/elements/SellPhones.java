package mas.coursework_ontology.elements;

import jade.content.AgentAction;
import jade.content.onto.annotations.Slot;
import jade.core.AID;

public class SellPhones implements AgentAction {
	Phone phone;
	AID buyer;
	int quantity;
	int unitPrice;
	int daysDue;
	int perDayPenalty;
	
	@Slot(mandatory = true)
	public Phone getPhone() {
		return phone;
	}
	public void setPhone(Phone phone) {
		this.phone = phone;
	}
	
	@Slot(mandatory = true)
	public AID getBuyer() {
		return buyer;
	}
	public void setBuyer(AID buyer) {
		this.buyer = buyer;
	}
	
	@Slot(mandatory = true)
	public int getQuantity() {
		return quantity;
	}
	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}
	
	@Slot(mandatory = true)
	public int getUnitPrice() {
		return unitPrice;
	}
	public void setUnitPrice(int unitPrice) {
		this.unitPrice = unitPrice;
	}
	
	@Slot(mandatory = true)
	public int getDaysDue() {
		return daysDue;
	}
	public void setDaysDue(int daysDue) {
		this.daysDue = daysDue;
	}
	
	@Slot(mandatory = true)
	public int getPerDayPenalty() {
		return perDayPenalty;
	}
	public void setPerDayPenalty(int perDayPenalty) {
		this.perDayPenalty = perDayPenalty;
	}
	
	
}
