package com.haozhang.sliding;

import android.content.Context;
import android.content.res.Resources;
import android.support.v4.view.NestedScrollingChild;
import android.support.v4.view.NestedScrollingParent;
import android.support.v4.view.NestedScrollingParentHelper;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Transformation;
import android.widget.FrameLayout;

public class SlidingNestedLayout extends FrameLayout implements NestedScrollingParent {
    private static final String TAG = "SlidingNestedLayout";

    private static final int ANIMATE_TO_START_DURATION = 200;
    private final DecelerateInterpolator mDecelerateInterpolator = new DecelerateInterpolator(1.35f);
    private int mSlidingMaxDistance;
    private NestedScrollingParentHelper mNestedScrollingParentHelper;
    int mTotalConsume;
    View mTarget;

    public SlidingNestedLayout(Context context) {
        super(context);
        init(context);
    }

    public SlidingNestedLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public SlidingNestedLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        Resources resources = context.getResources();
        mSlidingMaxDistance = resources.getDimensionPixelSize(R.dimen.sliding_max_distance);
        mNestedScrollingParentHelper = new NestedScrollingParentHelper(this);
    }

    /**
     * 判断是否需要进行嵌套处理
     *
     * @param child            直接子view
     * @param target           nested child view
     * @param nestedScrollAxes 滑动的方向
     * @return 是否处理
     */
    @Override
    public boolean onStartNestedScroll(View child, View target, int nestedScrollAxes) {
        if (nestedScrollAxes == ViewCompat.SCROLL_AXIS_VERTICAL) {
            return true;
        }
        return false;
    }


    @Override
    public void onNestedScrollAccepted(View child, View target, int axes) {
        mNestedScrollingParentHelper.onNestedScrollAccepted(child, target, axes);
    }

    /**
     * 先于target滑动
     *
     * @param target   nested child target
     * @param dx       将要滑动的x位移
     * @param dy       将要滑动的Y位移
     * @param consumed 输出消耗位移的数组
     */
    @Override
    public void onNestedPreScroll(View target, int dx, int dy, int[] consumed) {
        Log.d(TAG, "onNestedPreScroll() , dx = [" + dx + "], dy = [" + dy + "]");
        dy = -dy;

        if (dy > 0) {
            if (mTotalConsume < 0) {
                int preConsume = mTotalConsume + dy;
                if (preConsume >= 0) {
//                   Log.d(TAG, "底部空间不太够");
                    offsetTargetTopAndBottom(-mTotalConsume);
                    consumed[1] = mTotalConsume;
                    mTotalConsume = 0;
                } else {
//                  Log.d(TAG, "底部有空间，消耗：" + (-dy));
                    mTotalConsume += dy;
                    offsetTargetTopAndBottom(dy);
                    consumed[1] = -dy;
                }
            } else if (interceptPullDown(target)) {
//                Log.d(TAG, "顶部下滑 :");
                slidingToTargetTop();
                if (Math.abs(mTotalConsume) <= mSlidingMaxDistance) {
                    mTotalConsume += dy;
                    offsetTargetTopAndBottom(dy);
                    consumed[1] = -dy;
                } else {
//                  Log.d(TAG, "顶部下拉最大边界");
                    consumed[1] = 0;
                }
            }
        } else if (dy < 0) {
            //Log.d(TAG, "上滑");
            //Log.d(TAG, "current total consume :" + mTotalConsume);
            if (mTotalConsume > 0) {
                //Log.d(TAG, "顶部有Offset空间 :" + mTotalConsume + ",消耗掉");
                int preConsume = mTotalConsume + dy;
                if (preConsume >= 0) {
                    offsetTargetTopAndBottom(dy);
                    mTotalConsume = preConsume;
                    consumed[1] = -dy;
                    //Log.d(TAG, "空间还很多,继续消耗 :" + (-dy));
                } else {
                    //Log.d(TAG, "空间不太够了,只能消耗一部分:" + mTotalConsume);
                    offsetTargetTopAndBottom(-mTotalConsume);
                    consumed[1] = mTotalConsume;
                    mTotalConsume = 0;
                }
            } else if (interceptPullUp(target)) {
                //Log.d(TAG, "上滑 顶部没有OFFSET空间,切滑动到底部");
                slidingToTargetBottom();
                if (Math.abs(mTotalConsume) <= mSlidingMaxDistance) {
                    mTotalConsume += dy;
                    offsetTargetTopAndBottom(dy);
                    consumed[1] = -dy;
                } else {
//                    Log.d(TAG,"底部上拉最大边界");
                    consumed[1] = 0;
                }
            }

        }
//        Log.d(TAG, "caculate : mTotalConsume :" + mTotalConsume + ", consumed[1]:" + consumed[1]);
    }

    protected void slidingToTargetBottom() {

    }

    protected void slidingToTargetTop() {

    }

    private void offsetTargetTopAndBottom(int dy) {
        ensureTargetView();
        ViewCompat.offsetTopAndBottom(mTarget, dy);
    }

    private void animToStart() {
        mAnimateToStartPosition.reset();
        mAnimateToStartPosition.setDuration(ANIMATE_TO_START_DURATION);
        mAnimateToStartPosition.setInterpolator(mDecelerateInterpolator);
        mAnimateToStartPosition.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mTotalConsume = 0;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        this.clearAnimation();
        this.startAnimation(mAnimateToStartPosition);
    }


    void moveToStart(float interpolatedTime) {
        ensureTargetView();
        int targetTop = 0;
        targetTop = (mTotalConsume + (int) ((-mTotalConsume) * interpolatedTime));
        int offset = targetTop - mTarget.getTop();
        offsetTargetTopAndBottom(offset);
    }


    private final Animation mAnimateToStartPosition = new Animation() {
        @Override
        public void applyTransformation(float interpolatedTime, Transformation t) {
            moveToStart(interpolatedTime);
        }
    };

    public void ensureTargetView() {
        Log.d(TAG, "ensureTargetView() called");
        if (null == mTarget) {
            View child = getChildAt(0);
            if (child instanceof NestedScrollingChild) {
                mTarget = child;
            } else {
                Log.d(TAG, "child 不集成于 NestedScrollingChild ,包装");
            }
        }
    }

    private boolean interceptPullDown(View child) {
        // 判断是否可以下滑
        boolean intercept = ViewCompat.canScrollVertically(child, -1);
//        Log.d(TAG, "interceptPullDown:" + !intercept);
        return !intercept;
    }

    private boolean interceptPullUp(View child) {
        // 判断是否可以上滑
        boolean intercept = ViewCompat.canScrollVertically(child, 1);
//        Log.d(TAG, "interceptPullDown:" + !intercept);
        return !intercept;
    }

    @Override
    public void onNestedScroll(View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
//        Log.d(TAG, "onNestedScroll() called with: , dxConsumed = [" + dxConsumed + "], dyConsumed = [" + dyConsumed + "], dxUnconsumed = [" + dxUnconsumed + "], dyUnconsumed = [" + dyUnconsumed + "]");
    }

    @Override
    public boolean onNestedPreFling(View target, float velocityX, float velocityY) {
        return false;
    }

    @Override
    public boolean onNestedFling(View target, float velocityX, float velocityY, boolean consumed) {
        return false;
    }

    @Override
    public int getNestedScrollAxes() {
        return mNestedScrollingParentHelper.getNestedScrollAxes();
    }

    @Override
    public void onStopNestedScroll(View child) {
        mNestedScrollingParentHelper.onStopNestedScroll(child);
        animToStart();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (getChildCount() > 0) {
            final View child = getChildAt(0);
            int height = getMeasuredHeight();
            if (child.getMeasuredHeight() < height) {
                final FrameLayout.LayoutParams lp = (LayoutParams) child.getLayoutParams();

                int childWidthMeasureSpec = getChildMeasureSpec(widthMeasureSpec,
                        getPaddingLeft() + getPaddingRight(), lp.width);
                height -= getPaddingTop();
                height -= getPaddingBottom();
                int childHeightMeasureSpec =
                        MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);

                child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
            }
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        mTarget = null;
        final int childHeight = (getChildCount() > 0) ? getChildAt(0).getMeasuredHeight() : 0;
        final int scrollRange = Math.max(0, childHeight - (b - t - getPaddingBottom() - getPaddingBottom()));
        int scrollY = 0;
        if (scrollY > scrollRange) {
            scrollY = scrollRange;
        } else if (scrollY < 0) {
            scrollY = 0;
        }
        scrollTo(0, scrollY);
    }

}
