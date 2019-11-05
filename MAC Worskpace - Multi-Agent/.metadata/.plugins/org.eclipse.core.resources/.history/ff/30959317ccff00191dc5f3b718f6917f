package mas.coursework;

import java.util.HashMap;

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
				penalty += (currentDay-due+1) * dailyPenalty;
			}
		}
		return penalty;
	}
	
}
