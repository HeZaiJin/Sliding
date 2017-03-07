package com.haozhang.sliding;

import android.content.Context;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;

/**
 * @author HaoZhang
 * @date 2017/3/3.
 */

public class SlidingRefreshLayout extends SlidingNestedLayout {

    private static final String TAG = "SlidingRefreshLayout";

    public static final int HEADER_SLIDING_MODE_FOLLOW = 0;
    public static final int HEADER_SLIDING_MODE_STAY_TOP = 1;


    public static final int REFRESH_STATE_FREE = 0;
    public static final int REFRESH_STATE_REFRESHING = 1;
    public static final int REFRESH_STATE_REFRESH_READY = 2;
    public static final int REFRESH_STATE_REFRESH_PROCESS = 3;
    public static final int REFRESH_STATE_REFRESH_DONE = 4;


    public static final int REFRESH_STATE_LOADING = 4;


    View mHeader;
    View mFooter;

    onRefreshListener mOnRefreshListener;
    onLoadMoreListener mOnLoadMoreListener;

    private boolean mOnLoadMoreAble = true;

    private int mRefreshState = REFRESH_STATE_FREE;

    private int mHeaderStayMode = HEADER_SLIDING_MODE_FOLLOW;

    public SlidingRefreshLayout(Context context) {
        super(context);
        init(context);
    }

    public SlidingRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public SlidingRefreshLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        mHeader = LayoutInflater.from(context).inflate(R.layout.sliding_header_layout, null);
        mFooter = LayoutInflater.from(context).inflate(R.layout.sliding_footer_layout, null);
        addView(mHeader, 0);
        addView(mFooter, 1);
    }

    @Override
    protected int getTargetViewIndex() {
        return 2;
    }

    private void checkCurrentRefreshState() {
        if (mHeader.getTop() >= 0 && mRefreshState != REFRESH_STATE_REFRESH_READY && mRefreshState != REFRESH_STATE_REFRESHING) {
            mRefreshState = REFRESH_STATE_REFRESH_READY;
            if (null != mOnRefreshListener) {
//                post(new Runnable() {
//                    @Override
//                    public void run() {
                Log.d(TAG, "mOnRefreshListener.onRefreshingReady,top :" + mHeader.getTop());
                mOnRefreshListener.onRefreshingReady();
//                    }
//                });
            }
        } else if (mHeader.getTop() < 0 && mRefreshState != REFRESH_STATE_REFRESH_PROCESS ) {
            mRefreshState = REFRESH_STATE_REFRESH_PROCESS;
            if (null != mOnRefreshListener) {
                Log.d(TAG, "mOnRefreshListener.onRefreshProcess,top :" + mHeader.getTop());
                mOnRefreshListener.onRefreshProcess();
            }
        }
    }

    @Override
    protected void onSlidingOffsetTopAndBottom(int offset) {
        if (mSlidingProgress > 0) {
            checkCurrentRefreshState();
            // 处理header 滑动
            if (mHeaderStayMode == HEADER_SLIDING_MODE_FOLLOW) {
                // 跟随模式
                ViewCompat.offsetTopAndBottom(mHeader, offset);
            } else if (mHeaderStayMode == HEADER_SLIDING_MODE_STAY_TOP) {
                // 顶部悬停
                int height = mHeader.getHeight();
                int top = mHeader.getTop();
                Log.d(TAG, "getTop :" + top + ", offset :" + offset);
                int tmp = top + offset;
                /*if (tmp > 0) {
                    ViewCompat.offsetTopAndBottom(mHeader, -top);
                } else if (tmp < -height){

                    ViewCompat.offsetTopAndBottom(mHeader, offset);
                } else {
                    ViewCompat.offsetTopAndBottom(mHeader, offset);
                }*/
            }
        } else if (mSlidingProgress < 0) {
            // 处理footer 滑动
        } else {
            // reset
            int top = mHeader.getTop();
            Log.d(TAG, " reset ,current top :" + top);
            int height = mHeader.getHeight();
            if (top != -height) {
//                ViewCompat.offsetTopAndBottom(mHeader, -height - top);
            }
        }
    }

    @Override
    protected void onMoveToStartBegin(int from) {
        if (from >= mHeader.getHeight()) {
            mRefreshState = REFRESH_STATE_REFRESHING;
            if (null != mOnRefreshListener) {
                Log.d(TAG, "start onRefresh");
                mOnRefreshListener.onRefreshing();
            }
        } else if (from <= -mFooter.getHeight()) {
            mRefreshState = REFRESH_STATE_LOADING;
        } else {
            mRefreshState = REFRESH_STATE_FREE;
        }
    }

    int mHeaderOffsetValue;
    int mTargetOffsetValue;

    public void stopRefreshing() {
        mResetAnim.reset();
        mResetAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mRefreshState = REFRESH_STATE_FREE;
                mHeaderOffsetValue = 0;
                mTargetOffsetValue = 0;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        mResetAnim.setDuration(ANIMATE_TO_START_DURATION);
        mResetAnim.setInterpolator(mDecelerateInterpolator);
        mHeaderOffsetValue = -mHeader.getHeight() - mHeader.getTop();
        mTargetOffsetValue = mTarget.getTop();
        this.startAnimation(mResetAnim);
        mRefreshState = REFRESH_STATE_REFRESH_DONE;
        if (null != mOnRefreshListener) {
            mOnRefreshListener.onRefreshDone();
        }
    }


    private final Animation mResetAnim = new Animation() {
        @Override
        public void applyTransformation(float interpolatedTime, Transformation t) {
            if (0 != mHeaderOffsetValue) {
                int current_top = mHeader.getTop();
                int header = (int) (mHeaderOffsetValue * interpolatedTime) - mHeader.getTop();
                ViewCompat.offsetTopAndBottom(mHeader, header);
            }
            if (0 != mTargetOffsetValue) {
                int target = (mTargetOffsetValue - (int) (mTargetOffsetValue * interpolatedTime) - mTarget.getTop());
                ViewCompat.offsetTopAndBottom(mTarget, target);
            }
        }
    };


    @Override
    protected boolean checkIsWorking() {
        return mRefreshState == REFRESH_STATE_REFRESHING;
    }

    @Override
    protected void onSlidingProgress(float progerss) {

    }

    @Override
    protected int getHeaderSpace() {
        return mHeader.getHeight();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            if (child == mHeader || child == mFooter) {
                int childHeightSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
                int childWidthSpec = MeasureSpec.makeMeasureSpec(widthMeasureSpec, MeasureSpec.EXACTLY);
                child.measure(childWidthSpec, childHeightSpec);
            } else {
                measureChild(child, widthMeasureSpec, heightMeasureSpec);
            }
        }
        setMeasuredDimension(widthSize, heightSize);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (mRefreshState != REFRESH_STATE_FREE) {
            // 工作状态下 不更新layout
            return;
        }
        int count = getChildCount();
        if (count > 0) {
            for (int i = 0; i < count; i++) {
                View child = getChildAt(i);
                if (child == mHeader) {
                    child.layout(0, -child.getMeasuredHeight(), getMeasuredWidth(), 0);
                } else if (child == mFooter) {
                    child.layout(0, getMeasuredHeight(), getMeasuredWidth(), getMeasuredHeight() + child.getMeasuredHeight());
                } else {
                    child.layout(0, 0, getMeasuredWidth(), getMeasuredHeight());
                }
            }
        }
    }


    public SlidingRefreshLayout setOnRefreshListener(onRefreshListener listener) {
        this.mOnRefreshListener = listener;
        return this;
    }

    public SlidingRefreshLayout setOnLoadMoreListener(onLoadMoreListener listener) {
        this.mOnLoadMoreListener = listener;
        return this;
    }

    public SlidingRefreshLayout setLoadMoreEnable(boolean able) {
        this.mOnLoadMoreAble = able;
        return this;
    }

    public boolean isLoadMoreAble() {
        return mOnLoadMoreAble;
    }

    public interface onRefreshListener {
        /**
         * 开始刷新
         */
        public void onRefreshing();

        /**
         * 达到刷新边界
         */
        public void onRefreshingReady();


        public void onRefreshProcess();

        public void onRefreshDone();
    }

    public interface onLoadMoreListener {
        public void onLoadMore();
    }

}
