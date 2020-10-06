//
// ========================================================================
// Copyright (c) Webtide LLC and others.
//
// This program and the accompanying materials are made available under
// the terms of the Eclipse Public License 2.0 which is available at
// https://www.eclipse.org/legal/epl-2.0
//
// This Source Code may also be made available under the following
// Secondary Licenses when the conditions for such availability set
// forth in the Eclipse Public License, v. 2.0 are satisfied:
// the Apache License v2.0 which is available at
// https://www.apache.org/licenses/LICENSE-2.0
//
// SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
// ========================================================================
//

package net.webtide.github.api;

import java.io.IOException;
import java.io.Reader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.nio.charset.StandardCharsets.UTF_8;

public class GitHubApi
{
    private static final Logger LOG = LoggerFactory.getLogger(GitHubApi.class);
    private final URI apiURI;
    private final HttpClient client;
    private final HttpRequest.Builder baseRequest;
    private final Gson gson;

    public static GitHubApi connect()
    {
        String githubAppToken = System.getenv("GITHUB_TOKEN");
        if (StringUtils.isNotBlank(githubAppToken))
        {
            LOG.info("Connecting to GitHub with AppInstallation Token");
            return new GitHubApi(githubAppToken);
        }

        String[] configLocations = {
            ".github",
            ".github/oauth"
        };

        Path userHome = Paths.get(System.getProperty("user.home"));

        for (String configLocation : configLocations)
        {
            Path configPath = userHome.resolve(configLocation);
            if (Files.exists(configPath) && Files.isRegularFile(configPath))
            {
                try (Reader reader = Files.newBufferedReader(configPath, UTF_8))
                {
                    Properties props = new Properties();
                    props.load(reader);
                    String oauthToken = props.getProperty("oauth");
                    if (StringUtils.isNotBlank(oauthToken))
                    {
                        LOG.info("Connecting to GitHub with {} Token", configPath);
                        return new GitHubApi(oauthToken);
                    }
                }
                catch (IOException e)
                {
                    LOG.warn("Unable to read {}", configPath, e);
                }
            }
        }

        LOG.info("Connecting to GitHub with anonymous (no token)");
        return new GitHubApi(null);
    }

    private GitHubApi(String oauthToken)
    {
        this.apiURI = URI.create("https://api.github.com");

        this.client = HttpClient.newBuilder()
            .connectTimeout(Duration.of(2, ChronoUnit.SECONDS))
            .followRedirects(HttpClient.Redirect.NEVER)
            .build();
        this.baseRequest = HttpRequest.newBuilder()
            .header("Authorization", "Bearer " + oauthToken);
        this.gson = newGson();
    }

    /**
     * Create a Gson suitable for parsing Github JSON responses
     *
     * @return the Gson configured for Github JSON responses
     */
    public static Gson newGson()
    {
        return new GsonBuilder()
            .registerTypeAdapter(ZonedDateTime.class, new ISO8601TypeAdapter())
            .create();
    }

    public RateLimits getRateLimits() throws IOException, InterruptedException
    {
        URI endpointURI = apiURI.resolve("/rate_limit");
        HttpRequest request = baseRequest.copy()
            .GET()
            .uri(endpointURI)
            .header("Accept", "application/vnd.github.v3+json")
            .build();
        HttpResponse<String> response = client.send(request, responseInfo -> HttpResponse.BodySubscribers.ofString(UTF_8));
        if (response.statusCode() != 200)
            throw new GitHubApiException("Unable to get rate limits: status code: " + response.statusCode());
        return gson.fromJson(response.body(), RateLimits.class);
    }

    public User getSelf() throws IOException, InterruptedException
    {
        URI endpointURI = apiURI.resolve("/user");
        HttpRequest request = baseRequest.copy()
            .GET()
            .uri(endpointURI)
            .header("Accept", "application/vnd.github.v3+json")
            .build();
        HttpResponse<String> response = client.send(request, responseInfo -> HttpResponse.BodySubscribers.ofString(UTF_8));
        if (response.statusCode() != 200)
            throw new GitHubApiException("Unable to get user: status code: " + response.statusCode());
        return gson.fromJson(response.body(), User.class);
    }

    public String graphql(String query) throws IOException, InterruptedException
    {
        Map<String, String> map = new HashMap<>();
        map.put("query", query);

        String jsonQuery = gson.toJson(map);

        URI endpointURI = apiURI.resolve("/graphql");
        HttpRequest request = baseRequest.copy()
            .POST(HttpRequest.BodyPublishers.ofString(jsonQuery))
            .header("Content-Type", "application/json")
            .uri(endpointURI)
            .header("Accept", "application/vnd.github.v3+json")
            .build();
        HttpResponse<String> response = client.send(request, responseInfo -> HttpResponse.BodySubscribers.ofString(UTF_8));
        if (response.statusCode() != 200)
            throw new GitHubApiException("Unable to post graphql: status code: " + response.statusCode());
        return response.body();
    }
}
