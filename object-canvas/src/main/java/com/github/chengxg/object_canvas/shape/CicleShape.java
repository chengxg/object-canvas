package com.github.chengxg.object_canvas.shape;

import android.graphics.Canvas;
import android.graphics.Paint;

public class CicleShape extends Shape {
	public float x = 0;
	public float y = 0;
	public float r = 0;

	public CicleShape() {
		this.tagName = "Cicle";
		this.layout.setContent(2 * r, 2 * r);
	}

	public void drawShape(Canvas canvas, Paint paint, boolean isFill) {
		canvas.drawCircle(layout.getX() + r, layout.getY() + r, r, paint);
	}

	public CicleShape setR(float r) {
		this.r = r;
		this.layout.setContent(2 * r, 2 * r);
		this.layout.setPosition(x - this.r, y - this.r);
		return this;
	}

	public CicleShape setCenter(float x, float y) {
		this.x = x;
		this.y = y;
		this.layout.setPosition(x - this.r, y - this.r);
		return this;
	}
}
