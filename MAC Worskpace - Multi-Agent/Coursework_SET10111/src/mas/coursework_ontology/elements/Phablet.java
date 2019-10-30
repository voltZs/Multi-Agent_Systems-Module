package mas.coursework_ontology.elements;

import jade.content.onto.OntologyException;
import jade.content.onto.annotations.Slot;

public class Phablet extends Phone{

	@Override
	public Screen getScreen() throws OntologyException {
		String identifier = super.getScreen().getIdentifier();
		if(!identifier.equals("7\"")){
			throw new OntologyException("Screen identifier" + identifier + " for Phablet invalid.");
		}
		return super.getScreen();
	}

	@Override
	public Battery getBattery() throws OntologyException {
		String identifier = super.getBattery().getIdentifier();
		if(!identifier.equals("3000mAh")){
			throw new OntologyException("Battery identifier "+ identifier +" for Phablet invalid.");
		}
		return super.getBattery();
	}

}
