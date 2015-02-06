/**
 * 
 */

package com.john.util;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * 线程池工具，该工具提供了最大10个线程来运行调用者所要执行的工具
 * 
 * @author <a href="mailto:chen.jackson@gmail.com">Mike Chen</a>
 * @version 1.0
 */
public class ThreadPoolUtil {
    private static final ExecutorService pool;

    static {
        pool = Executors.newCachedThreadPool();
    }

    /**
     * 执行可运行的命令
     * 
     * @param command
     */
    public static void execute(Runnable command) {
        pool.execute(command);
    }

    /**
     * 提交一个带返回值的任务
     * 
     * @param <T>
     * @param task
     * @return
     */
    public static <T> Future<T> submit(Callable<T> task) {
        return pool.submit(task);
    }

    /**
     * 提交一个可运行的任务
     * 
     * @param task
     * @return
     */
    public static Future<?> submit(Runnable task) {
        return pool.submit(task);
    }

    /**
     * 提交一个可运行的任务
     * 
     * @param <T>
     * @param task
     * @param result
     * @return
     */
    public static <T> Future<T> submit(Runnable task, T result) {
        return pool.submit(task, result);
    }
}
