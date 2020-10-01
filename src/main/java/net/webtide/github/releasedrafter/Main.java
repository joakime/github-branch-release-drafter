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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;

public class Main
{
    public static void main(String[] args)
    {
        // Try to figure out how / what is passed into docker image ...
        if (args == null)
        {
            System.out.println("There are no command line arguments");
        }
        else
        {
            System.out.printf("There are %d args%n", args.length);
            for (int i = 0; i < args.length; i++)
            {
                System.out.printf("args[%d] = \"%s\"%n", i, args[i]);
            }
        }

        System.getenv().entrySet().stream()
            .sorted(Comparator.comparing(e -> e.getKey()))
            .forEach(entry -> System.err.printf("env[%s] = \"%s\"%n", entry.getKey(), entry.getValue()));

        System.getProperties().entrySet().stream()
            .filter(entry -> entry.getKey().toString().matches("^(java|jdk|sun|user|file|os|sun)\\..*"))
            .sorted(Comparator.comparing(e -> e.getKey().toString()))
            .forEach(entry -> System.err.printf("system.property[%s] = \"%s\"%n", entry.getKey(), entry.getValue()));

        String eventName = System.getenv("GITHUB_EVENT_NAME");
        if (eventName != null)
        {
            String eventPathName = System.getenv("GITHUB_EVENT_PATH");
            if (eventPathName != null)
            {
                Path eventPath = Paths.get(eventPathName);
                try
                {
                    String contents = IO.toString(eventPath);
                    System.out.println(contents);
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
            else
            {
                System.out.println("No github event path declared");
            }
        }
        else
        {
            System.out.println("No github event name/type declared");
        }
    }
}
