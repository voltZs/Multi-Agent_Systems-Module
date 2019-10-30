package mas.coursework_ontology.elements;

import jade.content.onto.annotations.Slot;

public class Battery extends Component {

	@Override
	@Slot(permittedValues = {"2000mAh", "3000mAh"})
	public String getIdentifier() {
		return super.getIdentifier();
	}

	@Override
	public void setIdentifier(String identifier) {
		super.setIdentifier(identifier);
	}
	
}
