package de.hsrm.mi.enia.moodplayer.business;

//(optional) 1:1 eure alte MP3Player-Playback-Logik ausgelagert
/**
 * MP3PlayerEngine (optional, aber empfehlenswert)

Wenn ihr nicht wollt, dass MoodPlayer 500 Zeilen bekommt:
	•	ihr kopiert eure aktuelle Audio-Logik (SimpleMinim, Threads, setGain/seek/time)
	•	MoodPlayer ruft nur noch engine.play(track), engine.pause(), engine.volume(x) etc.

➡️ Wenn ihr’s simpel halten wollt: erstmal weglassen und MoodPlayer direkt wie euren MP3Player bauen.
 */
public class MP3PlayerEngine {

}
