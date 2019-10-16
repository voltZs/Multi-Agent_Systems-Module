package mas.coursework;

import java.util.HashMap;

public class SupplyChainSimulation {

	public static void main(String[] args) {
		CustomerOrder cusOrder = new CustomerOrder();
		HashMap<String, Integer> cusOrderDet = cusOrder.orderDetails();
		System.out.println(cusOrderDet);
		
		ManufacturerOrder manOrder = new ManufacturerOrder();
		manOrder.addItem("screen"+cusOrderDet.get("screen"), 4);
		manOrder.addItem("battery"+cusOrderDet.get("battery"), 4);
		manOrder.addItem("ram"+cusOrderDet.get("ram"), 4);
		
		System.out.println(manOrder.orderDetails());
		
	}

}
