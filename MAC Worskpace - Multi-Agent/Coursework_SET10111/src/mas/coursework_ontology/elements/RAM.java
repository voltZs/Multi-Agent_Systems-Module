package mas.coursework_ontology.elements;

import jade.content.onto.annotations.Slot;

public class RAM extends Component{

	@Override
	@Slot(permittedValues = {"4Gb", "8Gb"})
	public String getIdentifier() {
		return super.getIdentifier();
	}

	@Override
	public void setIdentifier(String identifier) {
		super.setIdentifier(identifier);
	}

}
