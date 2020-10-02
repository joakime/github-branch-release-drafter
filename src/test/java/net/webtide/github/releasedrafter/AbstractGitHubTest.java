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

import org.junit.jupiter.api.BeforeAll;
import org.kohsuke.github.GitHub;

public class AbstractGitHubTest
{
    public static final String REPO_SELF = "joakime/github-branch-release-drafter";
    protected static GitHub github;

    @BeforeAll
    public static void connectToGithub() throws IOException
    {
        github = GitHubUtil.smartConnect();
    }
}
