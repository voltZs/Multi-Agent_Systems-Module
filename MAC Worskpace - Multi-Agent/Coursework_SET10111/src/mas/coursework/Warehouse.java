package mas.coursework;

import java.util.HashMap;

import mas.coursework_ontology.elements.Component;

public class Warehouse {
	int dailyCharge;
	HashMap<String, WarehouseSection> sections;
	
	public Warehouse(int dailyCharge){
		this.dailyCharge = dailyCharge;
		
		sections = new HashMap<>();
	}
	
	public Double calculateStorageCharge(){
		Double charge = 0.0;
		for (String sectionType : sections.keySet()){
			charge += sections.get(sectionType).sumSectionCharges();
		}
		return charge;
	}
	
	public void addToWarehouse(Component newComponent){
		String type = newComponent.getType();
		if(sections.get(type) == null){
			sections.put(type, new WarehouseSection());
		}
		sections.get(type).addToSection(newComponent);
	}
	
	/*
	 * Return the amount of items with specs in stock
	 */
	public int checkStock(String type, String identifier){
		int amount = 0;
		WarehouseSection section = sections.get(type);
		if(section != null){
			amount += section.checkSectionStock(identifier);
		}
		return amount;
	}
	
	/*
	 * Increment number of days stored for every item in warehouse
	 */
	public void incrementNewDay(){
		for(String sectionType : sections.keySet()){
			sections.get(sectionType).incrementNewDay();
		}
	}
	
	public String toString(){
		String result = "";
		result += "====== Warehouse stock ======\n";
		for(String sectionType : sections.keySet()){
			result += ("Section: "+ sectionType + "\n");
			result += "\t" + sections.get(sectionType).toString();
		}
		result += "=============================";
		return result;
	}
	
//	INNER CLASS ======================================
	private class WarehouseSection{
		HashMap<String, Aisle> aisles;
		public WarehouseSection(){
			aisles = new HashMap<>();
		}
		
		public void incrementNewDay() {
			for( String aisleIdentifier : aisles.keySet()){
				aisles.get(aisleIdentifier).incrementNewDay();
			}
		}

		public Double sumSectionCharges(){
			Double sum = 0.00;
			for(String aisleIdentifier: aisles.keySet()){
				sum += aisles.get(aisleIdentifier).sumAisleCharges();
			}
			return sum;
		}
		
		public void addToSection(Component comp){
			String compIdentifier = comp.getIdentifier();
			if(aisles.get(compIdentifier) == null){
				aisles.put(compIdentifier, new Aisle());
			}
			aisles.get(compIdentifier).addToAisle(comp);
		}
		
		public int checkSectionStock(String identifier) {
			int sectionAmount = 0;
			Aisle aisle = aisles.get(identifier);
			if(aisle != null){
				sectionAmount = aisle.checkAisleStock();
			}
			return sectionAmount;
		}
		
		public String toString(){
			String result = "";
			for(String aisleIdentifier : aisles.keySet()){; 
				result+= "Aisle: " + aisleIdentifier + "\n";
				result+= "\t";
				result+= aisles.get(aisleIdentifier).toString();
			}
			return result;
		}

		
//		INNER INNER CLASS ======================================
		private class Aisle{
			HashMap<Component, Integer> stock;
			
			public Aisle(){
				stock = new HashMap<>();
			}

			public void incrementNewDay() {
				for(Component comp : stock.keySet()){
					int currentAmnt = stock.get(comp);
					stock.put(comp, currentAmnt+1);
				}
			}

			public Double sumAisleCharges(){
				Double sum = 0.0;
				for(Component comp : stock.keySet()){
					sum += stock.get(comp) * dailyCharge;
				}
				return sum;
			}

			public void addToAisle(Component comp) {
				stock.put(comp, 0);
			}
			
			public int checkAisleStock() {
				return stock.keySet().size();
			}
			
			public String toString(){
				String result = "";
				for(Component comp : stock.keySet()){
					result+= "\t" + comp + " - days stored: " + stock.get(comp) + "\n";
				}
				return result;
			}
		}
	}
}
