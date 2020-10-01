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
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.kohsuke.github.GHBranch;
import org.kohsuke.github.GHRelease;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.PagedIterable;
import org.kohsuke.github.PagedIterator;

public class ListReleases
{
    public static void main(String[] args)
    {
        boolean showBranches = false;
        List<String> argList = new ArrayList<>();
        if (args != null)
            argList.addAll(Arrays.asList(args));
        if (argList.contains("--show-branches"))
            showBranches = true;
        try
        {
            GitHub github = GitHub.connect();
            GHRepository repo = github.getRepository("joakime/experiments-with-release-drafter");
            System.out.printf("Repo: %s%n", repo.getName());
            if (showBranches)
            {
                String defaultBranch = repo.getDefaultBranch();
                Map<String, GHBranch> branches = repo.getBranches();
                branches.forEach((name, branch) ->
                    System.out.printf("%s%s: %s%n", name, (name.equalsIgnoreCase(defaultBranch) ? "(DEFAULT)" : " "), branch.getSHA1()));
            }

            PagedIterable<GHRelease> releases = repo.listReleases();

            PagedIterator<GHRelease> releaseIter = releases.iterator();
            while (releaseIter.hasNext())
            {
                GHRelease release = releaseIter.next();
                System.out.printf("Release %s: (%s) [%s]",
                    release.getName(),
                    release.getTagName(),
                    release.getTargetCommitish());
                if (release.isDraft())
                    System.out.printf(" DRAFT");
                if (release.isPrerelease())
                    System.out.printf(" PRERELEASE");
                System.out.println();
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
