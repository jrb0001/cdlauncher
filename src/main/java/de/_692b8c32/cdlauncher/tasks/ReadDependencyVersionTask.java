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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.stream.Collectors;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 *
 * @author Jean-Rémy Buchs <jrb0001@692b8c32.de>
 */
public class ReadDependencyVersionTask extends TaskProgress {

    private final File modDir;

    private StringProperty version = new SimpleStringProperty();

    public ReadDependencyVersionTask(String name, File modDir, List<TaskProgress> dependencies) {
        super(name, dependencies);

        this.modDir = modDir;
    }

    @Override
    protected void doWork() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(modDir, "mod.yaml"))))) {
            List<String> lines = reader.lines().collect(Collectors.toList());
            boolean found = false;
            for (int i = 0; i < lines.size(); i++) {
                if (lines.get(i).startsWith("RequiresMods:")) {
                    found = true;
                } else if (found && !lines.get(i).startsWith("\t")) {
                    break;
                } else if (found && lines.get(i).contains(":")) {
                    version.setValue(lines.get(i).trim().split(":")[1].trim());
                    break;
                }
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public StringProperty versionProperty() {
        return version;
    }
}
