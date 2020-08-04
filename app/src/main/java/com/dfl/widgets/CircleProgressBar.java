package com.dfl.widgets;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.SweepGradient;
import android.util.AttributeSet;
import android.view.View;

public class CircleProgressBar extends View {

    private int progress = 0;
    private int maxProgress = 100;

    //绘制的Paint
    private Paint pathPaint = null; //绘制轨迹
    private Paint fillPaint = null; //绘制填充

    //绘制的矩形区域
    private RectF oval;

    //梯度渐变的填充颜色
    private int[] arcColors ={0xFF02C016, 0xFF3DF346, 0xFF40F1D5, 0xFF02C016};
    //    private int[] shadowsColors = new int[]{0xFF111111, 0x00AAAAAA, 0x00AAAAAA};

    //灰色轨迹 背景灰色
    private int pathColor = 0xFFF0EEDF;
    private int borderColor = 0xFFD2D1C4;   //边框灰色

    //环的路径宽度
    private int pathWidth = 35;
    private int width;
    private int height;

    //默认圆的半径
    private int radius = 120;

    private SweepGradient sweepGradient;
    private boolean reset = false;

    // 指定了光源的方向和环境光强度来添加浮雕效果
//    private EmbossMaskFilter emboss = null;
    // 设置光源的方向
//    float[] direction = new float[]{1, 1, 1};
    //设置环境光亮度
//    float light = 0.4f;
    // 选择要应用的反射等级
//    float specular = 6;
    // 向 mask应用一定级别的模糊
//    float blur = 3.5f;

    //指定了一个模糊的样式和半径来处理 Paint 的边缘
//    private BlurMaskFilter mBlur = null;
//    private OnCircleProgressListener mAbOnProgressListener = null;


    public CircleProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);

        pathPaint = new Paint();
        pathPaint.setAntiAlias(true);    // 设置是否抗锯齿
        pathPaint.setFlags(Paint.ANTI_ALIAS_FLAG);  // 帮助消除锯齿
        // 设置中空的样式
        pathPaint.setStyle(Paint.Style.STROKE);
        pathPaint.setDither(true);
        pathPaint.setStrokeJoin(Paint.Join.ROUND);

        fillPaint = new Paint();
        fillPaint.setAntiAlias(true);   // 设置是否抗锯齿
        fillPaint.setFlags(Paint.ANTI_ALIAS_FLAG);  // 帮助消除锯齿
        // 设置中空的样式
        fillPaint.setStyle(Paint.Style.STROKE);
        fillPaint.setDither(true);
        fillPaint.setStrokeJoin(Paint.Join.ROUND);

        oval = new RectF();
        //this.emboss = new EmbossMaskFilter(this.direction, this.light, this.specular, this.blur);
        //this.mBlur = new BlurMaskFilter(20.0F, BlurMaskFilter.Blur.NORMAL);
        sweepGradient = new SweepGradient((float) (this.width / 2), (float) (this.height / 2), this.arcColors, (float[]) null);
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
        invalidate();
    }

    public int getMaxProgress() {
        return maxProgress;
    }

    public void setMaxProgress(int maxProgress) {
        this.maxProgress = maxProgress;
        invalidate();
    }

    public int getPathColor() {
        return pathColor;
    }

    public void setPathColor(int pathColor) {
        this.pathColor = pathColor;
    }

    public int getBorderColor() {
        return borderColor;
    }

    public void setBorderColor(int borderColor) {
        this.borderColor = borderColor;
    }

    public int getPathWidth() {
        return pathWidth;
    }

    public void setPathWidth(int pathWidth) {
        this.pathWidth = pathWidth;
    }

    public void setReset(boolean reset) {
        this.reset = reset;
        if(reset){
            progress=0;
            invalidate();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (reset) {
            canvas.drawColor(0xFFFFFFFF);
            reset = false;
        }

        width = getMeasuredWidth();
        height = getMeasuredHeight();
        radius = getMeasuredWidth() / 2 - pathWidth;
        //画笔颜色
        pathPaint.setColor(pathColor);
        //设置画笔宽度
        pathPaint.setStrokeWidth(pathWidth);
        //添加浮雕效果
//        pathPaint.setMaskFilter(emboss);
        // 在中心的地方画个半径为r的圆  绘制背景
        canvas.drawCircle((width / 2),(height / 2),radius,pathPaint);
        pathPaint.setStrokeWidth(0.5F);
        pathPaint.setColor(borderColor);
        canvas.drawCircle((width / 2),(height / 2), (float)(radius + pathWidth / 2) + 0.5F, pathPaint);
        canvas.drawCircle( (width / 2), (height / 2),  (float)(radius - pathWidth / 2) - 0.5F, pathPaint);
        sweepGradient = new SweepGradient((float)(width / 2),(float)(height / 2),arcColors,null);
        fillPaint.setShader(sweepGradient);
//        fillPaint.setMaskFilter(this.mBlur);
        fillPaint.setStrokeCap(Paint.Cap.ROUND);
        fillPaint.setStrokeWidth(pathWidth);
        oval.set((width / 2 - radius), (height / 2 - radius), (width / 2 + radius),  (height / 2 + radius));
        canvas.drawArc(oval, -90.0F,  ((float)progress / (float)maxProgress) * 360.0F, false,fillPaint);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int height = MeasureSpec.getSize(heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        setMeasuredDimension(width, height);
    }
}
