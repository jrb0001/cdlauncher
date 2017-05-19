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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javafx.beans.property.StringProperty;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;

/**
 *
 * @author Jean-Rémy Buchs <jrb0001@692b8c32.de>
 */
public class GithubReleasesDownloadTask extends TaskProgress {

    private final File cacheFile;
    private final String baseUrl;
    private final StringProperty version;

    public GithubReleasesDownloadTask(String name, File cacheFile, String baseUrl, StringProperty version, List<TaskProgress> dependencies) {
        super(name, dependencies);
        this.cacheFile = cacheFile;
        this.baseUrl = baseUrl;
        this.version = version;
    }

    @Override
    public void doWork() {
        setProgress(-1);

        try {
            String realUrl = null;
            HttpClient client = HttpClients.createDefault();
            if (version.getValue() == null) {
                Pattern pattern = Pattern.compile("<ul class=\"release-downloads\"> <li> <a href=\"([^\"]*)\" rel=\"nofollow\">");
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(client.execute(new HttpGet(baseUrl + "latest")).getEntity().getContent()))) {
                    Matcher matcher = pattern.matcher(reader.lines().collect(Collectors.joining(" ")).replace("  ", ""));
                    if (matcher.find()) {
                        realUrl = baseUrl.substring(0, baseUrl.indexOf('/', baseUrl.indexOf('/', baseUrl.indexOf('/') + 1) + 1)) + matcher.group(1);
                    }
                }
            } else {
                realUrl = baseUrl + "download/" + version.getValue() + "/OpenRA-" + version.getValue() + ".zip";
            }

            if (realUrl == null) {
                throw new RuntimeException("Failed to find real url");
            } else {
                try (InputStream stream = client.execute(new HttpGet(realUrl)).getEntity().getContent()) {
                    Files.copy(stream, cacheFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }
            }
        } catch (IOException ex) {
            throw new RuntimeException("Could not download data", ex);
        }
    }
}
