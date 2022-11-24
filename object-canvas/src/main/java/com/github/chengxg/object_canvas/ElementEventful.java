package com.github.chengxg.object_canvas;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ElementEventful {
	protected Element mountElement;

	protected ArrayList<EventType> onClickArr;
	protected ArrayList<EventType> onDbclickArr;
	protected ArrayList<EventType> onTouchstartArr;
	protected ArrayList<EventType> onTouchmoveArr;
	protected ArrayList<EventType> onTouchendArr;
	protected ArrayList<EventType> onTouchcancelArr;

	protected int addEventCount = 0;

	public static class EventType {
		boolean isRemove = false;
		boolean useCapture = false;
		OnEvent onEvent;
	}

	public interface OnEvent {
		//返回 true 表示能触发
		public boolean callback(Event e);
	}

	public ElementEventful(Element element) {
		this.mountElement = element;
	}

	protected List<EventType> getEventListByAction(int action, boolean isCreateArr) {
		if (action == Event.Touchstart) {
			if (isCreateArr && onTouchstartArr == null) {
				onTouchstartArr = new ArrayList<>();
			}
			return onTouchstartArr;
		} else if (action == Event.Touchmove) {
			if (isCreateArr && onTouchmoveArr == null) {
				onTouchmoveArr = new ArrayList<>();
			}
			return onTouchmoveArr;
		} else if (action == Event.Touchend) {
			if (isCreateArr && onTouchendArr == null) {
				onTouchendArr = new ArrayList<>();
			}
			return onTouchendArr;
		} else if (action == Event.Touchcancel) {
			if (isCreateArr && onTouchcancelArr == null) {
				onTouchcancelArr = new ArrayList<>();
			}
			return onTouchcancelArr;
		} else if (action == Event.Click) {
			if (isCreateArr && onClickArr == null) {
				onClickArr = new ArrayList<>();
			}
			return onClickArr;
		} else if (action == Event.Dbclick) {
			if (isCreateArr && onDbclickArr == null) {
				onDbclickArr = new ArrayList<>();
			}
			return onDbclickArr;
		}
		return null;
	}

	public ElementEventful on(int action, OnEvent onEvent, boolean useCapture) {
		if (onEvent == null) {
			return this;
		}
		List<EventType> eventArr = getEventListByAction(action, true);
		if (eventArr != null) {
			EventType eventType = new EventType();
			eventType.useCapture = useCapture;
			eventType.onEvent = onEvent;
			eventArr.add(eventType);
			addEventCount++;
		}
		return this;
	}

	public ElementEventful on(int action, OnEvent onEvent) {
		return on(action, onEvent, false);
	}

	public ElementEventful off(int action) {
		List<EventType> eventArr = getEventListByAction(action, false);
		if (eventArr != null) {
			Iterator<EventType> iterator = eventArr.iterator();
			while (iterator.hasNext()) {
				EventType item = iterator.next();
				if (item != null && !item.isRemove) {
					item.isRemove = true;
					iterator.remove();
					addEventCount--;
				}
			}
		}
		return this;
	}

	public ElementEventful off(int action, OnEvent onEvent, boolean useCapture) {
		if (onEvent == null) {
			return this;
		}
		List<EventType> eventArr = getEventListByAction(action, false);
		if (eventArr != null) {
			Iterator<EventType> iterator = eventArr.iterator();
			while (iterator.hasNext()) {
				EventType item = iterator.next();
				if (item != null && !item.isRemove && item.useCapture == useCapture && item.onEvent == onEvent) {
					item.isRemove = true;
					iterator.remove();
					addEventCount--;
				}
			}
		}
		return this;
	}

	public ElementEventful off(int action, OnEvent onEvent) {
		return off(action, onEvent, false);
	}

	protected void exeOnEventCallback(List<EventType> eventArr, Event event, boolean useCapture) {
		if (eventArr != null) {
			int size = eventArr.size();
			if (size <= 0) {
				return;
			}
			EventType[] list = (EventType[]) eventArr.toArray(new EventType[size]);
			for (int i = 0; i < size; i++) {
				EventType item = list[i];
				if (item != null && !item.isRemove && item.onEvent != null && item.useCapture == useCapture) {
					item.onEvent.callback(event);
				}
			}
		}
	}

	public void handleEvent(int action, Event event, boolean isCapture) {
		switch (action) {
			case Event.Touchstart:
				exeOnEventCallback(onTouchstartArr, event, isCapture);
				break;
			case Event.Touchmove:
				exeOnEventCallback(onTouchmoveArr, event, isCapture);
				break;
			case Event.Touchend:
				exeOnEventCallback(onTouchendArr, event, isCapture);
				break;
			case Event.Touchcancel:
				exeOnEventCallback(onTouchcancelArr, event, isCapture);
				break;
			case Event.Click:
				exeOnEventCallback(onClickArr, event, isCapture);
				break;
		}
	}
}
