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

import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.web.WebView;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.html.HTMLAnchorElement;

/**
 *
 * @author Jean-Rémy Buchs <jrb0001@692b8c32.de>
 */
public class MainController implements Initializable {

    private final Application application;
    private final Preferences preferences;

    @FXML
    private WebView news;

    public MainController(Application application, Preferences preferences) {
        this.application = application;
        this.preferences = preferences;

        if (preferences.get("basedir", null) == null) {
            while (new OptionsController(application, preferences).selectDirectory() == null) {
                new Alert(Alert.AlertType.ERROR, "The launcher needs a directory to store temporary files. Press cancel if you want to close the application.", ButtonType.CANCEL, ButtonType.PREVIOUS).showAndWait().ifPresent(button -> {
                    if (button == ButtonType.CANCEL) {
                        throw new RuntimeException("User requested abort.");
                    }
                });
            }

            new Alert(Alert.AlertType.INFORMATION, "Do you want to use a prebuilt version of OpenRA? This is recommended unless you are an OpenRA developer.", ButtonType.NO, ButtonType.YES).showAndWait().ifPresent(button -> {
                if (button == ButtonType.YES) {
                    preferences.putBoolean("buildFromSources", false);
                }
                if (button == ButtonType.NO) {
                    preferences.putBoolean("buildFromSources", true);
                }
            });

            if (System.getProperty("os.name").toLowerCase().contains("win")) {
                preferences.put("commandMono", "");
            }
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            HttpClient client = HttpClients.createDefault();
            SyndFeed feed = new SyndFeedInput().build(new BufferedReader(new InputStreamReader(client.execute(new HttpGet("http://rss.moddb.com/mods/cc-tiberian-sun-project-nemesis/articles/feed/rss.xml")).getEntity().getContent())));
            feed.getAuthor();

            StringBuilder builder = new StringBuilder();
            builder.append("<style>");
            builder.append("* { color: white; background-color: black; }");
            builder.append("img { max-height: 50px; float: left; margin-right: 5px; margin-top: 20px; }");
            builder.append("p { margin-top: -30px; margin-bottom: 20px; min-height: 70px; }");
            builder.append("a { text-decoration: none; }");
            builder.append("</style>");
            for (SyndEntry entry : feed.getEntries()) {
                builder.append("<a href='").append(entry.getLink()).append("' target='_blank'><h2>").append(entry.getTitle()).append("</h2>");
                builder.append("<p>").append(entry.getDescription().getValue()).append("</p></a>");
            }

            news.getEngine().documentProperty().addListener((document, oldValue, newValue) -> {
                NodeList nodeList = newValue.getElementsByTagName("a");
                for (int i = 0; i < nodeList.getLength(); i++) {
                    Node node = nodeList.item(i);
                    EventTarget eventTarget = (EventTarget) node;
                    eventTarget.addEventListener("click", evt -> {
                        application.getHostServices().showDocument(((HTMLAnchorElement) evt.getCurrentTarget()).getHref());
                        evt.preventDefault();
                    }, false);
                }
            });

            news.getEngine().loadContent(builder.toString());
        } catch (IOException | IllegalArgumentException | FeedException ex) {
            Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, "Failed to load RSS", ex);
        }
    }

    public void exit() {
        Platform.exit();
    }

    public void update() {
        FXUtils.showScene(application, "/fxml/update.fxml");
    }

    public void launch() {
        if (new File(new File(preferences.get("basedir", null), "working").getAbsolutePath(), "OpenRA.Game.exe").exists()) {
            OraUtils.launchOpenRA("cd", preferences);
        } else {
            update();
        }
    }

    public void options() {
        FXUtils.showScene(application, "/fxml/options.fxml");
    }

    public void changelog() {
        application.getHostServices().showDocument("https://github.com/DoGyAUT/cd/commits");
    }

    public void github() {
        application.getHostServices().showDocument("https://github.com/DoGyAUT/cd");
    }

    public void moddb() {
        application.getHostServices().showDocument("http://www.moddb.com/mods/cc-tiberian-sun-project-nemesis");
    }

    public void discord() {
        application.getHostServices().showDocument("https://discord.gg/Wc8nX9T");
    }

    public void wikia() {
        application.getHostServices().showDocument("http://crystallized-doom.wikia.com/wiki/Crystallized_Doom_Wikia");
    }
}
