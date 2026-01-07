package de.hsrm.mi.enia.moodplayer.presentation.views;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import de.hsrm.mi.enia.moodplayer.presentation.uicomponents.MoodWheelPane;

public class MoodView extends BorderPane {

    // Header
    public Label headerLabel;

    // Navigation Buttons
    public Button toPlayerButton;
    public Button toPlaylistButton;
    public Button toStartButton;
    
    // MoodWheel UI
    public MoodWheelPane moodWheel;
    public Label hoverLabel;
    public Label selectedLabel;
    public Button confirmMoodButton;

    public MoodView() {

        // Header 
        HBox header = new HBox();
        header.setId("header");
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(20, 10, 20, 10));
        header.setSpacing(10);

        VBox headerText = new VBox();
        headerText.setId("header-text");
        headerText.setAlignment(Pos.CENTER_LEFT);

        headerLabel = new Label("Mood View");
        headerLabel.getStyleClass().add("main-text"); // falls du CSS nutzt

        headerText.getChildren().add(headerLabel);

        // Buttons zum View-Switchen
        toPlayerButton = new Button("Zum Player");
        toPlaylistButton = new Button("Zur Playlist");
        toStartButton = new Button("Zum Start");

        header.getChildren().addAll(headerText, toPlayerButton, toPlaylistButton, toStartButton);

        setTop(header);

        // ----- Center: Mood Wheel + Info + Confirm -----
        moodWheel = new MoodWheelPane();

        hoverLabel = new Label("Hover: -");
        hoverLabel.setStyle("-fx-font-size: 14px;");

        selectedLabel = new Label("Ausgewählt: -");
        selectedLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: 700;");

        confirmMoodButton = new Button("Mood bestätigen");
        confirmMoodButton.setStyle("-fx-font-size: 16px; -fx-padding: 10 22;");

        VBox centerBox = new VBox(12, hoverLabel, moodWheel, selectedLabel, confirmMoodButton);
        centerBox.setAlignment(Pos.CENTER);
        centerBox.setPadding(new Insets(20));

        setCenter(centerBox);

        setPadding(new Insets(0));
    }
}