package mas.coursework;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import jade.content.onto.OntologyException;
import jade.core.AID;
import mas.coursework_ontology.elements.Component;
import mas.coursework_ontology.elements.HasInStock;
import mas.coursework_ontology.elements.OrderPair;
import mas.coursework_ontology.elements.SellComponents;
import mas.coursework_ontology.elements.SellPhones;

public class ProfitBrain {
	private ArrayList<Double> dailyProfits;
	private HashMap<String, HashMap<String, ArrayList<BuyingOption>>> componentMarket = new HashMap<>();
	
	public ProfitBrain(){
		dailyProfits = new ArrayList<>();
		componentMarket = new HashMap<>();
	}
	
	/*
	 * Returns boolean - whether order is perceived to increase profit
	 */
	public boolean decideOnPhoneOrder(SellPhones order, PhoneOrdersManager manager) {
		return true;
	}
	
	/*
	 * Decide what orders to submit to the suppliers - returns an array of orders, one order per supplier
	 */
	public ArrayList<SellComponents> decideWhatToOrder(Warehouse warehouse, PhoneOrdersManager phoneOrdersMngr) {
		ArrayList<SellComponents> componentOrders = new ArrayList<>();
//		Right now just orders all components for phones that were ordered today
		SellComponents componentsOrder1 = new SellComponents();
//		AND ONLY ORDERS COMPONENTS FROM SUPPLIER ONE - NEEDS TO BBE CHANGED !!!  - based on buying options in componentMarket HashMap - right now it's set in teh manufacturer's behvaiour!!!
		ArrayList<OrderPair> orderPairs = new ArrayList<>();
		OrderPair pair = new OrderPair();
		for(SellPhones phoneOrder : phoneOrdersMngr.getNewOrders()){
			int amount = phoneOrder.getQuantity();
			ArrayList<Component> components = phoneOrdersMngr.getPhoneOrderComponents(phoneOrder);
			for (Component comp : components){
				pair = new OrderPair();
				pair.setOrderedItem(comp);
				pair.setQuantity(amount);
				orderPairs.add(pair);
			}
		}
		componentsOrder1.setOrderPairs(orderPairs);
		componentOrders.add(componentsOrder1);
		
		return componentOrders;
	}
	
	/*
	 * Decide what phone orders to ship today - only income of money
	 * (only 50 phones a day in total may be shipped)
	 * returns an array of PhoneOrders to sell
	 */
	public ArrayList<SellPhones> decideWhatToShip(Warehouse warehouse, PhoneOrdersManager phoneOrdersMngr){
		ArrayList<SellPhones> toBeShipped = new ArrayList<>();
//		CHANGE THIS - RIGHT NOW SHIPPING FIRST X ORDERS THAT CAN BE SHIPPED (all components in stock)!
//		
//		
		return toBeShipped;
	}
	
	/*
	 * Calculates this day's profit based on Total shipped orders' value minus charges for late orders
	 * minus storage charges in warehouse minus value of purchased components
	 */
	public void todaysProfit(Warehouse warehouse, PhoneOrdersManager phoneOrdersMngr) {
		Double totalOrdersShipped = totalValueOfOrdersShipped();
		Double lateOrderCharges= phoneOrdersMngr.calculateLateOrders();
		Double storageCharges = warehouse.calculateStorageCharge();
		Double componentPurchases = componentPurchaseValue(warehouse.getTodaysPurchases());
		System.out.println("Shipped orders: "+ totalOrdersShipped+ "\n" +
							"Late orders: "+ lateOrderCharges+ "\n" +
							"Storage chrgs: "+ storageCharges+ "\n" +
							"Component prchss: "+ componentPurchases + "\n");
		Double todaysProfit = totalOrdersShipped - lateOrderCharges -storageCharges - componentPurchases;
		
		dailyProfits.add(todaysProfit);
	}
	
	/*
	 * Return an array of profits for every day from the start of simulation
	 */
	public ArrayList<Double> getDailyProfits(){
		return dailyProfits;
	}
	
	private Double totalValueOfOrdersShipped() {
		return 0.0;
	}

	private Double componentPurchaseValue(ArrayList<SellComponents> todaysPurchases) {
		Double sum = 0.0;
//		System.out.println("Today's purchases from warehouse" + todaysPurchases);
		for(SellComponents order : todaysPurchases){
			AID seller = order.getSeller();
			for (OrderPair pair : order.getOrderPairs()){
				int amnt = pair.getQuantity();
				Component comp = (Component) pair.getOrderedItem();
//				System.out.println("Purchased " + amnt + " components - " + comp.getType() + ", " + comp.getIdentifier());
				ArrayList<BuyingOption> options = componentMarket.get(comp.getType()).get(comp.getIdentifier());
				System.out.println(options);
				for (BuyingOption option : options){
					if(option.supplierID.equals(seller)){
//						System.out.println(" for " + option.price * amnt);
						sum += option.price * amnt;
						break;
					}
				}
			}
		}
		return sum;
	}

	public void updateComponentMarket(HasInStock stockUpdate){
		Component comp = (Component) stockUpdate.getItem();
		int deliveryTime = stockUpdate.getDeliveryTime();
		int price = stockUpdate.getPrice();
		AID seller = stockUpdate.getOwner();
		BuyingOption option = new BuyingOption(seller, price, deliveryTime);
		
//		System.out.println("Adding " + comp + " price " + price + " seller " + seller);
		HashMap<String, ArrayList<BuyingOption>> optionsByType = componentMarket.get(comp.getType());
		ArrayList<BuyingOption> optionsByIdent = null;
		if(optionsByType == null){
			optionsByType = new HashMap<>();
			componentMarket.put(comp.getType(), optionsByType);
		} else {
			optionsByIdent = optionsByType.get(comp.getIdentifier());
		}
		if(optionsByIdent == null){
			optionsByIdent = new ArrayList<>();
			optionsByIdent.add(option);
			componentMarket.get(comp.getType()).put(comp.getIdentifier(), optionsByIdent);
		} else {
//			options for this component already exist - check if this specific option is existent
			for(BuyingOption existingOpt : optionsByIdent){
				if(existingOpt.supplierID.equals(option.supplierID)){
					return;
				}
			}
			optionsByIdent.add(option);
		}
	}
	
	public String compMarketToString(){
		String result = "";
		for(String type : componentMarket.keySet()){
			result += "\ttype: " + type + "\n";
			for(String identifier : componentMarket.get(type).keySet()){
				result += "\t\tidentifier: " + identifier + "\n";
				for(BuyingOption option : componentMarket.get(type).get(identifier)){
					result+="\t\t\tOption:\n";
					result += "\t\t\t\tsupplier: " + option.supplierID + "\n";
					result += "\t\t\t\tprice: " + option.price + "\n";
					result += "\t\t\t\tdeliv time: " + option.deliveryTime + "\n";
				}
			}
			
		}
		return result;
	}
	
	private class BuyingOption{
		public AID supplierID;
		public int price;
		public int deliveryTime;	
		
		public BuyingOption(AID supplierID, int price, int deliveryTime){
			this.supplierID = supplierID;
			this.price = price;
			this.deliveryTime = deliveryTime;
		}
	}

}


