package com.example.dezhou.servicedemo.BindService;

/**
 * 自定义的MyBinder接口用于保护服务中不想让外界访问的方法
 */
public interface IMyBinder {
    int invokeMethodInService();
}
