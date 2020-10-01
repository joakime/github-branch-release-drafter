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
import org.apache.commons.lang3.StringUtils;
import org.kohsuke.github.GHBranch;
import org.kohsuke.github.GHCommitPointer;
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
        Args args = new Args(commandLine);

        String repoName = args.get("repo");
        String branch = args.get("branch");
        String revLastVersion = args.get("revLastVersion");
        String revHead = args.get("revHead");

        if (repoName == null)
        {
            System.err.println("No repo provided");
            System.err.println("Usage: release-drafter.jar [options] --repo=[repo-ref]");
            System.exit(-1);
        }

        try
        {
            LOG.info("Connecting to GitHub");
            GitHub github = GitHub.connect();
            GitHubUtil.showCurrentRateLimit(github);

            LOG.info("Fetching repo to [{}]", repoName);
            GHRepository repo = github.getRepository(repoName);

            if (StringUtils.isBlank(branch))
            {
                branch = repo.getDefaultBranch();
            }

            GHBranch onBranch = repo.getBranch(branch);
            LOG.info("On branch: {}", onBranch);

            QueryPullRequests query = new QueryPullRequests();
            query.findPullRequestsForCommits(repo, onBranch, revLastVersion, revHead);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private void findPullRequestsForCommits(GHRepository repo, GHBranch branch, String fromSha, String toSha) throws IOException
    {
        String branchRef = "refs/heads/" + branch.getName();

        PagedIterable<GHPullRequest> pullRequestPagedIterable =
            repo.queryPullRequests()
                .state(GHIssueState.CLOSED)
                .direction(GHDirection.DESC)
                //.head(branchRef)
                .sort(GHPullRequestQueryBuilder.Sort.UPDATED)
                .list();

        for (GHPullRequest pullRequest : pullRequestPagedIterable.withPageSize(10))
        {
            LOG.info("pull-request[{}]", pullRequest.getUrl());
            LOG.info("   title: {}", pullRequest.getTitle());
            LOG.info("   merge-commit-sha: {}", pullRequest.getMergeCommitSha());
            LOG.info("   base: {}", toString(pullRequest.getBase()));
            LOG.info("   head: {}", toString(pullRequest.getHead()));
        }

        /*
        Date dateSince = repo.getCommit(fromSha).getCommitDate();
        Date dateUntil = repo.getCommit(toSha).getCommitDate();

        PagedIterable<GHCommit> commitIterable = repo.queryCommits()
            .from(branchRef)
            .since(dateSince)
            .until(dateUntil)
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

        repo.queryPullRequests()
            .base(branchRef)
            .direction(GHDirection.DESC)
            .head()
         */
    }

    private static String toString(GHCommitPointer commitPointer)
    {
        if (commitPointer == null)
            return "<null>";
        return String.format("CommitPointer[ref=%s,label=%s,sha=%s]", commitPointer.getRef(), commitPointer.getLabel(), commitPointer.getSha());
    }
}
