package com.github.chengxg.object_canvas;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;

public class ElementBox {
	public Element mountElement;

	public Paint paint;
	public Path borderPath;

	// 各个border圆角半径
	public float borderTopLeftRadius = 0;
	public float borderTopRightRadius = 0;
	public float borderBottomRightRadius = 0;
	public float borderBottomLeftRadius = 0;
	// 各边颜色
	public int borderLeftColor = 0;
	public int borderTopColor = 0;
	public int borderRightColor = 0;
	public int borderBottomColor = 0;
	//背景颜色
	public int backgroundColor = Color.TRANSPARENT;
	// 背景透明度
	public float opacity = 1f;
	//背景图片
	public Bitmap backgroundImage;

	public ElementBox(Element element) {
		this.mountElement = element;
	}

	public ElementBox clone() {
		ElementBox boxStyle = new ElementBox(this.mountElement);
		boxStyle.borderTopLeftRadius = this.borderTopLeftRadius;
		boxStyle.borderTopRightRadius = this.borderTopRightRadius;
		boxStyle.borderBottomRightRadius = this.borderBottomRightRadius;
		boxStyle.borderBottomLeftRadius = this.borderBottomLeftRadius;
		boxStyle.borderLeftColor = this.borderLeftColor;
		boxStyle.borderTopColor = this.borderTopColor;
		boxStyle.borderRightColor = this.borderRightColor;
		boxStyle.borderBottomColor = this.borderBottomColor;
		boxStyle.backgroundColor = this.backgroundColor;
		boxStyle.opacity = this.opacity;
		boxStyle.backgroundImage = this.backgroundImage;
		if (this.paint != null) {
			boxStyle.paint = new Paint(this.paint);
		}
		return boxStyle;
	}

	public ElementBox setBackgroundColor(int backgroundColor) {
		this.backgroundColor = backgroundColor;
		this.backgroundImage = null;
		this.updateFields();
		return this;
	}

	public ElementBox setOpacity(float opacity) {
		if (opacity < 0) {
			opacity = 0;
		}
		if (opacity > 1) {
			opacity = 1;
		}
		this.opacity = opacity;
		this.updateFields();
		return this;
	}

	public ElementBox setBackgroundImage(Bitmap backgroundImage) {
		this.backgroundColor = Color.TRANSPARENT;
		this.backgroundImage = backgroundImage;
		this.updateFields();
		return this;
	}

	public ElementBox setBorderWidth(float borderWidth) {
		mountElement.layout.setBorderWidth(borderWidth);
		return this;
	}

	public ElementBox setBorderColor(int color) {
		borderTopColor = color;
		borderRightColor = color;
		borderBottomColor = color;
		borderLeftColor = color;
		this.updateFields();
		return this;
	}

	public ElementBox setBorderRadius(float radius) {
		borderTopLeftRadius = radius;
		borderTopRightRadius = radius;
		borderBottomRightRadius = radius;
		borderBottomLeftRadius = radius;
		this.updateFields();
		return this;
	}

	public void updateFields() {
		this.mountElement.setUpdateView();
	}

	public Paint getPaint() {
		if (paint == null) {
			paint = new Paint();
		}
		return paint;
	}

	public void drawBoxBorder(Canvas canvas) {
		ElementLayout layout = mountElement.layout;
		if (layout == null) {
			return;
		}
		float width = layout.getWidth();
		float height = layout.getHeight();
		getPaint().setStyle(Paint.Style.STROKE);

		if (layout.borderTop > 0) {
			paint.setColor(borderTopColor);
			paint.setStrokeWidth(layout.borderTop * 2);

			if (borderTopLeftRadius > 0) {
				if (layout.borderLeft != layout.borderTop || borderLeftColor != borderTopColor) {
					canvas.drawArc(0, 0, borderTopLeftRadius * 2, borderTopLeftRadius * 2, 225, 45, false, paint);
				}
			}

			canvas.drawLine(borderTopLeftRadius, 0, width - borderTopRightRadius, 0, paint);

			if (borderTopRightRadius > 0) {
				float sweepAngle = 90;
				if (layout.borderTop != layout.borderRight || borderTopColor != borderRightColor) {
					sweepAngle = 45;
				}
				canvas.drawArc(width - borderTopRightRadius * 2, 0, width, borderTopRightRadius * 2, 270, sweepAngle, false, paint);
			}
		}

		if (layout.borderRight > 0) {
			paint.setColor(borderRightColor);
			paint.setStrokeWidth(layout.borderRight * 2);

			if (borderTopRightRadius > 0) {
				if (layout.borderTop != layout.borderRight || borderTopColor != borderRightColor) {
					canvas.drawArc(width - borderTopRightRadius * 2, 0, width, borderTopRightRadius * 2, -45, 45, false, paint);
				}
			}
			canvas.drawLine(width, borderTopRightRadius, width, height - borderBottomRightRadius, paint);
			if (borderBottomRightRadius > 0) {
				float sweepAngle = 90;
				if (layout.borderRight != layout.borderBottom || borderRightColor != borderBottomColor) {
					sweepAngle = 45;
				}
				canvas.drawArc(width - borderBottomRightRadius * 2, height - borderBottomRightRadius * 2, width, height, 0, sweepAngle, false, paint);
			}
		}

		if (layout.borderBottom > 0) {
			paint.setColor(borderBottomColor);
			paint.setStrokeWidth(layout.borderBottom * 2);

			if (borderBottomRightRadius > 0) {
				if (layout.borderRight != layout.borderBottom || borderRightColor != borderBottomColor) {
					canvas.drawArc(width - borderBottomRightRadius * 2, height - borderBottomRightRadius * 2, width, height, 45, 45, false, paint);
				}
			}
			canvas.drawLine(width - borderBottomRightRadius, height, borderBottomLeftRadius, height, paint);
			if (borderBottomLeftRadius > 0) {
				float sweepAngle = 90;
				if (layout.borderLeft != layout.borderBottom || borderBottomColor != borderLeftColor) {
					sweepAngle = 45;
				}
				canvas.drawArc(0, height - borderBottomLeftRadius * 2, borderBottomLeftRadius * 2, height, 90, sweepAngle, false, paint);
			}
		}

		if (layout.borderLeft > 0) {
			paint.setColor(borderLeftColor);
			paint.setStrokeWidth(layout.borderLeft * 2);

			if (borderBottomLeftRadius > 0) {
				if (layout.borderLeft != layout.borderBottom || borderBottomColor != borderLeftColor) {
					canvas.drawArc(0, height - borderBottomLeftRadius * 2, borderBottomLeftRadius * 2, height, 135, 45, false, paint);
				}
			}

			canvas.drawLine(0, height - borderBottomLeftRadius, 0, borderTopLeftRadius, paint);

			if (borderTopLeftRadius > 0) {
				float sweepAngle = 90;
				if (layout.borderLeft != layout.borderTop || borderLeftColor != borderTopColor) {
					sweepAngle = 45;
				}
				canvas.drawArc(0, 0, borderTopLeftRadius * 2, borderTopLeftRadius * 2, -180, sweepAngle, false, paint);
			}
		}
	}

	public void drawBox(Canvas canvas) {
		boolean isHaveBorder = isHaveBorder();
		boolean isHaveBackground = isHaveBackground();
		boolean isHaveRadius = isHaveRadius();
		boolean isSameBorder = isSameBorder();

		if (!isHaveBorder && !isHaveBackground) {
			return;
		}

		canvas.save();
		canvas.concat(mountElement.transform.getMatrix());

		if (paint == null) {
			paint = new Paint();
		}

		if (isHaveBorder) {
			if (isHaveRadius || isSameBorder) {
				float[] radii = new float[]{borderTopLeftRadius, borderTopLeftRadius, borderTopRightRadius, borderTopRightRadius, borderBottomRightRadius, borderBottomRightRadius, borderBottomLeftRadius, borderBottomLeftRadius};
				if (borderPath == null) {
					borderPath = new Path();
				}
				borderPath.reset();
				borderPath.addRoundRect(0, 0, mountElement.layout.getWidth(), mountElement.layout.getHeight(), radii, Path.Direction.CW);
			}
			if (isHaveRadius) {
				canvas.clipPath(borderPath);
			} else {
				canvas.clipRect(0, 0, mountElement.layout.getWidth(), mountElement.layout.getHeight());
			}
		} else {
			canvas.clipRect(0, 0, mountElement.layout.getWidth(), mountElement.layout.getHeight());
		}

		//画背景
		if (isHaveBackground) {
			if (backgroundImage != null) {
				RectF rect = new RectF(0, 0, mountElement.layout.getWidth(), mountElement.layout.getHeight());
				canvas.drawBitmap(backgroundImage, null, rect, paint);
//				canvas.drawBitmap(backgroundImage, 0, 0, paint);
			} else if (backgroundColor != Color.TRANSPARENT) {
				paint.setStyle(Paint.Style.FILL);
				paint.setColor(backgroundColor);
				paint.setAlpha((int) (opacity * 255));
				canvas.drawRect(0, 0, mountElement.layout.getWidth(), mountElement.layout.getHeight(), paint);
			}
		}

		//画边框
		if (isHaveBorder) {
			if (isSameBorder) {
				paint.setStyle(Paint.Style.STROKE);
				paint.setColor(borderTopColor);
				paint.setStrokeWidth(mountElement.layout.borderTop * 2);
				canvas.drawPath(borderPath, paint);
			} else {
				drawBoxBorder(canvas);
			}
		}

		canvas.restore();
	}

	public boolean isHaveRadius() {
		return borderTopLeftRadius > 0 && borderTopRightRadius > 0 && borderBottomRightRadius > 0 && borderBottomLeftRadius > 0;
	}

	public boolean isSameBorder() {
		ElementLayout layout = mountElement.layout;
		return layout.borderTop == layout.borderRight && layout.borderTop == layout.borderBottom && layout.borderTop == layout.borderLeft && borderTopColor == borderLeftColor && borderTopColor == borderRightColor && borderTopColor == borderBottomColor;
	}

	public boolean isHaveBorder() {
		ElementLayout layout = mountElement.layout;
		return layout.borderTop > 0 || layout.borderRight > 0 || layout.borderBottom > 0 || layout.borderLeft > 0;
	}

	public boolean isHaveBackground() {
		return backgroundColor != Color.TRANSPARENT || backgroundImage != null;
	}

	// --------------- getter/setter ------------------------------------

	public Path getBorderPath() {
		return borderPath;
	}

	public float getBorderTopLeftRadius() {
		return borderTopLeftRadius;
	}

	public float getBorderTopRightRadius() {
		return borderTopRightRadius;
	}

	public float getBorderBottomRightRadius() {
		return borderBottomRightRadius;
	}

	public float getBorderBottomLeftRadius() {
		return borderBottomLeftRadius;
	}

	public int getBorderLeftColor() {
		return borderLeftColor;
	}

	public int getBorderTopColor() {
		return borderTopColor;
	}

	public int getBorderRightColor() {
		return borderRightColor;
	}

	public int getBorderBottomColor() {
		return borderBottomColor;
	}

	public int getBackgroundColor() {
		return backgroundColor;
	}

	public float getOpacity() {
		return opacity;
	}

	public Bitmap getBackgroundImage() {
		return backgroundImage;
	}
}
