package de.hsrm.mi.enia.moodplayer.business;

import de.hsrm.mi.eibo.simpleplayer.SimpleAudioPlayer;
import de.hsrm.mi.eibo.simpleplayer.SimpleMinim;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;

/**
 * Das ist euer „MP3Player 2.0“:
	•	hält intern eine Playlist + aktuellen Trackindex
	•	steuert Wiedergabe (play/pause/resume/stop/skip/seek/volume) wie bisher
	•	zusätzlich: hält MoodProfile selectedMood
	•	kann eine Mood-Playlist erzeugen (getMoodPlaylist()), indem er filtert
 */

public class MoodPlayer {
	// Library-Objekte für Audio
    private SimpleMinim minim;
    private SimpleAudioPlayer audioPlayer;
    
    // Playlist-Zustand
    private Playlist currentPlaylist;
    private int currentIndex = 0;
    
    // Modi
    private boolean shuffle = false;
    private boolean repeat = false;
    
    private Mood selectedMood;

    // Thread-Flags
    private PlayThread playThread;
    private volatile boolean wasStopped = false;
    private volatile boolean isPaused = false;
    private volatile int playToken = 0; // damit nicht zwei Wiedergaben gleichzeitig
    private volatile boolean playing = false; 

    // aktuelle Zeit in Sekunden (für TimePane)
    private final IntegerProperty currentTime = new SimpleIntegerProperty(0);
    // damit volume als Property gilt > für Slider-Binding
    private final DoubleProperty volumeValue = new SimpleDoubleProperty(80.0); // 0..100
    private Thread timeThread;

    /** SimpleMinim mit blockierendem play() */
    public MoodPlayer() {
        minim = new SimpleMinim(false);  // false = man steuert Threading selbst
        
        // sobald sich der volume-Wert ändert > direkt am AudioPlayer anwenden
        volumeValue.addListener((obs, oldV, newV) -> applyVolumeToAudioPlayer());
    }

    /** gibt den aktuellen Player frei, pausiert und setzt audioPlayer auf null */
    private void releasePlayer() {
        if (audioPlayer != null) {
            try {
                audioPlayer.pause();
            } catch (Exception ignored) {}
            audioPlayer = null;
        }
    }

    // Playlist
    public Playlist getCurrentPlaylist () {
    	return currentPlaylist;
    }
    
    /** setzt eine neue Playlist */
    public void setPlaylist(Playlist playlist) {
        if (playlist == null) return;

        // wenn es dieselbe Playlist ist: nicht currentIndex resetten
        if (this.currentPlaylist == playlist) {
            return;
        }

        this.currentPlaylist = playlist;
        this.currentIndex = 0;
        System.out.println("Playlist gesetzt: " + playlist.getName());
    }

    // Time Property
    public IntegerProperty currentTimeProperty() {
        return currentTime;
    }

    public int getCurrentTime() {
        return currentTime.get();
    }

    private void setCurrentTime(int seconds) {
        this.currentTime.set(seconds);
    }
    
    // Volume Property
    /** Property für Lautstärke (0-100) damit beide Views senselben Wert binden können */
    public DoubleProperty volumePercentProperty() {
        return volumeValue;
    }

    public double getVolumePercent() {
        return volumeValue.get();
    }

    /** setzt Lautstärke als Prozentwert 0-100 und clamped den Bereich */
    public void setVolumePercent(double v) {
        if (v < 0) v = 0;
        if (v > 100) v = 100;
        volumeValue.set(v);
    }
    
    /** Lautstärke einstellen > wird von Controllern aufgerufen */
    public void volume(double value) {
        setVolumePercent(value); // triggert applyVolumeToAudioPlayer() automatisch
    }
    
    /** 
     * apply-Methode für db Lautstärke > private Hilfsmethode für volume() 
     * > wendet den aktuellen Volume-Wert auf AudioPlayer an
     */
    private void applyVolumeToAudioPlayer() {
        double v = getVolumePercent(); // 0..100

        if (audioPlayer == null) {
            System.out.println("[VOLUME] audioPlayer=null, gespeicherter Wert=" + v);
            return;
        }

        // 0 -> -60 dB (sehr leise/stumm), 100 -> 0 dB (normal)
        double gainDb = -60.0 + (v * 0.60);

        System.out.println("[VOLUME] Slider=" + v + " -> gainDb=" + gainDb);

        try {
            audioPlayer.setGain((float) gainDb);
            System.out.println("[VOLUME] setGain OK");
        } catch (Exception e) {
            System.out.println("[VOLUME] setGain FEHLER: " + e.getMessage());
        }

        // optionaler Fallback 
        try {
            float lin = (float) (v / 100.0);
            // falls es setVolume nicht gibt > catch macht nichts kaputt
            audioPlayer.getClass().getMethod("setVolume", float.class).invoke(audioPlayer, lin);
            System.out.println("[VOLUME] setVolume OK (Fallback), lin=" + lin);
        } catch (Exception ignored) {
            // ignorieren, wenn es die Methode nicht gibt
        }
    }

    
    // Playback
    /** aktuellen Song abspielen, lädt den TRack neu, startet Zeit-Thread & Play-Thread */
    public void play() {
        if (currentPlaylist == null || currentPlaylist.size() == 0) {
            System.out.println("Keine Playlist oder leer!");
            return;
        }

        // Index absichern
        if (currentIndex < 0) currentIndex = 0;
        if (currentIndex >= currentPlaylist.size()) currentIndex = 0;

        // neuer Token > alte Threads sollen ignoriert werden
        int myToken = ++playToken; 

        Track track = currentPlaylist.getTracks().get(currentIndex);
        System.out.println("Spiele: " + track);

        // vorherigen Player freigeben
        if (audioPlayer != null) {
            releasePlayer();
        }
        
        audioPlayer = minim.loadMP3File(track.getFilename()); // Track laden
        applyVolumeToAudioPlayer(); // direkt Volume auf den neu geladenen Player anwenden
 
        // wenn möglich Track-Länge bestimmen
        int lenSec = 0;
        try {
            lenSec = Math.max(0, audioPlayer.length() / 1000);
        } catch (Exception ignored) {}

        track.setLengthSec(lenSec);
        System.out.println("Track-Länge: " + lenSec + "s für " + track);

        // Status setzen
        wasStopped = false;
        isPaused = false;
        playing = true;
        setCurrentTime(0);

        startTimeThread(track.getLengthSec());

        // Thread bekommt den Token
        playThread = new PlayThread(myToken);
        playThread.start();
    }

    /** spielt eine einzelne Datei direkt ab > ohne Playlist */
    public void play(String filename) {
        if (audioPlayer != null) {
            releasePlayer();
        }
        audioPlayer = minim.loadMP3File(filename);
        applyVolumeToAudioPlayer();

        wasStopped = false;
        isPaused = false;
        playing = true;
        setCurrentTime(0);

        int myToken = ++playToken; // neuen Token erzeugen
        
        startTimeThread(0); // 0 = unbekannt, Timer zählt hier nicht sinnvoll mit
  
        playThread = new PlayThread(myToken);
        playThread.start();
    }

    /** pausiert die Wiedergabe */
    public void pause() {
        isPaused = true;
        playing = false;
        
        playToken++; // damit nicht doppelte Wiedergabe > alte Threads weg

        if (audioPlayer != null) {
            audioPlayer.pause();
        }

        stopTimeThread(false); // Zeit-Thread stoppen, Zeit aber nicht resetten
    }
    
    /** setzt die Wiedergabe nach Pause fort, ohne neu zu laden */
    public void resume() {
        // nur wenn wir pausiert und Player existiert
        if (!isPaused || audioPlayer == null) {
            return;
        }

        // neuer Token, damit alter Thread veraltet ist
        int myToken = ++playToken;

        wasStopped = false;
        isPaused = false;
        playing = true;

        // Timer ab aktueller Position weiterlaufen lassen
        startTimeThread(getCurrentTrackLengthSeconds());

        // neuen PlayThread starten (audioPlayer.play() läuft ab aktueller Position weiter)
        playThread = new PlayThread(myToken);
        playThread.start();
    }
    
    /** Konfort-Methode - Controller müssen nicht raten */
    public void playOrResume() {
        if (isPaused && audioPlayer != null) {
            resume();
        } else {
            play(); // normaler Start 
        }
    }

    /** stoppt die Wiedergabe komplett und setzt Zeit zurück */
    public void stop() {
        wasStopped = true;
        isPaused = false;
        playing = false;
        playToken++; // damit nicht doppelte Wiedergabe

        if (audioPlayer != null) {
            releasePlayer();
        }

        stopTimeThread(true); // Timer stoppen und auf 0 zurücksetzen
        System.out.println("Gestoppt - Auto-Play unterbrochen");
    }

    // Navigation
    /** nächster Song */
    public void skip() {
        if (currentPlaylist == null) return;

        if (shuffle) {
            currentIndex = (int) (Math.random() * currentPlaylist.size());
        } else {
            currentIndex++;
            if (currentIndex >= currentPlaylist.size()) {
                if (repeat) {
                    currentIndex = 0;
                } else {
                    System.out.println("Ende der Playlist erreicht.");
                    stop(); // sicherheitshalber
                    return;
                }
            }
        }

        wasStopped = false;
        isPaused = false;
        play();
    }

    /** vorheriger Song */
    public void skipBack() {
        if (currentPlaylist == null) return;

        currentIndex--;
        if (currentIndex < 0) {
            if (repeat) {
                currentIndex = currentPlaylist.size() - 1;
            } else {
                currentIndex = 0;
            }
        }

        wasStopped = false;
        isPaused = false;
        play();
    }

    // Modi
    /** Shuffle-Modus an/aus */
    public void shuffle(boolean on) {
        this.shuffle = on;
        System.out.println("Shuffle: " + (on ? "aktiv" : "aus"));
    }
    
    public boolean isShuffleOn() {
    	return shuffle;
    }

    /** Repeat-Modus an/aus */
    public void repeat(boolean on) {
        this.repeat = on;
        System.out.println("Repeat: " + (on ? "aktiv" : "aus"));
    }

    // Track/Seek/Time
    /** aktuellen Track für GUI */
    public Track getCurrentTrack() {
        if (currentPlaylist != null && currentIndex >= 0 && currentIndex < currentPlaylist.size()) {
            return currentPlaylist.getTracks().get(currentIndex);
        }
        return null;
    }
    
    /** um die Mood in PlayerView anzeigen zu können */
    public Mood getSelectedMood() {
    	return selectedMood;
    }
    
    /** wird von MoodViewController benutzt */
    public void setSelectedMood (Mood mood) {
        this.selectedMood = mood;

        // später: Playlist neu berechnen
    }
  
    
    /** springt im Track auf eine bestimmte Sekunde > für den Zeit-Slider */
    public void seekToSeconds(int seconds) {
        if (audioPlayer == null) return;

        // clamp
        if (seconds < 0) seconds = 0;

        int targetMillis = seconds * 1000;

        try {
            // absolute Position setzen (wie Minim AudioPlayer)
            audioPlayer.cue(targetMillis);

            // Time-Property sofort synchron
            setCurrentTime(seconds);
            return;

        } catch (Exception e) {
            // fallback, falls cue() nicht verfügbar ist
        }

        int currentMillis = audioPlayer.position();
        int diff = targetMillis - currentMillis;

        System.out.println("Seek (fallback) zu " + seconds + "s (" + targetMillis
                + " ms), aktuell " + currentMillis + " ms, diff=" + diff + " ms");

        audioPlayer.skip(diff);
        setCurrentTime(seconds);
    }

    /** Timer-Thread für currentTime */
    private void startTimeThread(int lengthSec) {
        stopTimeThread(false);

        timeThread = new Thread(() -> {
            // Timer startet ab aktueller Audio-Position
            int t = 0;
            if (audioPlayer != null) {
                try {
                    t = Math.max(0, audioPlayer.position() / 1000);
                } catch (Exception ignored) {}
            }

            while (!Thread.currentThread().isInterrupted()
                    && playing
                    && !wasStopped
                    && !isPaused
                    && (lengthSec <= 0 || t <= lengthSec)) {

                setCurrentTime(t);
                try { Thread.sleep(1000); } catch (InterruptedException e) { break; }
                t++;
            }
        });

        timeThread.setDaemon(true);
        timeThread.start();
    }

    private void stopTimeThread(boolean resetTime) {
        if (timeThread != null && timeThread.isAlive()) {
            timeThread.interrupt();
        }
        timeThread = null;
        if (resetTime) {
            setCurrentTime(0);
        }
    }

    /** 
     * Thread, der blockierendes audioPlayer.play() ausführt
     * danach wird Auto-Play gemacht (nächster Track), solange nicht Stop/Pause dazwischenkam
     */
    private class PlayThread extends Thread {
        private final int token;

        public PlayThread(int token) {
            this.token = token;
        }

        @Override
        public void run() {
            if (audioPlayer != null) {
                audioPlayer.play(); // blockiert bis Ende

                System.out.println("Song zu Ende: " + getCurrentTrack());

                // wenn inzwischen eine neue Wiedergabe gestartet wurde > ignorieren
                if (token != playToken) {
                    System.out.println("→ Thread ignoriert (Token veraltet)");
                    return;
                }

                if (wasStopped) {
                    System.out.println("→ Auto-Play NICHT gestartet (Stop)");
                    return;
                }
                if (isPaused) {
                    System.out.println("→ Auto-Play NICHT gestartet (Pause)");
                    return;
                }

                // Auto-Play: nächsten Index bestimmen
                int nextIndex;
                if (shuffle) {
                    nextIndex = (int) (Math.random() * currentPlaylist.size());
                } else {
                    nextIndex = currentIndex + 1;
                    if (nextIndex >= currentPlaylist.size()) {
                        if (repeat) nextIndex = 0;
                        else return;
                    }
                }

                currentIndex = nextIndex;

                // nur weitermachen (Auto-Play) wenn Token aktuell
                if (token == playToken) {
                    play();
                }
            }
        }
    }
    
    /** gibt die aktuelle Position des Players in Sekunden zurück */
    public int getCurrentPositionSeconds() {
        if (audioPlayer != null) {
            return audioPlayer.position() / 1000; // position() ist in Millisekunden
        }
        return 0;
    }

    /** Länge des aktuellen Tracks in Sekunden (für Slider-Max) */
    public int getCurrentTrackLengthSeconds() {
        if (audioPlayer != null) {
            return audioPlayer.length() / 1000;
        }
        // fallback: aus Track-Objekten
        if (currentPlaylist != null && currentIndex >= 0 && currentIndex < currentPlaylist.size()) {
            int len = currentPlaylist.getTracks().get(currentIndex).getLengthSec();
            if (len > 0) return len;
        }
        return 0;
    }
    
    public boolean isPlaying() {
        return playing && !isPaused && !wasStopped;
    }
    
    /** bestimmten Track aus der aktuellen Playlist abspielen, z.B. durch Klick */
    public void playTrack(Track track) {
        if (currentPlaylist == null || track == null) {
            System.out.println("Keine Playlist oder Track ist null");
            return;
        }

        int index = currentPlaylist.getTracks().indexOf(track);
        if (index < 0) {
            System.out.println("Track nicht in aktueller Playlist gefunden");
            return;
        }

        // aktuellen Index setzen
        currentIndex = index;

        // Flags für Auto-Play richtig setzen
        wasStopped = false;
        isPaused = false;

        System.out.println("Spiele Track aus Playlist: " + track);

        // normalen Play-Mechanismus benutzen
        play();
    }
    
    /**
     * wenn neues Lied gewählt wird > immer erst stop()
     * currentIndex wird auf geklickten Eintrag gesetzt
     * danach startet genau ein PlayThread + AudioPlayer
     */
    public void playTrackAtIndex(int index) {
        if (currentPlaylist == null || currentPlaylist.size() == 0) {
            System.out.println("Keine Playlist gesetzt.");
            return;
        }

        if (index < 0 || index >= currentPlaylist.size()) {
            System.out.println("Index außerhalb der Playlist: " + index);
            return;
        }

        // beendet laufende Wiedergabe 
        stop();  // setzt wasStopped=true, pausiert aktuellen Player

        currentIndex = index;
        play();  // nutzt deine bestehende play()-Logik mit Auto-Play
    }
    
}
    
