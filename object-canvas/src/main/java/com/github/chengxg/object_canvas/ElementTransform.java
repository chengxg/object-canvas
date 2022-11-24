package com.github.chengxg.object_canvas;

import android.graphics.Matrix;

import java.util.ArrayList;

public class ElementTransform {
	public static final byte Translate = 0;
	public static final byte Rotate = 1;
	public static final byte Scale = 2;
	public static final byte Skew = 3;
	// 默认变换顺序
	public static final int defaultTransformOrder = Translate << 6 | Rotate << 4 | Scale << 2 | Skew;

	public boolean isUpdateMatrix = true;//是否更新矩阵
	private Matrix matrix = new Matrix();//当前节点的矩阵
	private Matrix invertMatrix = new Matrix();//当前节点的逆矩阵

	public float positionX = 0;
	public float positionY = 0;
	public float rotate = 0;
	public float scaleX = 1;
	public float scaleY = 1;
	public float skewX = 0;
	public float skewY = 0;
	//rotate scale skew 变换点
	public float originX = 0;
	public float originY = 0;
	// 变换顺序
	public int transformOrder = defaultTransformOrder;

	private final Element mountElement;

	public ElementTransform(Element element) {
		this.mountElement = element;
	}

	public void clone(ElementTransform transform) {
		transform.positionX = this.positionX;
		transform.positionY = this.positionY;
		transform.rotate = this.rotate;
		transform.scaleX = this.scaleX;
		transform.scaleY = this.scaleY;
		transform.skewX = this.skewX;
		transform.skewY = this.skewY;
		transform.originX = this.originX;
		transform.originY = this.originY;
		transform.isUpdateMatrix = true;
		transform.matrix = new Matrix(this.matrix);
		transform.invertMatrix = new Matrix(this.invertMatrix);
	}

	public ElementTransform setOrigin(float originX, float originY) {
		this.originX = originX;
		this.originY = originY;
		this.isUpdateMatrix = true;
		this.mountElement.setUpdateView();
		return this;
	}

	public ElementTransform setTranslate(float positionX, float positionY) {
		this.positionX = positionX;
		this.positionY = positionY;
		this.isUpdateMatrix = true;
		this.mountElement.setUpdateView();
		return this;
	}

	public ElementTransform setRotate(float rotate) {
		this.rotate = rotate;
		this.isUpdateMatrix = true;
		this.mountElement.setUpdateView();
		return this;
	}

	public ElementTransform setScale(float scaleX, float scaleY) {
		this.scaleX = scaleX;
		this.scaleY = scaleY;
		this.isUpdateMatrix = true;
		this.mountElement.setUpdateView();
		return this;
	}

	public ElementTransform setSkew(float skewX, float skewY) {
		this.skewX = skewX;
		this.skewY = skewY;
		this.isUpdateMatrix = true;
		this.mountElement.setUpdateView();
		return this;
	}

	// 设置变换的顺序
	public ElementTransform setOrder(int o1, int o2, int o3, int o4) {
		this.transformOrder = o1 << 6 | o2 << 4 | o3 << 2 | o4;
		this.isUpdateMatrix = true;
		this.mountElement.setUpdateView();
		return this;
	}

	// 设置平移 和 旋转的次序
	public ElementTransform setOrder(int o1, int o2) {
		this.transformOrder = o1 << 6 | o2 << 4 | Scale << 2 | Skew;
		this.isUpdateMatrix = true;
		this.mountElement.setUpdateView();
		return this;
	}

	public Matrix getMatrix() {
		if (!isUpdateMatrix) {
			return matrix;
		}
		return updateMatrix();
	}

	public Matrix getInvertMatrix() {
		if (!isUpdateMatrix) {
			return invertMatrix;
		}
		updateMatrix();
		return invertMatrix;
	}

	public Matrix updateMatrix() {
		matrix.reset();
		invertMatrix.reset();
		setMatrix((transformOrder & 0b11000000) >> 6);
		setMatrix((transformOrder & 0b00110000) >> 4);
		setMatrix((transformOrder & 0b00001100) >> 2);
		setMatrix(transformOrder & 0b00000011);
		matrix.invert(invertMatrix);
		isUpdateMatrix = false;
		return matrix;
	}

	public void setMatrix(int order) {
		float left = mountElement.layout.left;
		float top = mountElement.layout.top;
		switch (order) {
			case Translate:
				matrix.postTranslate(positionX + left, positionY + top);
				break;
			case Rotate:
				if (rotate != 0) {
					matrix.postRotate(rotate, originX + left, originY + top);
				}
				break;
			case Scale:
				if (scaleX != 1 || scaleY != 1) {
					matrix.postScale(scaleX, scaleY, originX + left, originY + top);
				}
				break;
			case Skew:
				if (skewX != 0 || skewY != 0) {
					matrix.postSkew(skewX, skewY, originX + left, originY + top);
				}
				break;
		}
	}

	// 获取局部转全局 P1 = (M1*(M2*(M3*P0)))
	public Matrix getRootMatrix() {
		Matrix rootMatrix = new Matrix();
		Element node = mountElement;
		while (node != null) {
			rootMatrix.postConcat(node.transform.getMatrix());
			if (node.parent != null && node.parent.layout.childsMatrix != null) {
				rootMatrix.postConcat(node.parent.layout.childsMatrix);
			}
			node = node.parent;
		}
		return rootMatrix;
	}

	// 获取全局转局部 P0 = P1*M1¹*M2¹*M3¹
	public Matrix getRootInvertMatrix() {
		Matrix rootInvertMatrix = new Matrix();
		Element node = mountElement;
		while (node != null) {
			Matrix m_invert = new Matrix();
			node.transform.getMatrix().invert(m_invert);
			rootInvertMatrix.preConcat(m_invert);
			if (node.parent != null && node.parent.layout.childsMatrix != null) {
				node.parent.layout.childsMatrix.invert(m_invert);
				rootInvertMatrix.preConcat(m_invert);
			}
			node = node.parent;
		}
		return rootInvertMatrix;
	}

	// 获取全局转局部方法2 P0 = (M1¹*(M2¹*(M3¹*P1)))
	public Matrix getRootInvertMatrix2() {
		Matrix rootInvertMatrix = new Matrix();
		Element node = mountElement;
		ArrayList<Element> arr = new ArrayList<Element>();
		while (node != null) {
			arr.add(node);
			node = node.parent;
		}
		int size = arr.size();
		for (int i = size - 1; i >= 0; i--) {
			node = arr.get(i);
			if (node.parent != null && node.parent.layout.childsInvertMatrix != null) {
				rootInvertMatrix.postConcat(node.parent.layout.childsInvertMatrix);
			}
			rootInvertMatrix.postConcat(node.transform.invertMatrix);
		}
		return rootInvertMatrix;
	}

	public float[] globleToLocal(float x, float y) {
		float[] src = {x, y};
		float[] dst = {0, 0};
		getRootInvertMatrix().mapPoints(dst, src);
		return dst;
	}

	public float[] localToGloble(float x, float y) {
		float[] src = {x, y};
		float[] dst = {0, 0};
		getRootMatrix().mapPoints(dst, src);
		return dst;
	}

	// --------------- getter ------------------------------------

	public boolean isUpdateMatrix() {
		return isUpdateMatrix;
	}

	public float getPositionX() {
		return positionX;
	}

	public float getPositionY() {
		return positionY;
	}

	public float getRotate() {
		return rotate;
	}

	public float getScaleX() {
		return scaleX;
	}

	public float getScaleY() {
		return scaleY;
	}

	public float getSkewX() {
		return skewX;
	}

	public float getSkewY() {
		return skewY;
	}

	public float getOriginX() {
		return originX;
	}

	public float getOriginY() {
		return originY;
	}

	public int getTransformOrder() {
		return transformOrder;
	}
}
