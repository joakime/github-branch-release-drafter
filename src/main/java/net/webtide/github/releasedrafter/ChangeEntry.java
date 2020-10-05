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

import java.net.URL;
import java.util.Date;
import java.util.List;

/**
 * Represents a raw (unsorted) entry suitable for addition to the
 * release notes for this release.
 * <p>
 * This represents a pull request that is, not a draft, is closed, is merged,
 * and against the base branch that we are interested in.
 * </p>
 */
public class ChangeEntry
{
    private final URL pullRequestURI;
    private final int pullRequestId;
    private final String branch;
    private final String title;
    private final Date date;
    private final List<String> labels;
    private final String committer;
    private final String author;
    private boolean available = true;

    public ChangeEntry(URL pullRequestURI, int pullRequestId, String branch, String title, Date date, List<String> labels, String committer, String author)
    {
        this.pullRequestURI = pullRequestURI;
        this.pullRequestId = pullRequestId;
        this.branch = branch;
        this.title = title;
        this.date = date;
        this.labels = labels;
        this.committer = committer;
        this.author = author;
    }

    public URL getPullRequestURI()
    {
        return pullRequestURI;
    }

    public int getPullRequestId()
    {
        return pullRequestId;
    }

    public String getBranch()
    {
        return branch;
    }

    public String getTitle()
    {
        return title;
    }

    public Date getDate()
    {
        return date;
    }

    public List<String> getLabels()
    {
        return labels;
    }

    public String getCommitter()
    {
        return committer;
    }

    public String getAuthor()
    {
        return author;
    }

    public boolean isAvailable()
    {
        return available;
    }

    public void setAvailable(boolean flag)
    {
        this.available = available;
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder("ChangeEntry{");
        sb.append("pullRequestURI=").append(pullRequestURI);
        sb.append(", pullRequestId=").append(pullRequestId);
        sb.append(", branch='").append(branch).append('\'');
        sb.append(", date=").append(date);
        sb.append(", labels=").append(String.join(", ", labels));
        sb.append(", committer='").append(committer).append('\'');
        sb.append(", author='").append(author).append('\'');
        sb.append(", title='").append(title).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
