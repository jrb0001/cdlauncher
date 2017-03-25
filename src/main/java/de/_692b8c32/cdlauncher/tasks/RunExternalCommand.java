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
import java.util.List;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Jean-Rémy Buchs <jrb0001@692b8c32.de>
 */
public class RunExternalCommand extends TaskProgress {

    private final File workingDirectory;
    private final List<String> command;
    private final Function<Integer, ResultAction> resultFunction;

    public RunExternalCommand(String name, File workingDirectory, List<String> command, Function<Integer, ResultAction> resultFunction, List<TaskProgress> dependencies) {
        super(name, dependencies);
        this.workingDirectory = workingDirectory;
        this.command = command;
        this.resultFunction = resultFunction;
    }

    @Override
    protected void doWork() {
        setProgress(-1);

        try {
            Process p = new ProcessBuilder(command).directory(workingDirectory).inheritIO().start();
            int exitValue = p.waitFor();
            switch (resultFunction.apply(exitValue)) {
                case SUCCEED:
                    break;
                case RETRY:
                    doWork();
                    break;
                case FAIL:
                    throw new RuntimeException("External command exited with code " + exitValue);
            }
        } catch (IOException | InterruptedException ex) {
            Logger.getLogger(RunExternalCommand.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static enum ResultAction {
        SUCCEED, RETRY, FAIL;
    }
}
