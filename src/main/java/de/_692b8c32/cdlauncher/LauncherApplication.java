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

import com.sun.javafx.application.HostServicesDelegate;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;
import javax.swing.SwingUtilities;
import netscape.javascript.JSObject;

/**
 *
 * @author Jean-Rémy Buchs <jrb0001@692b8c32.de>
 */
public class LauncherApplication extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        installExceptionHandler();

        installHostServicesFix();

        FXUtils.init(this, stage);

        FXUtils.showScene(this, "/fxml/main.fxml");

        stage.setResizable(false);
        stage.setTitle("cdlauncher");
        stage.show();
    }

    private void installExceptionHandler() {
        Thread.setDefaultUncaughtExceptionHandler((thread, exception) -> {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Unhandled exception in thread " + thread.getName(), exception);
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Exception");
                alert.setHeaderText("Unhandled exception in thread \"" + thread.getName() + "\"");

                // Create expandable Exception.
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                exception.printStackTrace(pw);
                String exceptionText = sw.toString();

                Label label = new Label("The exception stacktrace was:");

                TextArea textArea = new TextArea(exceptionText);
                textArea.setEditable(false);
                textArea.setWrapText(true);

                textArea.setMaxWidth(Double.MAX_VALUE);
                textArea.setMaxHeight(Double.MAX_VALUE);
                GridPane.setVgrow(textArea, Priority.ALWAYS);
                GridPane.setHgrow(textArea, Priority.ALWAYS);

                GridPane expContent = new GridPane();
                expContent.setMaxWidth(Double.MAX_VALUE);
                expContent.add(label, 0, 0);
                expContent.add(textArea, 0, 1);

                // Set expandable Exception into the dialog pane.
                alert.getDialogPane().setContent(expContent);

                alert.showAndWait();
            });
        });
    }

    private void installHostServicesFix() {
        try {
            getHostServices().getCodeBase();
        } catch (NullPointerException ex) {
            Logger.getLogger(getClass().getName()).log(Level.WARNING, "Host services not set, installing fallback");

            try {
                Field field = HostServices.class.getDeclaredField("delegate");
                field.setAccessible(true);
                field.set(getHostServices(), new HostServicesDelegate() {
                    @Override
                    public String getCodeBase() {
                        return "";
                    }

                    @Override
                    public String getDocumentBase() {
                        return new File("").toURI().toString();
                    }

                    @Override
                    public void showDocument(String uri) {
                        SwingUtilities.invokeLater(() -> {
                            try {
                                Desktop.getDesktop().browse(URI.create(uri));
                            } catch (IOException ex) {
                                throw new RuntimeException("Failed to open browser", ex);
                            }
                        });
                    }

                    @Override
                    public JSObject getWebContext() {
                        return null;
                    }
                });

            } catch (SecurityException | IllegalArgumentException | IllegalAccessException | NoSuchFieldException ex1) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Failed to install fallback host services", ex1);
            }
        }
    }
}
