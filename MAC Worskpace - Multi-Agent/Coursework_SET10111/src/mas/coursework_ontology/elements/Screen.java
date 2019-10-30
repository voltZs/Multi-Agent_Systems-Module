package mas.coursework_ontology.elements;

import jade.content.onto.annotations.Slot;

public class Screen extends Component {

	@Override
	@Slot(permittedValues = {"5\"", "7\""})
	public String getIdentifier() {
		return super.getIdentifier();
	}

	@Override
	public void setIdentifier(String identifier) {
		super.setIdentifier(identifier);
	}

}
