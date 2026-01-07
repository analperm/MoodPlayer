package de.hsrm.mi.enia.moodplayer.presentation.views;

import de.hsrm.mi.enia.moodplayer.presentation.uicomponents.ControlPane;
import de.hsrm.mi.enia.moodplayer.presentation.uicomponents.TimePane;
import de.hsrm.mi.enia.moodplayer.business.Track;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * UI-Klasse für die Playlist-Ansicht
 *
 * enthält:
 * - ListView mit Tracks
 * - Mini-Trackinfo
 * - ControlPane
 * - TimePane
 */

public class PlaylistView extends BorderPane {
    public Label titleLabel;
    public Button toPlayerButton;
    public Button toMoodButton;
    public Button toStartButton;

    // UI-Elemente für Playlist 
    public ListView<Track> playlistListView; 
    public ProgressIndicator loadingIndicator;
    public Label statusLabel;

    public ControlPane controlPane;
    
    // Mini-Trackinfos über den Buttons
    public Label miniTitleLabel;
    public Label miniArtistLabel;
    public TimePane timePane;

    public PlaylistView() {

        // Header  
        HBox header = new HBox();
        header.setId("header");
        header.setAlignment(Pos.CENTER_LEFT);
        header.setSpacing(20);
        header.setPadding(new Insets(10, 15, 10, 15));
        header.getStyleClass().add("header");

        VBox headerText = new VBox();
        headerText.setId("header-text");
        headerText.setAlignment(Pos.CENTER_LEFT);

        titleLabel = new Label("Playlist View");
        titleLabel.getStyleClass().add("main-text");

        headerText.getChildren().addAll(titleLabel);
        
        // Buttons für View-Switch
        toStartButton = new Button("Zum Start");
        toPlayerButton = new Button("Zum Player");
        toMoodButton = new Button("Zur Mood");
        header.getChildren().addAll(headerText, toPlayerButton, toMoodButton, toStartButton);

        // Center
        playlistListView = new ListView<>();
        playlistListView.setPlaceholder(new Label("Keine Songs geladen"));

        loadingIndicator = new ProgressIndicator();
        loadingIndicator.setVisible(false); // anfangs versteckt

        statusLabel = new Label("Bereit");
        statusLabel.setId("statusLabel");

        StackPane centerStack = new StackPane();
        centerStack.getChildren().addAll(playlistListView, loadingIndicator);

        VBox centerBox = new VBox(10);
        centerBox.getChildren().addAll(centerStack, statusLabel);
        centerBox.setPadding(new Insets(10));
        centerBox.setFillWidth(true);

        // Bottom
        controlPane = new ControlPane();

        // Mini-Track-Info (über den Buttons) 
        miniTitleLabel = new Label("Kein Song ausgewählt");
        miniTitleLabel.getStyleClass().add("mini-track-title");

        miniArtistLabel = new Label("");
        miniArtistLabel.getStyleClass().add("mini-track-artist");
        
        timePane = new TimePane();

        VBox miniInfoBox = new VBox(2, miniTitleLabel, miniArtistLabel, timePane);
        miniInfoBox.setPadding(new Insets(8, 12, 6, 12));
        miniInfoBox.setAlignment(Pos.CENTER_LEFT);
        miniInfoBox.setId("mini-info-box");

        VBox bottomBox = new VBox(6);
        bottomBox.getChildren().addAll(miniInfoBox, controlPane);
        bottomBox.getStyleClass().add("bottomBox");

        // alles ins BorderPane einsetzen 
        this.setTop(header);
        this.setCenter(centerBox);
        this.setBottom(bottomBox); 
    }
}