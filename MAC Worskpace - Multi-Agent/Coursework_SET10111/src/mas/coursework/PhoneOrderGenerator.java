package mas.coursework;
import mas.coursework_ontology.elements.Component;
import mas.coursework_ontology.elements.Phablet;
import mas.coursework_ontology.elements.Phone;
import mas.coursework_ontology.elements.SellPhones;
import mas.coursework_ontology.elements.SmallPhone;

public class PhoneOrderGenerator {
	SellPhones order;
	Phone phone;
	Component screen;
	Component battery;
	Component ram;
	Component storage;
	int quantity;
	int unitPrice;
	int daysDue;
	int perDayPenalty;
	
	public PhoneOrderGenerator(){
	}
	
	public SellPhones getOrder(){
		order = new SellPhones();
		screen = new Component();
		battery = new Component();
		ram = new Component();
		storage = new Component();
		randomize();
		return order;
	}
	
	public void randomize(){
		screen.setType("screen");
		battery.setType("battery");
		ram.setType("ram");
		storage.setType("storage");
		
		if(Math.random() < 0.5){
			//small smartphone 
			phone = new SmallPhone();
			screen.setIdentifier("5");
			phone.setScreen(screen);
			battery.setIdentifier("2000");
			phone.setBattery(battery);
		} else {
			//phablet
			phone = new Phablet();
			screen.setIdentifier("7");
			phone.setScreen(screen);
			battery.setIdentifier("3000");
			phone.setBattery(battery);
		}
		
		if(Math.random() < 0.5){
			ram.setIdentifier("4");
		} else {
			ram.setIdentifier("8");
		}
		phone.setRam(ram);
		
		if(Math.random() < 0.5){
			storage.setIdentifier("64");
		} else {
			storage.setIdentifier("256");
		}
		phone.setStorage(storage);
		order.setPhone(phone);
		
		quantity = (int) Math.floor(1 + 50 * Math.random());
		order.setQuantity(quantity);
		unitPrice = (int) Math.floor(100 + 500* Math.random());
		order.setUnitPrice(unitPrice);
		daysDue = (int) Math.floor(1 + 10* Math.random());
		order.setDaysDue(daysDue);
		perDayPenalty = (int) (quantity * Math.floor(1 + 50 * Math.random()));
		order.setPerDayPenalty(perDayPenalty);
	}
}