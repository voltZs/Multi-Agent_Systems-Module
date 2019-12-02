package mas.coursework;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import jade.content.onto.OntologyException;
import jade.core.AID;
import mas.coursework_ontology.elements.Component;
import mas.coursework_ontology.elements.HasInStock;
import mas.coursework_ontology.elements.OrderPair;
import mas.coursework_ontology.elements.Phone;
import mas.coursework_ontology.elements.SellComponents;
import mas.coursework_ontology.elements.SellPhones;

public class ProfitBrain {
	private ArrayList<Double> dailyProfits;
	private HashMap<String, HashMap<String, ArrayList<BuyingOption>>> componentMarket = new HashMap<>();
	private ArrayList<SellPhones> shipToday;
	
	public ProfitBrain(){
		dailyProfits = new ArrayList<>();
		componentMarket = new HashMap<>();
		shipToday  = new ArrayList<>();
	}
	
	/*
	 * Returns boolean - whether order is perceived to increase profit
	 */
	public ArrayList<SellPhones> decideOnPhoneOrders(PhoneOrdersManager phoneOrdersMngr, Warehouse warehouse) {
		System.out.println("DECIDING WHAT PHONE ORDERS TO ACCEPT: =====================");
		ArrayList<SellPhones>  acceptedOrders = new ArrayList<>();
		ArrayList<SellPhones> receivedOrders = phoneOrdersMngr.getNewOrders();
		HashMap<SellPhones, Integer> pendingOrders = phoneOrdersMngr.getPhoneOrdersMatrix();
		
//		FOR NOW JUST ACCEPTING FIRST X ORDERS IF THEY CAN BE MANUFACTURED ON THE DAY THEYRE DUE
		for(SellPhones order : receivedOrders){
			int sumOfPhones = 0;
			for(SellPhones pendOrd : pendingOrders.keySet()){
				if(phoneOrdersMngr.calculateDueIn(pendOrd) == order.getDaysDue()){
					sumOfPhones += pendOrd.getQuantity();
				}
			}
			if(sumOfPhones + order.getQuantity() <=50){
				acceptedOrders.add(order);
			} else {
				break;
			}
		}
		return acceptedOrders;
	}
	
	/*
	 * Decide what orders to submit to the suppliers - returns an array of orders, one order per supplier
	 */
	public ArrayList<SellComponents> decideWhatToOrder(Warehouse warehouse, PhoneOrdersManager phoneOrdersMngr) {
		ArrayList<SellComponents> componentOrders = new ArrayList<>();
		
		System.out.println("DECIDING WHAT COMPONENTS TO ORDER: =====================");
		
		HashMap<SellPhones, Integer> pendingPhoneOrders = phoneOrdersMngr.getPhoneOrdersMatrix();
		for(SellPhones phoneOrder : pendingPhoneOrders.keySet()){
			int dueIn = phoneOrdersMngr.calculateDueIn(phoneOrder);
			System.out.println("\nChecking this order: "+ phoneOrder.getQuantity() + " phones due in " + dueIn);
			for(Component comp : PhoneOrdersManager.getPhoneOrderComponents(phoneOrder)){
				System.out.println("Component: "+ ((Component)comp).getType() + " - " + ((Component)comp).getIdentifier());
				BuyingOption cheapestOpt = getCheapestOption(comp);
				BuyingOption soonestOpt = getSoonestOption(comp);
				
				OrderPair pair = new OrderPair();
				pair.setOrderedItem(comp);
				pair.setQuantity(phoneOrder.getQuantity());
//				System.out.println("\tCheapest: \tdeliveryTime: " + cheapestOpt.deliveryTime*-1 + "\tprice: " + cheapestOpt.price + "\tseller: " + cheapestOpt.supplierID);
//				System.out.println("\tSoonest: \tdeliveryTime: " + soonestOpt.deliveryTime*-1 + "\tprice: " + soonestOpt.price + soonestOpt.supplierID);
				boolean cheapestBetter = ((cheapestOpt.deliveryTime*-1) - (soonestOpt.deliveryTime*-1)) * warehouse.getDaliyChargeAmnt() 
										< soonestOpt.price - cheapestOpt.price; 
				
				if(dueIn == cheapestOpt.deliveryTime*-1 && cheapestBetter){
					System.out.println("Cheapest hit");
					buildComponentOrder(componentOrders, cheapestOpt, pair);
				} else if(dueIn < cheapestOpt.deliveryTime*-1 && dueIn > soonestOpt.deliveryTime*-1){
//					DO NOTHING WE ARE TOO LATE TO ORDER FROM CHEAPEST BUT TOO SOON TO ORDER FROM SOONEST
				} else if(dueIn == soonestOpt.deliveryTime*-1){
					System.out.println("Soonest hit");
					buildComponentOrder(componentOrders, soonestOpt, pair);
				} else {
//					WE STILL HAVE TIME TO ORDER from cheapest
				}
			}
		}
		
		return componentOrders;
	}
	
	private void buildComponentOrder(ArrayList<SellComponents> componentOrders, BuyingOption buyingOpt, OrderPair pair){
		SellComponents componentOrder = null;
		ArrayList<OrderPair> pairs = null;
		
		for(SellComponents order : componentOrders){
			if(order.getSeller().equals(buyingOpt.supplierID)){
				componentOrder = order;
				pairs = (ArrayList<OrderPair>) componentOrder.getOrderPairs();
			}
		}
		if(componentOrder == null){
			componentOrder = new SellComponents();
			componentOrder.setSeller(buyingOpt.supplierID);
			componentOrders.add(componentOrder);
			pairs = new ArrayList();
		}
		pairs.add(pair);
		componentOrder.setOrderPairs((List<OrderPair>) pairs);
	}

	/*
	 * Decide what phone orders to ship today - only income of money
	 * (only 50 phones a day in total may be shipped)
	 * returns an array of PhoneOrders to sell
	 */
	public ArrayList<SellPhones> decideWhatToShip(Warehouse warehouse, PhoneOrdersManager phoneOrdersMngr){
		HashMap<SellPhones, Integer> pendingOrders = phoneOrdersMngr.getPhoneOrdersMatrix();
		for(SellPhones order : pendingOrders.keySet()){
			if(phoneOrdersMngr.calculateDueIn(order) == 0){
				shipToday.add(order);
			}
		}
		
		return shipToday;
	}
	
//	public ArrayList<SellPhones> decideWhatToShip(Warehouse warehouse, PhoneOrdersManager phoneOrdersMngr){
////		THIS ONE USES URGENCY MATRIX EHH
//		HashMap<SellPhones, Double> urgencyMatrix = new HashMap(); ;
//		
////		CREATE A SHIPPING URGENCY MATRIX BUT ONLY DEAL WITH ONES WE HAVE ENOUGH COMPONENTS IN STOCK FOR
//		HashMap<SellPhones, Double> temp = phoneOrdersUrgencyMatrix(phoneOrdersMngr.getPhoneOrdersMatrix());
//		for(SellPhones order : temp.keySet()){
//			if (warehouse.checkStockForOrder(order)){
//				urgencyMatrix.put(order, temp.get(order));
//			}
//		}
////		RETURN IF THERE'S NOTHING TO SHIP
//		if(urgencyMatrix.isEmpty()){
//			return shipToday;
//		}
//		
//		Warehouse warehouseCopy = (Warehouse) warehouse.cloneWarehouse();
//		int phonesChosen = 0;
//		while(true){
//			Double urgency = 0.0;
//			SellPhones mostUrgent = null;
//			System.out.println("Valid orders: "+ urgencyMatrix.size());
//			for(SellPhones phoneOrder : urgencyMatrix.keySet()){
////				System.out.println("Order: " + phoneOrder);
////				System.out.println(urgencyMatrix.get(phoneOrder));
//				if(urgencyMatrix.get(phoneOrder) >= urgency){
//					urgency = urgencyMatrix.get(phoneOrder) ;
//					mostUrgent = phoneOrder;
//				}
////				System.out.println("Inloop decision: " + mostUrgent);
//			}
////			System.out.println("Most urgent now is: " + mostUrgent);
//			
//			boolean underFiftyLimit = phonesChosen + mostUrgent.getQuantity() <= 50;
//			boolean enoughStock = warehouseCopy.checkStockForOrder(mostUrgent);
//			if(underFiftyLimit && enoughStock){
//				System.out.println("Adding to shipping" + mostUrgent + " with urgency " + urgencyMatrix.get(mostUrgent));
//				shipToday.add(mostUrgent);
//				urgencyMatrix.remove(mostUrgent);
//				phonesChosen += mostUrgent.getQuantity();
//				warehouseCopy.assembleOrder(mostUrgent);
//			} else {
//				System.out.println("underfiftylimit: " +underFiftyLimit + " enoughstock: " + enoughStock);
//				urgencyMatrix.remove(mostUrgent);
//			}
//			if(urgencyMatrix.size() == 0 || phonesChosen == 50){
//				System.out.println("Matrix size: " + urgencyMatrix.size() + " phones chosen: "+ phonesChosen);
//				break;
//			}
//		}
//		return shipToday;
//	}
	
	private HashMap<SellPhones, Double> phoneOrdersUrgencyMatrix(HashMap<SellPhones, Integer> ordersMatrix) {
		HashMap<SellPhones, Double> urgencyMatrix = new HashMap();
		
		HashMap<SellPhones, Integer> idealProfitMatrix = new HashMap();
		int minProfit = 0;
		int maxProfit = 0;
		boolean initialised = false;
		for(SellPhones order : ordersMatrix.keySet()){
			int idealProfit = (order.getUnitPrice()*order.getQuantity()) - estimateAssemblyPrice(order);		
			idealProfitMatrix.put(order, idealProfit);
			if(idealProfit< minProfit || !initialised){
				minProfit = idealProfit;
				initialised = true;
			}
			if(idealProfit> maxProfit){
				maxProfit = idealProfit;
			}
		}
		System.out.println("MIN: " + minProfit + " MAX: " + maxProfit);
		for(SellPhones order : ordersMatrix.keySet()){
			int idealProfit = idealProfitMatrix.get(order);
			Double penaltyProportion = (double)order.getPerDayPenalty() / (double)idealProfit;
			Double overdueUrgency = mapOverdueUrgency((double)ordersMatrix.get(order));
//			mapping (min,max) to (0,1)
			Double orderImportance =((double)idealProfit-minProfit)/(maxProfit-minProfit);
			
			Double shipmentUrgency = orderImportance * (1 + (penaltyProportion * overdueUrgency));
			urgencyMatrix.put(order, shipmentUrgency);
			
			System.out.println("idProfit: " + idealProfit + "\tdailyPen:" + order.getPerDayPenalty() + "\toverdue: " + ordersMatrix.get(order) );
			System.out.println(orderImportance + " * (1+ " + penaltyProportion + " * " + overdueUrgency + ") = " + shipmentUrgency);
		}
		return urgencyMatrix;
	}

	private int estimateAssemblyPrice(SellPhones order) {
		ArrayList<Component> phoneComponents = PhoneOrdersManager.getPhoneOrderComponents(order);
		int total = 0;
		for(Component comp : phoneComponents){
			int maxPrice = 0;
			for(BuyingOption opt : getComponentBuyingOptions(comp)){
				if(opt.price>maxPrice){
					maxPrice = opt.price;
				}
			}
			total += maxPrice;
		}
		total *= order.getQuantity();
		return 0;
	}
	
	public Double mapOverdueUrgency(Double daysOverdue){
		if(daysOverdue < 0){
			return (daysOverdue+10)/10;
		} else {
			return (daysOverdue +1) ;
		}
	}

	/*
	 * Calculates this day's profit based on Total shipped orders' value minus charges for late orders
	 * minus storage charges in warehouse minus value of purchased components
	 * and then resets shipped orders 
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
		shipToday.clear();
	}
	
	/*
	 * Return an array of profits for every day from the start of simulation
	 */
	public ArrayList<Double> getDailyProfits(){
		return dailyProfits;
	}
	
	public ArrayList<SellPhones> getShipToday(){
		return shipToday;
	}
	
	private Double totalValueOfOrdersShipped() {
		Double sum = 0.0;
		for(SellPhones shipped : shipToday){
			sum += shipped.getUnitPrice() * shipped.getQuantity();
		}
		return sum;
	}

	private Double componentPurchaseValue(ArrayList<SellComponents> todaysPurchases) {
		Double sum = 0.0;
//		System.out.println("Today's purchases from warehouse" + todaysPurchases);
		for(SellComponents order : todaysPurchases){
			System.out.println("In todays componenet purchases: ");
			System.out.println("\t seller " + order.getSeller());
			for(OrderPair pair : order.getOrderPairs()){
				System.out.println("item: "+ ((Component)pair.getOrderedItem()).getType() + " - " 
									+ ((Component)pair.getOrderedItem()).getIdentifier());
			}
		}
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
						System.out.println("bought"+ comp.getType() + " - " + comp.getIdentifier() +" for " + option.price + " * "+ amnt);
						System.out.println("bought from: " +seller);
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
	
	private BuyingOption getSoonestOption(Component comp) {
		ArrayList<BuyingOption> soonestOptions = getComponentBuyingOptions(comp);
		BuyingOption soonestOption = soonestOptions.get(0);
		for(BuyingOption opt : soonestOptions){
			if (opt.deliveryTime*-1 < soonestOption.deliveryTime*-1){
				soonestOption = opt;
			}
		}
		return soonestOption;
	}
	
	private BuyingOption getCheapestOption(Component comp){
		ArrayList<BuyingOption> buyingOptions = getComponentBuyingOptions(comp);
		BuyingOption cheapestOption = buyingOptions.get(0);
		for(BuyingOption opt : buyingOptions){
			if (opt.price < cheapestOption.price){
				cheapestOption = opt;
			}
		}
		return cheapestOption;
	}
	private ArrayList<BuyingOption> getComponentBuyingOptions(Component comp) {
		return componentMarket.get(comp.getType()).get(comp.getIdentifier());
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


