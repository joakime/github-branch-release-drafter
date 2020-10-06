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
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import net.webtide.github.releasedrafter.release.Category;
import net.webtide.github.releasedrafter.release.GHReleaseFinder;
import net.webtide.github.releasedrafter.release.ReleaseDraft;
import org.apache.commons.lang3.StringUtils;
import org.kohsuke.github.GHBranch;
import org.kohsuke.github.GHCommit;
import org.kohsuke.github.GHContent;
import org.kohsuke.github.GHFileNotFoundException;
import org.kohsuke.github.GHRelease;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GithubDraftUpdate
{
    private static final Logger LOG = LoggerFactory.getLogger(GithubDraftUpdate.class);

    public static void main(String[] commandLine)
    {
        try
        {
            Args args = new Args(commandLine);

            String repoName = args.getRequired("repo");
            String pushRef = args.getRequired("push-ref");
            String commitHeadSha = args.getRequired("commit-sha");
            String draftRepoName = args.get("draft-repo");

            GitHub github = GitHubUtil.smartConnect();
            GitHubUtil.showCurrentRateLimit(github);

            GHRepository repo = github.getRepository(repoName);
            GHRepository draftRepo = repo;
            if (StringUtils.isNotBlank(draftRepoName))
            {
                draftRepo = github.getRepository(draftRepoName);
            }

            GithubDraftUpdate draftUpdate = new GithubDraftUpdate();
            draftUpdate.update(github, repo, draftRepo, pushRef, commitHeadSha);
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

    public void update(GitHub github, GHRepository repo, GHRepository draftRepo, String pushRef, String commitHeadSha) throws IOException
    {
        // What was the last release (non-draft) on this commitish?
        GHRelease lastRelease = GHReleaseFinder.findLastReleaseOn(draftRepo, pushRef);
        Date dateOfLastRelease = getDateOfLastRelease(draftRepo, lastRelease, pushRef);

        // Find the latest active Draft for this commitish
        GHRelease draft = GHReleaseFinder.getActiveDraft(draftRepo, pushRef);

        // Query the pull requests to find the change set.
        GHCommit headCommit = repo.getCommit(commitHeadSha);
        Date dateTo = headCommit.getCommitDate();
        int maxPullRequestsToSearchThrough = 400; // TODO: this should be configurable

        List<ChangeEntry> changeSet = QueryPullRequests.findClosedAndMergedPullRequests(repo, pushRef, dateOfLastRelease, dateTo, maxPullRequestsToSearchThrough);

        // FIXME: base this off the yaml template / ReleaseDraft effort
        Map<String, String> props = new HashMap<>();
        props.put("repoName", repo.getFullName());
        props.put("push.ref", pushRef);
        props.put("push.headCommit", commitHeadSha);
        props.put("lastRelease.tagName", (lastRelease != null) ? lastRelease.getTagName() : "");
        props.put("lastRelease.publishedAt", (lastRelease != null) ? lastRelease.getPublished_at().toString() : "");
        props.put("priorRelease.date", dateOfLastRelease.toString());
        props.put("dateTo", dateTo.toString());

        updateReleaseDraft(repo, draft, changeSet, pushRef, props);
    }

    private static void updateReleaseDraft(GHRepository repo, GHRelease draft, List<ChangeEntry> changeSet, String branchRef, Map<String, String> props) throws IOException
    {
        try (StringWriter bodyWriter = new StringWriter();
             PrintWriter out = new PrintWriter(bodyWriter))
        {

            ReleaseDraft releaseDraft = loadReleaseDraft(repo, branchRef);

            if (releaseDraft != null)
            {
                for (Category category : releaseDraft.getCategories())
                {
                    List<ChangeEntry> subset = changeSet.stream()
                        .filter(ChangeEntry::isAvailable)
                        .filter((change) -> !Collections.disjoint(change.getLabels(), category.getLabels()))
                        .sorted(Comparator.comparingLong((c) -> c.getDate().getTime()))
                        .collect(Collectors.toList());

                    if (subset.isEmpty())
                        continue; // skip this category

                    System.out.println(String.format("entries for category %s/%s: %s", category.getTitle(), category.getLabels(), subset));

                    out.printf("## %s%n", category.getTitle());
                    for (ChangeEntry change : subset)
                    {
                        out.printf(" * %s @%s (#%d)%n", change.getTitle(), change.getAuthor(), change.getPullRequestId());
                        change.setAvailable(false); // disable this change from other categories
                    }
                }
            }

            // Any changeset entries with no matching categories (even ones with no labels)
            List<ChangeEntry> unwritten = changeSet.stream()
                .filter(ChangeEntry::isAvailable)
                .sorted(Comparator.comparingLong((c) -> c.getDate().getTime()))
                .collect(Collectors.toList());

            System.out.println( "entries unwritten: "+ unwritten);

            if (unwritten.size() > 0)
            {
                out.printf("## %,d Changes Found%n", changeSet.size());
                for (ChangeEntry change : changeSet)
                {
                    out.printf(" * %s @%s (#%d)%n", change.getTitle(), change.getAuthor(), change.getPullRequestId());
                }
            }

            // Add some markdown "comments" to track what was done.
            // See https://stackoverflow.com/questions/4823468/comments-in-markdown for hokey syntax
            out.println();
            out.printf("[//]: # (Release %s)%n", branchRef);
            for (Map.Entry<String, String> entry : props.entrySet())
            {
                out.printf("[//]: # (%s: %s)%n", entry.getKey(), entry.getValue());
            }

            out.flush();
            // Only update once all of the release body has been successfully generated
            draft.update().body(bodyWriter.toString()).update();
            System.out.println("Updated draft: " + draft);
        }
    }

    private static ReleaseDraft loadReleaseDraft(GHRepository repo, String ref) throws IOException
    {
        String ghConfigResource = ".github/release-config.yml";
        try
        {
            GHContent drafterContent = repo.getFileContent(ghConfigResource, ref);
            if (drafterContent.isFile())
            {
                try (InputStream input = drafterContent.read())
                {
                    ReleaseDraft releaseDraft = ReleaseDraft.load(input);
                    return releaseDraft;
                }
            }
        }
        catch (GHFileNotFoundException e)
        {
            // Not found in GitHub, use default
            LOG.debug("Not found on github: {}", ghConfigResource);
        }

        String jarResource = "release-config.yml";

        URL url = Main.class.getClassLoader().getResource(jarResource);
        if (url != null)
        {
            try (InputStream input = url.openStream())
            {
                ReleaseDraft releaseDraft = ReleaseDraft.load(input);
                return releaseDraft;
            }
        }

        // TODO: create raw ReleaseDraft in code?
        return null;
    }

    private static Date getDateOfLastRelease(GHRepository repo, GHRelease lastRelease, String targetCommitish) throws IOException
    {
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
