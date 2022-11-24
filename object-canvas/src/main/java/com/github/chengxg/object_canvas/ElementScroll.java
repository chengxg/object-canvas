package com.github.chengxg.object_canvas;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;

public class ElementScroll {
	public Element mountElement;
	public float barWidth = 5;//滚动条宽度
	public Paint paint;
	public boolean overflowX = true;//是否显示水平滚动条
	public boolean overflowY = true;//是否显示垂直滚动条

	public float scrollHeight;
	public float scrollWidth;
	public float scrollTop;
	public float scrollLeft;
	public float scrollTopDiff = 0;
	public float scrollLeftDiff = 0;

	public ElementScroll(Element mountElement) {
		this.mountElement = mountElement;
		scrollable();
	}

	// 渲染滚动条
	public void renderScroll(Canvas canvas) {
		ElementLayout layout = mountElement.layout;
		boolean isShowX = overflowX && layout.contentWidth < scrollWidth;
		boolean isShowY = overflowY && layout.contentHeight < scrollHeight;
		if (isShowY || isShowX) {
			if (paint == null) {
				paint = new Paint();
				paint.setColor(0xFF888888);
				paint.setAlpha(200);
			}
			canvas.save();
			canvas.concat(mountElement.transform.getMatrix());
		}
		if (isShowY) {
			// 竖向滚动条
			float x = layout.borderLeft + layout.paddingLeft + layout.contentWidth - barWidth;
			float barTop = (scrollTop - scrollTopDiff) * layout.contentHeight / scrollHeight + layout.borderTop + layout.paddingTop;
			float barH = layout.contentHeight * layout.contentHeight / scrollHeight;
			canvas.drawRoundRect(x, barTop, x + barWidth, barTop + barH, barWidth / 2, barWidth / 2, paint);
		}
		if (isShowX) {
			// 横向滚动条
			float y = layout.borderTop + layout.paddingTop + layout.contentHeight - barWidth;
			float barLeft = (scrollLeft - scrollLeftDiff) * layout.contentWidth / scrollWidth + layout.borderLeft + layout.paddingLeft;
			float barW = layout.contentWidth * layout.contentWidth / scrollWidth;
			canvas.drawRoundRect(barLeft, y, barLeft + barW, y + barWidth, barWidth / 2, barWidth / 2, paint);
		}
		if (isShowY || isShowX) {
			canvas.restore();
		}
	}

	// 滚动支持
	public void scrollable() {
		mountElement.setSilent(true).getEvent().on(Event.Touchstart, (event) -> {
			Element element = mountElement;
			float[] lastPos = element.transform.globleToLocal(event.x, event.y);
			updateScrollSize();
			float height = element.layout.contentHeight;
			float width = element.layout.contentWidth;
			if (height >= scrollHeight && width >= scrollWidth) {
				return false;
			}
			boolean isShowX = overflowX && element.layout.contentWidth < scrollWidth;
			boolean isShowY = overflowY && element.layout.contentHeight < scrollHeight;

			ElementEventful.OnEvent touchmoveEvent = new ElementEventful.OnEvent() {
				@Override
				public boolean callback(Event event) {
					float[] pos = element.transform.globleToLocal(event.x, event.y);
					float dx = pos[0] - lastPos[0];
					float dy = pos[1] - lastPos[1];
					lastPos[0] = pos[0];
					lastPos[1] = pos[1];
					Boolean scrollUse = (Boolean) event.params.get("scrollUse");
					// 已经被下一层级滚动条使用了
					if (scrollUse != null && scrollUse) {
						return false;
					}

					boolean isScrollEnd = false;
					if (isShowY) {
						scrollTop = element.layout.scrollTop - dy;
						if (scrollTop > scrollHeight - height + scrollTopDiff) {
							scrollTop = scrollHeight - height + scrollTopDiff;
							isScrollEnd = true;
						}
						if (scrollTop < scrollTopDiff) {
							scrollTop = scrollTopDiff;
							isScrollEnd = true;
						}
					}
					if (isShowX) {
						scrollLeft = element.layout.scrollLeft - dx;
						if (scrollLeft > scrollWidth - width + scrollLeftDiff) {
							scrollLeft = scrollWidth - width + scrollLeftDiff;
							isScrollEnd = true;
						}
						if (scrollLeft < scrollLeftDiff) {
							scrollLeft = scrollLeftDiff;
							isScrollEnd = true;
						}
					}
					element.layout.setScroll(scrollTop, scrollLeft);
					if (!isScrollEnd) {
						event.params.put("scrollUse", true);
					}
					return false;
				}
			};
			ElementEventful.OnEvent touchendEvent = new ElementEventful.OnEvent() {
				@Override
				public boolean callback(Event event) {
					element.getEvent().off(Event.Touchmove, touchmoveEvent);
					element.getEvent().off(Event.Touchend, this);
					return false;
				}
			};
			element.getEvent().on(Event.Touchmove, touchmoveEvent);
			element.getEvent().on(Event.Touchend, touchendEvent);
			return false;
		}, false);
	}

	//获取子类最大的布局高度和宽度
	public void updateScrollSize() {
		if (mountElement.childs == null) {
			scrollHeight = 0;
			scrollWidth = 0;
			return;
		}
		Matrix matrix = new Matrix();
		float[] local = {0, 0, 0, 0, 0, 0, 0, 0};
		float[] parent = {0, 0, 0, 0, 0, 0, 0, 0};

		float scrollMinX = 0;
		float scrollMinY = 0;
		float scrollMaxX = 0;
		float scrollMaxY = 0;

		float itemWidth = 0;
		float itemHeight = 0;
		float itemMinX = 0;
		float itemMinY = 0;
		float itemMaxX = 0;
		float itemMaxY = 0;
		for (Element item : mountElement.childs) {
			if (item != null && !item.ignore) {
				matrix.set(item.transform.getMatrix());
				//				if (item.layout.childsMatrix != null) {
				//					matrix.postConcat(item.layout.childsMatrix);
				//				}
				itemWidth = item.layout.getWidth();
				itemHeight = item.layout.getHeight();
				local[2] = itemWidth;
				local[5] = itemHeight;
				local[6] = itemWidth;
				local[7] = itemHeight;
				matrix.mapPoints(parent, local);
				itemMinX = Float.MAX_VALUE;
				itemMinY = Float.MAX_VALUE;
				itemMaxX = Float.MIN_VALUE;
				itemMaxY = Float.MIN_VALUE;
				// 找出4个点的极值
				for (int i = 0; i < 8; i++) {
					// x
					if (i % 2 == 0) {
						if (itemMinX > parent[i]) {
							itemMinX = parent[i];
						}
						if (itemMaxX < parent[i]) {
							itemMaxX = parent[i];
						}
					} else {
						if (itemMinY > parent[i]) {
							itemMinY = parent[i];
						}
						if (itemMaxY < parent[i]) {
							itemMaxY = parent[i];
						}
					}
				}
				if (itemMinX < scrollMinX) {
					scrollMinX = itemMinX;
				}
				if (itemMinY < scrollMinY) {
					scrollMinY = itemMinY;
				}
				if (itemMaxX > scrollMaxX) {
					scrollMaxX = itemMaxX;
				}
				if (itemMaxY > scrollMaxY) {
					scrollMaxY = itemMaxY;
				}
			}
		}
		if (scrollMinY < 0) {
			scrollTopDiff = scrollMinY;
		} else {
			scrollTopDiff = 0;
		}
		if (scrollMinX < 0) {
			scrollLeftDiff = scrollMinX;
		} else {
			scrollLeftDiff = 0;
		}
		scrollHeight = scrollMaxY - scrollMinY;
		scrollWidth = scrollMaxX - scrollMinX;
	}

	// --------------- getter/setter ------------------------------------
	public Element getMountElement() {
		return mountElement;
	}

	public void setMountElement(Element mountElement) {
		this.mountElement = mountElement;
	}

	public float getBarWidth() {
		return barWidth;
	}

	public void setBarWidth(float barWidth) {
		this.barWidth = barWidth;
	}

	public Paint getPaint() {
		return paint;
	}

	public void setPaint(Paint paint) {
		this.paint = paint;
	}

	public boolean isOverflowX() {
		return overflowX;
	}

	public void setOverflowX(boolean overflowX) {
		this.overflowX = overflowX;
	}

	public boolean isOverflowY() {
		return overflowY;
	}

	public void setOverflowY(boolean overflowY) {
		this.overflowY = overflowY;
	}

	public float getScrollHeight() {
		return scrollHeight;
	}

	public void setScrollHeight(float scrollHeight) {
		this.scrollHeight = scrollHeight;
	}

	public float getScrollWidth() {
		return scrollWidth;
	}

	public void setScrollWidth(float scrollWidth) {
		this.scrollWidth = scrollWidth;
	}

	public float getScrollTop() {
		return scrollTop;
	}

	public void setScrollTop(float scrollTop) {
		this.scrollTop = scrollTop;
	}

	public float getScrollLeft() {
		return scrollLeft;
	}

	public void setScrollLeft(float scrollLeft) {
		this.scrollLeft = scrollLeft;
	}

	public float getScrollTopDiff() {
		return scrollTopDiff;
	}

	public void setScrollTopDiff(float scrollTopDiff) {
		this.scrollTopDiff = scrollTopDiff;
	}

	public float getScrollLeftDiff() {
		return scrollLeftDiff;
	}

	public void setScrollLeftDiff(float scrollLeftDiff) {
		this.scrollLeftDiff = scrollLeftDiff;
	}
}
