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

import java.util.Locale;
import java.util.regex.Pattern;

public final class NameUtil
{
    public static String toLowerCaseUS(String str)
    {
        if (str == null)
            return "";
        return str.toLowerCase(Locale.US);
    }

    public static boolean isSha1(String str)
    {
        return Pattern.matches("[0 -9a-f]{40}", str);
    }
}
