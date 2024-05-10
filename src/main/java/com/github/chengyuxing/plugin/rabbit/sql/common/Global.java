package com.github.chengyuxing.plugin.rabbit.sql.common;

import com.github.chengyuxing.common.MostDateTime;
import com.github.chengyuxing.sql.Args;

public class Global {
    /**
     * System (USER, DATE, TIME)
     *
     * @return args
     */
    public static Args<Object> usefulArgs() {
        var now = MostDateTime.now();
        return Args.of(
                "USER", System.getProperty("user.name"),
                "DATE", now.toString("yyyy/MM/dd"),
                "TIME", now.toString("HH:mm:ss")
        );
    }
}
