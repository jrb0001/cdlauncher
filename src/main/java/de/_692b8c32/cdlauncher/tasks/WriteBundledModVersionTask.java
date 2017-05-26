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
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.stream.Collectors;
import javafx.beans.property.StringProperty;

/**
 *
 * @author Jean-Rémy Buchs <jrb0001@692b8c32.de>
 */
public class WriteBundledModVersionTask extends TaskProgress {

    private final File modsDir;

    private final StringProperty version;

    public WriteBundledModVersionTask(String name, File modsDir, StringProperty version, List<TaskProgress> dependencies) {
        super(name, dependencies);

        this.modsDir = modsDir;
        this.version = version;
    }

    @Override
    protected void doWork() {
        for (File modDir : modsDir.listFiles()) {
            if (!new File(modDir, "mod.yaml").exists()) {
                continue;
            }
            processMod(modDir);
        }
    }

    private void processMod(File modDir) {
        try {
            // File content can't be written while it is being read.
            String fixedFile;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(modDir, "mod.yaml"))))) {
                fixedFile = reader.lines().map(line -> line.replace("{DEV_VERSION}", versionProperty().getValue())).collect(Collectors.joining("\n"));
            }
            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(modDir, "mod.yaml"))))) {
                writer.write(fixedFile);
            }
        } catch (IOException ex) {
            throw new RuntimeException("Failed to write bundled mod version for " + modDir.getName(), ex);
        }
    }

    public StringProperty versionProperty() {
        return version;
    }
}
