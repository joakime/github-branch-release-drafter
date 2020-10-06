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

public class ReleasesTest
{
    @Test
    public void testFromJson() throws IOException
    {
        Path exampleJson = MavenTestingUtils.getTestResourcePathFile("github-api-examples/list-releases-eclipse-che-20-1.json");

        try (Reader reader = Files.newBufferedReader(exampleJson))
        {
            Gson gson = GitHubApi.newGson();
            Releases releases = gson.fromJson(reader, Releases.class);
            assertThat("releases.size", releases.size(), is(18));
        }
    }
}
