package com.github.chengxg.object_canvas;

import android.graphics.Canvas;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextDirectionHeuristic;
import android.text.TextDirectionHeuristics;
import android.text.TextPaint;

public class ElementText {
	public Element mountElement;
	public TextPaint textPaint;
	public boolean isUpdateLayout = true;
	public boolean isMatchHeight = true;
	public String text;

	public Layout.Alignment alignment = Layout.Alignment.ALIGN_NORMAL;
	public TextDirectionHeuristic textDir = TextDirectionHeuristics.FIRSTSTRONG_LTR;
	public float spacingMult = 1.0f;
	public float spacingAdd = 0;
	public boolean includepad = false;
	public StaticLayout.Builder layoutBuilder;
	public StaticLayout textLayout;

	public ElementText(Element element) {
		this.mountElement = element;
	}

	public ElementText clone() {
		ElementText elementText = new ElementText(mountElement);
		elementText.isUpdateLayout = true;
		elementText.isMatchHeight = this.isMatchHeight;
		elementText.text = this.text;
		elementText.alignment = this.alignment;
		elementText.textDir = this.textDir;
		elementText.spacingMult = this.spacingMult;
		elementText.spacingAdd = this.spacingAdd;
		elementText.includepad = this.includepad;
		if (this.textPaint != null) {
			elementText.textPaint = new TextPaint(this.textPaint);
		}
		return elementText;
	}

	public void drawText(Canvas canvas) {
		updateStaticLayout();
		if (text != null && !text.isEmpty() && textLayout != null) {
			canvas.save();
			canvas.concat(mountElement.transform.getMatrix());
			canvas.translate(mountElement.layout.getX(), mountElement.layout.getY());
			textLayout.draw(canvas);
			canvas.restore();
		}
	}

	public ElementText setColor(int color) {
		getTextPaint().setColor(color);
		return this;
	}

	public ElementText setTextSize(float textSize) {
		getTextPaint().setTextSize(textSize);
		return this;
	}

	public TextPaint getTextPaint() {
		if (textPaint == null) {
			textPaint = new TextPaint();
		}
		this.updateFields();
		return textPaint;
	}

	public void updateStaticLayout() {
		if (isUpdateLayout && text != null) {
			layoutBuilder = StaticLayout.Builder.obtain(text, 0, text.length(), getTextPaint(), (int) mountElement.layout.contentWidth).setAlignment(alignment).setLineSpacing(spacingAdd, spacingMult).setIncludePad(false);
			textLayout = layoutBuilder.build();
			if (isMatchHeight) {
				mountElement.layout.setContentHeight(textLayout.getHeight());
			}
		} else {
			textLayout = null;
		}
	}

	public void updateFields() {
		this.isUpdateLayout = true;
		this.mountElement.setUpdateView();
	}

	public ElementText setMatchHeight(boolean isMatchHeight) {
		this.isMatchHeight = isMatchHeight;
		this.updateFields();
		return this;
	}

	public ElementText setText(String text) {
		this.text = text;
		this.updateFields();
		return this;
	}

	public ElementText setAlignment(Layout.Alignment alignment) {
		this.alignment = alignment;
		this.updateFields();
		return this;
	}

	public ElementText setIncludePad(boolean includePad) {
		this.includepad = includePad;
		this.updateFields();
		return this;
	}

	public ElementText setLineSpacing(float spacingAdd, float spacingMult) {
		this.spacingAdd = spacingAdd;
		this.spacingMult = spacingMult;
		this.updateFields();
		return this;
	}

	public ElementText setTextDirection(TextDirectionHeuristic textDir) {
		this.textDir = textDir;
		this.updateFields();
		return this;
	}

	// --------------- getter ------------------------------------
	public boolean isUpdateLayout() {
		return isUpdateLayout;
	}

	public boolean isMatchHeight() {
		return isMatchHeight;
	}

	public String getText() {
		return text;
	}

	public Layout.Alignment getAlignment() {
		return alignment;
	}

	public TextDirectionHeuristic getTextDir() {
		return textDir;
	}

	public float getSpacingMult() {
		return spacingMult;
	}

	public float getSpacingAdd() {
		return spacingAdd;
	}

	public boolean isIncludepad() {
		return includepad;
	}

	public StaticLayout.Builder getLayoutBuilder() {
		return layoutBuilder;
	}

	public StaticLayout getTextLayout() {
		return textLayout;
	}
}
