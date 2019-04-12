package com.v5project.proxy;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * @author beou on 4/12/19 12:40
 */
public class QueueManager {

    private static QueueManager queueManagerInstance;
    private BlockingQueue<TrakObject> queue;


    private QueueManager() {
        queue = new ArrayBlockingQueue<>(1000);
    }

    public static QueueManager getInstance() {
        if (queueManagerInstance == null) {
            queueManagerInstance = new QueueManager();
        }
        return queueManagerInstance;
    }

    public TrakObject take() {
        try {
            return queue.take();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void put(TrakObject object) {
        try {
            queue.put(object);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
