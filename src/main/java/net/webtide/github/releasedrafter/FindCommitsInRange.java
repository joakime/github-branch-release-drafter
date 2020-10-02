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
import java.util.Date;

import net.webtide.github.releasedrafter.logging.Logging;
import org.apache.commons.lang3.StringUtils;
import org.kohsuke.github.GHBranch;
import org.kohsuke.github.GHCommit;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.PagedIterable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FindCommitsInRange
{
    static
    {
        Logging.config();
    }

    private static final Logger LOG = LoggerFactory.getLogger(QueryPullRequests.class);

    public static void main(String[] commandLine)
    {
        Args args = new Args(commandLine);

        String repoName = args.get("repo");
        String branch = args.get("branch");
        // The range ("from" -> "to")
        // - it can be a reference "master" "tag-name" which will be resolved
        //   against "refs/head/{name}" to find the
        // - it can be a sha1
        String from = args.get("from");
        String to = args.get("to");

        if (repoName == null)
        {
            System.err.println("No repo provided");
            System.err.println("Usage: release-drafter.jar [options] --repo=[repo-ref]");
            System.exit(-1);
        }

        try
        {
            GitHub github = GitHubUtil.smartConnect();
            GitHubUtil.showCurrentRateLimit(github);

            LOG.info("Fetching repo to [{}]", repoName);
            GHRepository repo = github.getRepository(repoName);

            if (StringUtils.isBlank(branch))
            {
                branch = repo.getDefaultBranch();
            }

            GHBranch onBranch = repo.getBranch(branch);
            LOG.info("On branch: {}", onBranch);

            FindCommitsInRange query = new FindCommitsInRange();
            String branchRef = "refs/head/" + onBranch.getName();

            Date fromDate = resolveDate(repo, "from", from);
            Date toDate = resolveDate(repo, "to", to);

            query.findCommitsInRange(repo, branchRef, fromDate, toDate);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private static Date resolveDate(GHRepository repo, String desc, String ref)
    {
        return null;
    }

    private void findCommitsInRange(GHRepository repo, String branchRef, Date fromDate, Date toDate) throws IOException
    {
        PagedIterable<GHCommit> commitIterable = repo.queryCommits()
            .from(branchRef)
            .since(fromDate)
            .until(toDate)
            .pageSize(20)
            .list();

        int maxCommits = 500;

        int found = 0;
        for (GHCommit commit : commitIterable.withPageSize(50))
        {
            if (maxCommits <= 0)
                break;

            LOG.info("commit[{}] ({}) {}",
                commit.getSHA1(),
                commit.getCommitDate(),
                commit.getLastStatus());

            maxCommits--;
            found++;
        }
        LOG.info("Found {} commits", found);
    }
}
