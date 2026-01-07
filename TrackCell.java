package de.hsrm.mi.enia.moodplayer.presentation.views;

import de.hsrm.mi.enia.moodplayer.business.Track;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * eigene ListCell für die Playlist.
 *
 * eeigt:
 * - Titel
 * - Artist
 * - Album
 *
 * wird von der ListView gecached.
 */

public class TrackCell extends ListCell<Track> {
	HBox content;
	
	// number ist nur zum Zeigen des Cachings eingeführt
	Label number;
	
	Label title;
	Label artist;
	Label album;
	
	public TrackCell() {
		content = new HBox();
		
		VBox trackInfo = new VBox();
		
		number = new Label();
		title = new Label();
		artist = new Label();
		album = new Label();
		
		number.getStyleClass().add("main-text");
		title.getStyleClass().add("text-1");
		artist.getStyleClass().add("text-2");
		album.getStyleClass().add("text-2");
		
		trackInfo.getChildren().addAll(title, artist, album);
		trackInfo.setSpacing(5);
		
		content.getChildren().addAll(number, trackInfo);
		content.setAlignment(Pos.CENTER_LEFT);
		content.setSpacing(20);
		content.setPadding(new Insets(5, 10, 5, 5));
		
		this.setGraphic(content);
	}
	
	public TrackCell(int nr) {
		this();
		number.setText("" + nr);
	}
	
	@Override
	public void updateItem(Track item, boolean empty) {
		super.updateItem(item, empty);
		
		if (!empty) {
			title.setText(item.getTitle());
			artist.setText(item.getArtist());
			album.setText(item.getAlbum());
			setGraphic(content);
		} else {
			setGraphic(null);
		}
	}

}

