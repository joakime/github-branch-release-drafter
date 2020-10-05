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

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
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

    public static Stream<Arguments> refSamples()
    {
        return Stream.of(
            Arguments.of("refs/heads/main", "main"),
            Arguments.of("master", "master"),
            Arguments.of("refs/heads/jetty-9.4.x", "jetty-9.4.x")
        );
    }

    @ParameterizedTest
    @MethodSource("refSamples")
    public void testToBranchName(String sample, String expected)
    {
        assertThat("Ref name [" + sample + "]", NameUtil.toBranchName(sample), is(expected));
    }
}
