package set10111.music_shop_ontology.elements;

import jade.content.onto.annotations.Slot;

public class VideoGame extends Item {
	private String title;
	private String publisher;
	private String platform;
	
	@Slot( mandatory = true)
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	
	@Slot( mandatory = true)
	public String getPublisher() {
		return publisher;
	}
	public void setPublisher(String publisher) {
		this.publisher = publisher;
	}
	
	@Slot( mandatory = true)
	public String getPlatform() {
		return platform;
	}
	public void setPlatform(String platform) {
		this.platform = platform;
	}
	
	
}
