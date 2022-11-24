package com.github.chengxg.object_canvas.shape;

import android.graphics.Canvas;
import android.graphics.Paint;

import com.github.chengxg.object_canvas.Element;

public class Shape extends Element {
	public Paint strokePaint = null;
	public Paint fillPaint = null;
	public DrawShapeCallback drawShapeCallback;

	public interface DrawShapeCallback {
		public void callback(Canvas canvas, Paint paint, boolean isFill);
	}

	public void render(Canvas canvas) {
		this.renderBox(canvas);
		this.renderShape(canvas);
		this.renderChilds(canvas);
		this.renderAfter(canvas);
	}

	public void renderShape(Canvas canvas) {
		if (strokePaint != null || fillPaint != null) {
			canvas.save();
			canvas.concat(this.transform.getMatrix());
		}
		if (strokePaint != null && strokePaint.getStrokeWidth() > 0) {
			if (this.drawShapeCallback != null) {
				this.drawShapeCallback.callback(canvas, strokePaint, false);
			} else {
				this.drawShape(canvas, strokePaint, false);
			}
		}
		if (fillPaint != null) {
			if (this.drawShapeCallback != null) {
				this.drawShapeCallback.callback(canvas, fillPaint, true);
			} else {
				this.drawShape(canvas, fillPaint, true);
			}
		}
		if (strokePaint != null || fillPaint != null) {
			canvas.restore();
		}
	}

	// 由子类实现
	public void drawShape(Canvas canvas, Paint paint, boolean isFill) {

	}

	// ---------------------- getter/setter ------
	public Shape setDrawShapeCallback(DrawShapeCallback drawShapeCallback) {
		this.drawShapeCallback = drawShapeCallback;
		return this;
	}

	public Paint getStrokePaint() {
		if (strokePaint == null) {
			strokePaint = new Paint();
			strokePaint.setStyle(Paint.Style.STROKE);
		}
		this.setUpdateView();
		return strokePaint;
	}

	public Paint getFillPaint() {
		if (fillPaint == null) {
			fillPaint = new Paint();
			fillPaint.setStyle(Paint.Style.FILL);
		}
		this.setUpdateView();
		return fillPaint;
	}

	public Shape setFill(int fillColor) {
		this.getFillPaint().setColor(fillColor);
		return this;
	}

	public Shape setStroke(int strokeColor) {
		this.getStrokePaint().setColor(strokeColor);
		return this;
	}

	public Shape setStrokeWidth(float strokeWidth) {
		this.getStrokePaint().setStrokeWidth(strokeWidth);
		return this;
	}

	public Shape setOpacity(float opacity) {
		if (opacity < 0) {
			opacity = 0;
		}
		if (opacity > 1) {
			opacity = 1;
		}
		this.getFillPaint().setAlpha((int) (opacity * 255));
		return this;
	}

}
