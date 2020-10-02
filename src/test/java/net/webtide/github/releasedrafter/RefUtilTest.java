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

import org.junit.jupiter.api.Test;
import org.kohsuke.github.GHCommit;
import org.kohsuke.github.GHRepository;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;

public class RefUtilTest extends AbstractGitHubTest
{
    @Test
    public void testFindReferenceViaRefsEntry() throws IOException
    {
        GHRepository repo = github.getRepository(REPO_SELF);
        GHCommit ref = RefUtil.findReference(repo, "refs/heads/main");
        assertThat("Ref to 'main'", ref, notNullValue());
    }

    @Test
    public void testFindReferenceViaMainBranch() throws IOException
    {
        GHRepository repo = github.getRepository(REPO_SELF);
        GHCommit ref = RefUtil.findReference(repo, "main");
        assertThat("Ref to 'main'", ref, notNullValue());
    }

    @Test
    public void testFindReferenceViaTag() throws IOException
    {
        GHRepository repo = github.getRepository(REPO_SELF);
        GHCommit ref = RefUtil.findReference(repo, "0.0.1");
        assertThat("Ref to tag '0.0.1'", ref, notNullValue());
    }

    @Test
    public void testFindReferenceViaSha1() throws IOException
    {
        GHRepository repo = github.getRepository(REPO_SELF);
        GHCommit ref = RefUtil.findReference(repo, "ee01be02632a239af50d6e60b2a6eeca3021c8b8");
        assertThat("Ref to tag '0.0.1'", ref, notNullValue());
    }
}
