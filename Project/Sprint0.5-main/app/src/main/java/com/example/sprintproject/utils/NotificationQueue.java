package com.example.sprintproject.utils;

import com.example.sprintproject.model.ThresholdNotification;
import java.util.LinkedList;
import java.util.Queue;

public class NotificationQueue {
    public interface OnShow {
        void onShow(ThresholdNotification item, Runnable onComplete);
    }

    private final Queue<ThresholdNotification> queue = new LinkedList<>();
    private boolean running = false;
    private final OnShow onShow;

    public NotificationQueue(OnShow onShow) {
        this.onShow = onShow;
    }
    public synchronized void enqueue(ThresholdNotification item) {
        if (item == null) {
            return;
        }
        queue.offer(item);
        run();
    }

    private synchronized void run() {
        if (running) {
            return;
        }
        running = true;
        processNext();
    }

    private void processNext() {
        ThresholdNotification item;
        synchronized (this) {
            item = queue.poll();
            if (item == null) {
                running = false;
                return;
            }
        }
        try {
            onShow.onShow(item, this::processNext);
        } catch (Exception e) {
            processNext();
        }
    }
}
