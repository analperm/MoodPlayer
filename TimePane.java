package de.hsrm.mi.enia.moodplayer.presentation.uicomponents;

import javafx.geometry.Insets;
import javafx.scene.layout.Priority;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;

/**
 * Zeitleiste für einen Track
 *
 * zeigt:
 * - aktuelle Zeit
 * - Slider zum Springen im Track
 * - verbleibende Zeit
 */

public class TimePane extends HBox {

    private final Label currentTimeLabel;
    private final Label remainingTimeLabel;
    private final Slider timeSlider;
    
    private int maxTimeSeconds = 0;

    public TimePane() {
    	
        currentTimeLabel = new Label("0:00");
        remainingTimeLabel = new Label ("-0:00");
        
        // Slider ohne Tickmarks
        timeSlider = new Slider(0, 360, 0);
        timeSlider.setShowTickMarks(false);
        timeSlider.setShowTickLabels(false);
        
        // Slider darf wachsen
        timeSlider.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(timeSlider, Priority.ALWAYS);

        // beim Bewegen nur Anzeige aktualisieren > Seek macht der Controller
        timeSlider.valueProperty().addListener((obs, oldV, newV) -> {
        	updateTimeLabels(newV.doubleValue());
        });

        setAlignment(Pos.CENTER_LEFT);
        setSpacing(10);
        setPadding(new Insets(5, 10, 5, 10));

        getChildren().addAll(currentTimeLabel, timeSlider, remainingTimeLabel);
    }

    /** Zugriff auf den Slider (für den Controller, zum Seek-Listener anhängen) */
    public Slider getSlider() {
        return timeSlider;
    }

    /** maximale Zeit in Sekunden setzen (damit der Slider zum Song passt) */
    public void setMaxTime(int seconds) {
    	this.maxTimeSeconds = seconds;
        timeSlider.setMax(seconds);
        updateTimeLabels(timeSlider.getValue());
    }

    /** Anzeige wieder auf 0:00 setzen (z.B. beim Track-Wechsel) */
    public void reset() {
        timeSlider.setValue(0);
        
        updateTimeLabels(0);
    }
    
    public void setCurrentTime(double seconds) {
    	timeSlider.setValue(seconds);
    	updateTimeLabels(seconds);
    }
    
    private void updateTimeLabels(double currentSeconds) {

        // linkes Label: aktuelle Zeit
        currentTimeLabel.setText(
            TimeConverter.numberToTimeString(currentSeconds)
        );

        // rechtes Label: Restzeit
        int remaining = Math.max(0, maxTimeSeconds - (int) currentSeconds);
        remainingTimeLabel.setText(
            "-" + TimeConverter.numberToTimeString(remaining)
        );
    }

}