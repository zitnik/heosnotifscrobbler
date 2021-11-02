package si.xlab.testnotifreceiver;

public class Song {
	private String artist;
	private String title;
	private boolean fromRadio; // affects last.fm's "chosenByUser" parameter
	
	Song(String artist, String title, boolean fromRadio) {
		this.artist = artist;
		this.title = title;
		this.fromRadio = fromRadio;
	}
	
	public String getArtist() {
		return artist;
	}
	
	public String getTitle() {
		return title;
	}
	
	public boolean isFromRadio() {
		return fromRadio;
	}
	
	@Override
	public String toString() {
		return String.format("%s - %s", artist, title);
	}
	
	@Override
	public int hashCode() {
		return toString().hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (!(obj instanceof Song))
			return false;
		return ((Song) obj).artist.equals(artist) && ((Song) obj).title.equals(title);
	}
}