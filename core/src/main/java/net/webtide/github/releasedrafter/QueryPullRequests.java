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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import net.webtide.github.releasedrafter.logging.Logging;
import org.kohsuke.github.GHDirection;
import org.kohsuke.github.GHIssueState;
import org.kohsuke.github.GHObject;
import org.kohsuke.github.GHPullRequest;
import org.kohsuke.github.GHPullRequestQueryBuilder;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.PagedIterable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QueryPullRequests
{
    static
    {
        Logging.config();
    }

    private static final Logger LOG = LoggerFactory.getLogger(QueryPullRequests.class);

    public static void main(String[] commandLine)
    {
        try
        {
            Args args = new Args(commandLine);

            String repoName = args.getRequired("repo");
            String branch = args.getRequired("branch");
            String from = args.getRequired("from");
            String to = args.getRequired("to");

            LOG.info("Connecting to GitHub");
            GitHub github = GitHub.connect();
            GitHubUtil.showCurrentRateLimit(github);

            LOG.info("Fetching repo to [{}]", repoName);
            GHRepository repo = github.getRepository(repoName);

            LOG.info("On branch: {}", branch);

            GHObject refFrom = RefUtil.findReference(repo, from);
            if (refFrom == null)
                throw new IllegalArgumentException("Unable to find 'from' Repository reference [" + from + "]");
            GHObject refTo = RefUtil.findReference(repo, from);
            if (refTo == null)
                throw new IllegalArgumentException("Unable to find 'to' Repository reference [" + to + "]");

            Date dateFrom = refFrom.getUpdatedAt();
            Date dateTo = refTo.getUpdatedAt();
            int maxHits = 100;

            Objects.requireNonNull(dateFrom, "Unable to find date for 'from' reference [" + from + "]: " + refFrom);
            Objects.requireNonNull(dateTo, "Unable to find date for 'to' reference [" + to + "]: " + refTo);

            QueryPullRequests.findClosedAndMergedPullRequests(repo, branch, dateFrom, dateTo, maxHits);
        }
        catch (Args.ArgException e)
        {
            System.err.printf("COMMAND LINE ERROR: %s%n", e.getMessage());
            System.err.println("Usage: release-drafter.jar [options]");
            System.exit(-1);
        }
        catch (IOException e)
        {
            LOG.warn("Oops", e);
        }
    }

    /**
     * Find the list of PullRequests that are CLOSED, and Merged, for a particular {@code base} Branch Name, within a range of dates.
     *
     * @param repo the repository to query for pull requests
     * @param baseBranchName the base branch name (just a branch name, not a full ref)
     * @param dateFrom the date of the oldest PR to return
     * @param dateTo the date of the newest PR to return
     * @param maxHits the maximum number of pull requests to return
     * @return the list of changes found
     */
    public static List<ChangeEntry> findClosedAndMergedPullRequests(GHRepository repo, String baseBranchName, Date dateFrom, Date dateTo, int maxHits) throws IOException
    {
        PagedIterable<GHPullRequest> pullRequestPagedIterable =
            repo.queryPullRequests()
                .base(baseBranchName)
                .state(GHIssueState.CLOSED)
                .direction(GHDirection.DESC)
                .sort(GHPullRequestQueryBuilder.Sort.UPDATED)
                .list();

        List<ChangeEntry> hits = new ArrayList<>();
        int maxPullRequests = 30;
        for (GHPullRequest pullRequest : pullRequestPagedIterable.withPageSize(10))
        {
            if (maxPullRequests <= 0)
                break;

            ChangeEntry changeEntry = ChangeEntryBuilder.from(repo, pullRequest);
            hits.add(changeEntry);
            // DumpPullRequestDetails.logPullRequest(LOG, repo, pullRequest);

            maxPullRequests--;
        }

        LOG.info("Found {} pull requests", hits.size());
        return hits;
    }

    private void findPullRequestsForCommits(GHRepository repo, String baseBranchName) throws IOException
    {
        PagedIterable<GHPullRequest> pullRequestPagedIterable =
            repo.queryPullRequests()
                .base(baseBranchName)
                .state(GHIssueState.CLOSED)
                .direction(GHDirection.DESC)
                .sort(GHPullRequestQueryBuilder.Sort.UPDATED)
                .list();

        int maxPullRequests = 30;

        int matchingCount = 0;
        for (GHPullRequest pullRequest : pullRequestPagedIterable.withPageSize(10))
        {
            if (maxPullRequests <= 0)
                break;

            DumpPullRequestDetails.logPullRequest(LOG, repo, pullRequest);

            maxPullRequests--;
            matchingCount++;
        }
        LOG.info("Found {} pull requests", matchingCount);
    }
}