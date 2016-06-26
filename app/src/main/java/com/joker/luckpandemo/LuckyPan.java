package com.joker.luckpandemo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Created by Joker on 2016/6/26.
 */
public class LuckyPan extends SurfaceView implements SurfaceHolder.Callback, Runnable {

    private static final String TAG = "LuckyPan";
    private SurfaceHolder mHolder;
    private Canvas mCanvas;

    /**
     * 转盘奖项
     */
    private String[] mStrings = new String[]{"单反相机", "IPAD", "恭喜发财", "IPHONE", "服装一套", "恭喜发财"};
    /**
     * 转盘交替颜色
     */
    private int[] mColors = new int[]{0xFFFFC300, 0xFFF17E01, 0xFFFFC300, 0xFFF17E01, 0xFFFFC300, 0xFFF17E01};

    private int mItemCount = 6;
    /**
     * 与图片对应的bitmap
     */
    private Bitmap[] mImgsBitmap;

    /**
     * 整个盘快的范围
     */
    private RectF mRange = new RectF();
    /**
     * 整个盘快的直径
     */
    private int mRadius;
    /**
     * 绘制盘快的画笔
     */
    private Paint mArcPaint;
    /**
     * 绘制文本的画笔
     */
    private Paint mTextPaint;
    /**
     * 盘快滚动的速度
     */
    private double mSpeed;
    /**
     * 开始角度
     */
    private volatile int mStartAngle = 0;
    /**
     * 判断是否点击了停止按钮
     */
    private boolean isShouldEnd;
    /**
     * 转盘的中心位置
     */
    private int mCenter;

    private int mPadding;
    /**
     * 背景图片
     */
    private Bitmap mBgBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.game_bg);

    private float mTextSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 20, getResources().getDisplayMetrics());


    /**
     * 用于绘制的线程
     */
    private Thread t;

    /**
     * 线程开启的标志位
     */
    private boolean isRunning;

    public LuckyPan(Context context) {
        this(context, null);
    }

    public LuckyPan(Context context, AttributeSet attrs) {
        super(context, attrs);
        // 对holer和canvas等的初始化
        mHolder = getHolder();
        mHolder.addCallback(this);

        // 可获得焦点
        setFocusable(true);
        setFocusableInTouchMode(true);
        // 设置常亮
        setKeepScreenOn(true);
    }

    public LuckyPan(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int width = Math.min(getMeasuredWidth(), getMeasuredHeight());
        Log.e(TAG, "width: " + width + "");
        // 设置半径
        mPadding = getPaddingLeft();
        mRadius = width - mPadding*2;
        // 设置圆心
        mCenter = mRadius / 2;
        Log.e(TAG, "mCenter: " + mCenter + "");
        setMeasuredDimension(width, width);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

        // 初始化画笔
        mArcPaint = new Paint();
        mArcPaint.setAntiAlias(true);
        mArcPaint.setDither(true);

        mTextPaint = new Paint();
        mTextPaint.setColor(0xffffffff);
        mTextPaint.setTextSize(mTextSize);

        // 初始化盘快绘制的范围
        mRange = new RectF(mPadding, mPadding, mPadding + mRadius, mPadding + mRadius);

        // 初始化图片
        mImgsBitmap = new Bitmap[mItemCount];




        // 开启子线程
        isRunning = true;
        t = new Thread(this);
        t.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // 销毁线程
        isRunning = false;
    }

    @Override
    public void run() {

        // 不断进行绘制
        while (isRunning) {
            long start = System.currentTimeMillis();
            draw();
            long end = System.currentTimeMillis();
            if (end - start < 50){
                SystemClock.sleep(50 - (end - start));
            }
        }
    }

    private void draw() {
        try {
            mCanvas = mHolder.lockCanvas();
            if (mCanvas != null) {
                // 绘制背景
                drawBg();
                // 绘制盘块
                float tempAngle = mStartAngle;
                float sweepAngle = 360 / mItemCount;

                for (int i = 0; i < mItemCount; i++){
                    mArcPaint.setColor(mColors[i]);
                    // 绘制盘块
                    mCanvas.drawArc(mRange, tempAngle, sweepAngle, true, mArcPaint);

                    // 绘制文本
                    drawText(tempAngle, sweepAngle, mStrings[i]);
                    tempAngle += sweepAngle;
                }
                mStartAngle += mSpeed;

                // 点击了停止按钮
                if (isShouldEnd){
                    mSpeed -= 1;
                }
                if (mSpeed <= 0){
                    mSpeed = 0;
                    isShouldEnd = false;
                }
            }
        } catch (Exception e) {
        } finally {
            if (mCanvas != null) {
                mHolder.unlockCanvasAndPost(mCanvas);
            }
        }
    }

    /**
     * 点击启动旋转
     */
    public void luckyStart(){
        mSpeed = 50;
        isShouldEnd = false;
    }

    public void luckyEnd(){
        isShouldEnd = true;
    }

    /**
     * 转盘是否在旋转
     * @return
     */
    public boolean isStart(){
        return mSpeed != 0;
    }

    /**
     * 是否停止
     * @return
     */
    public boolean isShouldEnd(){
        return isShouldEnd;
    }

    /**
     * 绘制文本内容
     * @param tempAngle
     * @param sweepAngle
     * @param string
     */
    private void drawText(float tempAngle, float sweepAngle, String string) {
        Path path = new Path();
        path.addArc(mRange, tempAngle, sweepAngle);

        // 利用水平偏移量让文字居中
        float textWidth = mTextPaint.measureText(string);
        int hOffset = (int) (mRadius * Math.PI / mItemCount / 2 - textWidth / 2);
        // 垂直偏移量
        int vOffset = mRadius / 2 / 6;

        mCanvas.drawTextOnPath(string, path, hOffset, vOffset, mTextPaint);

    }

    /**
     * 绘制背景
     */
    private void drawBg() {
        mCanvas.drawColor(0xFFFFFFFF);
        mCanvas.drawBitmap(mBgBitmap,
                null,
                new Rect(mPadding/2, mPadding/2, getMeasuredWidth() - mPadding/2, getMeasuredHeight() - mPadding / 2),
                null);

    }
}
