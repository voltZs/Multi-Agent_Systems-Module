package mas.coursework_ontology.elements;

import jade.content.onto.annotations.Slot;

public class OrderPair {
	private Item item;
	private int quantity;
	
	@Slot(mandatory = true)
	public Item getOrderedItem() {
		return item;
	}
	public void setOrderedItem(Item orderedItem) {
		this.item = orderedItem;
	}
	
	@Slot(mandatory = true)
	public int getQuantity() {
		return quantity;
	}
	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}
}
