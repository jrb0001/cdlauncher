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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.List;

/**
 *
 * @author Jean-Rémy Buchs <jrb0001@692b8c32.de>
 */
public class SetOptionsTask extends TaskProgress {

    private final File destinationDir;
    private final String barMode;

    public SetOptionsTask(String name, File destinationDir, String barMode, List<TaskProgress> dependencies) {
        super(name, dependencies);
        this.destinationDir = destinationDir;
        this.barMode = barMode;
    }

    @Override
    public void doWork() {
        setProgress(-1);

        try {
            String modYaml = new String(Files.readAllBytes(new File(destinationDir, "mod.yaml").toPath()));
            modYaml = modYaml.replace("-bottombar", "-" + barMode);
            Files.write(new File(destinationDir, "mod.yaml").toPath(), modYaml.getBytes(), StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException ex) {
            throw new RuntimeException("Failed to change mod.yaml", ex);
        }
    }
}
