package de.hsrm.mi.enia.moodplayer.business;

/**
 * Datenklasse für einen Track/Song
 * - enthält Pfad/Dateiname sowie Metadaten (Titel, Artist, Album) und Länge in Sekunden
 */
//wie bisher, aber + moodTags / valence/energy optional

public class Track {
	
	private final String filename; 
    private final String title;
    private final String artist;
    private final String album;
    private int lengthSec;    

    public Track(String filename, String title, String artist, String album, int lengthSec) {
        this.filename = filename;
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.lengthSec = 0;
    }

    // Getter & Setter
    public String getFilename() { 
    	return filename; 
    }
    
    public String getTitle()    { 
    	return title; 
    }
    
    public String getArtist()   { 
    	return artist; 
    }
    
    public String getAlbum()    { 
    	return album; 
    }
    
    public int getLengthSec()   { 
    	return lengthSec; 
    }
    
    public void setLengthSec(int lengthSec) {
    	this.lengthSec = lengthSec;
    }

    @Override public String toString() {
        return (title != null && !title.isBlank() ? title : filename) +
               (artist != null && !artist.isBlank() ? " – " + artist : "");
    }

}
