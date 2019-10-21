package mas.coursework_ontology.elements;

import jade.content.onto.annotations.Slot;

public class Component extends Item {
	private String type;
	private String identifier;
	
	@Slot(mandatory = true)
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	
	@Slot(mandatory = true)
	public String getIdentifier() {
		return identifier;
	}
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}
	
	

}
