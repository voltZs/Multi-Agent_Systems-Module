package mas.coursework;

import java.util.ArrayList;
import java.util.HashMap;

import mas.coursework_ontology.elements.Battery;
import mas.coursework_ontology.elements.Component;
import mas.coursework_ontology.elements.RAM;
import mas.coursework_ontology.elements.Screen;
import mas.coursework_ontology.elements.Storage;

public class SupplierCatalogue {
	
	private ArrayList<String[]> catalogue;
	
	public SupplierCatalogue(int type){
		catalogue = new ArrayList<>();
		ArrayList<String[]> csv = CSVReader.readCSV(
				"/Users/zsoltvarga/Napier/2019-2020/Trimester_1_(y4)/"
				+ "Multi-Agent Systems/Multi-Agent_Systems-Module/"
				+ "MAC Worskpace - Multi-Agent/Coursework_SET10111/"
				+ "Sellers_catalogue.csv");
		for (int i = 0; i<csv.size(); i++) {
			String[] line = csv.get(i);
			if (line.length >= 2 + type) {
				String[] newLine = {line[0], line[1], line[1+type]};
				catalogue.add(newLine);
			}
		}
	}
	
	public void printCalatogue(){
		for(String[] array : catalogue){
			for(String string : array){
				System.out.print(string+"\t");
			}
			System.out.println();
		}
	}
	
	public int checkIfSold(String componentType, String identifier){
		int price = 0;
		for(String[] array : catalogue){
			if(array[0].equals(componentType) && array[1].equals(identifier)){
				return Integer.parseInt(array[2]);
			}
		}
		return price;
	}
}
