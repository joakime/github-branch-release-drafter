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

import com.google.gson.Gson;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class RateLimitsTest
{
    @Test
    public void testFromJson()
    {
        String rawjson = "{\"resources\":{\"core\":{\"limit\":5000,\"used\":0,\"remaining\":5000,\"reset\":1601996886}," +
            "\"search\":{\"limit\":30,\"used\":0,\"remaining\":30,\"reset\":1601993346},\"graphql\":{\"limit\":5000,\"used\":0," +
            "\"remaining\":5000,\"reset\":1601996886},\"integration_manifest\":{\"limit\":5000,\"used\":0,\"remaining\":5000," +
            "\"reset\":1601996886},\"source_import\":{\"limit\":100,\"used\":0,\"remaining\":100,\"reset\":1601993346}," +
            "\"code_scanning_upload\":{\"limit\":500,\"used\":0,\"remaining\":500,\"reset\":1601996886}}," +
            "\"rate\":{\"limit\":5000,\"used\":0,\"remaining\":5000,\"reset\":1601996886}}";

        Gson gson = GitHubApi.newGson();
        RateLimits rateLimits = gson.fromJson(rawjson, RateLimits.class);
        System.out.println(rateLimits);
        assertThat(rateLimits.getRate().getLimit(), is(5000));
        assertThat(rateLimits.getResourceLimit("graphql").getRemaining(), is(5000));
    }
}
