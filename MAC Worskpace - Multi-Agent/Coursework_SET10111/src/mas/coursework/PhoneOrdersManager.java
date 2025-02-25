package mas.coursework;

import java.util.ArrayList;
import java.util.HashMap;

import jade.content.onto.OntologyException;
import mas.coursework_ontology.elements.Component;
import mas.coursework_ontology.elements.SellComponents;
import mas.coursework_ontology.elements.SellPhones;

public class PhoneOrdersManager {

	private HashMap<SellPhones, Integer> phoneOrders;
	private HashMap<SellPhones, Integer> assemblyRemaining;
	private ArrayList<SellPhones> phoneOrdersToday;
	
	public PhoneOrdersManager(){
		phoneOrders = new HashMap<>();
		assemblyRemaining = new HashMap<>();
		phoneOrdersToday = new ArrayList<>();
	}
	
	public void acceptOrder(SellPhones order){
		phoneOrders.put(order, 0);
		assemblyRemaining.put(order, order.getQuantity());
	}

	public void incrementNewDay() {
		for(SellPhones phoneOrder : phoneOrders.keySet()){
			int currentAmnt = phoneOrders.get(phoneOrder);
			phoneOrders.put(phoneOrder, currentAmnt + 1);
		}
	}
	
	public void assemble(SellPhones order, int amount){
		int currRemaining = assemblyRemaining.get(order);
		assemblyRemaining.put(order, currRemaining-amount);
	}
	
	public int remainingAssembly(SellPhones order){
		return assemblyRemaining.get(order);
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
	
	public String orderToString(SellPhones order) {
		String result = "";
		result += order;
		result += "\t\tDue: " + order.getDaysDue() + "\n";
		result += "\t\tPerDayPenalty: " + order.getPerDayPenalty() + "\n";
		result += "\t\tUnitPrice: " + order.getUnitPrice() + "\n";
		result += "\t\tUnits: " + order.getQuantity() + "\n";
		try {
			result += "\t\tscreen: " + order.getPhone().getScreen().getIdentifier() + "\n";
			result += "\t\tbattery: " + order.getPhone().getBattery().getIdentifier() + "\n";
		} catch (OntologyException e) {
			e.printStackTrace();
		}
		result += "\t\tram:" + order.getPhone().getRam().getIdentifier()  + "\n";
		result += "\t\tstorage: " + order.getPhone().getStorage().getIdentifier()  + "\n";
		return result;
	}
	
	public int calculateDueIn(SellPhones order){
		int result = (order.getDaysDue() - phoneOrders.get(order));
		return result;
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
	
	public void matrixToString(HashMap<SellPhones, Double> matrix){
		for(SellPhones order : matrix.keySet()){
			System.out.println("\t"+ order + " - urgency: " + matrix.get(order));
		}
	}

	public ArrayList<SellPhones> getNewOrders() {
		return phoneOrdersToday;
	}
	
	public HashMap<SellPhones, Integer> getPhoneOrdersMatrix(){
		return phoneOrders;
	}
	
	/*
	 * Takes a SellPhones object (order) and returns it's components in an arraylist
	 */
	public static ArrayList<Component> getPhoneOrderComponents(SellPhones order){
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

	public void phoneOrderReceived(SellPhones order) {
		phoneOrdersToday.add(order);
	}

	public void clearTodaysOrders() {
		phoneOrdersToday.clear();
	}

	public void shipOrder(SellPhones shippedOrder) {
		phoneOrders.remove(shippedOrder);
		assemblyRemaining.remove(shippedOrder);
	}

}
