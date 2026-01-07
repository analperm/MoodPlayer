package de.hsrm.mi.enia.moodplayer.presentation.views;

import de.hsrm.mi.enia.moodplayer.business.MoodPlayer;

import de.hsrm.mi.enia.moodplayer.business.Track;
import de.hsrm.mi.enia.moodplayer.presentation.MoodPlayerGUI;
import de.hsrm.mi.enia.moodplayer.presentation.uicomponents.TimePane;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;

import java.net.URL;

/**
 * Controller für die PlayerView
 *
 * verbindet:
 * - Buttons (Play/Pause, Skip, Shuffle)
 * - TimePane (Anzeige + Seek)
 * - Volume-Slider
 *
 * synchronisiert UI-Zustand mit dem MP3Player
 */

public class PlayerViewController extends BaseController<PlayerView> {

    Button playButton;
    Button skipButton;
    Button skipBackButton;
    ToggleButton shuffleButton;
    Slider volumeSlider;
    TimePane timePane;

    Label trackTitleLabel;
    Label trackArtistLabel;
    Label trackAlbumLabel;
    ImageView coverImageView;

    private final MoodPlayer player;

    private Timeline timeUpdater;
    private Track lastShownTrack = null;
    

    /** einfacher EventHandler als Member-Klasse (für Skip) */
    public class SkipHandler implements EventHandler<ActionEvent> {
        @Override
        public void handle(ActionEvent event) {
            player.skip();

            // UI sofort auf neuen Track umstellen
            timePane.reset();
            updateTrackInfo();
            syncPlayPauseIcon();
        }
    }

    public PlayerViewController(MoodPlayer player) {
        root = new PlayerView();

        playButton = root.controlPane.playButton;
        skipButton = root.controlPane.skipButton;
        skipBackButton = root.controlPane.skipBackButton;
        shuffleButton = root.controlPane.shuffleButton;
        volumeSlider = root.controlPane.volumeSlider;
        timePane = root.timePane;

        trackTitleLabel = root.trackTitleLabel;
        trackArtistLabel = root.trackArtistLabel;
        trackAlbumLabel = root.trackAlbumLabel;
        coverImageView = root.coverImageView;

        this.player = player;
        initialize();
    }

    /** initialisiert die kpmplette Verdrahtung zwischen View und Player */
    @Override
    public void initialize() {
        
    	// Switch zu anderen Views
    	root.toPlaylistButton.setOnAction(e -> MoodPlayerGUI.switchRoot("playlistView"));
    	root.toMoodButton.setOnAction(e -> MoodPlayerGUI.switchRoot("moodView"));
    	root.toStartButton.setOnAction(e -> MoodPlayerGUI.switchRoot("startView"));

        // Startzustand Icons aus Player ableiten
        syncPlayPauseIcon();
        applyShuffleStyle(player.isShuffleOn(), shuffleButton);

        // Skip
        skipButton.addEventHandler(ActionEvent.ACTION, new SkipHandler());

        // Play/Pause
        playButton.addEventHandler(ActionEvent.ACTION, event -> {
            if (!player.isPlaying()) {
                player.playOrResume();
                updateTrackInfo(); // setzt maxTime passend
            } else {
                player.pause();
            }
            syncPlayPauseIcon();
            // Timeline läuft immer > nichts mehr starten/stoppen
        });

        // SkipBack
        skipBackButton.addEventHandler(ActionEvent.ACTION, event -> {
            player.skipBack();

            timePane.reset();
            updateTrackInfo();
            syncPlayPauseIcon();
        });

        // Shuffle
        shuffleButton.selectedProperty().addListener((obs, oldValue, newValue) -> {
            player.shuffle(newValue);
            applyShuffleStyle(newValue, shuffleButton);
        });

        // Volume (0..100) durch Birectional Binding > synchron in beiden Ansichten
        // Startwert + Sync (beidseitig)
        volumeSlider.valueProperty().bindBidirectional(player.volumePercentProperty());

        // Timeline (Zeit + Trackwechsel) > läuft immer
        timeUpdater = new Timeline(
            new KeyFrame(Duration.millis(250), e -> {

                 // Trackwechsel erkennen
            	 syncPlayPauseIcon();
                 applyShuffleStyle(player.isShuffleOn(), shuffleButton);

                 Track current = player.getCurrentTrack();
                 if (current != lastShownTrack) {
                     lastShownTrack = current;
                     timePane.reset();
                     updateTrackInfo();
                 }

                // nur wenn gespielt wird: Zeit fortschreiben
                if (player.isPlaying() && !timePane.getSlider().isValueChanging()) {
                    timePane.setCurrentTime(player.getCurrentPositionSeconds());
                }

                // Shuffle-Icon konsistent halten (falls in anderer View geändert)
                applyShuffleStyle(player.isShuffleOn(), shuffleButton);
            })
        );
        timeUpdater.setCycleCount(Timeline.INDEFINITE);
        timeUpdater.play();

        // initiales UI
        updateTrackInfo();

        // Seek (Slider loslassen)
        timePane.getSlider().valueChangingProperty().addListener((obs, wasChanging, isChanging) -> {
            if (!isChanging) {
                int sekunden = (int) timePane.getSlider().getValue();
                player.seekToSeconds(sekunden);

                // nach Seek UI konsistent setzen
                timePane.setCurrentTime(sekunden);
            }
        });
        
        root.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                syncPlayPauseIcon();
                applyShuffleStyle(player.isShuffleOn(), shuffleButton);
                updateTrackInfo();
                timePane.setCurrentTime(player.getCurrentPositionSeconds());
            }
        });
        
    }

    /** setzt Shuffle-Icon/CSS passende zum Zustand */
    private void applyShuffleStyle(boolean shuffleIson, ToggleButton btn) {
        btn.getStyleClass().removeAll("shuffle", "shuffle-disabled");
        // Icon zeigt nächste Aktion
        btn.getStyleClass().add(shuffleIson ? "shuffle-disabled" : "shuffle");
        btn.setSelected(shuffleIson);
    }

    /** aktualisiert alle Track-Infos in der PlayerView */
    private void updateTrackInfo() {
        Track t = player.getCurrentTrack();

        if (t == null) {
            trackTitleLabel.setText("Kein Titel");
            trackArtistLabel.setText("");
            trackAlbumLabel.setText("");
            coverImageView.setImage(null);
            timePane.reset();
            timePane.setMaxTime(0);
            return;
        }

        trackTitleLabel.setText(
            (t.getTitle() != null && !t.getTitle().isBlank()) ? t.getTitle() : "Unbekannter Titel"
        );
        trackArtistLabel.setText(
            (t.getArtist() != null && !t.getArtist().isBlank()) ? t.getArtist() : "Unbekannter Artist"
        );
        trackAlbumLabel.setText(
            (t.getAlbum() != null && !t.getAlbum().isBlank()) ? t.getAlbum() : "Unbekanntes Album"
        );

        int len = t.getLengthSec();
        if (len <= 0) len = player.getCurrentTrackLengthSeconds();
        timePane.setMaxTime(Math.max(0, len));

        Image cover = chooseCoverForTrack(t);
        coverImageView.setImage(cover);

        timePane.setCurrentTime(player.getCurrentPositionSeconds());
    }

    /** synchronisiert das Play/Pause-Icon mit dem aktuellen Player-Zustand */
    private void syncPlayPauseIcon() {
        boolean playingNow = player.isPlaying();
        playButton.getStyleClass().removeAll("play", "pause");
        playButton.getStyleClass().add(playingNow ? "pause" : "play");
    }

    /** wählt das passende Coverbild für einen Track */
    private Image chooseCoverForTrack(Track t) {
        if (t == null) return loadImageOrNull("/assets/covers/cover_default.png");

        String title = t.getTitle() != null ? t.getTitle().toLowerCase() : "";
        String path = null;

        if (title.contains("bring mich nach hause")) path = "/assets/covers/cover_01.jpg";
        else if (title.contains("drei worte")) path = "/assets/covers/cover_02.jpg";
        else if (title.contains("love will be with you")) path = "/assets/covers/cover_03.jpg";
        else if (title.contains("last membrane")) path = "/assets/covers/cover_04.jpg";

        return loadImageOrNull(path);
    }

    /**
     * lädt ein Bild aus den Ressourcen (classpath) und gibt es als Image zurück
     * > falls die Ressource nicht existiert oder nicht geladen werden kann, gibt null zurück &
     */
    private Image loadImageOrNull(String resourcePath) {
        try {
            if (resourcePath == null) return null;
            URL url = getClass().getResource(resourcePath);
            if (url != null) return new Image(url.toExternalForm());
            System.out.println("Cover-Ressource nicht gefunden: " + resourcePath);
        } catch (Exception e) {
            System.out.println("Fehler beim Laden von " + resourcePath + ": " + e.getMessage());
        }
        return null;
    }
}
