package com.haozhang.sliding;

import android.content.Context;
import android.content.res.Resources;
import android.support.v4.view.NestedScrollingParent;
import android.support.v4.view.NestedScrollingParentHelper;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ScrollView;
import android.widget.Scroller;

public class SlidingNestedLayout extends FrameLayout implements NestedScrollingParent {
    private static final String TAG = "SlidingNestedLayout";

    private static final float DRAG_RATE = .5f;
    protected Scroller mScroller;
    private NestedScrollingParentHelper mNestedScrollingParentHelper;

    protected int mInitialDownY;


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

    void init(Context context) {
        Resources resources = context.getResources();
        mScroller = new Scroller(context, new DecelerateInterpolator(), true);
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
        // 如果child不能滑动,parent消耗
        if (mNestedScroll) {
            Log.d(TAG, "onNestedPreScroll() called with:, dx = [" + dx + "], dy = [" + dy);
            int offset = (int) (-dy * DRAG_RATE);
            mTotalConsume += offset;
            mTarget.offsetTopAndBottom(offset);
            consumed[1] = dy;
        }
        Log.d(TAG, "mTotalConsume :" + mTotalConsume);
        if (mTotalConsume != 0) {
            int offset = (int) (-dy * DRAG_RATE);
            int tmp = mTotalConsume;
            mTotalConsume += offset;
            if ((tmp - mTotalConsume) > (tmp + mTotalConsume)) {
                mTarget.offsetTopAndBottom(tmp);
                consumed[1] = tmp + mTotalConsume;
                mTotalConsume = 0;
            } else {
                mTarget.offsetTopAndBottom(offset);
                consumed[1] = dy;
            }
        }
    }

    public void ensureTargetView() {
        if (null == mTarget) {
            mTarget = getChildAt(0);
        }
    }

    int mTotalConsume;
    View mTarget;
    int mScrollState = 0;
    int mLastY;
    boolean mNestedScroll;

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        ensureTargetView();
        int y = (int) ev.getY();
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mInitialDownY = y;
                mLastY = y;
                break;
            case MotionEvent.ACTION_MOVE:
                View child = mTarget;
                if (y - mInitialDownY > 0) {
                    //相对起始位置下滑
                    Log.d(TAG, "下滑~~~~~~~");
                    if (interceptPullDown(child)) {
                        Log.d(TAG, "NNNNNNNNNNNNN child 不能下滑");
                        mNestedScroll = true;
                    } else {
                        Log.d(TAG, "YYYYYYYYYYYYY child 能下滑");
                        mNestedScroll = false;
                    }

                } else if (y - mInitialDownY < 0) {
                    Log.d(TAG, "上滑~~~~~~~");
                    if (interceptPullUp(child)) {
                        mNestedScroll = true;
                    } else {
                        mNestedScroll = false;
                    }
                }
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                Log.d(TAG, "event :" + ev);
                onTouchEventUp(ev);
                mScrollState = 0;
                mNestedScroll = false;
                break;

        }
        return super.onInterceptTouchEvent(ev);
    }

    protected void onTouchEventUp(MotionEvent event) {
        Log.d(TAG, "onTouchEventUp");
        smoothScrollTo(0, 0);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);
    }

    public boolean canChildScrollUp(View target) {

        if (android.os.Build.VERSION.SDK_INT < 14) {
            if (target instanceof AbsListView) {
                final AbsListView absListView = (AbsListView) target;
                return absListView.getChildCount() > 0
                        && (absListView.getFirstVisiblePosition() > 0 || absListView.getChildAt(0)
                        .getTop() < absListView.getPaddingTop());
            } else {
                return ViewCompat.canScrollVertically(target, -1) || target.getScrollY() > 0;
            }
        } else {
            return ViewCompat.canScrollVertically(target, -1);
        }
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
    public void onNestedScroll(View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
        //Log.d(TAG, "onNestedScroll() called with: , dxConsumed = [" + dxConsumed + "], dyConsumed = [" + dyConsumed + "], dxUnconsumed = [" + dxUnconsumed + "], dyUnconsumed = [" + dyUnconsumed + "]");
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
//        smoothScrollTo(0, 0);
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

    private void smoothScrollTo(int fx, int fy) {
        int dx = fx - mScroller.getFinalX();
        int dy = fy - mScroller.getFinalY();
        mScroller.startScroll(mScroller.getFinalX(), mScroller.getFinalY(), dx, dy);
        invalidate();
    }

    private void smoothScrollBy(int dx, int dy) {
        int finalY = mScroller.getFinalY();
        mScroller.startScroll(mScroller.getFinalX(), finalY, dx, dy);
        invalidate();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
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
