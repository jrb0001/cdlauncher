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

import de._692b8c32.cdlauncher.OraUtils;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.prefs.Preferences;

/**
 *
 * @author Jean-Rémy Buchs <jrb0001@692b8c32.de>
 */
public class RegisterModMetadataTask extends TaskProgress {

    private final File modDir;
    private final Preferences preferences;

    public RegisterModMetadataTask(String name, File modDir, Preferences preferences, List<TaskProgress> dependencies) {
        super(name, dependencies);

        this.modDir = modDir;
        this.preferences = preferences;
    }

    @Override
    protected void doWork() {
        try {
            Files.list(new File(modDir, "mods").toPath()).filter(Files::isDirectory).filter(path -> Files.isRegularFile(path.resolve("mod.yaml"))).map(Path::getFileName).map(Object::toString).forEach(this::registerMod);
        } catch (IOException ex) {
            throw new RuntimeException("Failed to enumerate bundled mods", ex);
        }
    }

    private void registerMod(String name) {
        try {
            String launchpath = new File(preferences.get("basedir", null), "working").getAbsolutePath() + "/OpenRA.Game.exe";
            Process p = OraUtils.launchUtility(name, Arrays.asList("--register-mod", launchpath, "user"), preferences);
            p.waitFor();
        } catch (InterruptedException ex) {
            throw new RuntimeException("Failed to register mod " + name, ex);
        }
    }
}
