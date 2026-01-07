package de.hsrm.mi.enia.moodplayer.presentation.views;

import javafx.scene.layout.Pane;

/**
 * Gemeinsame Basisklasse für alle Controller.
 * Speichert das Root-Node der View und erzwingt initialize().
 */
public abstract class BaseController<T extends Pane> {

    protected T root;

    /** Wird von jedem Controller implementiert */
    public abstract void initialize();

    /** Gibt das Root-Element zurück, damit MoodPlayerGUI es in die Scene setzen kann */
    public T getRoot() {
        return root;
    }
}