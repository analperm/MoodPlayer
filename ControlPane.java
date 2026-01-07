package de.hsrm.mi.enia.moodplayer.presentation.uicomponents;

// wie bei euch (Play/Skip/Shuffle/Volume)package de.hsrm.mi.enia.mp3player.presentation.uicomponents;

import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.HBox;

/**
 * wiederverwendbare Steuerleiste für Play/Pause, Skip, Shuffle und Lautstärke
 * wird sowohl in PlayerView als auch in PlaylistView verwendet
 * enthält nur UI-Elemente, keine Logik
 */

public class ControlPane extends HBox {
	public Button playButton;
	public Button skipButton;
	public Button skipBackButton;
	public ToggleButton shuffleButton; 
	public Slider volumeSlider;
	
	public ControlPane() {
		// Play/Pause Button (Icon per CSS)
		playButton = new Button("");
		playButton.getStyleClass().add("icon-button");
		playButton.setId("play-button");
		playButton.getStyleClass().add("play");
		
		// Skip Buttons
		skipButton = new Button("");
		skipButton.getStyleClass().add("icon-button");
		skipButton.setId("skip-button");
		
		skipBackButton = new Button("");
		skipBackButton.getStyleClass().add("icon-button");
		skipBackButton.setId("skipback-button");
		
		// Shuffle Toggle
		shuffleButton = new ToggleButton("");
		shuffleButton.getStyleClass().add("icon-button");
		shuffleButton.setId("shuffel-button");
		
		// Volume Slieder (0-100%, Start bei 80%)
		volumeSlider = new Slider(0, 100, 80); 
		volumeSlider.setPrefWidth(150);
		
		// zusammenbauen
		this.getChildren().addAll(
				skipBackButton,
				playButton,
				skipButton,
				shuffleButton,
				volumeSlider
		);
		
		this.setId("control-view");
	}

}
