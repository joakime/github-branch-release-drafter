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

package net.webtide.github.releasedrafter;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;

import org.eclipse.jetty.toolchain.test.MavenTestingUtils;
import org.junit.jupiter.api.Test;
import org.kohsuke.github.GHEventPayload;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class EventHandlerTest extends AbstractGitHubTest
{
    @Test
    public void testParsePushEventFromDisk() throws IOException
    {
        Path eventPath = MavenTestingUtils.getTestResourcePathFile("event-examples/event-push.json");
        try (Reader reader = Files.newBufferedReader(eventPath))
        {
            GHEventPayload.Push push = github.parseEventPayload(reader, GHEventPayload.Push.class);
            assertNotNull(push, "GH Event Push parse should not have been null");
            assertEquals("joakime/bogus-repo", push.getRepository().getFullName(), "Repo full name");
            assertEquals("refs/heads/master", push.getRef(), "Push ref");
            assertEquals("49e261a9e014543668539485b65f7bdccbf70bc5", push.getHead(), "Head commitish");
        }
    }
}
