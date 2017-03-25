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
package de._692b8c32.cdlauncher.tasks;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;

/**
 *
 * @author Jean-Rémy Buchs <jrb0001@692b8c32.de>
 */
public abstract class TaskProgress implements Runnable {

    private final String name;
    private final List<TaskProgress> dependencies;
    private final DoubleProperty progress = new SimpleDoubleProperty();

    public TaskProgress(String name, List<TaskProgress> dependencies) {
        this.name = name;
        this.dependencies = dependencies;
    }

    public String getName() {
        return name;
    }

    public double getProgress() {
        return progress.get();
    }

    public void setProgress(double progress) {
        Platform.runLater(() -> this.progress.set(progress));
    }

    public DoubleProperty progressProperty() {
        return progress;
    }

    @Override
    public void run() {
        try {
            while (dependencies.stream().anyMatch(dependency -> dependency.getProgress() != 1)) {
                Thread.sleep(100);
            }
        } catch (InterruptedException ex) {
            Logger.getLogger(TaskProgress.class.getName()).log(Level.SEVERE, "Exception while waiting for dependencies", ex);
        }

        doWork();

        setProgress(1);
    }

    protected abstract void doWork();
}
