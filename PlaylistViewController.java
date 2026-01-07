package de.hsrm.mi.enia.moodplayer.presentation.views;

import de.hsrm.mi.enia.moodplayer.business.MoodPlayer;

import de.hsrm.mi.enia.moodplayer.business.Playlist;
import de.hsrm.mi.enia.moodplayer.business.PlaylistManager;
import de.hsrm.mi.enia.moodplayer.business.Track;
import de.hsrm.mi.enia.moodplayer.presentation.MoodPlayerGUI;
import de.hsrm.mi.enia.moodplayer.presentation.uicomponents.TimePane;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleButton;
import javafx.util.Callback;
import javafx.util.Duration;

/**
 * Controller für die PlaylistView
 *
 * Aufgaben:
 * - Playlist asynchron laden
 * - Doppelklick auf Track → Abspielen
 * - Synchronisation mit PlayerView
 * - Lautstärke & Play/Pause synchron halten
 */

public class PlaylistViewController extends BaseController<PlaylistView> {

    private ListView<Track> playlistView;

    // Playlist-Referenz
    private Playlist playlist;
    private PlaylistManager playlistManager;
    private MoodPlayer player;

    // Buttons aus dem ControlPane
    private Button playButton;
    private Button skipButton;
    private Button skipBackButton;
    private ToggleButton shuffleButton;
    private Slider volumeSlider;

    private Timeline playStateUpdater;

    private TimePane timePane;
    private Timeline timeUpdater;
    private Track lastShownTrack = null;

    // UI-Model für die ListView
    private ObservableList<Track> items;

    public PlaylistViewController(MoodPlayer player) {
        root = new PlaylistView();

        this.player = player;
        this.playlist = player.getCurrentPlaylist();
        this.playlistManager = new PlaylistManager();

        playlistView = root.playlistListView;
        timePane = root.timePane;

        // Buttons aus dem ControlPane holen
        this.playButton = root.controlPane.playButton;
        this.skipButton = root.controlPane.skipButton;
        this.skipBackButton = root.controlPane.skipBackButton;
        this.shuffleButton = root.controlPane.shuffleButton;
        this.volumeSlider = root.controlPane.volumeSlider;

        initialize();
    }

    /** intitialisiert den Controller */
    @Override
    public void initialize() {
        // ListView Setup (TrackCell)
        playlistView.setCellFactory(new Callback<ListView<Track>, ListCell<Track>>() {
            private int counter = -1;

            @Override
            public ListCell<Track> call(ListView<Track> view) {
                counter++;
                return new TrackCell(counter);
            }
        });

        // ObservableList einmal setzen (wird später per setAll aktualisiert)
        items = FXCollections.observableArrayList();
        if (playlist != null) {
            items.setAll(playlist.getTracks());
        }
        playlistView.setItems(items);

        // Selection Listener > Mini-Info aktualisieren
        playlistView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Track>() {
            @Override
            public void changed(ObservableValue<? extends Track> observable, Track oldTrack, Track newTrack) {
                if (newTrack != null) {
                    updateMiniTrackInfo(newTrack);
                }
            }
        });

        // Switch-Buttons zu anderen Views
        root.toPlayerButton.setOnAction(e -> MoodPlayerGUI.switchRoot("playerView"));
        root.toMoodButton.setOnAction(e -> MoodPlayerGUI.switchRoot("moodView"));
        root.toStartButton.setOnAction(e -> MoodPlayerGUI.switchRoot("startView"));
        

        // Doppelklick > Track abspielen
        playlistView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Track selected = playlistView.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    player.setPlaylist(playlist);
                    player.playTrack(selected);

                    // TimePane sofort passend setzen
                    updateTimePaneForCurrentTrack();
                }
            }
        });

        // Play/Pause
        playButton.setOnAction(event -> {
            if (player.isPlaying()) {
                player.pause();
                return;
            }

            // wenn ein Track existiert (Pause/Resume-Fall) > einfach weiter
            if (player.getCurrentTrack() != null) {
                player.playOrResume();
                updateTimePaneForCurrentTrack();
                return;
            }

            // sonst: Track starten (selected oder erster)
            Track selected = playlistView.getSelectionModel().getSelectedItem();
            if (selected == null && playlist != null && !playlist.getTracks().isEmpty()) {
                selected = playlist.getTracks().get(0);
                playlistView.getSelectionModel().select(0);
            }

            if (selected != null) {
                player.setPlaylist(playlist);
                player.playTrack(selected);

                updateTimePaneForCurrentTrack();
            }
        });

        // Skip / SkipBack
        skipButton.setOnAction(event -> {
            player.skip();
            updateTimePaneForCurrentTrack();
        });

        skipBackButton.setOnAction(event -> {
            player.skipBack();
            updateTimePaneForCurrentTrack();
        });

        // Shuffle
        applyShuffleStyle(player.isShuffleOn(), shuffleButton);

        shuffleButton.selectedProperty().addListener((obs, oldValue, newValue) -> {
            player.shuffle(newValue);
            applyShuffleStyle(newValue, shuffleButton);
        });

        // Volume (0..100) durch Birectional Binding > synchron in beiden Ansichten
        volumeSlider.valueProperty().bindBidirectional(player.volumePercentProperty());

        // Seek Listener für TimePane
        setupSeekListener();

        // Async load
        loadPlaylistAsync();

        // Updater starten
        startPlayStateUpdater(); // Icon + Auswahl folgt Player
        startTimeUpdater(); // Slider + Zeit folgt Player
        
        root.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                // View wurde wirklich angezeigt > sofort alles syncen
                setPlayIcon(player.isPlaying());
                applyShuffleStyle(player.isShuffleOn(), shuffleButton);
                syncSelectionToCurrentTrack();
                updateTimePaneForCurrentTrack();
            }
        });

    }

    /** setzt das visuelle Shuffle-Icon abhängig vom aktuellen Shuffle-Zustand */
    private void applyShuffleStyle(boolean shuffleIsOn, ToggleButton btn) {
        btn.getStyleClass().removeAll("shuffle", "shuffle-disabled");
        btn.getStyleClass().add(shuffleIsOn ? "shuffle-disabled" : "shuffle");
        btn.setSelected(shuffleIsOn);
    }

    private Track lastHighlighted = null;

    /**
     * Startet einen periodischen Updater (Timeline), der:
     * - das Play/Pause-Icon aktuell hält
     * - den aktuell spielenden Track in der Playlist markiert
     * - Shuffle-Status synchron hält (z. B. wenn er in der PlayerView geändert wurde)
     * läuft dauerhaft im Hintergrund, solange die View existiert
     */
    private void startPlayStateUpdater() {
        playStateUpdater = new Timeline(
            new KeyFrame(Duration.millis(200), e -> {

                // Play/Pause Icon
                setPlayIcon(player.isPlaying());

                // Track in Liste markieren, wenn Track wechselt
                Track current = player.getCurrentTrack();
                if (current != null && current != lastHighlighted) {
                    lastHighlighted = current;
                    syncSelectionToCurrentTrack(); // markiert + scrollt + mini info
                }

                // Shuffle-Icon konsistent halten (falls in anderer View geändert)
                applyShuffleStyle(player.isShuffleOn(), shuffleButton);
            })
        );
        playStateUpdater.setCycleCount(Timeline.INDEFINITE);
        playStateUpdater.play();
    }

    /**
     * startet den Zeit-Updater für das TimePane

     * - erkennt Trackwechsel und passt Maximalzeit an
     * - aktualisiert regelmäßig die aktuelle Abspielzeit
     * - verhindert Konflikte, wenn der Nutzer den Slider selbst bewegt
     * Timeline läuft dauerhaft und liest den Zustand aus dem MP3Player
     */
    private void startTimeUpdater() {

        updateTimePaneForCurrentTrack();

        timeUpdater = new Timeline(
            new KeyFrame(Duration.millis(250), e -> {

                Track current = player.getCurrentTrack();

                // Trackwechsel > MaxTime + aktuelle Zeit neu setzen
                if (current != lastShownTrack) {
                    lastShownTrack = current;
                    updateTimePaneForCurrentTrack();
                }

                // nur wenn gespielt wird: Zeit fortschreiben
                if (player.isPlaying() && !timePane.getSlider().isValueChanging()) {
                    timePane.setCurrentTime(player.getCurrentPositionSeconds());
                }
            })
        );

        timeUpdater.setCycleCount(Timeline.INDEFINITE);
        timeUpdater.play(); // wichtig immer laufen lassen
    }

    /** lädt Playlist asynchron aus einer M3U-Datei */
    private void loadPlaylistAsync() {
        Task<Void> loadTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                updateMessage("Lade Playlist...");

                Playlist loadedPlaylist = playlistManager.loadM3U("Playliste/MeineErstePlaylist.m3u");

                if (loadedPlaylist != null) {
                    for (Track track : loadedPlaylist.getTracks()) {
                        playlist.addTrack(track);
                    }
                }

                updateMessage("Fertig");
                return null;
            }
        };

        loadTask.setOnRunning(event -> {
            root.loadingIndicator.setVisible(true);
            playlistView.setDisable(true);
        });

        loadTask.setOnSucceeded(event -> {
            root.loadingIndicator.setVisible(false);
            playlistView.setDisable(false);

            root.statusLabel.textProperty().unbind();
            root.statusLabel.setText(playlist.size() + " Songs geladen");

            // Items aktualisieren (keine neue Liste erzeugen)
            items.setAll(playlist.getTracks());
            playlistView.refresh();

            // Mini-Info / Auswahl ggf. syncen
            syncSelectionToCurrentTrack();

            // TimePane ggf. aktualisieren
            updateTimePaneForCurrentTrack();
        });

        loadTask.setOnFailed(event -> {
            root.loadingIndicator.setVisible(false);
            playlistView.setDisable(false);
            root.statusLabel.textProperty().unbind();
            root.statusLabel.setText("Fehler beim Laden!");
            if (loadTask.getException() != null) loadTask.getException().printStackTrace();
        });

        root.statusLabel.textProperty().bind(loadTask.messageProperty());

        Thread loadThread = new Thread(loadTask);
        loadThread.setDaemon(true);
        loadThread.start();
    }

    /** aktualisiert Mini-Track-Infos unterhalb der Playlist */
    private void updateMiniTrackInfo(Track t) {
        if (t == null) {
            root.miniTitleLabel.setText("Kein Song ausgewählt");
            root.miniArtistLabel.setText("");
            return;
        }

        String title = (t.getTitle() != null && !t.getTitle().isBlank()) ? t.getTitle() : "Unbekannter Titel";
        String artist = (t.getArtist() != null && !t.getArtist().isBlank()) ? t.getArtist() : "Unbekannter Artist";

        root.miniTitleLabel.setText(title);
        root.miniArtistLabel.setText(artist);
    }

    /** synchronisiert die Auswahl der Playlist mit dem aktuell spielenden Track */
    private void syncSelectionToCurrentTrack() {
        Track current = player.getCurrentTrack();
        if (current == null || playlist == null) return;

        int idx = playlist.getTracks().indexOf(current);
        if (idx >= 0) {
            playlistView.getSelectionModel().select(idx);
            playlistView.scrollTo(idx);
            updateMiniTrackInfo(current);
        }
    }

    /** setzt das Play/Pause-Icon abhängig vom aktuellen Player-Zustand */
    private void setPlayIcon(boolean playing) {
        playButton.getStyleClass().removeAll("play", "pause");
        playButton.getStyleClass().add(playing ? "pause" : "play");
    }

    /** synchronisiert das TimePane mit dem aktuell spielenden TRack */
    private void updateTimePaneForCurrentTrack() {
        Track t = player.getCurrentTrack();

        if (t == null) {
            timePane.reset();
            timePane.setMaxTime(0);
            return;
        }

        int len = t.getLengthSec();
        if (len <= 0) len = player.getCurrentTrackLengthSeconds();

        timePane.setMaxTime(Math.max(0, len));
        timePane.setCurrentTime(player.getCurrentPositionSeconds());
    }

    /** 
     * richtet den Seek-Listener für den Zeit-Slieder ein 
     *  sobald Nutzer den Slider loslässt: springt der Player zur entsprechenden Position
     *  und die UI wird sofort synchronisiert
     */
    private void setupSeekListener() {
        timePane.getSlider().valueChangingProperty().addListener((obs, wasChanging, isChanging) -> {
            if (!isChanging) {
                int sek = (int) timePane.getSlider().getValue();
                player.seekToSeconds(sek);
                timePane.setCurrentTime(sek);
            }
        });
    }
}
