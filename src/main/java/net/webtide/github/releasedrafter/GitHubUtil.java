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
import java.time.Instant;

import net.webtide.github.releasedrafter.logging.Logging;
import org.apache.commons.lang3.StringUtils;
import org.kohsuke.github.GHRateLimit;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class GitHubUtil
{
    static
    {
        Logging.config();
    }

    private static final Logger LOG = LoggerFactory.getLogger(GitHubUtil.class);

    public static GitHub smartConnect() throws IOException
    {
        String githubAppToken = System.getenv("GITHUB_TOKEN");
        if (StringUtils.isNotBlank(githubAppToken))
        {
            LOG.info("Connecting to GitHub with AppInstallation Token");
            return new GitHubBuilder().withAppInstallationToken(githubAppToken).build();
        }
        else
        {
            LOG.info("Connecting to GitHub with environment / properties file configuration");
            return GitHub.connect();
        }
    }

    public static void showCurrentRateLimit(GitHub github) throws IOException
    {
        GHRateLimit rateLimit = github.getRateLimit();
        long epochSecNow = Instant.now().getEpochSecond();
        long epochReset = rateLimit.getResetEpochSeconds();
        long resetInSeconds = epochReset - epochSecNow;
        LOG.info("RateLimit [limit={}, remaining={}, resetInSeconds={}]", rateLimit.getLimit(), rateLimit.getRemaining(), resetInSeconds);
    }
}
