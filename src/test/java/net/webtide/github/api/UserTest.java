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
import java.nio.file.Files;
import java.nio.file.Path;

import com.google.gson.Gson;
import org.eclipse.jetty.toolchain.test.MavenTestingUtils;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class UserTest
{
    @Test
    public void testFromJson() throws IOException
    {
        Path exampleJson = MavenTestingUtils.getTestResourcePathFile("github-api-examples/user.json");

        try (Reader reader = Files.newBufferedReader(exampleJson))
        {
            Gson gson = GitHubApi.newGson();
            User user = gson.fromJson(reader, User.class);
            assertThat("login", user.getLogin(), is("octocat"));
            assertThat("name", user.getName(), is("monalisa octocat"));
            assertThat("email", user.getEmail(), is("octocat@github.com"));

            assertThat("createdAt", ISO8601TypeAdapter.toISO8601(user.getCreatedAt()), is("2008-01-14T04:33:35Z"));
            assertThat("updatedAt", ISO8601TypeAdapter.toISO8601(user.getUpdatedAt()), is("2008-01-14T04:33:35Z"));
        }
    }
}
