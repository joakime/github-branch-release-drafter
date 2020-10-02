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

import org.kohsuke.github.GHObject;
import org.kohsuke.github.GHRepository;

public class RefUtil
{
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
     * @param ref the reference to look up.
     */
    public static GHObject findReference(GHRepository repo, String ref)
    {
        return null;
    }
}
