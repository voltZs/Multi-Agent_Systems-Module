package mas.coursework;

import java.util.HashMap;

public class CustomerOrder {

	private HashMap<String, Integer> details;
	
	public CustomerOrder(){
		this.details = new HashMap<String, Integer>();
		this.randomize();
	}

	public void randomize() {
		if(Math.random() < 0.5){
			//small smartphone 
			details.put("screen", 5);
			details.put("battery", 2000);
		} else {
			//phablet
			details.put("screen", 7);
			details.put("battery", 3000);
		}
		
		if(Math.random() < 0.5){
			details.put("ram", 4);
		} else {
			details.put("ram", 8);
		}
		
		if(Math.random() < 0.5){
			details.put("storage", 64);
		} else {
			details.put("storage", 256);
		}
		
		details.put("quantity", (int) Math.floor(1 + 50 * Math.random()));
	}
	
	public HashMap<String, Integer> orderDetails(){

		return this.details;
	}
	
}
