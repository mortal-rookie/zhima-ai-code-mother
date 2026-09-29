package com.example.zhimaaicodemother.monitor;

import javax.management.monitor.Monitor;

/**
 * 监控上下文持有者(同线程内共享)
 */
public class MonitorContextHolder {

    private static final ThreadLocal<MonitorContext> CONTEXT_HOLDER = new ThreadLocal<>();

    /**
     * 获取当前线程的监控上下文
     */
    public static  MonitorContext getContext(){return CONTEXT_HOLDER.get();}

    /**
     * 设置监控上下文
     */
    public static void setContext(MonitorContext context){CONTEXT_HOLDER.set(context);}

    /**
    * 清除监控上下文
    */
    public static  void clearContext(){ CONTEXT_HOLDER.remove();}
}
