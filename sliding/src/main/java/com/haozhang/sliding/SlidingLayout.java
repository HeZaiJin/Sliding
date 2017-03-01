package com.haozhang.sliding;

import android.content.Context;
import android.content.res.Resources;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.DecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ScrollView;
import android.widget.Scroller;


/**
 * 支持上下阻力滑动回弹的ViewGroup
 * @author HaoZhang
 */
public class SlidingLayout extends FrameLayout {
    private static final String TAG = "SlidingLayout";

    private static final float DEFAULT_DECAY_VALUE = 0.5f;
    private int mSlidingMaxDistance;
    private Scroller mScroller;
    private float mLastY;
    int mSlidingState = 0;
    private int mInitialDownY;

    public SlidingLayout(Context context) {
        super(context);
        init(context);
    }

    public SlidingLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public SlidingLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    void init(Context context) {
        Resources resources = context.getResources();
        mSlidingMaxDistance = resources.getDimensionPixelSize(R.dimen.sliding_max_distance);
        mScroller = new Scroller(context, new DecelerateInterpolator(), true);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
    }

    private void smoothScrollTo(int fx, int fy) {
        int dx = fx - mScroller.getFinalX();
        int dy = fy - mScroller.getFinalY();
        mScroller.startScroll(mScroller.getFinalX(), mScroller.getFinalY(), dx, dy);
        invalidate();
    }

    private void smoothScrollBy(int dx, int dy) {
        int finalY = mScroller.getFinalY();
        int absY = Math.abs(finalY);
        if (absY > mSlidingMaxDistance) {
            dy *= DEFAULT_DECAY_VALUE;
        }
        mScroller.startScroll(mScroller.getFinalX(), finalY, dx, dy);
        invalidate();
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                return false;
            case MotionEvent.ACTION_MOVE:
                float y = event.getY();
                float dy = mLastY - y;
                smoothScrollBy(0, (int) dy);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                smoothScrollTo(0, 0);
                break;
        }
        mLastY = event.getY();
        return true;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        int y = (int) event.getY();
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            mInitialDownY = y;
        }
        if (event.getAction() == MotionEvent.ACTION_MOVE) {
            if (((mSlidingState == 1) && (y - mInitialDownY <= 0)) || (mSlidingState == 2) && (y - mInitialDownY >= 0)) {
                // fuck code , just stupid , should use nested
                // 模拟down事件
                MotionEvent down = event;
                down.setAction(MotionEvent.ACTION_UP);
                dispatchTouchEvent(down);
                down.setAction(MotionEvent.ACTION_DOWN);
                dispatchTouchEvent(down);
            }
        }
        return super.dispatchTouchEvent(event);
    }


    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        boolean intercept = false;
        // 记录此次触摸事件的y坐标
        int y = (int) event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                mSlidingState = 0;
                mInitialDownY = y;
                mLastY = y;
                intercept = false;
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                if (Math.abs( y - mInitialDownY) < ViewConfiguration.getTouchSlop()){
                    return false;
                }
                View child = getChildAt(0);
                if (y >= mLastY) {
                    mSlidingState = 1;
                    // 下滑操作
                    intercept = interceptPullDown(child);
                } else {
                    mSlidingState = 2;
                    // 上滑动
                    intercept = interceptPullUp(child);
                }
                break;
            }
            // Up事件
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                intercept = false;
                break;
        }
        return intercept;
    }

    private boolean interceptPullDown(View child) {
        boolean intercept = true;
        if (child instanceof ScrollView) {
            intercept = interceptPullDownScrollView(child);
        } else if (child instanceof AdapterView) {
            intercept = interceptPullDownAdapterView(child);
        } else if (child instanceof RecyclerView) {
            intercept = interceptPullDownRecyclerView(child);
        }
        Log.d(TAG, "interceptPullDown :" + intercept);
        return intercept;
    }

    private boolean interceptPullUp(View child) {
        boolean intercept = true;
        if (child instanceof ScrollView) {
            intercept = interceptPullUpScrollView(child);
        } else if (child instanceof AdapterView) {
            intercept = interceptPullUpAdapterView(child);
        } else if (child instanceof RecyclerView) {
            intercept = interceptPullUpRecyclerView(child);
        }
        Log.d(TAG, "interceptPullUp :" + intercept);
        return intercept;
    }

    private boolean interceptPullDownAdapterView(View child) {
        boolean intercept = true;
        AdapterView adapterChild = (AdapterView) child;
        // 判断AbsListView是否已经到达内容最顶部
        if (adapterChild.getFirstVisiblePosition() != 0
                || adapterChild.getChildAt(0).getTop() != 0) {
            // 如果没有达到最顶端，则仍然将事件下放
            intercept = false;
        }
        return intercept;
    }

    private boolean interceptPullUpAdapterView(View child) {
        boolean intercept = false;
        AdapterView adapterChild = (AdapterView) child;

        // 判断AbsListView是否已经到达内容最底部
        if (adapterChild.getLastVisiblePosition() == adapterChild.getCount() - 1
                && (adapterChild.getChildAt(adapterChild.getChildCount() - 1).getBottom() == getMeasuredHeight())) {
            // 如果到达底部，则拦截事件
            intercept = true;
        }
        return intercept;
    }

    private boolean interceptPullDownScrollView(View child) {
        boolean intercept = false;
        if (child.getScrollY() <= 0) {
            intercept = true;
        }
        return intercept;
    }

    private boolean interceptPullUpScrollView(View child) {
        boolean intercept = false;
        ScrollView scrollView = (ScrollView) child;
        View scrollChild = scrollView.getChildAt(0);

        if (scrollView.getScrollY() >= (scrollChild.getHeight() - scrollView.getHeight())) {
            intercept = true;
        }
        return intercept;
    }

    private boolean interceptPullDownRecyclerView(View child) {
        boolean intercept = false;
        RecyclerView recyclerChild = (RecyclerView) child;
        if (recyclerChild.computeVerticalScrollOffset() <= 0)
            intercept = true;

        return intercept;
    }

    private boolean interceptPullUpRecyclerView(View child) {
        boolean intercept = false;

        RecyclerView recyclerChild = (RecyclerView) child;
        if (recyclerChild.computeVerticalScrollExtent() + recyclerChild.computeVerticalScrollOffset()
                >= recyclerChild.computeVerticalScrollRange())
            intercept = true;

        return intercept;
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
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
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

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            postInvalidate();
        }
        super.computeScroll();
    }
}
