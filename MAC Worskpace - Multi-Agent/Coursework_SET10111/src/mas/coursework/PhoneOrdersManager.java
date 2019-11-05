package mas.coursework;

import java.util.ArrayList;
import java.util.HashMap;

import jade.content.onto.OntologyException;
import mas.coursework_ontology.elements.Component;
import mas.coursework_ontology.elements.SellComponents;
import mas.coursework_ontology.elements.SellPhones;

public class PhoneOrdersManager {

	private HashMap<SellPhones, Integer> phoneOrders;
	
	public PhoneOrdersManager(){
		phoneOrders = new HashMap<>();
	}
	
	public void addOrder(SellPhones order){
		phoneOrders.put(order, 0);
	}

	public void incrementNewDay() {
		for(SellPhones phoneOrder : phoneOrders.keySet()){
			int currentAmnt = phoneOrders.get(phoneOrder);
			phoneOrders.put(phoneOrder, currentAmnt + 1);
		}
	}
	
	/*
	 * This is to be called before a new day is incremented, and after all orders have been shipped for the day
	 */
	public Double calculateLateOrders(){
		Double penalty = 0.0;
		for(SellPhones phoneOrder : phoneOrders.keySet()){
			int due = phoneOrder.getDaysDue();
			int dailyPenalty = phoneOrder.getPerDayPenalty();
			int currentDay = phoneOrders.get(phoneOrder);
//			including the due day as this method is to be called after the orders for the day have been shipped - meaning there's a penalty for "today"
			if(currentDay >= due){
				penalty += calculatePenalty(phoneOrder);
			}
		}
		return penalty;
	}

	public String ordersToString() {
		String result = "";
		for(SellPhones order : phoneOrders.keySet()){
			result += "Phone Order:\n"
					+"\tPending:" + phoneOrders.get(order) + "\n"
					+"\tOverdue: " + (phoneOrders.get(order) - order.getDaysDue() +1) + "\n"
					+"\tPenalty now: " + calculatePenalty(order) + "\n";
			result += orderToString(order);
		}
		return result;
	}
	
	private String orderToString(SellPhones order) {
		String result = "";
		result += "\t\tDue: " + order.getDaysDue() + "\n";
		result += "\t\tPerDayPenalty: " + order.getPerDayPenalty() + "\n";
		result += "\t\tPhone:\n";
		try {
			result += "\t\tscreen: " + order.getPhone().getScreen() + "\n";
			result += "\t\tbattery: " + order.getPhone().getBattery()+ "\n";
		} catch (OntologyException e) {
			e.printStackTrace();
		}
		result += "\t\tram:" + order.getPhone().getRam() + "\n";
		result += "\t\tstorage: " + order.getPhone().getStorage() + "\n";
		return result;
	}

	public Double calculateUrgency(SellPhones order){
		return 1.0;
	}
	
	public int calculatePenalty(SellPhones order){
		int result = (phoneOrders.get(order) - order.getDaysDue()) +1;
		if(result < 0){
			result = 0;
		} else {
			result *= order.getPerDayPenalty();
		}
		return result;
	}
	
	public HashMap<SellPhones, Double> generateUrgencyMatrix(ArrayList<SellPhones> orders){
		HashMap<SellPhones, Double> matrix = new HashMap<>();
		for(SellPhones order : orders){
			matrix.put(order, calculateUrgency(order));
		}
		return matrix;
	}
	
	public void matrixToString(HashMap<SellPhones, Double> matrix){
		for(SellPhones order : matrix.keySet()){
			System.out.println("\t"+ order + " - urgency: " + matrix.get(order));
		}
	}

	public ArrayList<SellPhones> getNewOrders() {
		ArrayList<SellPhones> newOrders = new ArrayList<>();
		for(SellPhones order : phoneOrders.keySet()){
			if(phoneOrders.get(order) == 0){
				newOrders.add(order);
			}
		}
		return newOrders;
	}
	
	/*
	 * Takes a SellPhones object (order) and returns it's components in an arraylist
	 */
	public ArrayList<Component> getPhoneOrderComponents(SellPhones order){
		ArrayList<Component> components = new ArrayList<>();
		try {
			components.add(order.getPhone().getScreen());
			components.add(order.getPhone().getRam());
			components.add(order.getPhone().getBattery());
			components.add(order.getPhone().getStorage());
		} catch (OntologyException e) {
			e.printStackTrace();
		}
		return components;
	}

}
