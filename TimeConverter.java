package de.hsrm.mi.enia.moodplayer.presentation.uicomponents;

/**
 * Hilfsklasse zur Umrechnung von Sekunden in mm:ss.
 */

public class TimeConverter {

    /** wandelt Sekunden in einen String um */
    public static String numberToTimeString(double secondsTotal) {
        int total = (int) Math.floor(secondsTotal);
        int minutes = total / 60;
        int seconds = total % 60;
        return String.format("%d:%02d", minutes, seconds);
    }
}