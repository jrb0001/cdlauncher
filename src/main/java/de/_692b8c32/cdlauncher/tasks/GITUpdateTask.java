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
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.errors.RepositoryNotFoundException;
import org.eclipse.jgit.lib.ProgressMonitor;
import org.eclipse.jgit.util.FileUtils;

/**
 *
 * @author Jean-Rémy Buchs <jrb0001@692b8c32.de>
 */
public class GITUpdateTask extends TaskProgress implements ProgressMonitor {

    private final File cacheDir;
    private final String repoUri;
    private double totalTasks;
    private double finishedTasks;
    private double totalWork;

    public GITUpdateTask(String name, File cacheDir, String repoUri, List<TaskProgress> dependencies) {
        super(name, dependencies);
        this.cacheDir = cacheDir;
        this.repoUri = repoUri;
    }

    @Override
    public void doWork() {
        try {
            Git git;
            if (!cacheDir.exists()) {
                cacheDir.mkdirs();
                git = Git.cloneRepository().setURI(repoUri).setDirectory(cacheDir).setNoCheckout(true).setProgressMonitor(this).call();
            } else {
                git = Git.open(cacheDir);
                git.fetch().setProgressMonitor(this).call();
            }
            git.close();
        } catch (RepositoryNotFoundException | InvalidRemoteException ex) {
            Logger.getLogger(GITUpdateTask.class.getName()).log(Level.SEVERE, "Could not find repository", ex);
            try {
                FileUtils.delete(cacheDir);
                run();
            } catch (IOException | StackOverflowError ex1) {
                throw new RuntimeException("Fix of broken repository failed", ex1);
            }
        } catch (GitAPIException | IOException ex) {
            throw new RuntimeException("Could not download data", ex);
        }
    }

    @Override
    public void start(int totalTasks) {
        this.totalTasks = totalTasks + 1;
    }

    @Override
    public void beginTask(String title, int totalWork) {
        this.totalWork = totalWork;
    }

    @Override
    public void update(int completed) {
        setProgress((finishedTasks + completed / totalWork) / totalTasks);
    }

    @Override
    public void endTask() {
        finishedTasks++;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }
}
