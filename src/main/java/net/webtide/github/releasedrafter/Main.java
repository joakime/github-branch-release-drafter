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
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.Map;

import net.webtide.github.releasedrafter.logging.Logging;
import net.webtide.github.releasedrafter.release.GHReleaseFinder;
import org.apache.commons.lang3.StringUtils;
import org.kohsuke.github.GHBranch;
import org.kohsuke.github.GHCommit;
import org.kohsuke.github.GHEventPayload;
import org.kohsuke.github.GHRef;
import org.kohsuke.github.GHRelease;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHTag;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.PagedIterable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main
{
    static
    {
        Logging.config();
    }

    private static final Logger LOG = LoggerFactory.getLogger(Main.class);

    public static void main(String[] commandLine)
    {
        try
        {
            GitHub github = GitHubUtil.smartConnect();

            GitHubUtil.showCurrentRateLimit(github);

            String eventType = System.getenv("GITHUB_EVENT_NAME");
            if (eventType.equalsIgnoreCase("push"))
            {
                Main.handlePushEvent(github);
            }
            else
            {
                Main.handleInfoDump(github, commandLine);

                System.err.println("Skipping Execution: Unrecognized Github Event Type: [" + eventType + "]");
                System.exit(0);
            }
        }
        catch (IOException e)
        {
            LOG.warn("Oops", e);
        }
    }

    private static void handleInfoDump(GitHub github, String[] commandLine) throws IOException
    {
        try
        {
            Args args = new Args(commandLine);
            boolean showBranches = args.containsKey("show-branches");
            boolean showReleases = args.containsKey("show-releases");
            boolean showTags = args.containsKey("show-tags");
            boolean showRefs = args.containsKey("show-refs");
            String repoName = args.getRequired("repo");

            LOG.info("Fetching repo to [{}]", repoName);
            GHRepository repo = github.getRepository(repoName);

            if (showRefs)
            {
                LOG.info("--- Refs");
                for (GHRef ref : repo.getRefs())
                {
                    LOG.info("ref: {}", ref.getRef());
                }
            }

            if (showBranches)
            {
                LOG.info("--- Branches");
                String defaultBranch = repo.getDefaultBranch();
                Map<String, GHBranch> branches = repo.getBranches();
                branches.forEach((name, branch) ->
                    {
                        String label = "";
                        if (name.equalsIgnoreCase(defaultBranch))
                            label = "DEFAULT";
                        String sha1 = branch.getSHA1();
                        String commitDate = "n/a";
                        try
                        {
                            GHCommit commit = repo.getCommit(sha1);
                            commitDate = commit.getCommitDate().toString();
                        }
                        catch (IOException e)
                        {
                            e.printStackTrace();
                        }
                        LOG.info("branch: {} ({}): {} ({})", name, label, sha1, commitDate);
                    }
                );
            }

            if (showReleases)
            {
                LOG.info("--- Releases");
                int maxReleasesToShow = 100;
                PagedIterable<GHRelease> releases = repo.listReleases();
                for (GHRelease release : releases.withPageSize(100))
                {
                    if (maxReleasesToShow <= 0)
                        break;
                    String label = "";
                    if (release.isDraft())
                        label += " DRAFT";
                    if (release.isPrerelease())
                        label += " PRERELEASE";
                    LOG.info("Release {} tag=[{}] targetCommitish=[{}] {}",
                        release.getName(),
                        release.getTagName(),
                        release.getTargetCommitish(),
                        label);
                    maxReleasesToShow--;
                }
            }

            if (showTags)
            {
                LOG.info("--- Tags");
                PagedIterable<GHTag> tags = repo.listTags();
                int maxTagsToShow = 100;
                for (GHTag tag : tags)
                {
                    if (maxTagsToShow <= 0)
                        break;
                    GHCommit commit = tag.getCommit();
                    LOG.info("Tag {}: {}", tag.getName(), commit.getSHA1());
                    maxTagsToShow--;
                }
            }
        }
        catch (Args.ArgException e)
        {
            System.err.printf("COMMAND LINE ERROR: %s%n", e.getMessage());
            System.err.println("Usage: release-drafter.jar [options]");
            System.exit(-1);
        }
    }

    private static void handlePushEvent(GitHub github) throws IOException
    {
        String eventPathEnv = System.getenv("GITHUB_EVENT_PATH");
        if (StringUtils.isBlank(eventPathEnv))
        {
            throw new RuntimeException("No GITHUB_EVENT_PATH env value found");
        }

        Path eventPath = Paths.get(eventPathEnv);
        try (Reader reader = Files.newBufferedReader(eventPath))
        {
            GHEventPayload.Push push = github.parseEventPayload(reader, GHEventPayload.Push.class);

            // What target commitish is this push on?
            String repoName = push.getRepository().getFullName();
            String targetCommitish = push.getRef();

            GHRepository repo = github.getRepository(repoName);

            // What was the last release (non-draft) on this commitish?
            Date dateOfLastRelease = getDateOfLastRelease(repo, targetCommitish);

            // Find the latest active Draft for this commitish
            GHRelease draft = GHReleaseFinder.getActiveDraft(repo, targetCommitish);

            // Query the pull requests to find the change set.
            GHCommit headCommit = repo.getCommit(push.getHead());
            Date dateTo = headCommit.getCommitDate();
            int maxPullRequestsToSearchThrough = 400;

            List<ChangeEntry> changeSet = QueryPullRequests.findClosedAndMergedPullRequests(repo, targetCommitish, dateOfLastRelease, dateTo, maxPullRequestsToSearchThrough);

            // FIXME: base this off the yaml template
            try (StringWriter bodyWriter = new StringWriter();
                 PrintWriter out = new PrintWriter(bodyWriter))
            {
                out.printf("## Release %s%n", targetCommitish);
                out.printf(" * repo: %s%n", repoName);
                out.printf(" * dateOfLastRelease: %s%n", dateOfLastRelease.toString());
                out.printf(" * head-commit: %s%n", push.getHead());
                out.println();

                out.printf("## %,d Changes Found$%n", changeSet.size());
                for (ChangeEntry change : changeSet)
                {
                    out.printf(" * %s @%s (#%d)%n", change.getTitle(), change.getAuthor(), change.getPullRequestId());
                }

                out.flush();
                draft.update().body(bodyWriter.toString()).update();
                System.out.println("Updated draft: " + draft);
            }
        }
    }

    private static Date getDateOfLastRelease(GHRepository repo, String targetCommitish) throws IOException
    {
        // What was the last release (non-draft) on this commitish?
        GHRelease lastRelease = GHReleaseFinder.findLastReleaseOn(repo, targetCommitish);

        if (lastRelease != null)
        {
            return lastRelease.getPublished_at();
        }

        // TODO: if there is no last release, we should find a reasonable Date to
        //       limit the search of pull requests from.
        //       This can either be the start of the branch (first commit in branch)
        //       or based on some kind of information present in the draft below
        //       which would let the devs manage the "last release" within the draft
        //       itself with some kind of parseable text.

        GHBranch branch = repo.getBranch(NameUtil.toBranchName(targetCommitish));
        // TODO: we have a branch, we should be able to query git to find the first branch point

        // Fallback to Repository Creation Date
        return repo.getCreatedAt();
    }
}
