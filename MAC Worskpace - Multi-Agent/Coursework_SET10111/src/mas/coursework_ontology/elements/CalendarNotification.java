package mas.coursework_ontology.elements;

import jade.content.Predicate;
import jade.content.onto.annotations.Slot;

public class CalendarNotification implements Predicate {
	private boolean newDay;
	private boolean done;
	
	public boolean isNewDay() {
		return newDay;
	}
	public void setNewDay(boolean newDay) {
		this.newDay = newDay;
	}
	public boolean isDone() {
		return done;
	}
	public void setDone(boolean dayDone) {
		this.done = dayDone;
	}
}
