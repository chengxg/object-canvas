package com.github.chengxg.object_canvas.shape;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

public class ImageShape extends Shape {
	public Bitmap bitmap;

	public ImageShape() {
		super.tagName = "Image";
	}

	public void drawShape(Canvas canvas, Paint paint, boolean isFill) {
		RectF rect = new RectF(layout.getX(), layout.getY(), layout.contentWidth, layout.contentHeight);
		canvas.drawBitmap(bitmap, null, rect, paint);
	}

	public ImageShape setBitmap(Bitmap bitmap) {
		this.bitmap = bitmap;
		this.setFill(Color.BLACK);
		return this;
	}

}

