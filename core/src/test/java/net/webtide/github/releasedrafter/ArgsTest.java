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

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ArgsTest
{
    @Test
    public void testNoArgs()
    {
        Args args = new Args();
        assertThat("size", args.size(), is(0));
    }

    @Test
    public void testOneArg()
    {
        Args args = new Args("--show-branches");
        assertThat("size", args.size(), is(1));
        assertTrue(args.containsKey("show-branches"), "show-branches exists");
    }

    @Test
    public void testBadArg()
    {
        assertThrows(IllegalArgumentException.class, () -> new Args("joakime/bogus-repo"));
    }
}
