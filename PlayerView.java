package de.hsrm.mi.enia.moodplayer.presentation.views;

import de.hsrm.mi.enia.moodplayer.presentation.uicomponents.ControlPane;
import de.hsrm.mi.enia.moodplayer.presentation.uicomponents.TimePane;
import de.hsrm.mi.enia.moodplayer.business.Track;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * UI-Klasse für die Player-Ansicht
 *
 * enthält:
 * - Header mit Switch-Button
 * - Cover + Trackinfos
 * - TimePane
 * - ControlPane
 */

public class PlayerView extends BorderPane {

    // Header
    public Label headerLabel;
    public Button toPlaylistButton;
    public Button toMoodButton;
    public Button toStartButton;

    // Now-Playing Infos
    public Label trackTitleLabel;
    public Label trackArtistLabel;
    public Label trackAlbumLabel;
    public ImageView coverImageView;

    // Controls
    public ControlPane controlPane;
    public TimePane timePane;

    public PlayerView() {

        // Header
        HBox header = new HBox();
        header.setId("header");
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(20, 10, 20, 10));
        header.setSpacing(10);

        VBox headerText = new VBox();
        headerText.setId("header-text");
        headerText.setAlignment(Pos.CENTER_LEFT);

        headerLabel = new Label("Player View");
        headerLabel.getStyleClass().add("main-text");

        headerText.getChildren().add(headerLabel);

        // Buttons zum View-Switchen
        toPlaylistButton = new Button("Zur Playlist");
        toMoodButton = new Button("Change Mood");
        toStartButton = new Button("Zum Start");
        header.getChildren().addAll(headerText, toPlaylistButton, toMoodButton, toStartButton);
        

        // Center
        coverImageView = new ImageView();
        coverImageView.setFitWidth(220);
        coverImageView.setFitHeight(220);
        coverImageView.setPreserveRatio(true);

        trackTitleLabel = new Label("Titel");
        trackTitleLabel.getStyleClass().add("main-text");

        trackArtistLabel = new Label("Artist");
        trackAlbumLabel = new Label("Album");

        VBox metaBox = new VBox(5, trackTitleLabel, trackArtistLabel, trackAlbumLabel);
        metaBox.setAlignment(Pos.CENTER);

        VBox centerBox = new VBox(15, coverImageView, metaBox);
        centerBox.setAlignment(Pos.CENTER);
        centerBox.setPadding(new Insets(20));
        
        // Bottom
        timePane = new TimePane();
        timePane.setMaxWidth(Double.MAX_VALUE);
        VBox.setVgrow(timePane, Priority.NEVER);

        controlPane = new ControlPane();
        controlPane.setPadding(new Insets(10, 10, 20, 10));
        
        VBox bottomBox = new VBox(10, timePane, controlPane);
        bottomBox.setPadding(new Insets(0, 20, 0, 20)); // links/rechts Abstand wie in Player-Apps
        bottomBox.setFillWidth(true);
        bottomBox.getStyleClass().add("bottomBox");        

        // BorderPane zusammenschrauben
        setTop(header);
        setCenter(centerBox);
        setBottom(bottomBox);
        setPadding(new Insets(0));
    }
}