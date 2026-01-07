package de.hsrm.mi.enia.moodplayer.presentation;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.layout.Pane;

import java.util.HashMap;
import java.util.Map;

import de.hsrm.mi.enia.moodplayer.business.MoodPlayer;
import de.hsrm.mi.enia.moodplayer.business.Playlist;
import de.hsrm.mi.enia.moodplayer.presentation.views.PlayerViewController;
import de.hsrm.mi.enia.moodplayer.presentation.views.PlaylistViewController;
import de.hsrm.mi.enia.moodplayer.presentation.views.MoodViewController;
import de.hsrm.mi.enia.moodplayer.presentation.views.StartViewController;

/**
 * Zentrale JavaFX Application.
 * 
 * - Initialisierung der Business-Logik (MP3Player, Playlist)
 * - Erzeugen der Views (PlayerView, PlaylistView)
 * - Umschalten zwischen den Views
 * - Lebenszyklus der Anwendung (start / stop)
 */

public class MoodPlayerGUI extends Application {
    
    // View-Switching System
    private static Stage stage;
    
    // Map speichert alle Views nach Namen
    private static Map<String, Pane> views;

    // Business-Objekte
    private MoodPlayer player;
    private Playlist playlist;
    
    // Controller der Views
    private PlayerViewController playerViewController;
    private PlaylistViewController playlistViewController;
    private MoodViewController moodViewController;
    private StartViewController startViewController;
    
    /** wird vor start() aufgerufen & initialisiert Business-Logik und View-Map */
    @Override
    public void init() {
        System.out.println("[App] Initialisierung...");
        
        // View-Map initialisieren
        views = new HashMap<>();

        // Business-Logik initialisieren
        player = new MoodPlayer();
        playlist = new Playlist("Meine Playlist");
        
        // Playlist an Player übergeben
        player.setPlaylist(playlist);
        
        System.out.println("[App] Business-Logik initialisiert");
    }
    
    /** Startpunkt der JavaFX-GUI > baut Scene, Views & Stage zusammen */
    @Override
    public void start(Stage primaryStage) throws Exception {
        stage = primaryStage;
        
        System.out.println("[App] Starte GUI...");
        
        // StartView erstellen
        startViewController = new StartViewController();
        Pane startView = startViewController.getRoot();
        views.put("startView", startView);
        
        // PlayerView erstellen
        playerViewController = new PlayerViewController(player);
        Pane playerView = playerViewController.getRoot();
        views.put("playerView", playerView);

        // PlaylistView erstellen 
        playlistViewController = new PlaylistViewController( player);
        Pane playlistView = playlistViewController.getRoot();
        views.put("playlistView", playlistView);
        
        // MoodView erstellen
        moodViewController = new MoodViewController();
        Pane moodView = moodViewController.getRoot();
        views.put("moodView", moodView);
        
        // Startansicht: PlayerView
        Scene scene = new Scene(startView, 800, 600);

        // optionales CSS laden
        var cssUrl = getClass().getResource("/style.css");
        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
            System.out.println("[App] CSS geladen: " + cssUrl);
        } else {
            System.out.println("[App] CSS NICHT gefunden!");
        }

        // Stage konfigurieren
        primaryStage.setScene(scene);
        primaryStage.setTitle("MP3 Player");

        // beim Schließen Player sauber stoppen
        primaryStage.setOnCloseRequest(event -> {
            cleanup();
        });
        
        primaryStage.show();
        
        System.out.println("[App] GUI gestartet - Zeige Player-View");
    }
    
    /** stoppt Anwednung */
    @Override
    public void stop() {
        System.out.println("[App] Anwendung wird beendet...");
        cleanup();
    }
    
    /** gibt  Ressourcen beim Beenden frei */
    private void cleanup() {
        if (player != null) {
            player.stop();
            System.out.println("[App] Player gestoppt");
        }
        System.out.println("[App] Ressourcen freigegeben");
    }

    /** ermöglicht das Umschalten zwischen Views > wird von Controllern genutzt */
    public static void switchRoot(String viewName) {
        Scene scene = stage.getScene();
        Pane nextRoot = views.get(viewName);
        
        if (nextRoot != null) {
            scene.setRoot(nextRoot);
            System.out.println("[App] View gewechselt zu: " + viewName);
        } else {
            System.err.println("[App] View nicht gefunden: " + viewName);
        }
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}
