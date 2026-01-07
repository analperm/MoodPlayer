package de.hsrm.mi.enia.moodplayer.presentation.views;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class StartView extends BorderPane {

    // Navigation (links)
    public Button toPlayerButton;
    public Button toPlaylistButton;
    public Button toMoodButton;

    // Content Buttons
    public Button moodSelectButton;

    public StartView() {
        setPadding(new Insets(20));

        // ----- LEFT NAV -----
        VBox nav = new VBox(10);
        nav.setPadding(new Insets(10));
        nav.setPrefWidth(180);

        toPlayerButton = new Button("Zum Player");
        toPlaylistButton = new Button("Zur Playlist");
        toMoodButton = new Button("Zur Mood");

        // Alle Nav-Buttons gleich breit
        toPlayerButton.setMaxWidth(Double.MAX_VALUE);
        toPlaylistButton.setMaxWidth(Double.MAX_VALUE);
        toMoodButton.setMaxWidth(Double.MAX_VALUE);

        nav.getChildren().addAll(toPlayerButton, toPlaylistButton, toMoodButton);
        setLeft(nav);

        // ----- CENTER CONTENT -----
        VBox content = new VBox(18);
        content.setAlignment(Pos.TOP_CENTER);
        content.setPadding(new Insets(20));

        Label screenTitle = new Label("Screen 1: Start");
        screenTitle.setStyle("-fx-font-size: 44px; -fx-font-weight: 800;");

        // Placeholder für großes Bild (später ImageView)
        Rectangle imagePlaceholder = new Rectangle(520, 220);
        imagePlaceholder.setArcWidth(10);
        imagePlaceholder.setArcHeight(10);
        imagePlaceholder.setFill(Color.LIGHTGRAY);
        imagePlaceholder.setStroke(Color.GRAY);

        Label welcome = new Label("WELCOME BACK, User");
        welcome.setStyle("-fx-font-size: 32px; -fx-font-weight: 700;");

        Label question = new Label("Wie fühlst du dich heute?");
        question.setStyle("-fx-font-size: 18px;");

        moodSelectButton = new Button("Mood auswählen");
        moodSelectButton.setStyle("-fx-font-size: 18px; -fx-padding: 14 28;");

        content.getChildren().addAll(screenTitle, imagePlaceholder, welcome, question, moodSelectButton);

        // Damit es wie eine “Card” wirkt:
        StackPane card = new StackPane(content);
        card.setPadding(new Insets(20));
        card.setStyle(
            "-fx-background-color: white;" +
            "-fx-background-radius: 12;" +
            "-fx-border-radius: 12;" +
            "-fx-border-color: #dddddd;"
        );

        // Hintergrund (leichtes Pastell)
        setStyle("-fx-background-color: #f6dfe6;"); // zartes rosa wie im Mockup

        setCenter(card);
    }
}