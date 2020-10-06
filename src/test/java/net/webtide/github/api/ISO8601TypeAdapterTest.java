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

package net.webtide.github.api;

import java.time.Month;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class ISO8601TypeAdapterTest
{
    @Test
    public void testToISO8601()
    {
        ZonedDateTime zonedDateTime = ZonedDateTime.of(2020, 5, 5,
            10, 20, 30, 0,
            ZoneId.of("UTC"));
        String actual = ISO8601TypeAdapter.toISO8601(zonedDateTime);
        assertThat(actual, is("2020-05-05T10:20:30Z"));
    }

    @Test
    public void testParseISO8601()
    {
        ZonedDateTime zonedDateTime = ISO8601TypeAdapter.parseISO8601("2008-01-14T04:33:35Z");
        assertThat("zonedDateTime.year", zonedDateTime.getYear(), is(2008));
        assertThat("zonedDateTime.month", zonedDateTime.getMonth(), is(Month.JANUARY));
        assertThat("zonedDateTime.day", zonedDateTime.getDayOfMonth(), is(14));
        assertThat("zonedDateTime.hour", zonedDateTime.getHour(), is(4));
        assertThat("zonedDateTime.minute", zonedDateTime.getMinute(), is(33));
        assertThat("zonedDateTime.second", zonedDateTime.getSecond(), is(35));
        assertThat("zonedDateTime.tzOffset", zonedDateTime.getOffset().getId(), is("Z"));
    }
}
