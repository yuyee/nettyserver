package com.sunstrider.common.utils;

import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sunstrider.common.config.Configuration;

/**
 * 后台任务助手类
 */
public class BackgroundHelper {

    /**
     * 配置参数路径: 后台任务助手核心线程数
     */
    public static final String CONF_BACKGROUND_CORE_THREAD_SIZE = "background.core-thread-size";

    /**
     * 配置参数路径: 后台任务助手等待任务数上限(-1=无限制)
     */
    public static final String CONF_BACKGROUND_MAX_WAITING_TASK_COUNT = "background.max-waiting-task-count";

    /**
     * 后台任务助手等待任务数上限(-1=无限制)
     */
    private static int maxWaitingTaskCount = -1;

    /**
     * 后台执行线程池
     */
    private static ScheduledExecutorService backgroundExecSrv = null;

    /** 日志记录 */
    private static final Logger LOG = LoggerFactory
        .getLogger(BackgroundHelper.class);

    /**
     * 提交任务, 立即执行
     * 
     * @param task
     *            任务实例
     * @return null=提交失败(任务数到达上限)
     */
    public static Future<?> submitTask(Runnable task) {
        if (maxWaitingTaskCount >= 0
            && ((ThreadPoolExecutor) backgroundExecSrv).getQueue().size() > maxWaitingTaskCount) {
            // 达到任务上限
            return null;
        }

        try {
            return backgroundExecSrv.submit(task);
        } catch (RejectedExecutionException e) {
            return null;
        }
    }

    /**
     * 提交任务, 延迟指定的时间后执行一次
     * 
     * @param task
     *            任务实例
     * @param delay
     *            延迟时间量
     * @param unit
     *            延迟时间单位
     * @return null=提交失败(任务数到达上限)
     */
    public static Future<?> scheduleTask(Runnable task, long delay,
        TimeUnit unit) {
        if (maxWaitingTaskCount >= 0
            && ((ThreadPoolExecutor) backgroundExecSrv).getQueue().size() > maxWaitingTaskCount) {
            // 达到任务上限
            return null;
        }

        try {
            return backgroundExecSrv.schedule(task, delay, unit);
        } catch (RejectedExecutionException e) {
            return null;
        }
    }

    /**
     * 提交任务, 按指定的间隔时间周期性执行
     * 
     * @param task
     *            任务实例
     * @param initialDelay
     *            首次执行的延迟时间量
     * @param delay
     *            再次执行的延迟时间量
     * @param unit
     *            延迟时间单位(对initialDelay/delay都有效)
     * @return null=提交失败(任务数到达上限)
     */
    public static Future<?> scheduleTaskWithFixedDelay(Runnable task,
        long initialDelay, long delay, TimeUnit unit) {
        if (maxWaitingTaskCount >= 0
            && ((ThreadPoolExecutor) backgroundExecSrv).getQueue().size() > maxWaitingTaskCount) {
            // 达到任务上限
            return null;
        }

        try {
            return backgroundExecSrv.scheduleWithFixedDelay(task, initialDelay,
                delay, unit);
        } catch (RejectedExecutionException e) {
            return null;
        }
    }

    /**
     * 初始化后台线程池助手
     * 
     * @param configuration
     */
    public static synchronized void initialize(Configuration configuration) {
        if (backgroundExecSrv != null)
            return;

        // 默认5个核心线程
        int coreSize = configuration.getRootNode().getInteger(
            CONF_BACKGROUND_CORE_THREAD_SIZE, 5);
        if (coreSize <= 0) {
            LOG.warn("ignore illegal configuration <{}>={}",
                CONF_BACKGROUND_CORE_THREAD_SIZE, coreSize);
            coreSize = 5;
        }
        LOG.info("configuration <{}>={}", CONF_BACKGROUND_CORE_THREAD_SIZE,
            coreSize);

        // 默认5个核心线程
        maxWaitingTaskCount = configuration.getRootNode().getInteger(
            CONF_BACKGROUND_MAX_WAITING_TASK_COUNT, -1);
        if (maxWaitingTaskCount < 0) {
            maxWaitingTaskCount = -1;
        }
        LOG.info("configuration <{}>={}",
            CONF_BACKGROUND_MAX_WAITING_TASK_COUNT, maxWaitingTaskCount);

        backgroundExecSrv = Executors.newScheduledThreadPool(coreSize,
            new DefaultDaemonThreadFactory());
    }

    /**
     * 废弃后台线程池助手
     */
    public static synchronized void dispose() {
        try {
            backgroundExecSrv.shutdown();
        } catch (Throwable th) {
            // ignore
        }

        // 等待操作完毕
        try {
            backgroundExecSrv.awaitTermination(30, TimeUnit.SECONDS);
        } catch (Exception ex) {}

        backgroundExecSrv = null;
    }

    // -------------------------------------------

    /**
     * daemon模式线程工厂
     * 
     * @author qiu_sheng
     */
    public static class DefaultDaemonThreadFactory extends
        DefaultNamedThreadFactory {

        public DefaultDaemonThreadFactory() {
            this("daemonpool-" + poolNumber.getAndIncrement());
        }

        public DefaultDaemonThreadFactory(String poolName) {
            super(poolName, true);
        }
    }

    /**
     * 可指定线程池名称的线程工厂
     * 
     * @author qiu_sheng
     */
    public static class DefaultNamedThreadFactory implements ThreadFactory {
        static final AtomicInteger poolNumber = new AtomicInteger(1);

        final ThreadGroup group;

        final AtomicInteger threadNumber = new AtomicInteger(1);

        final String namePrefix;

        final boolean daemon;

        public DefaultNamedThreadFactory() {
            SecurityManager s = System.getSecurityManager();
            group = (s != null) ? s.getThreadGroup() : Thread.currentThread()
                .getThreadGroup();
            namePrefix = "daemonpool-" + poolNumber.getAndIncrement()
                + "-thread-";

            this.daemon = false;
        }

        public DefaultNamedThreadFactory(String poolName, boolean daemon) {
            SecurityManager s = System.getSecurityManager();
            group = (s != null) ? s.getThreadGroup() : Thread.currentThread()
                .getThreadGroup();

            if (poolName == null) {
                namePrefix = "daemonpool-" + poolNumber.getAndIncrement()
                    + "-thread-";
            } else {
                poolNumber.getAndIncrement();
                namePrefix = poolName + "-";
            }

            this.daemon = daemon;
        }

        public Thread newThread(Runnable r) {
            Thread t = new Thread(group, r, namePrefix
                + threadNumber.getAndIncrement(), 0);
            if (t.getPriority() != Thread.NORM_PRIORITY)
                t.setPriority(Thread.NORM_PRIORITY);

            if (t.isDaemon() != this.daemon)
                t.setDaemon(this.daemon);

            return t;
        }
    }
}
