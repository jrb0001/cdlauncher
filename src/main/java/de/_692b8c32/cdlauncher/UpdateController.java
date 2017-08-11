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

import de._692b8c32.cdlauncher.tasks.ExtractZipTask;
import de._692b8c32.cdlauncher.tasks.GITCheckoutTask;
import de._692b8c32.cdlauncher.tasks.GITUpdateTask;
import de._692b8c32.cdlauncher.tasks.GithubReleasesDownloadTask;
import de._692b8c32.cdlauncher.tasks.ReadDependencyVersionTask;
import de._692b8c32.cdlauncher.tasks.RegisterModMetadataTask;
import de._692b8c32.cdlauncher.tasks.RunExternalCommand;
import de._692b8c32.cdlauncher.tasks.SetOptionsTask;
import de._692b8c32.cdlauncher.tasks.TaskProgress;
import de._692b8c32.cdlauncher.tasks.WriteBundledModVersionTask;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.When;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.ProgressBarTableCell;

/**
 *
 * @author Jean-Rémy Buchs <jrb0001@692b8c32.de>
 */
public class UpdateController implements Initializable {

    @FXML
    private TableView<TaskProgress> progressTable;
    @FXML
    private TableColumn<TaskProgress, String> nameColumn;
    @FXML
    private TableColumn<TaskProgress, Double> progressColumn;

    private final Application application;
    private final Preferences preferences;

    public UpdateController(Application application, Preferences preferences) {
        this.application = application;
        this.preferences = preferences;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        File oraSourceCache = new File(preferences.get("basedir", null), "ora.git");
        File oraBinaryCache = new File(preferences.get("basedir", null), "ora.zip");
        File cdCache = new File(preferences.get("basedir", null), "cd.git");
        File stCache = new File(preferences.get("basedir", null), "soundtrack.git");
        File destination = new File(preferences.get("basedir", null), "working");

        TaskProgress downloadModTask = new GITUpdateTask("Mod (download)", cdCache, "https://github.com/DoGyAUT/cd.git", Arrays.asList());
        TaskProgress preCheckoutModTask = new GITCheckoutTask("Mod (precheckout)", cdCache, null, "master", preferences.get("modRef", "refs/remotes/origin/master"), Arrays.asList(downloadModTask));
        ReadDependencyVersionTask readDependencyVersionTask = new ReadDependencyVersionTask("Mod (read dependency version)", cdCache, Arrays.asList(preCheckoutModTask));

        TaskProgress oraFilesReadyTask;
        TaskProgress oraInstallationReadyTask;
        if (preferences.getBoolean("buildFromSources", true)) {
            String makeCommand = preferences.get("commandMake", "make").isEmpty() ? new File(destination, "make.cmd").getAbsolutePath() : preferences.get("commandMake", "make");
            TaskProgress downloadOraSourceTask = new GITUpdateTask("Open RA (download sources)", oraSourceCache, "https://github.com/DoGyAUT/OpenRA.git", Arrays.asList());
            TaskProgress checkoutOraSourceTask = new GITCheckoutTask("Open RA (checkout sources)", oraSourceCache, destination, "cd", new When(readDependencyVersionTask.versionProperty().isNotNull()).then(new ReadOnlyStringWrapper("refs/tags/").concat(readDependencyVersionTask.versionProperty())).otherwise("refs/remotes/origin/cd"), Arrays.asList(downloadOraSourceTask, readDependencyVersionTask));
            TaskProgress downloadOraSourceDependenciesTask = new RunExternalCommand("Open RA (download dependencies)", destination, Arrays.asList(makeCommand, "dependencies"), code -> code == 0 ? RunExternalCommand.ResultAction.SUCCEED : code == 2 ? RunExternalCommand.ResultAction.RETRY : RunExternalCommand.ResultAction.FAIL, Arrays.asList(checkoutOraSourceTask));
            TaskProgress compileOraSourceTask = new RunExternalCommand("Open RA (compile sources)", destination, Arrays.asList(makeCommand, "all"), code -> code == 0 ? RunExternalCommand.ResultAction.SUCCEED : code == 2 ? RunExternalCommand.ResultAction.RETRY : RunExternalCommand.ResultAction.FAIL, Arrays.asList(checkoutOraSourceTask, downloadOraSourceDependenciesTask));
            TaskProgress writeBundledModVersionTask = new WriteBundledModVersionTask("Open RA (set version of bundled mods)", new File(destination, "mods"), readDependencyVersionTask.versionProperty(), Arrays.asList(checkoutOraSourceTask, compileOraSourceTask, readDependencyVersionTask));

            oraFilesReadyTask = checkoutOraSourceTask;
            oraInstallationReadyTask = writeBundledModVersionTask;
            progressTable.setItems(FXCollections.observableList(new ArrayList<>(Arrays.asList(
                    downloadOraSourceTask,
                    checkoutOraSourceTask,
                    downloadOraSourceDependenciesTask,
                    compileOraSourceTask,
                    writeBundledModVersionTask
            ))));
        } else {
            TaskProgress downloadOraBinaryTask = new GithubReleasesDownloadTask("Open RA (download binaries)", oraBinaryCache, "https://github.com/DoGyAUT/OpenRA/releases/", readDependencyVersionTask.versionProperty(), Arrays.asList(readDependencyVersionTask));
            TaskProgress extractOraBinaryTask = new ExtractZipTask("Open RA (extract binaries)", oraBinaryCache, destination, 0, Arrays.asList(downloadOraBinaryTask));

            oraFilesReadyTask = extractOraBinaryTask;
            oraInstallationReadyTask = extractOraBinaryTask;
            progressTable.setItems(FXCollections.observableList(new ArrayList<>(Arrays.asList(
                    downloadOraBinaryTask,
                    extractOraBinaryTask
            ))));
        }

        TaskProgress checkoutModTask = new GITCheckoutTask("Mod (checkout)", cdCache, new File(destination, "mods/cd"), "master", preferences.get("modRef", "refs/remotes/origin/master"), Arrays.asList(preCheckoutModTask, oraFilesReadyTask));
        TaskProgress setOptionsTask = new SetOptionsTask("Apply options", new File(destination, "mods/cd"), preferences.get("barMode", "bottombar"), Arrays.asList(checkoutModTask));

        progressTable.getItems().addAll(FXCollections.observableList(Arrays.asList(
                downloadModTask,
                preCheckoutModTask,
                readDependencyVersionTask,
                checkoutModTask,
                setOptionsTask
        )));

        if (preferences.getBoolean("downloadSoundtrack", true)) {
            TaskProgress downloadSoundtrackTask = new GITUpdateTask("Soundtrack (download)", stCache, "https://github.com/jrb0001/cdsoundtrack.git", Arrays.asList());
            TaskProgress checkoutSoundtrackTask = new GITCheckoutTask("Soundtrack (checkout)", stCache, new File(destination, "mods/cd/audio/data/theme"), "master", "refs/remotes/origin/master", Arrays.asList(downloadSoundtrackTask, checkoutModTask)); // TODO: Use preferences.

            progressTable.getItems().addAll(FXCollections.observableList(Arrays.asList(
                    downloadSoundtrackTask,
                    checkoutSoundtrackTask
            )));
        }

        TaskProgress registerModMetadataTask = new RegisterModMetadataTask("Register mod metadata", destination, preferences, Arrays.asList(setOptionsTask, oraInstallationReadyTask));
        progressTable.getItems().add(registerModMetadataTask);

        progressTable.getItems().forEach(task -> new Thread(task, "Task " + task.getName()).start());

        nameColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().getName()));
        progressColumn.setCellValueFactory(cell -> cell.getValue().progressProperty().asObject());

        progressColumn.setCellFactory(ProgressBarTableCell.forTableColumn());

        nameColumn.prefWidthProperty().bind(progressTable.widthProperty().multiply(0.5));
        progressColumn.prefWidthProperty().bind(progressTable.widthProperty().multiply(0.4975));

        new Thread(() -> {
            try {
                while (progressTable.getItems().stream().anyMatch(task -> task.getProgress() != 1)) {
                    Thread.sleep(100);
                }
            } catch (InterruptedException ex) {
                Logger.getLogger(UpdateController.class.getName()).log(Level.SEVERE, "Failed to wait for end of all tasks.", ex);
            }

            Platform.runLater(() -> FXUtils.showScene(application, "/fxml/main.fxml"));
        }, "Taskwatcher").start();
    }
}
