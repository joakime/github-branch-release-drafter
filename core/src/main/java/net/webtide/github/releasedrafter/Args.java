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

import java.util.HashMap;

import org.apache.commons.lang3.StringUtils;

public class Args extends HashMap<String, String>
{
    public static class ArgException extends RuntimeException
    {
        public ArgException(String format, Object... args)
        {
            super(String.format(format, args));
        }
    }

    public Args(String... args)
    {
        super();
        for (String arg : args)
        {
            if (!arg.startsWith("--"))
            {
                throw new ArgException("Unrecognized option: %s", arg);
            }

            int idxEqual = arg.indexOf('=');
            if (idxEqual > 0)
            {
                put(arg.substring(2, idxEqual), arg.substring(idxEqual + 1));
            }
            else
            {
                put(arg.substring(2), null);
            }
        }
    }

    public String getRequired(String key)
    {
        String value = get(key);
        if (StringUtils.isBlank(value))
        {
            throw new ArgException("Missing required value for option --%s=<value>", key);
        }
        return value;
    }

    public int getInteger(String key)
    {
        String value = get(key);
        if (value == null)
            throw new ArgException("Missing required value for option --%s=<number>", key);
        try
        {
            return Integer.parseInt(value);
        }
        catch (NumberFormatException e)
        {
            throw new ArgException("Invalid number value for option --%s=%s", key, value);
        }
    }
}
