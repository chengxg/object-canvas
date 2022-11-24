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
import android.graphics.Matrix;
import android.graphics.RectF;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Element {
	public static long currentId = 0;
	public long id = 0;
	public String name = "";

	public String tagName = "div"; //标签名称
	public boolean ignore = false; //是否隐藏不渲染
	public boolean silent = false; //是否响应事件, 为true才响应事件

	public Body root = null; //根节点 引用
	public Element parent = null; //父节点 引用
	public ArrayList<Element> childs = null; //子节点引用

	public ElementTransform transform = new ElementTransform(this);// transform变换引用
	public ElementLayout layout = new ElementLayout(this);//布局引用
	public ElementEventful event = null; //事件引用
	public ElementBox box = null;//盒子模型
	public ElementText textContent = null; //内容文字
	public ElementScroll scroll = null; //滚动条
	public Map<String,Object> params = null; //自定义的其他参数
	public RenderAfterCallback renderAfterCallback = null; // 自身及子类渲染完成后的回调

	public interface RenderAfterCallback {
		public void callback(Canvas canvas);
	}

	public Element() {
		this.id = Element.currentId++;
	}

	// clone节点
	public Element cloneNode(Element element) {
		if (element == null) {
			element = new Element();
		}
		element.name = this.name;
		element.ignore = this.ignore;
		element.silent = this.silent;
		element.root = this.root;
		element.parent = this.parent;
		element.renderAfterCallback = this.renderAfterCallback;
		this.transform.clone(element.transform);
		this.layout.clone(element.layout);
		if (this.box != null) {
			element.box = this.box.clone();
		}
		if (this.textContent != null) {
			element.textContent = this.textContent.clone();
		}
		if (this.childs != null) {
			element.childs = new ArrayList<>();
			for (Element item : this.childs) {
				if (item != null) {
					element.addChild(item.cloneNode(null));
				}
			}
		}
		return element;
	}

	// 根据name查询节点元素
	public Element getElementByName(String name) {
		if (this.name.equals(name)) {
			return this;
		}
		if (this.childs != null) {
			for (Element item : childs) {
				if (item != null) {
					Element target = item.getElementByName(name);
					if (target != null) {
						return target;
					}
				}
			}
		}
		return null;
	}

	public void addChild(Element element) {
		addChild(element, -1);
	}

	public void addChild(Element element, int index) {
		if (childs == null) {
			childs = new ArrayList<>();
		}
		element.parent = this;
		int idx = childs.indexOf(element);
		if (idx > -1) {
			return;
		}
		if (index <= childs.size() && index >= 0) {
			childs.add(index, element);
		} else {
			int nullIndex = childs.indexOf(null);
			if (nullIndex > -1) {
				childs.set(nullIndex, element);
			} else {
				childs.add(element);
			}
		}
		element.setRoot(this.root);
	}

	public void setRoot(Body root) {
		this.root = root;
		if (this.childs != null && this.childs.size() > 0) {
			for (Element item : childs) {
				if (item != null) {
					item.setRoot(root);
				}
			}
		}
	}

	public void remove() {
		if (parent != null) {
			parent.removeChild(this);
		}
	}

	public void removeChild(Element element) {
		if (childs != null) {
			int index = childs.indexOf(element);
			if (index > -1) {
				element.parent = null;
				element.setRoot(null);
				childs.set(index, null);
			}
		}
	}

	public void render(Canvas canvas) {
		//		if (name != null && name.equals("testsection1")) {
		//			Log.d("testsection1", "testsection1");
		//		}
		this.renderBox(canvas);
		this.renderText(canvas);
		this.renderChilds(canvas);
		this.renderAfter(canvas);
		this.renderScroll(canvas);
	}

	public void renderBox(Canvas canvas) {
		// 画box
		if (box != null) {
			box.drawBox(canvas);
		}
	}

	public void renderText(Canvas canvas) {
		if (textContent != null) {
			textContent.drawText(canvas);
		}
	}

	public void renderScroll(Canvas canvas) {
		if (scroll != null) {
			scroll.renderScroll(canvas);
		}
	}

	public void renderChilds(Canvas canvas) {
		// 画子元素
		if (childs != null && childs.size() > 0) {
			boolean isStartDrawChild = false;
			int saveId = 0;
			for (Element item : childs) {
				if (item != null && !item.ignore && item.layout.isActiveViewPort()) {
					if (!isStartDrawChild) {
						isStartDrawChild = true;
						saveId = canvas.save();
						canvas.concat(transform.getMatrix());
						float x = layout.getX();
						float y = layout.getY();
						canvas.clipRect(x, y, layout.contentWidth + x, layout.contentHeight + y);
						if (layout.childsMatrix != null) {
							canvas.concat(layout.childsMatrix);
						}
					}
					item.render(canvas);
				}
			}
			if (saveId > 0) {
				canvas.restoreToCount(saveId);
			}
		}
	}

	public void renderAfter(Canvas canvas) {
		// 画自身
		if (renderAfterCallback != null) {
			canvas.save();
			canvas.concat(this.transform.getMatrix());
			renderAfterCallback.callback(canvas);
			canvas.restore();
		}
	}

	public float[] globalToLocal(float x, float y) {
		return transform.globleToLocal(x, y);
	}

	public float[] localToGlobal(float x, float y) {
		return transform.localToGloble(x, y);
	}

	public RectF getRectBoundingByPoints(RectF rectBounding, float[] localCornerPoints) {
		int len = localCornerPoints.length;
		if (len % 2 != 0) {
			return null;
		}

		float minX = Float.MAX_VALUE;
		float maxX = Float.MIN_VALUE;
		float minY = Float.MAX_VALUE;
		float maxY = Float.MIN_VALUE;
		for (int i = 0; i < len; i++) {
			if (i % 2 == 0) {
				if (localCornerPoints[i] <= minX) {
					minX = localCornerPoints[i];
				}
				if (localCornerPoints[i] >= maxX) {
					maxX = localCornerPoints[i];
				}
			} else {
				if (localCornerPoints[i] <= minY) {
					minY = localCornerPoints[i];
				}
				if (localCornerPoints[i] >= maxY) {
					maxY = localCornerPoints[i];
				}
			}
		}
		if (rectBounding == null) {
			rectBounding = new RectF();
		}
		rectBounding.left = minX;
		rectBounding.top = minY;
		rectBounding.right = maxX;
		rectBounding.bottom = maxY;
		return rectBounding;
	}

	/**
	 * 递归判断全局坐标, 是否位于自身元素或子元素内
	 */
	public Element getDeepActiveEventElement(float x, float y, Matrix pInvertMatrix) {
		if (pInvertMatrix == null) {
			pInvertMatrix = this.transform.getRootInvertMatrix2();
		} else {
			pInvertMatrix.postConcat(this.transform.getInvertMatrix());
		}
		if (childs != null) {
			int size = childs.size();
			Matrix newMatrix = null;
			boolean isNewMatrix = false;
			for (int i = size - 1; i >= 0; i--) {
				Element item = childs.get(i);
				if (item != null && !item.ignore && item.layout.isActiveViewPort()) {
					if (!isNewMatrix) {
						newMatrix = new Matrix(pInvertMatrix);
						isNewMatrix = true;
					} else {
						newMatrix.set(pInvertMatrix);
					}
					if (layout.childsInvertMatrix != null) {
						newMatrix.postConcat(layout.childsInvertMatrix);
					}
					Element ele = item.getDeepActiveEventElement(x, y, newMatrix);
					if (ele != null) {
						return ele;
					}
				}
			}
		}

		if (isActiveEventElement(x, y, pInvertMatrix)) {
			return this;
		}
		return null;
	}

	/**
	 * 判断全局坐标, 是否位于自身元素内
	 */
	public boolean isActiveEventElement(float x, float y, Matrix rootInvertMatrix) {
		if (this.silent && this.event != null && this.event.addEventCount > 0) {
			if (rootInvertMatrix == null) {
				rootInvertMatrix = this.transform.getRootInvertMatrix();
			}
			float width = this.layout.getWidth();
			float height = this.layout.getHeight();
			// 全局变局部坐标 来判断当前点是否在元素内
			float[] src = {x, y};
			float[] dst = {0, 0};
			rootInvertMatrix.mapPoints(dst, src);
			float lx = dst[0];
			float ly = dst[1];
			return 0 <= lx && lx <= width && 0 <= ly && ly <= height;
		}
		return false;
	}

	public boolean isRootView() {
		Matrix rootMatrix = this.transform.getRootMatrix();
		float width = this.layout.getWidth();
		float height = this.layout.getHeight();
		float rootWidth = this.root.layout.getWidth();
		float rootHeight = this.root.layout.getHeight();
		// 全局变局部坐标 来判断当前点是否在元素内
		float[] src = {0, 0, width, height};
		float[] dst = {0, 0, 0, 0};
		rootMatrix.mapPoints(dst, src);
		return 0 <= dst[0] && dst[2] <= rootWidth && 0 <= dst[1] && dst[3] <= rootHeight;
		//		return false;
	}

	// 设置更新view
	public void setUpdateView() {
		if (this.root != null) {
			this.root.isNeedUpdate = true;
		}
	}

	// 添加box样式
	public ElementBox setBox() {
		if (box == null) {
			box = new ElementBox(this);
		}
		return box;
	}

	public Element clearBox() {
		if (box != null) {
			box.paint = null;
			box.mountElement = null;
		}
		box = null;
		return this;
	}

	public ElementText setTextContent(String text) {
		if (textContent == null) {
			textContent = new ElementText(this);
		}
		textContent.setText(text);
		return textContent;
	}

	public ElementScroll setScroll() {
		if (scroll == null) {
			scroll = new ElementScroll(this);
		}
		return scroll;
	}

	public Element clearScroll() {
		if (scroll != null) {
			scroll.paint = null;
			scroll.mountElement = null;
		}
		scroll = null;
		return this;
	}

	// ----------- getter/setter 开始 -------------

	public Element setName(String name) {
		this.name = name;
		return this;
	}

	public Element setTagName(String tagName) {
		this.tagName = tagName;
		return this;
	}

	public Element setIgnore(boolean ignore) {
		this.ignore = ignore;
		return this;
	}

	public Element setSilent(boolean silent) {
		this.silent = silent;
		return this;
	}

	public Body getRoot() {
		return root;
	}

	public Element getParent() {
		return parent;
	}

	public ElementTransform getTransform() {
		return transform;
	}

	public ElementLayout getLayout() {
		return layout;
	}

	public ElementEventful getEvent() {
		if (event == null) {
			event = new ElementEventful(this); //事件引用
		}
		return event;
	}

	public Map<String,Object> getParams() {
		if (params == null) {
			params = new HashMap<>();
		}
		return params;
	}

	public Element setRenderAfterCallback(RenderAfterCallback renderSelfCallback) {
		this.renderAfterCallback = renderSelfCallback;
		return this;
	}

	// -------------------------------------------

}
