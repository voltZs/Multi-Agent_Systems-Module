package mas.coursework_ontology.elements;

import jade.content.onto.annotations.Slot;

public class Storage extends Component{

	@Override
	@Slot(permittedValues = {"64Gb", "256Gb"})
	public String getIdentifier() {
		return super.getIdentifier();
	}

	@Override
	public void setIdentifier(String identifier) {
		super.setIdentifier(identifier);
	}
	
}
