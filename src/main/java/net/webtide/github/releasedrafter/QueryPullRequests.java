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
import org.kohsuke.github.GHCommit;
import org.kohsuke.github.GHDirection;
import org.kohsuke.github.GHIssueState;
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

            GitHub github = GitHubUtil.smartConnect();
            GitHubUtil.showCurrentRateLimit(github);

            LOG.info("Fetching repo to [{}]", repoName);
            GHRepository repo = github.getRepository(repoName);

            LOG.info("On branch: {}", branch);

            GHCommit refFrom = RefUtil.findReference(repo, from);
            if (refFrom == null)
                throw new IllegalArgumentException("Unable to find 'from' Repository reference [" + from + "]");
            GHCommit refTo = RefUtil.findReference(repo, to);
            if (refTo == null)
                throw new IllegalArgumentException("Unable to find 'to' Repository reference [" + to + "]");

            Date dateFrom = refFrom.getCommitDate();
            Date dateTo = refTo.getCommitDate();
            int maxHits = 100;

            Objects.requireNonNull(dateFrom, "Unable to find date for 'from' reference [" + from + "]: " + refFrom);
            Objects.requireNonNull(dateTo, "Unable to find date for 'to' reference [" + to + "]: " + refTo);

            LOG.info("In Date Range: {} [{}] -> {} [{}]", dateFrom, from, dateTo, to);

            List<ChangeEntry> changes = QueryPullRequests.findClosedAndMergedPullRequests(repo, branch, dateFrom, dateTo, maxHits);
            for (ChangeEntry entry : changes)
            {
                LOG.info(entry.toString());
            }
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
     * @param maxPullRequestsToSearchThrough the maximum number of pull requests to search through to find end range (will terminate search early if reached)
     * @return the list of changes found
     */
    public static List<ChangeEntry> findClosedAndMergedPullRequests(GHRepository repo, String baseBranchName, Date dateFrom, Date dateTo, int maxPullRequestsToSearchThrough) throws IOException
    {
        PagedIterable<GHPullRequest> pullRequestPagedIterable =
            repo.queryPullRequests()
                .base(baseBranchName)
                .state(GHIssueState.CLOSED)
                .direction(GHDirection.DESC)
                .sort(GHPullRequestQueryBuilder.Sort.UPDATED)
                .list();

        List<ChangeEntry> hits = new ArrayList<>();

        LOG.debug("Finding PullRequests (CLOSED, MERGED, !DRAFT, branch={}, dateFrom={}, dateTo={})", baseBranchName, dateFrom, dateTo);

        long tsFrom = dateFrom.getTime();
        long tsTo = dateTo.getTime();

        boolean foundFirstEntryByDate = false;
        int searchPullRequests = 0;

        for (GHPullRequest pullRequest : pullRequestPagedIterable.withPageSize(10))
        {
            if (searchPullRequests > maxPullRequestsToSearchThrough)
            {
                LOG.debug("Searched through too many pull requests: {}", maxPullRequestsToSearchThrough);
                break;
            }

            searchPullRequests++;
            Date updateDate = pullRequest.getUpdatedAt();
            long tsEntry = updateDate.getTime();
            if (!foundFirstEntryByDate)
            {
                if (tsEntry > tsTo)
                {
                    LOG.debug("Skipping too new {} PR #{} - {}", updateDate, pullRequest.getNumber(), pullRequest.getTitle());
                }
                else
                {
                    hits.add(ChangeEntryBuilder.from(repo, pullRequest));
                    foundFirstEntryByDate = true;
                }
            }
            else
            {
                if (tsEntry < tsFrom)
                {
                    LOG.debug("Skipping too old {} PR #{} - {}", updateDate, pullRequest.getNumber(), pullRequest.getTitle());
                    break; // we are done searching the list
                }

                if (pullRequest.isDraft())
                {
                    LOG.debug("Skipping draft PR #{} - {}", pullRequest.getNumber(), pullRequest.getTitle());
                    continue;
                }

                // Refresh the PullRequest so we get access to the merge information.
                // (which isn't present in the results from GHPullRequestQueryBuilder)
                // We do this as late as possible to reduce the number of API calls we make.
                // TODO: review if still needed
                // pullRequest.refresh();
                if (!pullRequest.isMerged())
                {
                    LOG.debug("Skipping unmerged PR #{} - {}", pullRequest.getNumber(), pullRequest.getTitle());
                    continue;
                }

                hits.add(ChangeEntryBuilder.from(repo, pullRequest));
            }
        }

        LOG.info("Found {} pull requests (from {} searched)", hits.size(), searchPullRequests);
        return hits;
    }
}
