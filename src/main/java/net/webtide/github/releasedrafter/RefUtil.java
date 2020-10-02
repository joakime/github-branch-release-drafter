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

import org.kohsuke.github.GHCommit;
import org.kohsuke.github.GHFileNotFoundException;
import org.kohsuke.github.GHRef;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHTagObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RefUtil
{
    private static final Logger LOG = LoggerFactory.getLogger(RefUtil.class);

    /**
     * Obtain an arbitrary Repository object from provided reference.
     * <p>
     * Lookup is the following.
     * </p>
     * <ol>
     *     <li>If reference starts with <code>"refs/"</code> treat as a raw String reference</li>
     *     <li>If reference is 40 characters and is all hexadecimal, treat it as a sha1 reference</li>
     *     <li>All others are used as <code>"refs/heads/{name}</code></li>
     * </ol>
     *
     * @param repo the repository to lookup reference against.
     * @param ref the commit the reference points to
     */
    public static GHCommit findReference(GHRepository repo, String ref) throws IOException
    {
        if (ref.startsWith("refs/"))
        {
            GHRef ghref = repo.getRef(ref);
            return getCommitFor(repo, ghref.getObject());
        }
        else if (NameUtil.isSha1(ref))
        {
            return repo.getCommit(ref);
        }
        else
        {
            // Try a branch reference: "refs/heads/{name}"
            try
            {
                GHRef ghref = repo.getRef("refs/heads/" + ref);
                return getCommitFor(repo, ghref.getObject());
            }
            catch (GHFileNotFoundException e)
            {
                LOG.trace("Not found: refs/heads/{}", ref, e);
            }

            // Try a tags reference: "refs/tags/{name}"
            try
            {
                GHRef ghref = repo.getRef("refs/tags/" + ref);
                return getCommitFor(repo, ghref.getObject());
            }
            catch (GHFileNotFoundException e)
            {
                LOG.trace("Not found: refs/tags/{}", ref, e);
            }
        }
        throw new GHFileNotFoundException("Not a recognized reference: " + ref);
    }

    private static GHCommit getCommitFor(GHRepository repo, GHRef.GHObject obj) throws IOException
    {
        if ("commit".equals(obj.getType()))
        {
            return repo.getCommit(obj.getSha());
        }
        else if ("tag".equals(obj.getType()))
        {
            GHTagObject tagObj = repo.getTagObject(obj.getSha());
            return getCommitFor(repo, tagObj.getObject());
        }
        LOG.info("GHRef.obj.type: {}", obj.getType());
        LOG.info("GHRef.obj.sha: {}", obj.getSha());
        throw new RuntimeException("findReference() doesn't support object type: " + obj.getType());
    }
}
