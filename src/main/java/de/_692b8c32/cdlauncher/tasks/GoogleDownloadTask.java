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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;

/**
 *
 * @author Jean-Rémy Buchs <jrb0001@692b8c32.de>
 */
public class GoogleDownloadTask extends TaskProgress {

    private final File cacheFile;
    private final String fileUrl;

    public GoogleDownloadTask(String name, File cacheFile, String fileUrl, List<TaskProgress> dependencies) {
        super(name, dependencies);
        this.cacheFile = cacheFile;
        this.fileUrl = fileUrl;
    }

    @Override
    public void doWork() {
        setProgress(-1);

        try {
            HttpClient client = HttpClients.createDefault();
            String realUrl = null;
            Pattern pattern = Pattern.compile("<a id=\"uc-download-link\" class=\"goog-inline-block jfk-button jfk-button-action\" href=\"([^\"]*)\">");
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(client.execute(new HttpGet(fileUrl)).getEntity().getContent()))) {
                for (String line : reader.lines().collect(Collectors.toList())) {
                    Matcher matcher = pattern.matcher(line);
                    if (matcher.find()) {
                        realUrl = fileUrl.substring(0, fileUrl.lastIndexOf('/')) + matcher.group(1).replace("&amp;", "&");
                        break;
                    }
                }
            }

            if (realUrl == null) {
                throw new RuntimeException("Failed to find real url");
            } else {
                try (InputStream stream = client.execute(new HttpGet(realUrl)).getEntity().getContent()) {
                    Files.copy(stream, cacheFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(GoogleDownloadTask.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
