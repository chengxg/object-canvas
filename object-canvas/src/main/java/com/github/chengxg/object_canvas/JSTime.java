package com.github.chengxg.object_canvas;

import java.util.ArrayList;

/**
 * 仿照js实现的软件定时器
 */
public class JSTime {
	private static final long timeoutMaxId = Long.MAX_VALUE - 2; // timeoutId为正数
	private static final long timeIntervalMinId = -timeoutMaxId; // timeInterval为负数
	private long createTimeoutId = 1;
	private long createTimeIntevalId = -1;
	// 所有的定时器
	private final ArrayList<TimerStruct> timeArr = new ArrayList<TimerStruct>(10);

	public static class TimerStruct {
		public long startTime = 0;//计时开始时间
		public long periodTime = 0;//计时时长
		public Callback callback = null;//回调函数
		public SimpleCallback simpleCallback = null;//简单回调函数
		public Object parameter = null;//回调函数 参数
		public int parameterInt = 0;//回调函数 int参数
		public long id = 0;//id
		public boolean isInterval = false;// 是否为 setInterval
	}

	public interface Callback {
		public void run(int parameterInt, Object parameter);
	}

	public interface SimpleCallback {
		public void run();
	}

	private long millis() {
		return System.currentTimeMillis();
	}

	private long micros() {
		return System.nanoTime() / 1000;
	}

	//在循环中不断刷新
	public void refresh() {
		long t = micros();
		for (TimerStruct time : timeArr) {
			if (time != null && time.id != 0) {
				if (time.periodTime >= 0 && t - time.startTime >= time.periodTime) {
					boolean isFree = false;
					long id = time.id;
					if (!time.isInterval) {
						// setTimeout 执行完毕就销毁
						isFree = true;
					} else {
						// setInteval 不断进行
						time.startTime = t;
					}

					//执行回调
					if (time.callback != null) {
						try {
							time.callback.run(time.parameterInt, time.parameter);
						} catch (Exception ignored) {}
					} else if (time.simpleCallback != null) {
						try {
							time.simpleCallback.run();
						} catch (Exception ignored) {}
					}

					//重新获取时间, 避免回调函数中新增JSTime, 而造成startTime比t大的bug
					t = micros();
					// 防止在回调函数里调用了 clearTime 而引发bug
					if (isFree && time.id == id) {
						// setTimeout 执行完毕就销毁
						time.id = 0;
					}
				}
			}
		}
	}

	private long baseSetTimer(boolean isInterval, SimpleCallback simpleCallback, Callback callback, long time, int parameterInt, Object parameter) {
		TimerStruct t = null;
		// 找出废弃的对象来复用
		for (TimerStruct item : timeArr) {
			if (item != null && item.id == 0) {
				t = item;
				break;
			}
		}
		if (t == null) {
			t = new TimerStruct();
			timeArr.add(t);
		}
		t.simpleCallback = simpleCallback;
		t.callback = callback;
		t.parameterInt = parameterInt;
		t.parameter = parameter;
		t.periodTime = time;
		t.startTime = micros();
		t.isInterval = isInterval;
		// setInteval
		if (isInterval) {
			if (createTimeIntevalId < timeIntervalMinId) {
				createTimeIntevalId = -1000;
			}
			createTimeIntevalId--;
			t.id = createTimeIntevalId;
		} else {
			// setTimeout
			if (createTimeoutId > timeoutMaxId) {
				createTimeoutId = 1000;
			}
			createTimeoutId++;
			t.id = createTimeoutId;
		}

		return t.id;
	}

	// 简单 setTimeout
	public long setTimeout(SimpleCallback simpleCallback, double time) {
		return baseSetTimer(false, simpleCallback, null, (int) (time * 1000), 0, null);
	}

	// 带参数的 setTimeout
	public long setTimeout(Callback callback, double time, int parameterInt, Object parameter) {
		return baseSetTimer(false, null, callback, (int) (time * 1000), parameterInt, parameter);
	}

	// 简单 setInterval
	public long setInterval(SimpleCallback simpleCallback, double time) {
		return baseSetTimer(true, simpleCallback, null, (int) (time * 1000), 0, null);
	}

	// 带参数的 setInterval
	public long setInterval(Callback callback, double time, int parameterInt, Object parameter) {
		return baseSetTimer(true, null, callback, (int) (time * 1000), parameterInt, parameter);
	}

	// 取消执行
	public boolean clearTime(long timeoutId) {
		if (timeoutId == 0) {
			return false;
		}
		for (TimerStruct item : timeArr) {
			if (item != null && item.id == timeoutId) {
				return true;
			}
		}
		return false;
	}

	// 全部取消执行
	public boolean clearTimeAll() {
		for (TimerStruct item : timeArr) {
			if (item != null) {
				item.id = 0;
			}
		}
		return true;
	}
}
