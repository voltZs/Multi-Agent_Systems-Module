/**
 * 
 */
package set10111.music_shop_ontology.elements;

import java.util.List;

import jade.content.onto.annotations.AggregateSlot;
import jade.content.onto.annotations.Slot;

public class Single extends Item {
	private String name;
	private List<Track> tracks;
	
	@Slot(mandatory = true)
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	@AggregateSlot(cardMin = 2, cardMax =2)
	public List<Track> getTracks() {
		return tracks;
	}
	
	public void setTracks(List<Track> tracks) {
		this.tracks = tracks;
	}
	
}
