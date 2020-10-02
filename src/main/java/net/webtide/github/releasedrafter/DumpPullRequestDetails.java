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
import java.util.Collection;
import java.util.stream.Collectors;

import net.webtide.github.releasedrafter.logging.Logging;
import org.kohsuke.github.GHCommit;
import org.kohsuke.github.GHCommitPointer;
import org.kohsuke.github.GHIssue;
import org.kohsuke.github.GHLabel;
import org.kohsuke.github.GHPullRequest;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHUser;
import org.kohsuke.github.GitHub;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DumpPullRequestDetails
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
            int pullRequestNumber = args.getInteger("pr");

            GitHub github = GitHubUtil.smartConnect();
            GitHubUtil.showCurrentRateLimit(github);

            LOG.info("Fetching repo to [{}]", repoName);
            GHRepository repo = github.getRepository(repoName);

            LOG.info("Fetching pull request [{}]", pullRequestNumber);
            GHPullRequest pullRequest = repo.getPullRequest(pullRequestNumber);
            if (pullRequest == null)
            {
                LOG.warn("Unable to find pull request [{}]", pullRequestNumber);
            }
            else
            {
                logPullRequest(LOG, repo, pullRequest);
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

    public static void logPullRequest(Logger LOG, GHRepository repo, GHPullRequest pullRequest)
    {
        LOG.info("pull-request[{}]", pullRequest.getUrl());
        LOG.info("   number: {}", pullRequest.getNumber());
        LOG.info("   state: {}", pullRequest.getState());
        try
        {
            LOG.info("   is-draft: {}", pullRequest.isDraft());
        }
        catch (IOException e)
        {
            LOG.debug("Unable to get Draft status", e);
        }
        try
        {
            LOG.info("   is-merged: {}", pullRequest.isMerged());
        }
        catch (IOException e)
        {
            LOG.debug("Unable to get Merge status", e);
        }
        LOG.info("   title: {}", pullRequest.getTitle());
        try
        {
            Collection<GHLabel> labels = pullRequest.getLabels();
            if (labels != null && !labels.isEmpty())
            {
                LOG.info("   labels: {}", labels.stream()
                    .map(GHLabel::getName)
                    .map(NameUtil::toLowerCaseUS)
                    .sorted()
                    .collect(Collectors.joining(", ", "[", "]")));
            }
        }
        catch (IOException e)
        {
            LOG.debug("Unable to get labels", e);
        }
        // Useless github internal Detail - LOG.info("   id: {}", pullRequest.getId());
        LOG.info("   base: {}", toString(pullRequest.getBase()));
        LOG.info("   head: {}", toString(pullRequest.getHead()));

        try
        {
            // A mere curiosity / detail.
            LOG.info("   commits: {}", pullRequest.getCommits());
            LOG.info("   changed-files: {}", pullRequest.getChangedFiles());
            LOG.info("   deletions: {}", pullRequest.getDeletions());
            LOG.info("   additions: {}", pullRequest.getAdditions());
        }
        catch (IOException e)
        {
            LOG.debug("Unable to get commit counts", e);
        }

        // Not useful - LOG.info("   issue-url: {}", pullRequest.getIssueUrl());
        // Useless github internal detail - LOG.info("   node-id: {}", pullRequest.getNodeId());

        if (pullRequest.getPullRequest() != null)
        {
            GHIssue.PullRequest pr = pullRequest.getPullRequest();
            LOG.info("   issue.pr: {}", pr);
        }

        if (pullRequest.getClosedAt() != null)
        {
            // Somewhat useful (should be fallback value if merge-commit-date is unavailable)
            LOG.info("   closed-at: {}", pullRequest.getClosedAt());
        }

        try
        {
            GHCommit mergeCommit = repo.getCommit(pullRequest.getMergeCommitSha());
            if (mergeCommit != null)
            {
                if (pullRequest.getMergedAt() != null)
                    LOG.info("   merged-at: {}", pullRequest.getMergedAt());
                LOG.info("   merge-commit-sha: {}", pullRequest.getMergeCommitSha());
                LOG.info("   merge-commit-date: {}", mergeCommit.getCommitDate());
                // Interesting - "web-flow" seen on committer (the bot that test if the PR is mergeable or not)
                LOG.info("   merge-commit-committer: {}", toString(mergeCommit.getCommitter()));
                // If committed by github, this will be the author.
                LOG.info("   merge-commit-author: {}", toString(mergeCommit.getAuthor()));
            }
        }
        catch (IOException e)
        {
            LOG.info("   merge (Fetch Failure): {}: {}", e.getClass().getName(), e.getMessage(), e);
        }
    }

    private static String toString(GHUser user)
    {
        if (user == null)
            return "<null>";
        return user.getLogin();
    }

    private static String toString(GHCommitPointer commitPointer)
    {
        if (commitPointer == null)
            return "<null>";

        return commitPointer.getLabel();
    }
}
