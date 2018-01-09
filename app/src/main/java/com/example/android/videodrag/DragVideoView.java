
package com.example.android.videodrag;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

import java.lang.ref.WeakReference;


public class DragVideoView extends LinearLayout {
    private static final String TAG = "DragVideoView";

    private static final float SCALE_RATIO = 0.2f; //缩放比例

    private float VIDEO_RATIO = 16f / 9f; //默认宽高比，实际会根据视频的实际宽高比重新计算。

    private ViewDragHelper mDragHelper;

    private View mVideoView;

    private boolean mIsFinishInit = false;

    private boolean mIsMinimum = true;

    private int mVerticalRange; //垂直拖动范围

    private int mHorizontalRange;//横向拖动范围

    private int mFullScreenTop; //全屏时top坐标

    private int mFullScreenLeft;//全屏时left坐标

    private int mTop;//移动过程中的top值

    private int mLeft;//移动过程中的left值

    private int mPlayerMaxWidth;//全屏时视频宽度

    private float mVerticalOffset = 1f;//垂直方向变化百分比

    private WeakReference<Callback> mCallback;

    /*初始时位置坐标*/
    private int mStartLeft = 0;
    private int mStartTop = 0;
    private int mStartWidth = 0;
    private int mStartHeight = 0;

    private Context mContext;


    public DragVideoView(Context context) {
        this(context, null);
    }

    public DragVideoView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DragVideoView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        mDragHelper = ViewDragHelper.create(this, 1f, new DragHelperCallback());
        setBackgroundColor(Color.BLACK);
        mContext = context;
    }

    public void restorePosition() {
        this.setAlpha(0f);
        mIsMinimum = true;
        mVerticalOffset = 1f;
    }

    public void show(int mediaWidth, int mediaHeight, int left, int top, int width, int height, float rotateAngle) {
        mStartLeft = left;
        mStartTop = top;
        mStartWidth = width;
        mStartHeight = height;
        float mediaAspectRatio = mediaWidth * 1.0f / mediaHeight; //计算视频实际宽高比
        if (rotateAngle == 90 || rotateAngle == 270) {  //旋转的视频根据旋转角度计算宽高比
            mediaAspectRatio = mediaHeight * 1.0f / mediaWidth;
        }
        float viewAspectRatio = getWidth() * 1.0f / getHeight();
        VIDEO_RATIO = mediaAspectRatio;
        if (mediaAspectRatio >= viewAspectRatio) {//根据宽高比初始化变量
            mFullScreenTop = getPaddingTop() + (int) (getHeight() - (getWidth() / VIDEO_RATIO)) / 2;
            mFullScreenLeft = getPaddingLeft();
            mPlayerMaxWidth = getWidth();
        } else {
            mFullScreenTop = getPaddingTop();
            mFullScreenLeft = getPaddingLeft() + (int) (getWidth() - (getHeight() * VIDEO_RATIO)) / 2;
            mPlayerMaxWidth = (int) (getHeight() * VIDEO_RATIO);
        }

        mVideoView.layout(mStartLeft, mStartTop, mStartLeft + mStartWidth, mStartTop + mStartHeight);//初始化layout
        this.setAlpha(1f);
        animateToFullScreen();//从缩略图放大到全屏的动画
    }

    private GestureDetector mGestureDetector = new GestureDetector(mContext, new GestureDetector.OnGestureListener() {
        @Override
        public boolean onDown(MotionEvent e) {
            return false;
        }

        @Override
        public void onShowPress(MotionEvent e) {

        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            Log.i(TAG, "onSingleTapUp");
            animateToOriginPosition();
            return false;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            return false;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            Log.i(TAG, "onLongPress");
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            return false;
        }
    });

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        return mDragHelper.shouldInterceptTouchEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean isHit = mDragHelper.isViewUnder(mVideoView, (int) event.getX(), (int) event.getY());
        mGestureDetector.onTouchEvent(event); //接收点击事件，单击回到原界面。
        mDragHelper.processTouchEvent(event); //交给ViewDragHelper处理
        return isHit;
    }

    private void maximize() {
        Log.i(TAG, "maximize");
        mIsMinimum = false;
        slideVerticalTo(0f);
    }

    private boolean slideVerticalTo(float slideOffset) {//滑动到垂直方向上某位置
        int topBound = mFullScreenTop;
        int y = (int) (topBound + slideOffset * mVerticalRange);
        if (mDragHelper.smoothSlideViewTo(mVideoView, mIsMinimum ?
                (int) (mPlayerMaxWidth * (1 - SCALE_RATIO)) : getPaddingLeft() + mFullScreenLeft, y)) {
            ViewCompat.postInvalidateOnAnimation(this);
            return true;
        }
        return false;
    }

    private void animateToOriginPosition() {
        mVideoView.setPivotX(0);
        mVideoView.setPivotY(0);
        int width = mVideoView.getWidth();
        int height = mVideoView.getHeight();
        ObjectAnimator anim1 = ObjectAnimator.ofFloat(mVideoView, "translationX", 0, mStartLeft - mVideoView.getX());
        ObjectAnimator anim2 = ObjectAnimator.ofFloat(mVideoView, "translationY", 0, mStartTop - mVideoView.getY());
        ObjectAnimator anim3 = ObjectAnimator.ofFloat(mVideoView, "scaleX", mVideoView.getScaleX(), mStartWidth * 1.0f /
                width);
        ObjectAnimator anim4 = ObjectAnimator.ofFloat(mVideoView, "scaleY", mVideoView.getScaleY(), mStartHeight *
                1.0f / height);
        AnimatorSet animSet = new AnimatorSet();
        animSet.play(anim1).with(anim2).with(anim3).with(anim4);
        animSet.setDuration(300);
        animSet.start();
        int alpha = 0;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            alpha = getBackground().getAlpha();
        }
        ValueAnimator valueAnimator = ValueAnimator.ofInt(alpha, 0);
        valueAnimator.setDuration(300);
        valueAnimator.start();
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                getBackground().setAlpha((int) animation.getAnimatedValue());
            }
        });
        animSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mCallback.get().onVideoDisappear();
                mVideoView.clearAnimation();
                mVideoView.setTranslationX(0);
                mVideoView.setTranslationY(0);
                mVideoView.setScaleX(1f);
                mVideoView.setScaleY(1f);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
    }

    private void animateToFullScreen() {
        mVideoView.setPivotX(0);
        mVideoView.setPivotY(0);
        int width = mVideoView.getWidth();
        int height = mVideoView.getHeight();
        ObjectAnimator anim1 = ObjectAnimator.ofFloat(mVideoView, "translationX", 0, mFullScreenLeft - mStartLeft);
        ObjectAnimator anim2 = ObjectAnimator.ofFloat(mVideoView, "translationY", 0, mFullScreenTop - mStartTop);
        ObjectAnimator anim3 = ObjectAnimator.ofFloat(mVideoView, "scaleX", mVideoView.getScaleX(), mPlayerMaxWidth *
                1.0f / width);
        ObjectAnimator anim4 = ObjectAnimator.ofFloat(mVideoView, "scaleY", mVideoView.getScaleY(), mPlayerMaxWidth *
                1.0f / VIDEO_RATIO / height);
        AnimatorSet animSet = new AnimatorSet();
        animSet.play(anim1).with(anim2).with(anim3).with(anim4);
        animSet.setDuration(300);
        animSet.start();

        ValueAnimator valueAnimator = ValueAnimator.ofInt(0, 255);
        valueAnimator.setDuration(300);
        valueAnimator.start();
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                getBackground().setAlpha((int) animation.getAnimatedValue());
            }
        });
        animSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mVideoView.clearAnimation();
                mVideoView.setTranslationX(0);
                mVideoView.setTranslationY(0);
                mVideoView.setScaleX(1f);
                mVideoView.setScaleY(1f);
                mVerticalOffset = 0;
                mTop = mFullScreenTop;
                mLeft = mFullScreenLeft;
                requestLayoutLightly(); //动画结束后重绘layout，之后借助ViewDragHelper实现拖拽变化
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
    }

    private class DragHelperCallback extends ViewDragHelper.Callback {
        @Override
        public boolean tryCaptureView(View child, int pointerId) {//当前view是否允许拖动
            if (child != mVideoView) {
                Log.e(TAG, "child is not video view");
                return false;
            } else {
                return true;
            }
        }

        @Override
        public void onViewDragStateChanged(int state) { //当ViewDragHelper状态发生变化时回调（IDLE,DRAGGING,SETTING[自动滚动时]）
            Log.i(TAG, "onViewDragStateChanged=" + state);
        }

        @Override
        public int getViewVerticalDragRange(View child) { //垂直方向拖动的最大距离
            return mVerticalRange;
        }

        @Override
        public int getViewHorizontalDragRange(View child) { //横向拖动的最大距离
            return mHorizontalRange;
        }

        @Override
        public int clampViewPositionVertical(View child, int top, int dy) {//该方法中对child移动的边界进行控制，left , top 分别为即将移动到的位置
            int topBound = mFullScreenTop;
            int bottomBound = topBound + mVerticalRange;
            return Math.min(Math.max(top, topBound), bottomBound);
        }

        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) { //返回横向坐标左右边界值
            int leftBound = mFullScreenLeft;
            int rightBound = mHorizontalRange;
            return Math.min(Math.max(left, leftBound), rightBound);
        }

        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
            //view在拖动过程坐标发生变化时会调用此方法，包括两个时间段：手动拖动和自动滚动
            mTop = top;
            mLeft = left;
            mVerticalOffset = (float) (top - mFullScreenTop) / mVerticalRange;
            float ratio = (top - mFullScreenTop) * 1.0f / (getHeight() * 2);
            getBackground().setAlpha((int) (255 - (ratio * 255)));
            requestLayoutLightly();
        }

        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {//
            if (yvel < 0 || (yvel == 0 && mVerticalOffset < 0.5f)) {
                maximize(); //回到最大化界面
            } else {
                animateToOriginPosition();//缩回原界面，结束。
            }
        }

    }

    @Override
    public void computeScroll() {
        if (mDragHelper.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        if (getChildCount() != 1)
            throw new RuntimeException("this ViewGroup should contains only 1 view");
        mVideoView = getChildAt(0);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        Log.i(TAG, "onMeasure()");
        int maxWidth = MeasureSpec.getSize(widthMeasureSpec);
        int maxHeight = MeasureSpec.getSize(heightMeasureSpec);

        setMeasuredDimension(resolveSizeAndState(maxWidth, widthMeasureSpec, 0),
                resolveSizeAndState(maxHeight, heightMeasureSpec, 0));
        if (!mIsFinishInit) {
            mHorizontalRange = getMeasuredWidth();
            mVerticalRange = getMeasuredHeight();
            mFullScreenTop = getPaddingTop() + (getMeasuredHeight() - (getMeasuredWidth() / 16 * 9)) / 2;
            mFullScreenLeft = 0;
            restorePosition();
            mIsFinishInit = true;
        }
    }

    private void justMeasurePlayer() {
        int widthCurSize = (int) (mPlayerMaxWidth * (1f - mVerticalOffset * (1f - SCALE_RATIO)));
        int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(widthCurSize, MeasureSpec.EXACTLY);
        int heightSize = (int) (MeasureSpec.getSize(childWidthMeasureSpec) / VIDEO_RATIO);
        int childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(heightSize, MeasureSpec.EXACTLY);
        mVideoView.measure(childWidthMeasureSpec, childHeightMeasureSpec);
    }

    private void onLayoutLightly() {
        mVideoView.layout(mLeft, mTop, mLeft + mVideoView.getMeasuredWidth(), mTop + mVideoView.getMeasuredHeight());
    }

    private void requestLayoutLightly() {
        justMeasurePlayer();
        onLayoutLightly();
        ViewCompat.postInvalidateOnAnimation(this);//进行重绘
    }

    public void setCallback(Callback callback) {
        mCallback = new WeakReference<>(callback);
    }

    public interface Callback {
        void onVideoDisappear();
    }
}
