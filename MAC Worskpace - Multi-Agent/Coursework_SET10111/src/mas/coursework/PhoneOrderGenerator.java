package mas.coursework;
import mas.coursework_ontology.elements.Battery;
import mas.coursework_ontology.elements.Component;
import mas.coursework_ontology.elements.Phablet;
import mas.coursework_ontology.elements.Phone;
import mas.coursework_ontology.elements.RAM;
import mas.coursework_ontology.elements.Screen;
import mas.coursework_ontology.elements.SellPhones;
import mas.coursework_ontology.elements.SmallPhone;
import mas.coursework_ontology.elements.Storage;

public class PhoneOrderGenerator {
	int penaltyRange;
	
	SellPhones order;
	Phone phone;
	Screen screen;
	Battery battery;
	RAM ram;
	Storage storage;
	int quantity;
	int unitPrice;
	int daysDue;
	int perDayPenalty;
	
	public PhoneOrderGenerator(int penaltyRange){
		this.penaltyRange = penaltyRange;
	}
	
	public SellPhones getOrder(){
		order = new SellPhones();
		screen = new Screen();
		screen.setType("screen");
		battery = new Battery();
		battery.setType("battery");
		ram = new RAM();
		ram.setType("ram");
		storage = new Storage();
		storage.setType("storage");
		randomize();
		return order;
	}
	
	public void randomize(){
		if(Math.random() < 0.5){
			//small smartphone 
			phone = new SmallPhone();
			screen.setIdentifier("5\"");
			phone.setScreen(screen);
			battery.setIdentifier("2000mAh");
			phone.setBattery(battery);
		} else {
			//phablet
			phone = new Phablet();
			screen.setIdentifier("7\"");
			phone.setScreen(screen);
			battery.setIdentifier("3000mAh");
			phone.setBattery(battery);
		}
		
		if(Math.random() < 0.5){
			ram.setIdentifier("4Gb");
		} else {
			ram.setIdentifier("8Gb");
		}
		phone.setRam(ram);
		
		if(Math.random() < 0.5){
			storage.setIdentifier("64Gb");
		} else {
			storage.setIdentifier("256Gb");
		}
		phone.setStorage(storage);
		order.setPhone(phone);
		
		quantity = (int) Math.floor(1 + 50 * Math.random());
		order.setQuantity(quantity);
		unitPrice = (int) Math.floor(100 + 500* Math.random());
		order.setUnitPrice(unitPrice);
		daysDue = (int) Math.floor(1 + 10* Math.random());
		order.setDaysDue(daysDue);
		perDayPenalty = (int) (quantity * Math.floor(1 + penaltyRange * Math.random()));
		order.setPerDayPenalty(perDayPenalty);
	}
}