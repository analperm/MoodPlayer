package de.hsrm.mi.enia.moodplayer.presentation.uicomponents;

import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Circle;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * eigenes JavaFX-UI-Element
 * zeigt vereinfachtes Mood Wheel und wertet Maus-Interaktion aus
 */

public class MoodWheelPane extends Pane {

	/** mögliche Mood-Kategorien für die erste Version */
    public enum Mood {
        JOYFUL, CALM, ENERGETIC, SAD, ANGRY, PEACEFUL, STRESSED, FOCUSED
    }

    // alle gezeichneten Segmente
    private final List<Arc> segments = new ArrayList<>();
    
    private final List<Text> labels = new ArrayList<>();
    
    // Array aller Mood-Werte
    private final Mood[] moods = Mood.values();

    private Mood hoveredMood = null; // aktuell gehoverte Mood
    private Mood selectedMood = null; // aktuell ausgewählte Mood

    private Consumer<Mood> onHoverChanged; // Callback, der aufgerufen wird, wenn sich Hover-Mood ändert
    private Consumer<Mood> onSelectedChanged; // Callback, der aufgerufen wird, wenn sich Auswahl ändert

    // Wheel Geometrie: Mittelpunkt (cx, cy) und Radius
    private double cx = 200;
    private double cy = 200;
    private double radius = 160;
    
    // Farben pro Segment (einfaches, klares Set)
    private final Color[] colors = new Color[] {
            Color.web("#F7D53D"), // JOYFUL
            Color.web("#4DB6AC"), // CALM
            Color.web("#FF8A65"), // ENERGETIC
            Color.web("#7986CB"), // SAD
            Color.web("#E57373"), // ANGRY
            Color.web("#81C784"), // PEACEFUL
            Color.web("#BA68C8"), // STRESSED
            Color.web("#90A4AE")  // FOCUSED
    };

    /** Konstruktor: baut komplette Komponente auf */
    public MoodWheelPane() {
        setPrefSize(400, 400); // Größe

        // optional: Hintergrund-Kreis (nur Optik)
        Circle border = new Circle(cx, cy, radius);
        border.setFill(null);
        border.setStrokeWidth(2);
        getChildren().add(border);

        buildSegments();

        addEventHandler(MouseEvent.MOUSE_MOVED, this::handleMouseMove); // Hover-Logik
        addEventHandler(MouseEvent.MOUSE_EXITED, e -> setHovered(null)); // Hover löschen
        addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
        	// Klick übernimmt die zuletzt gehoverte Mood als feste Auswahl
            if (hoveredMood != null) setSelected(hoveredMood);
        });
    }

    /** erzeugt die grafischen Segmente des Wheels */
    private void buildSegments() {
        double segSize = 360.0 / moods.length; // Kreis mit 360 Grad durch Anzahl der Moods

        // für jede Mood wird ein Arc-Segment (Tortenstück) erstellt
        for (int i = 0; i < moods.length; i++) {
            // Arc-Segment
            Arc arc = new Arc(cx, cy, radius, radius, i * segSize, segSize);
            arc.setType(ArcType.ROUND);
            arc.setFill(colors[i % colors.length]);
            arc.setStroke(javafx.scene.paint.Color.WHITE);
            arc.setStroke(Color.WHITE);
            arc.setStrokeWidth(2);
            arc.setOpacity(0.75);

            segments.add(arc);
            getChildren().add(arc);

            // Label an die Mitte des Segments setzen
            double midAngleDeg = i * segSize + segSize / 2.0;
            double labelRadius = radius * 0.60; // Text etwas weiter innen

            double rad = Math.toRadians(midAngleDeg);
            double lx = cx + Math.cos(rad) * labelRadius;
            double ly = cy + Math.sin(rad) * labelRadius;

            Text t = new Text(moods[i].name());
            t.setStyle("-fx-font-size: 12px; -fx-font-weight: 700;");
            t.setFill(Color.BLACK);

            // Text ungefähr zentrieren
            t.setX(lx - 22); // grob zentriert (für MVP ok)
            t.setY(ly + 4);

            labels.add(t);
            getChildren().add(t);
        }
        
        // Labels immer über den Segmenten anzeigen
        for (Text t : labels) {
            t.toFront();
        }
        // initiales Styling (Hover/Selected)
        // updateStyles();
    }

    /** wird bei jeder Mausbewegung über dem Pane aufgerufen -> soll Mausposition in ein Segment umrechnen */
    private void handleMouseMove(MouseEvent e) {
    	// Maus-Koordinaten holen
        double mx = e.getX();
        double my = e.getY();

        // Abstand zum Mittelpunkt berechnen: außerhalb des Kreises? -> nichts hover
        double dx = mx - cx; // Vektor zum Mittelpunkt der Maus
        double dy = my - cy;
        double dist = Math.sqrt(dx * dx + dy * dy); // Abstand der Maus zum Mittelpunkt
        if (dist > radius) {
            setHovered(null);
            return;
        }

        // Winkel berechnen
        double angle = Math.toDegrees(Math.atan2(dy, dx));
        angle = (angle + 360) % 360; // normalisieren auf 0..359

        int idx = (int) Math.floor(angle / (360.0 / moods.length));
        idx = Math.max(0, Math.min(idx, moods.length - 1));
        setHovered(moods[idx]);
    }

    /** setzt die aktuell gehoverte Mood */
    private void setHovered(Mood mood) {
        if (hoveredMood == mood) return;
        hoveredMood = mood;
        // updateStyles();

        if (onHoverChanged != null) onHoverChanged.accept(hoveredMood);
    }

    /** speichert die endgültig ausgewählte Mood (nach Klick) */
    private void setSelected(Mood mood) {
        selectedMood = mood;
        // updateStyles();

        if (onSelectedChanged != null) onSelectedChanged.accept(selectedMood);
    }

    /** aktualisiert die Optik aller Segmente abhängig von Hover/Selektion */
    private void updateStyles() {
        // Reset
        for (int i = 0; i < segments.size(); i++) {
            Arc arc = segments.get(i);
            arc.setOpacity(0.35);
            arc.setStrokeWidth(0);
        }

        // Hover Highlight
        if (hoveredMood != null) {
            int idx = hoveredMood.ordinal();
            Arc a = segments.get(idx);
            a.setOpacity(0.6);
            a.setStrokeWidth(2);
            a.setStroke(Color.BLACK);   
            
        }

        // Selected Highlight (stärker)
        if (selectedMood != null) {
            int idx = selectedMood.ordinal();
            Arc a = segments.get(idx);
            a.setOpacity(0.85);
            a.setStrokeWidth(3);
            a.setStroke(Color.WHITE);  
        }
    }

    /** gibt die aktuell ausgewählte Mood zurück - wird vom Button im Controller genutzt */
    public Mood getSelectedMood() {
        return selectedMood;
    }

    /** registriert Callback, sobald sich Hover-Mood ändert */
    public void setOnHoverChanged(Consumer<Mood> cb) {
        this.onHoverChanged = cb;
    }

    /** registriert Callback, sobald Nutzer eine Mood durch Klick auswählt */
    public void setOnSelectedChanged(Consumer<Mood> cb) {
        this.onSelectedChanged = cb;
    }
}