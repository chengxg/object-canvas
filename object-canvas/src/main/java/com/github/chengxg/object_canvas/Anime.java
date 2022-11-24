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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Anime {
	private static long createId = 0;
	private final ArrayList<Instance> animeInstanceArr = new ArrayList<Instance>(); //所有的动画实例
	public boolean isUpdate = false;//是否更新

	private long millis() {
		return System.currentTimeMillis();
	}

	// 动画实例状态
	public static class AnimeInstanceState {
		public static final int Stop = 0;
		public static final int Suspend = 1;
		public static final int Start = 2;
	}

	// 动画执行的阶段
	public static class AnimeProcess {
		public static final int Delay = 3;
		public static final int Play = 4;
		public static final int EndDelay = 5;
		public static final int End = 6;
	}

	// 缓动函数回调
	public interface Easing {
		public double callback(double x);
	}

	// 属性改变回调
	public interface ValueChange {
		public void callback(double x, Instance animate, PropKeyFrame propKeyFrame);
	}

	// 动画完成回调
	public interface Done {
		public boolean callback(double x);
	}

	// 默认属性改变回调, 改变属性值只能为数字
	private final ValueChange defaultChangePropCallback = (double px, Instance animate, PropKeyFrame propKeyFrame) -> {
		if (propKeyFrame == null || propKeyFrame.propName == null) {
			return;
		}
		ArrayList<Object> targets = propKeyFrame.targets == null ? animate.targets : propKeyFrame.targets;
		if (targets == null) {
			return;
		}
		for (Object obj : targets) {
			try {
				// 首字母大写
				char[] propNameChars = propKeyFrame.propName.toCharArray();
				if (propNameChars[0] >= 'a' && propNameChars[0] <= 'z') {
					propNameChars[0] = (char) (propNameChars[0] - 32);
				}
				Method updateMethod = obj.getClass().getMethod("set" + new String(propNameChars), float.class);
				float cx = (float) px * (propKeyFrame.endValue - propKeyFrame.startValue) + propKeyFrame.startValue;
				updateMethod.invoke(obj, cx);
				isUpdate = true;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	};

	//属性关键帧
	public static class PropKeyFrame {
		public ArrayList<Object> targets;// 动画作用目标对象
		public String propName;//作用对应的属性名称
		public float startValue = 0;//属性开始值
		public float endValue = 0;//属性结束值
		public int delay = 0;//动画开始延迟
		public int endDelay = 0;//动画结束延迟
		public int duration = 0;//动画持续时间
		public int frameIntevalTime = 0;//关键帧间隔
		public Easing easing;//缓动函数
		public ValueChange change;//动画改变回调

		public int process = AnimeProcess.Delay;//动画阶段
		public long startTime = 0;//动画开始时间
		public long lastTickTime = 0;//上次执行动画时间

		public PropKeyFrame addTarget(Object target) {
			if (this.targets == null) {
				this.targets = new ArrayList<>();
			}
			this.targets.add(target);
			return this;
		}

		public PropKeyFrame removeTarget(Object target) {
			if (this.targets != null) {
				this.targets.remove(target);
			}
			return this;
		}

		public void setBaseParams(String jsonParams, Map<String,Object> quoteParams) {
			try {
				JSONObject params = new JSONObject(jsonParams);
				this.setBaseParams(params, quoteParams);
			} catch (JSONException ignored) {
			}
		}

		/**
		 * 设置基础参数
		 *
		 * @param jsonParams  基础参数
		 * @param quoteParams 引用参数
		 */
		public void setBaseParams(JSONObject jsonParams, Map<String,Object> quoteParams) {

			if (jsonParams.has("propName")) {
				this.propName = jsonParams.optString("propName");
			} else {
				this.propName = null;
			}

			if (jsonParams.has("startValue")) {
				this.startValue = (float) jsonParams.optDouble("startValue");
			} else {
				this.startValue = 0;
			}

			if (jsonParams.has("endValue")) {
				this.endValue = (float) jsonParams.optDouble("endValue");
			} else {
				this.endValue = 0;
			}

			if (jsonParams.has("duration")) {
				this.duration = jsonParams.optInt("duration");
			} else {
				this.duration = 0;
			}

			if (jsonParams.has("delay")) {
				this.delay = jsonParams.optInt("delay");
			} else {
				this.delay = 0;
			}

			if (jsonParams.has("endDelay")) {
				this.endDelay = jsonParams.optInt("endDelay");
			} else {
				this.endDelay = 0;
			}

			if (jsonParams.has("frameIntevalTime")) {
				this.frameIntevalTime = jsonParams.optInt("frameIntevalTime");
			} else {
				this.frameIntevalTime = 5;
			}

			if (quoteParams != null && jsonParams.has("targets")) {
				String targetsName = jsonParams.optString("targets");
				if (targetsName.charAt(0) == '@') {
					targetsName = targetsName.substring(1);
					this.targets = (ArrayList<Object>) quoteParams.get(targetsName);
				}
			}

			if (quoteParams != null && jsonParams.has("easing")) {
				String easingName = jsonParams.optString("easing");
				if (easingName.charAt(0) == '@') {
					easingName = easingName.substring(1);
					this.easing = (Easing) quoteParams.get(easingName);
				} else {
					this.easing = AnimeEasings.getEasing(easingName);
				}
			}
			if (this.easing == null) {
				this.easing = AnimeEasings::linear;
			}

			if (quoteParams != null && jsonParams.has("change")) {
				String changeName = jsonParams.optString("change");
				if (changeName.charAt(0) == '@') {
					changeName = changeName.substring(1);
					this.change = (ValueChange) quoteParams.get(changeName);
				}
			}
		}
	}

	// 动画实例
	public class Instance extends PropKeyFrame {
		public boolean isRemove = false;//是否被移除了
		public long id = 0;//id
		public long pauseTime = 0;        //动画暂停时的时间
		public int state = AnimeInstanceState.Stop;//状态
		public int exeCount = 0;//执行次数
		public int loopCount = 1;//重复执行次数, 0为无限循环
		public boolean isGoBack = false;// 是否往返
		public boolean direction = true;//动画执行方向
		public Done done;   //动画执行完成回调, 返回true,立即销毁, 否则一直保留在内存中
		public Map<String,ArrayList<PropKeyFrame>> propKeyFrames;//包含的属性关键帧
		//        public ArrayList<ArrayList<PropKeyFrame>> propKeyFrames;//包含的属性关键帧

		public ArrayList<Instance> timeline;//时间轴
		public Instance parent;//实例父级, timeline内的子动画会设置parent

		// 重置关键帧
		private void resetPropKeyFrames() {
			if (propKeyFrames == null) {
				return;
			}
			long t = millis();
			startTime = t;
			//正向执行
			if (direction) {
				for (String key : propKeyFrames.keySet()) {
					ArrayList<PropKeyFrame> frames = propKeyFrames.get(key);
					assert frames != null;
					for (PropKeyFrame frame : frames) {
						frame.process = AnimeProcess.Delay;
						frame.startTime = t;
					}
				}
			}
			//反向执行
			if (!direction) {
				for (String key : propKeyFrames.keySet()) {
					ArrayList<PropKeyFrame> frames = propKeyFrames.get(key);
					assert frames != null;
					for (PropKeyFrame frame : frames) {
						frame.process = AnimeProcess.EndDelay;
						frame.startTime = t;
					}
				}
			}
		}

		// 获取当前正在执行的关键帧
		private PropKeyFrame getActivePropFrame(String key) {
			ArrayList<PropKeyFrame> frames = propKeyFrames.get(key);
			if (frames == null || frames.size() == 0) {
				return null;
			}
			return getActivePropFrame(frames);
		}

		// 获取当前正在执行的关键帧
		private PropKeyFrame getActivePropFrame(ArrayList<PropKeyFrame> frames) {
			int len = frames.size();
			if (direction) {
				for (PropKeyFrame frame : frames) {
					if (frame == null) {
						continue;
					}
					if (frame.process != AnimeProcess.End) {
						return frame;
					}
				}
			} else {
				for (int i = len - 1; i >= 0; i--) {
					PropKeyFrame keyFrame = frames.get(i);
					if (keyFrame == null) {
						continue;
					}
					if (keyFrame.process != AnimeProcess.End) {
						return keyFrame;
					}
				}
			}
			return null;
		}

		// 获取第一个关键帧
		private PropKeyFrame getStartPropFrame(ArrayList<PropKeyFrame> frames) {
			int len = frames.size();
			if (direction) {
				for (PropKeyFrame frame : frames) {
					if (frame == null) {
						continue;
					}
					return frame;
				}
			} else {
				for (int i = len - 1; i >= 0; i--) {
					PropKeyFrame keyFrame = frames.get(i);
					if (keyFrame == null) {
						continue;
					}
					return keyFrame;
				}
			}
			return null;
		}

		// 触发动画执行
		protected void tick() {
			if (isRemove) {
				return;
			}
			if (state != AnimeInstanceState.Start) {
				return;
			}

			if (timeline != null) {
				Instance current = getTimelineStartAnimate(true);
				if (current != null) {
					current.tick();
				} else {
					current = getTimelineStartAnimate(false);
					if (current != null) {
						current.state = AnimeInstanceState.Start;
						current.tick();
					}
				}
				return;
			}

			//正向执行
			if (direction) {
				if (propKeyFrames != null) {
					forwardTickPropKeyFrames();
					return;
				}
				forwardTickSelf();
			}
			//反向执行
			if (!direction) {
				if (propKeyFrames != null) {
					reverseTickPropKeyFrames();
					return;
				}
				reverseTickSelf();
			}
		}

		// 自身正向执行动画
		private void forwardTickSelf() {
			long t = millis();
			if (process == AnimeProcess.Delay) {
				if (t - startTime < delay) {
					return;
				}
				process = AnimeProcess.Play;
				lastTickTime = t;
			}
			if (process == AnimeProcess.Play) {
				long animatePassTime = t - startTime - delay;
				if (animatePassTime < duration) {
					if (t - lastTickTime < frameIntevalTime) {
						return;
					}
					lastTickTime = t;
					if (change != null) {
						//刷新动画
						double pt = (double) animatePassTime / duration;
						change.callback(easing.callback(pt), this, null);
						isUpdate = true;
					}
				} else {
					process = AnimeProcess.EndDelay;
					if (change != null) {
						change.callback(1, this, null);
						isUpdate = true;
					}
				}
			}

			if (process == AnimeProcess.EndDelay) {
				long endDelayPassTime = t - startTime - delay - duration;
				if (endDelayPassTime < endDelay) {
					return;
				}
				periodComplete();
			}
		}

		// 关键正向执行
		private void forwardTickPropKeyFrames() {
			long t = millis();
			for (String key : propKeyFrames.keySet()) {
				ArrayList<PropKeyFrame> frames = propKeyFrames.get(key);
				if (frames == null || frames.size() == 0) {
					continue;
				}
				PropKeyFrame frame = getActivePropFrame(frames);
				if (frame == null) {
					continue;
				}

				if (frame.process == AnimeProcess.Delay) {
					if (t - frame.startTime < frame.delay) {
						break;
					}
					frame.process = AnimeProcess.Play;
					frame.lastTickTime = t;
				}

				if (frame.process == AnimeProcess.Play) {
					long animatePassTime = t - frame.startTime - frame.delay;
					if (animatePassTime <= frame.duration) {
						if (t - frame.lastTickTime < frame.frameIntevalTime) {
							break;
						}
						frame.lastTickTime = t;
						//刷新动画
						double pt = (double) animatePassTime / frame.duration;
						double y = frame.easing.callback(pt);
						if (frame.change != null) {
							frame.change.callback(y, this, frame);
							isUpdate = true;
						}
					} else {
						frame.process = AnimeProcess.EndDelay;
						if (frame.change != null) {
							frame.change.callback(1, this, frame);
							isUpdate = true;
						}
					}
				}

				if (frame.process == AnimeProcess.EndDelay) {
					long endDelayPassTime = t - frame.startTime - frame.delay - frame.duration;
					if (endDelayPassTime < frame.endDelay) {
						break;
					}
					//当前帧 结束
					frame.process = AnimeProcess.End;
					keyframeComplete(frames);
				}
			}
		}

		// 自身反向执行
		private void reverseTickSelf() {
			long t = millis();
			if (process == AnimeProcess.EndDelay) {
				if (t - startTime < endDelay) {
					return;
				}
				process = AnimeProcess.Play;
				lastTickTime = t;
			}
			if (process == AnimeProcess.Play) {
				long animatePassTime = t - startTime - endDelay;
				if (animatePassTime < duration) {
					if (t - lastTickTime < frameIntevalTime) {
						return;
					}
					lastTickTime = t;
					if (change != null) {
						//刷新动画
						double pt = 1 - (double) animatePassTime / duration;
						change.callback(easing.callback(pt), this, null);
						isUpdate = true;
					}
				} else {
					process = AnimeProcess.Delay;
					if (change != null) {
						change.callback(0, this, null);
						isUpdate = true;
					}
				}
			}

			if (process == AnimeProcess.Delay) {
				long endDelayPassTime = t - startTime - endDelay - duration;
				if (endDelayPassTime < delay) {
					return;
				}
				periodComplete();
			}
		}

		// 反向执行关键帧
		private void reverseTickPropKeyFrames() {
			long t = millis();
			for (String key : propKeyFrames.keySet()) {
				ArrayList<PropKeyFrame> frames = propKeyFrames.get(key);
				if (frames == null || frames.size() == 0) {
					continue;
				}
				PropKeyFrame frame = getActivePropFrame(frames);
				if (frame == null) {
					continue;
				}

				if (frame.process == AnimeProcess.EndDelay) {
					if (t - frame.startTime < frame.endDelay) {
						break;
					}
					frame.process = AnimeProcess.Play;
					frame.lastTickTime = t;
				}

				if (frame.process == AnimeProcess.Play) {
					long animatePassTime = t - frame.startTime - frame.endDelay;
					if (animatePassTime <= frame.duration) {
						if (t - frame.lastTickTime < frame.frameIntevalTime) {
							break;
						}
						frame.lastTickTime = t;
						if (frame.change != null) {
							//刷新动画
							double pt = 1 - (double) animatePassTime / frame.duration;
							frame.change.callback(frame.easing.callback(pt), this, frame);
							isUpdate = true;
						}
					} else {
						frame.process = AnimeProcess.Delay;
						if (frame.change != null) {
							frame.change.callback(0, this, frame);
							isUpdate = true;
						}
					}
				}

				if (frame.process == AnimeProcess.Delay) {
					long endDelayPassTime = t - frame.startTime - frame.endDelay - frame.duration;
					if (endDelayPassTime < frame.delay) {
						break;
					}
					//当前帧 结束
					frame.process = AnimeProcess.End;
					keyframeComplete(frames);
				}
			}
		}

		/**
		 * 获取时间轴的开始执行的动画
		 *
		 * @param isStartState 是否在进行中的动画
		 * @return Instance
		 */
		private Instance getTimelineStartAnimate(boolean isStartState) {
			//正向执行
			if (direction) {
				int size = timeline.size();
				for (int i = 0; i < size; i++) {
					Instance item = timeline.get(i);
					if (item != null && !item.isRemove) {
						if (isStartState) {
							if (item.state == AnimeInstanceState.Start) {
								return item;
							}
						} else {
							return item;
						}
					}
				}
			}
			//反向执行
			if (!direction) {
				int size = timeline.size();
				for (int i = size - 1; i >= 0; i--) {
					Instance item = timeline.get(i);
					if (item != null && !item.isRemove) {
						if (isStartState) {
							if (item.state == AnimeInstanceState.Start) {
								return item;
							}
						} else {
							return item;
						}
					}
				}
			}
			return null;
		}

		// 重设时间轴动画
		private void resetTimelineAnimate() {
			for (Instance item : timeline) {
				if (item != null && !item.isRemove) {
					item.state = AnimeInstanceState.Start;
				}
			}
			Instance start = getTimelineStartAnimate(false);
			if (start != null) {
				start.state = AnimeInstanceState.Start;
				start.loopCount = 1;
				start.exeCount = 0;
				start.direction = direction;
				start.isGoBack = false;
				start.resetPropKeyFrames();
			}
		}

		// 动画是否执行完成
		private boolean checkAnimateComplete() {
			for (String key : propKeyFrames.keySet()) {
				ArrayList<PropKeyFrame> frames = propKeyFrames.get(key);
				if (frames != null) {
					for (PropKeyFrame frame : frames) {
						if (frame.process != AnimeProcess.End) {
							return false;
						}
					}
				}
			}
			return true;
		}

		// 关键帧完成
		private void keyframeComplete(ArrayList<PropKeyFrame> frames) {
			PropKeyFrame next = getActivePropFrame(frames);
			if (next == null) {
				//所有属性动画, 执行完成
				boolean isComplete = checkAnimateComplete();
				if (isComplete) {
					periodComplete();
				}
				return;
			}
			next.startTime = millis();
		}

		//动画执行一个周期完成
		private void periodComplete() {
			if (isGoBack) {
				direction = !direction;
			}
			exeCount++;

			//执行完成
			if (loopCount > 0 && exeCount >= loopCount) {
				state = AnimeInstanceState.Suspend;  //暂停运行
				exeCount = 0;    //计数清0
				process = AnimeProcess.End;
				if (parent != null) {
					parent.animateComplete();
				}
				if (done != null) {
					done.callback(exeCount);
				}
			} else {
				startTime = millis();
				state = AnimeInstanceState.Start;
				if (direction) {
					process = AnimeProcess.Delay;
				} else {
					process = AnimeProcess.EndDelay;
				}
				resetPropKeyFrames();
				if (done != null) {
					done.callback(exeCount);
				}
			}
		}

		// 动画执行完成
		private void animateComplete() {
			Instance next = getTimelineStartAnimate(true);
			if (next != null) {
				next.state = AnimeInstanceState.Start;
				next.loopCount = 1;
				next.exeCount = 0;
				next.direction = direction;
				next.isGoBack = false;
				next.resetPropKeyFrames();
				return;
			}

			if (isGoBack) {
				direction = !direction;
			}

			exeCount++;
			//执行完成
			if (loopCount > 0 && exeCount >= loopCount) {
				state = AnimeInstanceState.Suspend;  //暂停运行
				exeCount = 0;    //计数清0
				if (done != null) {
					done.callback(exeCount);
				}
				resetTimelineAnimate();
			} else {
				resetTimelineAnimate();
			}
		}

		//---------------------------------------------------
		public Instance restart() {
			if (isRemove) {
				return this;
			}
			long t = millis();
			startTime = t;
			lastTickTime = t;
			pauseTime = t;
			exeCount = 0;
			state = AnimeInstanceState.Start;
			if (direction) {
				process = AnimeProcess.Delay;
			} else {
				process = AnimeProcess.EndDelay;
			}

			if (timeline != null) {
				resetTimelineAnimate();
				return this;
			}

			if (propKeyFrames != null) {
				for (String key : propKeyFrames.keySet()) {
					ArrayList<PropKeyFrame> frames = propKeyFrames.get(key);
					if (frames == null || frames.size() <= 0) {
						continue;
					}
					for (PropKeyFrame frame : frames) {
						if (frame == null) {
							continue;
						}
						if (direction) {
							frame.process = AnimeProcess.Delay;
						} else {
							frame.process = AnimeProcess.EndDelay;
						}
						frame.startTime = t;
					}
				}
			}
			return this;
		}

		private void resumePropKeyFrames() {
			long t = millis();
			for (String key : propKeyFrames.keySet()) {
				ArrayList<PropKeyFrame> frames = propKeyFrames.get(key);
				if (frames == null || frames.size() == 0) {
					continue;
				}
				for (PropKeyFrame frame : frames) {
					frame.startTime = t - (pauseTime - frame.startTime);
					frame.lastTickTime = t - (pauseTime - frame.lastTickTime);
				}
			}
		}

		private void resumeTimeline() {
			long t = millis();
			for (Instance item : timeline) {
				if (item == null || item.isRemove) {
					continue;
				}
				item.startTime = t - (pauseTime - item.startTime);
				item.lastTickTime = t - (pauseTime - item.lastTickTime);
				if (item.propKeyFrames != null) {
					item.resumePropKeyFrames();
				}
			}
		}

		public void resume() {
			if (isRemove) {
				return;
			}
			state = AnimeInstanceState.Start;

			long t = millis();
			startTime = t - (pauseTime - startTime);
			lastTickTime = t - (pauseTime - lastTickTime);

			if (timeline != null) {
				resumeTimeline();
				return;
			}

			if (propKeyFrames != null) {
				resumePropKeyFrames();
			}
		}

		public void play() {
			if (isRemove) {
				return;
			}
			if (state == AnimeInstanceState.Stop || state == AnimeInstanceState.Start) {
				restart();
				return;
			}
			if (state == AnimeInstanceState.Suspend) {
				resume();
			}
		}

		public void pause() {
			if (isRemove) {
				return;
			}
			state = AnimeInstanceState.Suspend;
			long t = millis();
			pauseTime = t;
			if (timeline != null) {
				for (Instance item : timeline) {
					item.pauseTime = t;
				}
			}
		}

		public void stop() {
			if (isRemove) {
				return;
			}
			state = AnimeInstanceState.Stop;
			pauseTime = millis();
		}

		public void clear() {
			isRemove = true;
			targets = null;
			propKeyFrames = null;
			parent = null;
			timeline = null;
			int index = animeInstanceArr.indexOf(this);
			if (index > -1) {
				animeInstanceArr.set(index, null);
			}
		}

		public Instance addTarget(Object target) {
			if (this.targets == null) {
				this.targets = new ArrayList<>();
			}
			this.targets.add(target);
			return this;
		}

		public Instance removeTarget(Object target) {
			if (this.targets != null) {
				this.targets.remove(target);
			}
			return this;
		}

		public Instance add(String jsonParams, Map<String,Object> quoteParams) {
			Instance item = create(jsonParams, quoteParams, false);
			add(item);
			return this;
		}

		public Instance add(Instance item) {
			item.parent = this;
			item.state = AnimeInstanceState.Stop;
			item.loopCount = 1;
			item.exeCount = 0;
			if (timeline == null) {
				timeline = new ArrayList<>();
			}
			timeline.add(item);
			animeInstanceArr.remove(item);
			return this;
		}

		/**
		 * 设置动画实例的相关属性
		 *
		 * @param jsonParams  {loopCount:0,duration:600,isGoBack:true,direction:true,delay:0,endDelay:0,start:0,value:400,easing:'easeInOutQuart',change:null}
		 * @param quoteParams 引用方法参数
		 * @return Instance
		 */
		public Instance setParams(String jsonParams, Map<String,Object> quoteParams) {
			if (jsonParams != null) {
				try {
					JSONObject params = new JSONObject(jsonParams);
					this.setParams(params, quoteParams);
				} catch (JSONException ignored) {
				}
			}
			return this;
		}

		public Instance setParams(JSONObject jsonParams, Map<String,Object> quoteParams) {
			this.setBaseParams(jsonParams, quoteParams);
			if (jsonParams.has("loopCount")) {
				this.loopCount = jsonParams.optInt("loopCount");
			} else {
				this.loopCount = 1;
			}
			if (jsonParams.has("isGoBack")) {
				this.isGoBack = jsonParams.optBoolean("isGoBack");
			} else {
				this.isGoBack = false;
			}

			if (jsonParams.has("direction")) {
				this.direction = jsonParams.optBoolean("direction");
			} else {
				this.direction = true;
			}

			Iterator<String> iter = jsonParams.keys();
			while (iter.hasNext()) {
				String prop = (String) iter.next();
				try {
					Object val = jsonParams.get(prop);
					//配置为数组, 为关键帧
					if (val instanceof JSONArray) {
						JSONArray keyFrameParams = (JSONArray) val;
						this.setPropKeyFrames(null, prop, keyFrameParams, quoteParams);
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			return this;
		}

		/**
		 * 设置动画属性关键帧
		 *
		 * @param propName       属性名称
		 * @param keyFrameParams 关键帧参数 [{startValue:0,endValue:0,duration:1000,delay:0,endDelay:0,frameIntevalTime:0,easing:null,change:null}]
		 * @param quoteParams    引用方法参数
		 * @return Instance
		 */
		public Instance setPropKeyFrames(ArrayList<Object> targets, String propName, String keyFrameParams, Map<String,Object> quoteParams) {
			try {
				JSONArray params = new JSONArray(keyFrameParams);
				this.setPropKeyFrames(targets, propName, params, quoteParams);
			} catch (JSONException ignored) {
			}
			return this;
		}

		public Instance setPropKeyFrames(Object target, String propName, String keyFrameParams, Map<String,Object> quoteParams) {
			try {
				ArrayList<Object> targets = new ArrayList<>();
				targets.add(target);
				JSONArray params = new JSONArray(keyFrameParams);
				this.setPropKeyFrames(targets, propName, params, quoteParams);
			} catch (JSONException ignored) {
			}
			return this;
		}

		public Instance setPropKeyFrames(ArrayList<Object> targets, String propName, JSONArray keyFrameParams, Map<String,Object> quoteParams) {
			try {
				int len = keyFrameParams.length();
				ArrayList<PropKeyFrame> keyFrames = new ArrayList<PropKeyFrame>();
				if (this.propKeyFrames == null) {
					this.propKeyFrames = new HashMap<>();
				}
				this.propKeyFrames.put(propName, keyFrames);
				for (int i = 0; i < len; i++) {
					JSONObject frameParams = (JSONObject) keyFrameParams.get(i);
					PropKeyFrame frameObj = new PropKeyFrame();
					keyFrames.add(frameObj);
					frameObj.setBaseParams(frameParams, quoteParams);
					if (frameObj.propName == null || frameObj.propName.isEmpty()) {
						frameObj.propName = propName;
					}
					if (frameObj.targets == null) {
						frameObj.targets = targets;
					}

					if (frameObj.frameIntevalTime <= 0) {
						frameObj.frameIntevalTime = this.frameIntevalTime;
					}

					if (frameObj.change == null) {
						frameObj.change = defaultChangePropCallback;
					}
				}

				PropKeyFrame lastFrame = keyFrames.get(0);
				for (int i = 1; i < len; i++) {
					PropKeyFrame item = keyFrames.get(i);
					item.startValue = lastFrame.endValue;
					lastFrame = item;
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return this;
		}
	}

	// 创建 动画实例
	public Instance create(String jsonParams, Map<String,Object> quoteParams, boolean isAddAnimeArr) {
		Instance animate = new Instance();
		createId++;
		animate.id = createId;
		if (isAddAnimeArr) {
			animeInstanceArr.add(animate);
		}
		animate.setParams(jsonParams, quoteParams);
		return animate;
	}

	public Instance create(String jsonParams, Map<String,Object> quoteParams) {
		return create(jsonParams, quoteParams, true);
	}

	// 定时刷新动画
	public void refresh() {
		for (Instance item : animeInstanceArr) {
			if (item == null) {
				continue;
			}
			item.tick();
		}
	}

	// 将实例添加到动画列表中
	public void addAnime(Instance animeInstance) {
		int index = animeInstanceArr.indexOf(animeInstance);
		if (index == -1) {
			animeInstanceArr.add(animeInstance);
		}
	}

	public void clear(Instance animeInstance) {
		int index = animeInstanceArr.indexOf(animeInstance);
		if (index > -1) {
			animeInstance.clear();
			animeInstanceArr.set(index, null);
		}
	}

	public void clearAll() {
		for (Instance item : animeInstanceArr) {
			if (item != null) {
				item.clear();
			}
		}
		animeInstanceArr.clear();
	}

}
