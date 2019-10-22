package mas.coursework_ontology.elements;

import jade.content.onto.annotations.Slot;

public class Phablet extends Phone{
	private Component screen;
	private Component battery;	
	
	@Slot(mandatory = true)
	public Component getScreen() {
		return screen;
	}
	public void setScreen(Component screen) {
		this.screen = screen;
	}
	
	@Slot(mandatory = true)
	public Component getBattery() {
		return battery;
	}
	public void setBattery(Component battery) {
		this.battery = battery;
	}
}
