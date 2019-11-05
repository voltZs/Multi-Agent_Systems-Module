package mas.coursework;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import mas.coursework_ontology.elements.Component;
import mas.coursework_ontology.elements.OrderPair;
import mas.coursework_ontology.elements.SellComponents;

public class Warehouse {
	int dailyCharge;
	HashMap<String, WarehouseSection> sections;
	ArrayList<SellComponents> purchasedToday;
	ArrayList<ExpectedDelivery> expectedDeliveries;

	public Warehouse(int dailyCharge) {
		this.dailyCharge = dailyCharge;

		sections = new HashMap<>();
		purchasedToday = new ArrayList<>();
		expectedDeliveries = new ArrayList();
	}

	public Double calculateStorageCharge() {
		Double charge = 0.0;
		for (String sectionType : sections.keySet()) {
			charge += sections.get(sectionType).sumSectionCharges();
		}
		return charge;
	}

	/*
	 * Return the amount of items with specs in stock
	 */
	public int checkStock(String type, String identifier) {
		int amount = 0;
		WarehouseSection section = sections.get(type);
		if (section != null) {
			amount += section.checkSectionStock(identifier);
		}
		return amount;
	}

	public String toString() {
		String result = "";
		result += "====== Warehouse stock ======\n";
		for (String sectionType : sections.keySet()) {
			result += ("Section: " + sectionType + "\n");
			result += sections.get(sectionType).toString();
		}
		result += "=============================";
		return result;
	}

	public void receiveComponents(SellComponents order) {
		for (OrderPair pair : order.getOrderPairs()) {
			Component comp = (Component) pair.getOrderedItem();
			int amount = pair.getQuantity();
			System.out.println("Adding " + amount + " of " + comp.getType() + ", " + comp.getIdentifier());
			for (int i = 0; i < amount; i++) {
				expectedDeliveries.add(new ExpectedDelivery(comp, order.getDeliveryTime()));
			}
		}
		// also keep track of what component orders were received from suppliers
		// today - this is cleared every time a new day is incremented
		purchasedToday.add(order);
	}

	/*
	 * Increment number of days stored for every item in warehouse
	 */
	public void incrementNewDay() {
		ArrayList<ExpectedDelivery> moveThese = new ArrayList<>();
		for (ExpectedDelivery delivery : expectedDeliveries) {
			delivery.incrementDays();
			if (delivery.daysTillArrival == 0) {
				moveThese.add(delivery);
			}
		}
		for (ExpectedDelivery delivery : moveThese) {
			addToWarehouse(delivery.component);
			expectedDeliveries.remove(expectedDeliveries.indexOf(delivery));
		}
		purchasedToday.clear();
	}

	public void addToWarehouse(Component newComponent) {
		String type = newComponent.getType();
		if (sections.get(type) == null) {
			sections.put(type, new WarehouseSection());
		}
		sections.get(type).addToSection(newComponent);
	}

	public ArrayList<SellComponents> getTodaysPurchases() {
		return purchasedToday;
	}

	// INNER CLASS ======================================
	private class WarehouseSection {
		HashMap<String, Aisle> aisles;

		public WarehouseSection() {
			aisles = new HashMap<>();
		}

		public Double sumSectionCharges() {
			Double sum = 0.00;
			for (String aisleIdentifier : aisles.keySet()) {
				sum += aisles.get(aisleIdentifier).sumAisleCharges();
			}
			return sum;
		}

		public void addToSection(Component comp) {
			String compIdentifier = comp.getIdentifier();
			if (aisles.get(compIdentifier) == null) {
				aisles.put(compIdentifier, new Aisle());
			}
			aisles.get(compIdentifier).addToAisle(comp);
		}

		public int checkSectionStock(String identifier) {
			int sectionAmount = 0;
			Aisle aisle = aisles.get(identifier);
			if (aisle != null) {
				sectionAmount = aisle.checkAisleStock();
			}
			return sectionAmount;
		}

		public String toString() {
			String result = "";
			for (String aisleIdentifier : aisles.keySet()) {
				;
				result += "\tAisle: " + aisleIdentifier + "\n";
				result += aisles.get(aisleIdentifier).toString();
			}
			return result;
		}

		// INNER INNER CLASS ======================================
		private class Aisle {
			Component component;
			int amount;

			public Aisle() {
				component = null;
				amount = 0;
			}

			public int sumAisleCharges() {
				int result = dailyCharge * amount;
				return result;
			}

			public void addToAisle(Component comp) {
				if(component == null){
					this.component = comp;
				}
				amount++;
			}

			public int checkAisleStock() {
				return amount;
			}

			public String toString() {
				String result = "";
				if(component != null && amount != 0){
					result += "\t\t-Amount: " + amount + "\n";
				}
				return result;
			}
		}
	}

	// INNER CLASS =======
	private class ExpectedDelivery {
		public Component component;
		public int daysTillArrival;

		public ExpectedDelivery(Component component, int daysTillArrival) {
			this.component = component;
			this.daysTillArrival = daysTillArrival;
		}

		public void incrementDays() {
			daysTillArrival++;
		}
	}
}
