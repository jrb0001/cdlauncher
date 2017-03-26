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
import java.util.List;
import java.util.zip.ZipFile;
import org.eclipse.jgit.util.FileUtils;

/**
 *
 * @author Jean-Rémy Buchs <jrb0001@692b8c32.de>
 */
public class ExtractZipTask extends TaskProgress {

    private final File cacheFile;
    private final File destinationDir;
    private final int skipSourceParts;

    public ExtractZipTask(String name, File cacheFile, File destinationDir, int skipSourceParts, List<TaskProgress> dependencies) {
        super(name, dependencies);
        this.cacheFile = cacheFile;
        this.destinationDir = destinationDir;
        this.skipSourceParts = skipSourceParts;
    }

    @Override
    public void doWork() {
        setProgress(-1);

        try {
            FileUtils.delete(destinationDir, FileUtils.RECURSIVE | FileUtils.SKIP_MISSING);
            try (ZipFile zipFile = new ZipFile(cacheFile)) {
                zipFile.stream().filter(entry -> !entry.isDirectory()).forEach(entry -> {
                    try {
                        String name = entry.getName();
                        for (int i = 0; i < skipSourceParts; i++) {
                            name = name.substring(name.indexOf("/"));
                        }
                        File file = new File(destinationDir, name);
                        file.getParentFile().mkdirs();
                        Files.copy(zipFile.getInputStream(entry), file.toPath());
                    } catch (IOException ex) {
                        throw new RuntimeException("Failed to extract file from zip", ex);
                    }
                });
            }
        } catch (IOException ex) {
            throw new RuntimeException("Failed to extract zip", ex);
        }
    }
}
