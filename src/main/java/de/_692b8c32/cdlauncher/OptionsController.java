/*
 * Copyright (C) 2017 Jean-Rémy Buchs <jrb0001@692b8c32.de>
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package de._692b8c32.cdlauncher;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;

/**
 *
 * @author Jean-Rémy Buchs <jrb0001@692b8c32.de>
 */
public class OptionsController implements Initializable {

    @FXML
    private CheckBox buildFromSources;
    @FXML
    private TextField commandMake;
    @FXML
    private TextField commandMono;
    @FXML
    private CheckBox rightBar;
    @FXML
    private CheckBox downloadSoundtrack;

    private final Application application;
    private final Preferences preferences;

    public OptionsController(Application application, Preferences preferences) {
        this.application = application;
        this.preferences = preferences;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        buildFromSources.setSelected(preferences.getBoolean("buildFromSources", true));
        commandMake.setText(preferences.get("commandMake", "make"));
        commandMono.setText(preferences.get("commandMono", "mono"));
        rightBar.setSelected(preferences.get("barMode", "bottombar").equals("rightbar"));
        downloadSoundtrack.setSelected(preferences.getBoolean("downloadSoundtrack", true));

        commandMake.disableProperty().bind(buildFromSources.selectedProperty().not());
    }

    public void cancel() {
        FXUtils.showScene(application, "/fxml/main.fxml");
    }

    public void save() {
        preferences.putBoolean("buildFromSources", buildFromSources.isSelected());
        preferences.put("commandMake", commandMake.getText());
        preferences.put("commandMono", commandMono.getText());
        preferences.put("barMode", rightBar.isSelected() ? "rightbar" : "bottombar");
        preferences.putBoolean("downloadSoundtrack", downloadSoundtrack.isSelected());

        FXUtils.showScene(application, "/fxml/main.fxml");
    }

    public File selectDirectory() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Choose directory for temporary files");
        directoryChooser.setInitialDirectory(new File(preferences.get("basedir", System.getProperty("java.io.tmpdir"))));
        File directory = directoryChooser.showDialog(null);

        if (directory != null) {
            preferences.put("basedir", directory.getAbsolutePath());
            try {
                preferences.sync();
            } catch (BackingStoreException ex) {
                Logger.getLogger(OptionsController.class.getName()).log(Level.SEVERE, "Failed to save preferences", ex);
            }
        }
        return directory;
    }
}
