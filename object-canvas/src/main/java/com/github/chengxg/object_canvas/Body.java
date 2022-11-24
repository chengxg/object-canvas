/*
 * 面向对象操作canvas
 * github: https://github.com/chengxg/object-canvas
 * @Author: chengxg
 *
 *  The MIT License (MIT)
 *
 *  Copyright (c) 2022-2023 by Chengxg
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */
package com.github.chengxg.object_canvas;

import android.graphics.Canvas;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.HashMap;

public class Body extends Element {
    public final HashMap<Integer, Event> eventPointers = new HashMap<>();
    public final Anime anime = new Anime();
    public boolean isNeedUpdate = true;// 是否需要更新视图
    public boolean isUpdating = false;//是否正在更新视图中
    public long lastUpdateTime = 0;//上次视图更新时间
    public View view;

    // 设置 以dp为单位
    public Element setDensityScale(float density) {
        transform.setScale(transform.scaleX * density, transform.scaleY * density);
        return this;
    }

    public Body() {
        this.init();
    }

    public Body(View view) {
        this.view = view;
        this.init();
    }

    private void init() {
        this.root = this;
        this.parent = null;
        this.silent = true;
        this.event = new ElementEventful(this);
        childs = new ArrayList<>();
    }

    public void addChild(Element element) {
        int idx = childs.indexOf(element);
        if (idx > -1) {
            return;
        }
        super.addChild(element);
    }

    public void addChild(Element element, int index) {
        int idx = childs.indexOf(element);
        if (idx > -1) {
            return;
        }
        super.addChild(element, index);
    }

    public void render(Canvas canvas) {
        this.isUpdating = true;
        anime.refresh();
        super.render(canvas);
        this.isNeedUpdate = false;
        this.anime.isUpdate = false;
        this.isUpdating = false;
        this.lastUpdateTime = System.currentTimeMillis();
        view.invalidate();
    }

    //传播自定义事件
    public void propagationEvent(int eventAction, Event event) {
        Element node = event.target;
        if (node == null) {
            return;
        }
        ArrayList<Element> arr = new ArrayList<>();
        while (true) {
            if (node != null) {
                arr.add(node);
            } else {
                break;
            }
            node = node.parent;
        }

        int size = arr.size();
        //先执行 捕获事件
        for (int i = size - 1; i >= 0; i--) {
            Element item = arr.get(i);
            if (item != null && item.silent && item.event != null) {
                if (event.isStopPropagation) {
                    return;
                }
                event.isCapture = true;
                event.current = item;
                item.event.handleEvent(eventAction, event, true);
            }
        }
        //执行冒泡事件
        for (int i = 0; i < size; i++) {
            Element item = arr.get(i);
            if (item != null && item.silent && item.event != null) {
                if (event.isStopPropagation) {
                    return;
                }
                event.isCapture = false;
                event.current = item;
                item.event.handleEvent(eventAction, event, false);
            }
        }
    }

    // 处理原生事件
    public void dispatchEvent(MotionEvent event) {
        int action = event.getActionMasked();

        if (action != MotionEvent.ACTION_MOVE) {
            int actionIndex = event.getActionIndex();
            int pointerId = event.getPointerId(actionIndex);
            int pointerIndex = event.findPointerIndex(pointerId);
            float x = event.getX(pointerIndex);
            float y = event.getY(pointerIndex);

            Event myEvent = eventPointers.get(pointerId);
            if (myEvent == null) {
                myEvent = new Event();
                myEvent.events = eventPointers;
            }
            myEvent.x = x;
            myEvent.y = y;
            myEvent.pointerId = pointerId;
            myEvent.localX = 0;
            myEvent.localY = 0;
            myEvent.motionEvent = event;
            myEvent.isStopPropagation = false;
            myEvent.params.clear();
            Element target = getDeepActiveEventElement(x, y, null);

            if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_POINTER_DOWN) {
                myEvent.target = target;
                myEvent.action = Event.Touchstart;
                eventPointers.put(pointerId, myEvent);
                if (target != null) {
                    propagationEvent(Event.Touchstart, myEvent);
                    if (!myEvent.isStopPropagation) {
                        // 存储开始点击事件的参数
                        myEvent.pointerParams.put("clickStartX", x);
                        myEvent.pointerParams.put("clickStartY", y);
                        myEvent.pointerParams.put("clickStartTarget", target);
                    }
                }
            }

            if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_POINTER_UP) {
                myEvent.action = Event.Touchend;
                if (myEvent.target == null) {
                    myEvent.target = target;
                }
                if (myEvent.target != null) {
                    propagationEvent(Event.Touchend, myEvent);
                }
                // 分发 点击事件
                if (!myEvent.isStopPropagation) {
                    Float startX = (Float) myEvent.pointerParams.get("clickStartX");
                    Float startY = (Float) myEvent.pointerParams.get("clickStartY");
                    Element startTarget = (Element) myEvent.pointerParams.get("clickStartTarget");
                    if (startTarget == target && startX != null && startY != null) {
                        float diffX = Math.abs(x - startX);
                        float diffY = Math.abs(y - startY);
                        if (diffX < 10 && diffY < 10) {
                            Event clickEvent = myEvent.clone();
                            clickEvent.action = Event.Click;
                            propagationEvent(Event.Click, clickEvent);
                        }
                    }
                }
                eventPointers.remove(pointerId);
            }

            if (action == MotionEvent.ACTION_CANCEL) {
                myEvent.action = Event.Touchcancel;
                if (target != null) {
                    propagationEvent(Event.Touchcancel, myEvent);
                }
                eventPointers.remove(pointerId);
            }
        } else {
            for (Event item : eventPointers.values()) {
                if (item.target != null) {
                    int pointerIndex = event.findPointerIndex(item.pointerId);
                    float x = event.getX(pointerIndex);
                    float y = event.getY(pointerIndex);
                    float[] dst = item.target.globalToLocal(x, y);
                    item.x = x;
                    item.y = y;
                    item.localX = dst[0];
                    item.localY = dst[1];
                    item.motionEvent = event;
                    item.action = Event.Touchmove;
                    item.isStopPropagation = false;
                    item.params.clear();
                    propagationEvent(Event.Touchmove, item);
                }
            }
        }
    }

    // --------------- getter/setter ------------------------------------

    public boolean isNeedUpdate() {
        return isNeedUpdate;
    }

    public void setNeedUpdate(boolean needUpdate) {
        isNeedUpdate = needUpdate;
    }

    public boolean isUpdating() {
        return isUpdating;
    }

    public void setUpdating(boolean updating) {
        isUpdating = updating;
    }

    public long getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(long lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    public View getView() {
        return view;
    }

    public void setView(View view) {
        this.view = view;
    }
}
