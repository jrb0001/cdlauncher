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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.prefs.Preferences;

/**
 *
 * @author Jean-Rémy Buchs <jrb0001@692b8c32.de>
 */
public class OraUtils {

    public static Process launchOpenRA(String mod, Preferences preferences) {
        return launchExecutable("OpenRA.Game.exe", Arrays.asList("Game.Mod=" + mod), preferences);
    }

    public static Process launchUtility(String mod, List<String> args, Preferences preferences) {
        List<String> command = new ArrayList<>();
        command.add(mod);
        command.addAll(args);

        return launchExecutable("OpenRA.Utility.exe", command, preferences);
    }

    public static Process launchExecutable(String executable, List<String> args, Preferences preferences) {
        List<String> command = new ArrayList<>();

        try {
            if (preferences.get("commandMono", "mono").isEmpty()) {
                command.add(new File(preferences.get("basedir", null), "working").getAbsolutePath() + "/" + executable);
            } else {
                command.add(preferences.get("commandMono", "mono"));
                command.add(executable);
            }
            command.addAll(args);

            return new ProcessBuilder(command).directory(new File(preferences.get("basedir", null), "working")).inheritIO().start();
        } catch (IOException ex) {
            throw new RuntimeException("Failed to launch " + executable, ex);
        }
    }
}
