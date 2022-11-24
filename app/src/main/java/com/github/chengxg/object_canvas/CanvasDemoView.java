package com.github.chengxg.object_canvas;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.github.chengxg.object_canvas.Anime;
import com.github.chengxg.object_canvas.Body;
import com.github.chengxg.object_canvas.Element;
import com.github.chengxg.object_canvas.Event;
import com.github.chengxg.object_canvas.shape.CicleShape;
import com.github.chengxg.object_canvas.shape.ImageShape;

public class CanvasDemoView extends View {
    public Body root = null;
    public int screenWidth = 0;
    public int screenHeight = 0;
    public float screenWidthDp = 0;
    public float screenHeightDp = 0;

    public CanvasDemoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        DisplayMetrics dm = getResources().getDisplayMetrics();
        screenWidth = dm.widthPixels;
        screenHeight = dm.heightPixels;
        screenWidthDp = screenWidth / dm.density;
        screenHeightDp = screenHeight / dm.density;
        root = new Body(this);
        root.setDensityScale(dm.density);
        root.layout.setContent((float) screenWidthDp, (float) screenHeightDp);
        root.setBox().setBackgroundColor(0xfff2f2f2);
        initPage(root);
    }

    public void initPage(Element parent) {
        //@formatter:off
        Element page = new Element(); parent.addChild(page);
            Element section1 = new Element();page.addChild(section1);
                Element title = new Element();section1.addChild(title);
                Element content = new Element();section1.addChild(content);
                    ImageShape leftCnt = new ImageShape();content.addChild(leftCnt);
                    Element rightCnt = new Element();content.addChild(rightCnt);
                        Element cntTitle = new Element();rightCnt.addChild(cntTitle);
                        Element cntDesc = new Element();rightCnt.addChild(cntDesc);
            Element sectionAnimate = new Element();page.addChild(sectionAnimate);
            Element sectionScroll = new Element();page.addChild(sectionScroll);
        //@formatter:on

        page.getLayout().setPadding(10).setContentMatchParent(0);
        page.setScroll();

        section1.setName("testsection1").getLayout().setBorderWidth(1).setPadding(10).setContentMatchParent(150);
        section1.setBox().setBackgroundColor(Color.WHITE).setBorderColor(0xffcccccc).setBorderRadius(10);

        title.setName("testTitle").getLayout().setPadding(10, 5).setBorderBottom(1).setContentMatchParent(40);
        title.setTextContent("Section1").setColor(0xff333333).setTextSize(16);
        title.setBox().setBorderColor(0xffcccccc);

        content.getLayout().setPadding(5).setContentMatchParent(80).setPosition(0, title.layout.top + title.getLayout().getHeight() + 10);

        Bitmap picIcon = BitmapFactory.decodeResource(this.getResources(), R.drawable.picture_icon);
        leftCnt.getLayout().setContent(48, 48);
        leftCnt.setBitmap(picIcon);

        rightCnt.getLayout().setPosition(60, 0).setContent(content.layout.contentWidth - 60, 60);

        cntTitle.getLayout().setPadding(0, 0).setBorderWidth(1).setContentMatchParent(30);
        cntTitle.setTextContent("标题").setColor(0xff333333).setTextSize(16);
        cntTitle.setBox().setBorderColor(0xffcccccc);

        cntDesc.getLayout().setPadding(0, 0).setBorderWidth(1).setContentMatchParent(24).setPosition(0, 30);
        cntDesc.setTextContent("内容描述").setColor(0xff666666).setTextSize(14);
        cntDesc.setBox().setBorderColor(0xffcccccc);

        // 动画
        sectionAnimate.getLayout().setBorderWidth(1).setPadding(10).setPosition(0, section1.getLayout().getHeight() + section1.getLayout().top + 15).setContentMatchParent(300);
        sectionAnimate.setBox().setBackgroundColor(Color.WHITE).setBorderColor(0xffcccccc).setBorderRadius(10);
        float width = sectionAnimate.getLayout().getWidth();
        float height = sectionAnimate.getLayout().getHeight();
        for (int i = 0; i < 500; i++) {
            CicleShape cicle = new CicleShape();
            sectionAnimate.addChild(cicle);
            int color = ((int) (Math.random() * 0xffffff)) | 0xff000000;
            cicle.setFill(color);
            cicle.setR((float) (30 * Math.random() + 5)).setCenter((float) (width * Math.random()), (float) (height * Math.random()));
            cicle.getParams().put("dir", color % 2 == 0 ? 1 : -1);
        }
        Anime.Instance anime = root.anime.create("{loopCount:0,duration:100,isGoBack:true,easing:'easeInOutQuart',change:null}", null);
        anime.change = (double px, Anime.Instance animate, Anime.PropKeyFrame propKeyFrame) -> {
            if (sectionAnimate.childs != null) {
                for (Element item : sectionAnimate.childs) {
                    CicleShape cicle = (CicleShape) item;
                    int dir = (int) cicle.getParams().get("dir");
                    float cx = cicle.x + dir * (float) (Math.random());
                    float cy = cicle.y + dir * (float) (Math.random());
                    if (cx < 0 || cx > width) {
                        dir = dir * -1;
                    }
                    if (cy < 0 || cy > height) {
                        dir = dir * -1;
                    }
                    cicle.getParams().put("dir", dir);
                    cicle.setCenter(cx, cy);
                }
            }
            root.setUpdateView();
        };
        anime.restart();

        //滚动条
        sectionScroll.getLayout().setBorderWidth(1).setPadding(10).setPosition(0, sectionAnimate.getLayout().getHeight() + sectionAnimate.getLayout().top + 15).setContentMatchParent(300);
        sectionScroll.setBox().setBackgroundColor(Color.WHITE).setBorderColor(0xffcccccc).setBorderRadius(10);
        for (int i = 0; i < 20; i++) {
            //@formatter:off
            Element item = new Element();
            sectionScroll.addChild(item);
            Element text = new Element();
            item.addChild(text);
            //@formatter:on
            float itemHeight = 36;
            item.getLayout().setPadding(5).setBorderWidth(1).setBoxSize(sectionScroll.layout.contentWidth, itemHeight).setPosition(0, (itemHeight + 5) * i + 5);
            item.setBox().setBorderColor(0xffcccccc).setBorderRadius(10);

            text.getLayout().setContentMatchParent(14);
            text.setTextContent("scroll item" + i).setTextSize(14).setColor(Color.GREEN);

            final int idx = i;
            item.setSilent(true).getEvent().on(Event.Touchstart, (Event event) -> {
                event.current.setBox().setBackgroundColor(0xffe0e0e0);
                return false;
            }).on(Event.Touchend, (Event event) -> {
                event.current.setBox().setBackgroundColor(Color.WHITE);
                return false;
            }).on(Event.Click, (Event event) -> {
                Log.d("click", "item" + idx);
                return false;
            });
        }
        sectionScroll.setScroll().updateScrollSize();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        long t = System.currentTimeMillis();
        root.render(canvas);
        long end = System.currentTimeMillis();
        //Log.d("onDraw", (end - t) + "ms");
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        root.dispatchEvent(event);
        return true;
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(screenWidth, screenHeight);
    }
}
