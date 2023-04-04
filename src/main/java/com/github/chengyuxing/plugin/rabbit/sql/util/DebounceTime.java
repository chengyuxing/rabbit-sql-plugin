package com.github.chengyuxing.plugin.rabbit.sql.util;

import java.util.Timer;
import java.util.TimerTask;

public class DebounceTime {
    private Timer timer;
    private final Runnable runnable;
    private final long delay;


    public DebounceTime(Runnable runnable, long delay) {
        this.runnable = runnable;
        this.delay = delay;
    }

    public void trigger() {
        if (timer != null) {
            timer.cancel();
        }
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                timer = null;
                runnable.run();
            }
        }, delay);
    }
}
