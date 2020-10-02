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
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.kohsuke.github.GHCommit;
import org.kohsuke.github.GHLabel;
import org.kohsuke.github.GHPullRequest;
import org.kohsuke.github.GHRepository;

public class ChangeEntryBuilder
{
    public static ChangeEntry from(GHRepository repo, GHPullRequest pullRequest) throws IOException
    {
        URL pullRequestURI = pullRequest.getUrl();
        int pullRequestId = pullRequest.getNumber();
        String branch = pullRequest.getBase().getLabel();
        String title = pullRequest.getTitle();
        Date date = pullRequest.getClosedAt();
        List<String> labels = pullRequest.getLabels().stream()
            .map(GHLabel::getName)
            .map(NameUtil::toLowerCaseUS)
            .sorted()
            .collect(Collectors.toList());

        String committer = null;
        String author = null;

        String commitSha = pullRequest.getMergeCommitSha();
        if (StringUtils.isNotBlank(commitSha))
        {
            GHCommit mergeCommit = repo.getCommit(pullRequest.getMergeCommitSha());
            if (mergeCommit != null)
            {
                if (mergeCommit.getCommitDate() != null)
                    date = mergeCommit.getCommitDate();
                if (mergeCommit.getCommitter() != null)
                    committer = mergeCommit.getCommitter().getLogin();
                if (mergeCommit.getAuthor() != null)
                    author = mergeCommit.getAuthor().getLogin();
            }
        }

        return new ChangeEntry(pullRequestURI, pullRequestId, branch, title, date, labels, committer, author);
    }
}
