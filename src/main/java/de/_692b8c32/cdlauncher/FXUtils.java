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

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.prefs.Preferences;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 *
 * @author Jean-Rémy Buchs <jrb0001@692b8c32.de>
 */
public class FXUtils {

    private static final Map<Application, Stage> stages = new ConcurrentHashMap<>();

    public static void init(Application application, Stage stage) {
        stages.put(application, stage);
    }

    public static void showScene(Application application, String path) {
        if (!stages.containsKey(application)) {
            throw new IllegalArgumentException("FXUtils.init() wasn't called yet for this application: " + application);
        }

        FXMLLoader loader = new FXMLLoader();
        loader.setControllerFactory(cls -> {
            try {
                return cls.getDeclaredConstructor(Application.class, Preferences.class).newInstance(application, Preferences.userNodeForPackage(application.getClass()));
            } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                throw new RuntimeException("Failed to instanciate " + cls.getName(), ex);
            }
        });

        try {
            Scene scene = new Scene(loader.load(application.getClass().getResourceAsStream(path)));
            stages.get(application).setScene(scene);
        } catch (IOException ex) {
            throw new RuntimeException("Failed to load scene", ex);
        }
    }
}
