package mas.coursework_ontology.elements;

import jade.content.onto.annotations.Slot;

public class Phone extends Item {
//	protected Component screen;
//	protected Component battery;	
	protected Component storage;
	protected Component ram;	
//	
//	@Slot (mandatory = true)
//	public Component getScreen() {
//		return screen;
//	}
//	public void setScreen(Component screen) {
//		this.screen = screen;
//	}
//	
//	@Slot (mandatory = true)
//	public Component getBattery() {
//		return battery;
//	}
//	public void setBattery(Component battery) {
//		this.battery = battery;
//	}
	
	@Slot (mandatory = true)
	public Component getStorage() {
		return storage;
	}
	public void setStorage(Component storage) {
		this.storage = storage;
	}
	
	@Slot (mandatory = true)
	public Component getRam() {
		return ram;
	}
	public void setRam(Component ram) {
		this.ram = ram;
	}

}
