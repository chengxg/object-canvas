package com.github.chengxg.object_canvas;

import android.view.MotionEvent;

import java.util.HashMap;
import java.util.Map;

public class Event {
    //事件类型
    public static final int Touchstart = 0;
    public static final int Touchmove = 2;
    public static final int Touchend = 1;
    public static final int Touchcancel = 3;
    public static final int Click = 11;
    public static final int Dbclick = 12;

    public int action = -1;
    public int pointerId = 0;
    public float x = 0;//全局坐标
    public float y = 0;
    public float localX = 0;//对象的局部坐标
    public float localY = 0;
    public Element target; //事件目标节点
    public Element current;//当前执行的节点

    public boolean isCapture = false;//是否处于事件捕捉阶段
    public boolean isStopPropagation = false;//是否阻止事件传播

    public HashMap<Integer, Event> events;//所有的触摸点
    public MotionEvent motionEvent;//原生触摸事件
    public Map<String, Object> pointerParams = new HashMap<String, Object>(); // 用于存储这个触摸点的数据, 中间move等不清空
    public Map<String, Object> params = new HashMap<String, Object>(); // 用于存储用户定义的一些其他的参数

    // 停止事件传播
    public void stopPropagation() {
        isStopPropagation = true;
    }

    public Event clone() {
        Event event = new Event();
        event.action = this.action;
        event.pointerId = this.pointerId;
        event.x = this.x;
        event.y = this.y;
        event.target = this.target;
        event.current = this.current;
        event.isCapture = this.isCapture;
        event.isStopPropagation = this.isStopPropagation;
        event.events = this.events;
        event.motionEvent = this.motionEvent;
        event.pointerParams = this.pointerParams;
        event.params = this.params;
        return event;
    }
}
