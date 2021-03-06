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
import java.nio.file.Paths;
import java.util.Map;

import net.webtide.github.releasedrafter.logging.Logging;
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

        String draftRepoName = System.getenv("INPUT_DRAFT_REPO");

        Path eventPath = Paths.get(eventPathEnv);
        try (Reader reader = Files.newBufferedReader(eventPath))
        {
            GHEventPayload.Push push = github.parseEventPayload(reader, GHEventPayload.Push.class);

            String repoName = push.getRepository().getFullName();

            GHRepository repo = github.getRepository(repoName);
            GHRepository draftRepo = repo;
            if (StringUtils.isNotBlank(draftRepoName))
            {
                draftRepo = github.getRepository(draftRepoName);
            }
            LOG.info("repoName {} with draftRepo fullName: {}", repoName, draftRepo.getFullName());

            GithubDraftUpdate githubDraftUpdate = new GithubDraftUpdate();
            githubDraftUpdate.update(github, repo, draftRepo, push.getRef(), push.getHead());
        }
    }
}
