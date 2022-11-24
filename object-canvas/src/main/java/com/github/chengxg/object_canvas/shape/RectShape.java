package com.github.chengxg.object_canvas.shape;

import android.graphics.Canvas;
import android.graphics.Paint;

public class RectShape extends Shape {
	public RectShape() {
		this.tagName = "Rect";
	}

	public void drawShape(Canvas canvas, Paint paint, boolean isFill) {
		float x = layout.getX();
		float y = layout.getY();
		canvas.drawRect(x, y, layout.contentWidth + x, layout.contentHeight + y, paint);
	}

}
