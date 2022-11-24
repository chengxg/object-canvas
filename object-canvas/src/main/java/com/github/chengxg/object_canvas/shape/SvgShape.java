package com.github.chengxg.object_canvas.shape;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;

import androidx.core.graphics.PathParser;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import java.io.InputStream;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

// TODO
public class SvgShape extends Shape {
    public float scaleX = 1;
    public ArrayList<Data> pathList = new ArrayList<>();
    public int fill;
    public int stroke;

    private static class Data {
        long mFillColor;
        Path mPath;
    }

    public SvgShape() {
        this.tagName = "svg";
        this.setFill(Color.BLACK);
    }

    public void drawShape(Canvas canvas, Paint paint, boolean isFill) {
        if (fillPaint != null) {
            fillPaint.setStyle(Paint.Style.FILL);
            for (Data item : pathList) {
                if (item.mFillColor > 0) {
                    paint.setColor((int) item.mFillColor);
                } else {
                    paint.setColor(fill);
                }
                canvas.drawPath(item.mPath, paint);
            }
        }
        if (strokePaint != null) {
            paint.setStyle(Paint.Style.STROKE);
            paint.setColor((int) stroke);
            for (Data item : pathList) {
                canvas.drawPath(item.mPath, paint);
            }
        }
    }

    public void setScale() {
        float minX = Float.MAX_VALUE;
        float maxX = Float.MIN_VALUE;
        float minY = Float.MAX_VALUE;
        float maxY = Float.MIN_VALUE;

        for (Data item : pathList) {
            RectF rectF = new RectF();
            item.mPath.computeBounds(rectF, true);
            if (rectF.left <= minX) {
                minX = rectF.left;
            }
            if (rectF.right >= maxX) {
                maxX = rectF.right;
            }
            if (rectF.top <= minY) {
                minY = rectF.top;
            }
            if (rectF.bottom >= maxY) {
                maxY = rectF.bottom;
            }
        }

        float currentWidth = maxX - minX;
        float currentHeight = maxY - minY;
        if (layout.contentWidth > 0) {
            scaleX = layout.contentWidth / currentWidth;
        } else {
            scaleX = 1;
            layout.contentWidth = currentWidth;
            layout.contentHeight = currentHeight;
        }

        Matrix matrix = new Matrix();
        matrix.setScale(scaleX, scaleX);
        for (Data item : pathList) {
            item.mPath.transform(matrix);
        }
    }

    public void resolveSvgFile(InputStream is) {
        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document document = builder.parse(is);
            // 得到整个xml的每个节点
            NodeList svgPaths = document.getElementsByTagName("path");
            pathList.clear();
            // 遍历这些子节点
            for (int i = 0; i < svgPaths.getLength(); i++) {
                org.w3c.dom.Element pathNode = (org.w3c.dom.Element) svgPaths.item(i);
                String dStr = pathNode.getAttribute("d");
                if (dStr != null && !dStr.isEmpty()) {
                    Data data = new Data();
                    pathList.add(data);
                    data.mPath = PathParser.createPathFromPathData(dStr);
                    String fillStr = pathNode.getAttribute("fill");
                    if (fillStr != null && !fillStr.isEmpty()) {
                        data.mFillColor = 0x00FFFFFFFFL & Color.parseColor(fillStr);
                    }
                }
            }

            setScale();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}