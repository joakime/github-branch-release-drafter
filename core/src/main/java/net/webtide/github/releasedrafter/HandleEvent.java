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

import net.webtide.github.releasedrafter.logging.Logging;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HandleEvent
{
    static
    {
        Logging.config();
    }

    private static final Logger LOG = LoggerFactory.getLogger(HandleEvent.class);

    public static void main(String[] commandLine)
    {
        Args args = new Args(commandLine);

        String repoName = args.get("repo");

        if (repoName == null)
        {
            System.err.println("No repo provided");
            System.err.println("Usage: release-drafter.jar [options]");
            System.exit(-1);
        }

        try
        {
            LOG.info("Connecting to GitHub");
            GitHub github = GitHub.connect();

            GitHubUtil.showCurrentRateLimit(github);

            LOG.info("Fetching repo to [{}]", repoName);
            GHRepository repo = github.getRepository(repoName);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
