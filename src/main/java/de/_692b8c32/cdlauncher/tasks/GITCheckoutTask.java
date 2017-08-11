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
import java.nio.file.StandardCopyOption;
import java.util.List;
import javafx.beans.binding.StringExpression;
import javafx.beans.property.ReadOnlyStringWrapper;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.errors.RepositoryNotFoundException;
import org.eclipse.jgit.util.FileUtils;

/**
 *
 * @author Jean-Rémy Buchs <jrb0001@692b8c32.de>
 */
public class GITCheckoutTask extends TaskProgress {

    private final File cacheDir;
    private final File destinationDir;
    private final StringExpression startPoint;
    private final String branch;

    public GITCheckoutTask(String name, File cacheDir, File destinationDir, String branch, String startPoint, List<TaskProgress> dependencies) {
        this(name, cacheDir, destinationDir, branch, new ReadOnlyStringWrapper(startPoint), dependencies);
    }

    public GITCheckoutTask(String name, File cacheDir, File destinationDir, String branch, StringExpression startPoint, List<TaskProgress> dependencies) {
        super(name, dependencies);
        this.cacheDir = cacheDir;
        this.destinationDir = destinationDir;
        this.startPoint = startPoint;
        this.branch = branch;
    }

    @Override
    public void doWork() {
        setProgress(-1);

        try {
            Git git = Git.open(cacheDir);
            git.reset().setMode(ResetCommand.ResetType.HARD).call();
            git.checkout().setAllPaths(true).setForce(true).setName(branch).setStartPoint(startPoint.getValue()).call();

            if (destinationDir != null) {
                FileUtils.delete(destinationDir, FileUtils.RECURSIVE | FileUtils.SKIP_MISSING);
                destinationDir.mkdirs();
                Files.list(cacheDir.toPath()).filter(path -> !(".git".equals(path.getFileName().toString()))).forEach(path -> {
                    try {
                        Files.move(path, destinationDir.toPath().resolve(path.getFileName()), StandardCopyOption.ATOMIC_MOVE);
                    } catch (IOException ex) {
                        throw new RuntimeException("Failed to move " + path.getFileName(), ex);
                    }
                });
            }
            git.close();
        } catch (RepositoryNotFoundException | InvalidRemoteException ex) {
            throw new RuntimeException("Could not find repository");
        } catch (GitAPIException | IOException ex) {
            throw new RuntimeException("Could not checkout data", ex);
        }
    }
}
