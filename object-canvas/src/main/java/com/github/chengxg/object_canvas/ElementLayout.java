package com.github.chengxg.object_canvas;

import android.graphics.Matrix;

public class ElementLayout {
	public Element mountElement;

	//自身box模型
	public float paddingLeft = 0;
	public float paddingTop = 0;
	public float paddingRight = 0;
	public float paddingBottom = 0;
	public float borderLeft = 0;
	public float borderTop = 0;
	public float borderRight = 0;
	public float borderBottom = 0;
	public float contentWidth = 0;
	public float contentHeight = 0;

	//TODO 相对于兄弟元素
	public float marginLeft = 0;
	public float marginTop = 0;
	public float marginRight = 0;
	public float marginBottom = 0;

	//相对于父级
	public float left = 0; //布局计算 控制的属性
	public float top = 0;

	//视图滚动计算
	public Matrix childsMatrix = null; //滚动, paddingleft,boroderleft矩阵
	public Matrix childsInvertMatrix = null; //滚动, paddingleft,boroderleft矩阵
	public float scrollTop = 0;
	public float scrollLeft = 0;

	public ElementLayout(Element element) {
		this.mountElement = element;
	}

	public void clone(ElementLayout layout) {
		layout.paddingLeft = this.paddingLeft;
		layout.paddingTop = this.paddingTop;
		layout.paddingRight = this.paddingRight;
		layout.paddingBottom = this.paddingBottom;
		layout.borderLeft = this.borderLeft;
		layout.borderTop = this.borderTop;
		layout.borderRight = this.borderRight;
		layout.borderBottom = this.borderBottom;
		layout.contentWidth = this.contentWidth;
		layout.contentHeight = this.contentHeight;
		layout.left = this.left;
		layout.top = this.top;
		layout.scrollTop = this.scrollTop;
		layout.scrollLeft = this.scrollLeft;
		if (this.childsMatrix != null) {
			layout.childsMatrix = new Matrix(this.childsMatrix);
			layout.childsInvertMatrix = new Matrix(this.childsInvertMatrix);
		}
	}

	public void updateFields() {
		this.updateChildsMatrix();
		this.mountElement.transform.isUpdateMatrix = true;
		this.mountElement.setUpdateView();
	}

	public ElementLayout setPosition(float left, float top) {
		this.left = left;
		this.top = top;
		this.updateFields();
		return this;
	}

	public ElementLayout setMargin(float marginLeft, float marginTop, float marginRight, float marginBottom) {
		this.marginLeft = marginLeft;
		this.marginTop = marginTop;
		this.marginRight = marginRight;
		this.marginBottom = marginBottom;
		this.updateFields();
		return this;
	}

	public ElementLayout setBorderWidth(float borderWidth) {
		this.borderTop = borderWidth;
		this.borderRight = borderWidth;
		this.borderBottom = borderWidth;
		this.borderLeft = borderWidth;
		this.updateFields();
		return this;
	}

	public ElementLayout setBorderWidth(float borderLeftRightWidth, float borderTopBottomWidth) {
		this.borderTop = borderTopBottomWidth;
		this.borderRight = borderLeftRightWidth;
		this.borderBottom = borderTopBottomWidth;
		this.borderLeft = borderLeftRightWidth;
		this.updateFields();
		return this;
	}

	public ElementLayout setBorderWidth(float borderLeft, float borderTop, float borderRight, float borderBottom) {
		this.borderLeft = borderLeft;
		this.borderTop = borderTop;
		this.borderRight = borderRight;
		this.borderBottom = borderBottom;
		this.updateFields();
		return this;
	}

	public ElementLayout setPadding(float paddingLeft, float paddingTop, float paddingRight, float paddingBottom) {
		this.paddingLeft = paddingLeft;
		this.paddingTop = paddingTop;
		this.paddingRight = paddingRight;
		this.paddingBottom = paddingBottom;
		this.updateFields();
		return this;
	}

	public ElementLayout setPadding(float topBottomPadding, float leftRightPadding) {
		this.paddingLeft = leftRightPadding;
		this.paddingTop = topBottomPadding;
		this.paddingRight = leftRightPadding;
		this.paddingBottom = topBottomPadding;
		this.updateFields();
		return this;
	}

	public ElementLayout setPadding(float padding) {
		this.paddingLeft = padding;
		this.paddingTop = padding;
		this.paddingRight = padding;
		this.paddingBottom = padding;
		this.updateFields();
		return this;
	}

	public ElementLayout setContent(float contentWidth, float contentHeight) {
		this.contentWidth = contentWidth;
		this.contentHeight = contentHeight;
		this.updateFields();
		return this;
	}

	public ElementLayout setContentMatchParent(float height) {
		if (this.mountElement.parent == null) {
			return this;
		}
		this.left = 0;
		if (height <= 0) {
			this.top = 0;
			height = this.mountElement.parent.layout.contentHeight;
		}
		this.setBoxSize(this.mountElement.parent.layout.contentWidth, height);
		this.updateChildsMatrix();
		this.mountElement.setUpdateView();
		return this;
	}

	public void updateChildsMatrix() {
		float dx = -this.scrollLeft + getX();
		float dy = -this.scrollTop + getY();
		if (childsMatrix == null) {
			childsMatrix = new Matrix();
			childsInvertMatrix = new Matrix();
		}
		childsMatrix.setTranslate(dx, dy);
		childsMatrix.invert(childsInvertMatrix);
	}

	public ElementLayout setScroll(float scrollTop, float scrollLeft) {
		boolean isUpdate = false;
		if (this.scrollTop != scrollTop) {
			this.scrollTop = scrollTop;
			isUpdate = true;
		}
		if (this.scrollLeft != scrollLeft) {
			this.scrollLeft = scrollLeft;
			isUpdate = true;
		}
		if (isUpdate) {
			this.updateChildsMatrix();
			this.mountElement.setUpdateView();
		}
		return this;
	}

	public ElementLayout setBoxSize(float width, float height) {
		float contentWidth = width - borderLeft - paddingLeft - paddingRight - borderRight;
		float contentHeight = height - borderTop - paddingTop - paddingBottom - borderBottom;
		if (contentWidth < 0) {
			contentWidth = 0;
		}
		if (contentHeight < 0) {
			contentHeight = 0;
		}
		this.contentWidth = contentWidth;
		this.contentHeight = contentHeight;
		this.updateFields();
		return this;
	}

	public float getX() {
		return borderLeft + paddingLeft;
	}

	public float getY() {
		return borderTop + paddingTop;
	}

	public float getWidth() {
		return borderLeft + paddingLeft + contentWidth + paddingRight + borderRight;
	}

	public float getHeight() {
		return borderTop + paddingTop + contentHeight + paddingBottom + borderBottom;
	}

	//是否在父级布局中可见
	public boolean isActiveViewPort() {
		if (mountElement.parent == null) {
			return true;
		}
		ElementLayout viewportLayout = mountElement.parent.layout;
		//判断两个矩形是否相交
		//(x1,y1) (x2,y2)为第一个矩形左下和右上角的两个点
		//(x3,y3) (x4,y4)为第二个矩形左下角和右上角的两个点
		float diff = 0; //扩大一点范围
		float x1 = left - diff;
		float y1 = top + this.getHeight() + diff;
		float x2 = left + this.getWidth() + diff;
		float y2 = top - diff;

		float x3 = viewportLayout.scrollLeft;
		float y3 = viewportLayout.scrollTop + viewportLayout.contentHeight;
		float x4 = viewportLayout.scrollLeft + viewportLayout.contentWidth;
		float y4 = viewportLayout.scrollTop;
		return Math.max(x1, x3) <= Math.min(x2, x4) && Math.min(y1, y3) >= Math.max(y2, y4);
	}

	// ----------------------------- setter 开始 --------------------------------------
	public ElementLayout setPaddingLeft(float paddingLeft) {
		this.paddingLeft = paddingLeft;
		this.updateFields();
		return this;
	}

	public ElementLayout setPaddingTop(float paddingTop) {
		this.paddingTop = paddingTop;
		this.updateFields();
		return this;
	}

	public ElementLayout setPaddingRight(float paddingRight) {
		this.paddingRight = paddingRight;
		this.updateFields();
		return this;
	}

	public ElementLayout setPaddingBottom(float paddingBottom) {
		this.paddingBottom = paddingBottom;
		this.updateFields();
		return this;
	}

	public ElementLayout setBorderLeft(float borderLeft) {
		this.borderLeft = borderLeft;
		this.updateFields();
		return this;
	}

	public ElementLayout setBorderTop(float borderTop) {
		this.borderTop = borderTop;
		this.updateFields();
		return this;
	}

	public ElementLayout setBorderRight(float borderRight) {
		this.borderRight = borderRight;
		this.updateFields();
		return this;
	}

	public ElementLayout setBorderBottom(float borderBottom) {
		this.borderBottom = borderBottom;
		this.updateFields();
		return this;
	}

	public ElementLayout setContentWidth(float contentWidth) {
		this.contentWidth = contentWidth;
		this.updateFields();
		return this;
	}

	public ElementLayout setContentHeight(float contentHeight) {
		this.contentHeight = contentHeight;
		this.updateFields();
		return this;
	}

	public ElementLayout setMarginLeft(float marginLeft) {
		this.marginLeft = marginLeft;
		this.updateFields();
		return this;
	}

	public ElementLayout setMarginTop(float marginTop) {
		this.marginTop = marginTop;
		this.updateFields();
		return this;
	}

	public ElementLayout setMarginRight(float marginRight) {
		this.marginRight = marginRight;
		this.updateFields();
		return this;
	}

	public ElementLayout setMarginBottom(float marginBottom) {
		this.marginBottom = marginBottom;
		this.updateFields();
		return this;
	}

	public ElementLayout setLeft(float left) {
		this.left = left;
		this.updateFields();
		return this;
	}

	public ElementLayout setTop(float top) {
		this.top = top;
		this.updateFields();
		return this;
	}

	public ElementLayout setScrollTop(float scrollTop) {
		this.scrollTop = scrollTop;
		this.updateFields();
		return this;
	}

	public ElementLayout setScrollLeft(float scrollLeft) {
		this.scrollLeft = scrollLeft;
		this.updateFields();
		return this;
	}
	// ----------------------------- setter 结束 --------------------------------------

	// ----------------------------- getter -------------------------------------------
	public float getPaddingLeft() {
		return paddingLeft;
	}

	public float getPaddingTop() {
		return paddingTop;
	}

	public float getPaddingRight() {
		return paddingRight;
	}

	public float getPaddingBottom() {
		return paddingBottom;
	}

	public float getBorderLeft() {
		return borderLeft;
	}

	public float getBorderTop() {
		return borderTop;
	}

	public float getBorderRight() {
		return borderRight;
	}

	public float getBorderBottom() {
		return borderBottom;
	}

	public float getContentWidth() {
		return contentWidth;
	}

	public float getContentHeight() {
		return contentHeight;
	}

	public float getMarginLeft() {
		return marginLeft;
	}

	public float getMarginTop() {
		return marginTop;
	}

	public float getMarginRight() {
		return marginRight;
	}

	public float getMarginBottom() {
		return marginBottom;
	}

	public float getLeft() {
		return left;
	}

	public float getTop() {
		return top;
	}

	public Matrix getChildsMatrix() {
		return childsMatrix;
	}

	public Matrix getChildsInvertMatrix() {
		return childsInvertMatrix;
	}

	public float getScrollTop() {
		return scrollTop;
	}

	public float getScrollLeft() {
		return scrollLeft;
	}
}
