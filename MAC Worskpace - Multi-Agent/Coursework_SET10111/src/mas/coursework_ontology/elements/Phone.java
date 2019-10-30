package mas.coursework_ontology.elements;

import jade.content.onto.OntologyException;
import jade.content.onto.annotations.Slot;

public class Phone extends Item {
	protected Screen screen;
	protected Battery battery;	
	protected Storage storage;
	protected RAM ram;
	
	@Slot(mandatory=true)
	public Screen getScreen() throws OntologyException {
		return screen;
	}
	public void setScreen(Screen screen) {
		this.screen = screen;
	}
	
	@Slot(mandatory=true)
	public Battery getBattery() throws OntologyException {
		return battery;
	}
	public void setBattery(Battery battery) {
		this.battery = battery;
	}
	
	@Slot(mandatory=true)
	public Storage getStorage() {
		return storage;
	}
	public void setStorage(Storage storage) {
		this.storage = storage;
	}
	
	@Slot(mandatory=true)
	public RAM getRam() {
		return ram;
	}
	public void setRam(RAM ram) {
		this.ram = ram;
	}	
	
	
}
