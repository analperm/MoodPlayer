package de.hsrm.mi.enia.moodplayer.presentation.views;

import de.hsrm.mi.enia.moodplayer.presentation.MoodPlayerGUI;

public class StartViewController extends BaseController<StartView> {

    public StartViewController() {
        root = new StartView();
        initialize();
    }

    @Override
    public void initialize() {

        // Links-Navigation
        root.toPlayerButton.setOnAction(e -> MoodPlayerGUI.switchRoot("playerView"));
        root.toPlaylistButton.setOnAction(e -> MoodPlayerGUI.switchRoot("playlistView"));
        root.toMoodButton.setOnAction(e -> MoodPlayerGUI.switchRoot("moodView"));

        // Hauptbutton -> Mood View 
        root.moodSelectButton.setOnAction(e -> MoodPlayerGUI.switchRoot("moodView"));
    }
}