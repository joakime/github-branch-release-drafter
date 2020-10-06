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
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.notNullValue;

public class GitHubApiTest
{
    private static final String PROJECT_OWNER = "joakime";
    private static final String PROJECT_REPO = "github-branch-release-drafter";

    @Test
    public void testRateLimits() throws IOException, InterruptedException
    {
        GitHubApi github = GitHubApi.connect();
        RateLimits rateLimits = github.getRateLimits();

        assertThat(rateLimits.getResourceLimit("core"), notNullValue());
        assertThat(rateLimits.getResourceLimit("search"), notNullValue());
        assertThat(rateLimits.getResourceLimit("graphql"), notNullValue());
        assertThat(rateLimits.getRate(), notNullValue());
    }

    @Test
    public void testSelf() throws IOException, InterruptedException
    {
        GitHubApi github = GitHubApi.connect();
        User self = github.getSelf();

        assertThat(self.getLogin(), notNullValue());
    }

    @Test
    public void testListReleases() throws IOException, InterruptedException
    {
        GitHubApi github = GitHubApi.connect();
        Releases releases = github.listReleases(PROJECT_OWNER, PROJECT_REPO, 10, 1);

        assertThat(releases.size(), greaterThanOrEqualTo(1));
        assertThat("release.get(0)", releases.get(0).getAuthor().getLogin(), notNullValue());
    }

    @Test
    public void testGraphQL_WhoAmI() throws IOException, InterruptedException
    {
        GitHubApi github = GitHubApi.connect();

        StringBuilder whoami = new StringBuilder();
        whoami.append("query {");
        whoami.append("  viewer {");
        whoami.append("    login");
        whoami.append("  }");
        whoami.append("}");

        String rawJson = github.graphql(whoami.toString());
        System.out.println(rawJson);

        Gson gson = GitHubApi.newGson();
        Map<?, ?> map = gson.fromJson(rawJson, HashMap.class);
        assertThat("map", map, hasKey("data"));
        Map<?, ?> data = (Map<?, ?>)map.get("data");
        assertThat("map.data", data, hasKey("viewer"));
        Map<?, ?> viewer = (Map<?, ?>)data.get("viewer");
        assertThat("map.data.viewer", viewer, hasKey("login"));
        String login = (String)viewer.get("login");
        assertThat("map.data.viewer.login", login, notNullValue());
    }
}
