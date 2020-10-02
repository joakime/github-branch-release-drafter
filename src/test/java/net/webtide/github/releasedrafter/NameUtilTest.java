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

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class NameUtilTest
{
    @ParameterizedTest
    @ValueSource(strings = {
        "40664784f32d4ed04b2837c57fd21921f25535e3",
        "546566da5cc949a12df609a5188fffd3d0e1af9b"
    })
    public void testValidSha1(String sample)
    {
        assertTrue(NameUtil.isSha1(sample), "isSha1(" + sample + ")==GOOD");
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "",
        "-",
        "abcdefg",
        "refs/heads/master",
        "refs/heads/feature/zed-1",
        "refs/pull/12/head"
    })
    public void testBadSha1(String sample)
    {
        assertFalse(NameUtil.isSha1(sample), "isSha1(" + sample + ")==BAD");
    }
}
