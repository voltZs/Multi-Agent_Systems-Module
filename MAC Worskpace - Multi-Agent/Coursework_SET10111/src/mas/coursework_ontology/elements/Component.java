package mas.coursework_ontology.elements;

import jade.content.onto.annotations.Slot;

public class Component extends Item {
	private String identifier;
	
	@Slot(mandatory = true)
	public String getIdentifier() {
		return identifier;
	}
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}
	
	

}
