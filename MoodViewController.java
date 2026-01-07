package de.hsrm.mi.enia.moodplayer.presentation.views;

import de.hsrm.mi.enia.moodplayer.presentation.MoodPlayerGUI;

public class MoodViewController extends BaseController<MoodView> {

    public MoodViewController() {
        root = new MoodView();
        initialize();
    }

    @Override
    public void initialize() {
        // Labels für Wheel
        root.moodWheel.setOnHoverChanged(m ->
        root.hoverLabel.setText("Hover: " + (m == null ? "-" : m))
        );

	    root.moodWheel.setOnSelectedChanged(m ->
	        root.selectedLabel.setText("Ausgewählt: " + (m == null ? "-" : m))
	    );

	    root.confirmMoodButton.setOnAction(e -> {
	    	var selected = root.moodWheel.getSelectedMood();
	    	System.out.println("[Mood] bestätigt: " + selected);
	    	MoodPlayerGUI.switchRoot("playerView");
	    });

        // später: player.setMood(selected) / filter setzen
        // fürs erste: z.B. zurück zum Player
    	root.toPlayerButton.setOnAction(e -> MoodPlayerGUI.switchRoot("playerView"));
        root.toPlaylistButton.setOnAction(e -> MoodPlayerGUI.switchRoot("playlistView"));
        root.toStartButton.setOnAction(e -> MoodPlayerGUI.switchRoot("startView"));
    
    }
}