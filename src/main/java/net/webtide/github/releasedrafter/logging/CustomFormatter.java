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

package net.webtide.github.releasedrafter.logging;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class CustomFormatter extends Formatter
{
    private static final String format =
        "%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS:%4$-6s:%3$s [%6$s] %5$s%7$s%n";
    private final Date dat = new Date();

    /**
     * Format the LogRecord.
     *
     * @param record the log record
     * @return the formatted record
     */
    public String format(LogRecord record)
    {
        dat.setTime(record.getMillis());
        String source;
        if (record.getSourceClassName() != null)
        {
            source = record.getSourceClassName();
            if (record.getSourceMethodName() != null)
            {
                source += " " + record.getSourceMethodName();
            }
        }
        else
        {
            source = record.getLoggerName();
        }
        String message = formatMessage(record);
        String threadId = String.valueOf(record.getThreadID());
        String throwable = "";
        if (record.getThrown() != null)
        {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            pw.println();
            record.getThrown().printStackTrace(pw);
            pw.close();
            throwable = sw.toString();
        }

        return String.format(format, dat, // %1
            source, // %2
            record.getLoggerName(), // %3
            record.getLevel().getName(), // %4
            message, // %5
            threadId, // %6
            throwable // %7
        );
    }
}