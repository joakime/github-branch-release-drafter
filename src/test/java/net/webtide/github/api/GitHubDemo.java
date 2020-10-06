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
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.platform.commons.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GitHubDemo
{
    private static final Logger LOG = LoggerFactory.getLogger(GitHubDemo.class);

    public static void main(String[] args) throws IOException, InterruptedException
    {
        GitHubApi github = GitHubApi.connect();
        LOG.info("Rate Limits: {}", github.getRateLimits());

        AtomicInteger count = new AtomicInteger();
        github.streamReleases("joakime", "github-branch-release-drafter", 10)
            .forEach((release) ->
            {
                count.getAndIncrement();
                StringBuilder result = new StringBuilder();
                result.append("Release: ");
                if (StringUtils.isNotBlank(release.getName()))
                    result.append("name=").append(release.getName());
                else
                    result.append("tagName=").append(release.getTagName());
                result.append(" targetCommitish=").append(release.getTargetCommitish());
                if (release.isDraft())
                    result.append(" (DRAFT)");
                if (release.isPrerelease())
                    result.append(" (PRE-RELEASE)");
                result.append(" - publishedAt=").append(release.getPublishedAt());
                System.out.println(result);
            });
        System.out.printf("Found %,d releases%n", count.get());
    }
}
