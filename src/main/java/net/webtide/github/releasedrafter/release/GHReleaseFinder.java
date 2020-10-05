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

package net.webtide.github.releasedrafter.release;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import net.webtide.github.releasedrafter.NameUtil;
import org.apache.commons.lang3.StringUtils;
import org.kohsuke.github.GHRelease;
import org.kohsuke.github.GHReleaseBuilder;
import org.kohsuke.github.GHRepository;

public class GHReleaseFinder
{
    protected GHRepository ghRepository;

    public GHReleaseFinder(GHRepository ghRepository)
    {
        this.ghRepository = Objects.requireNonNull(ghRepository);
    }

    public GHRelease findByName(String name) throws IOException
    {
        Optional<GHRelease> ghRelease = streamFrom(ghRepository)
            .filter(release -> StringUtils.equalsIgnoreCase(name, release.getName()))
            .findFirst();

        return ghRelease.get();
    }

    public GHRelease findByTagName(String tagName) throws IOException
    {
        Optional<GHRelease> ghRelease = streamFrom(ghRepository)
            .filter(release -> StringUtils.equalsIgnoreCase(tagName, release.getTagName()))
            .findFirst();

        return ghRelease.get();
    }

    public static Stream<GHRelease> streamFrom(GHRepository ghRepository) throws IOException
    {
        return StreamSupport.stream(ghRepository.listReleases().spliterator(), false);
    }

    public static GHRelease findLastReleaseOn(GHRepository repo, String targetCommitish) throws IOException
    {
        Optional<GHRelease> optDraft = GHReleaseFinder.streamFrom(repo)
            .filter((r) -> targetCommitish.equals(r.getTargetCommitish()))
            .filter((r) -> !r.isDraft())
            .findFirst();

        return optDraft.orElse(null);
    }

    public static GHRelease getActiveDraft(GHRepository repo, String targetCommitish) throws IOException
    {
        Optional<GHRelease> optDraft = GHReleaseFinder.streamFrom(repo)
            .filter((r) -> targetCommitish.equals(r.getTargetCommitish()))
            .filter(GHRelease::isDraft)
            .findFirst();

        if (optDraft.isPresent())
        {
            return optDraft.get();
        }

        // Draft doesn't exist, create it.
        String branchName = NameUtil.toBranchName(targetCommitish);
        return new GHReleaseBuilder(repo, "unset-" + branchName)
            .draft(true)
            .commitish(targetCommitish)
            .name(branchName)
            .body("# New Draft for " + branchName)
            .create();
    }
}
