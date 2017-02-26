package com.haozhang.sliding;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.Scroller;

/**
 * @author HaoZhang
 * @date 2017/2/23.
 */
public class SlidingLayout extends ViewGroup {
    private static final String TAG = "SlidingLayout";

    private static final int DEFULT_SLIDING_MIN_DISTANCE = 10;
    private int mSlidingMaxDistance;
    private Scroller mScroller;

    private float mLastY;

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

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float y = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
                float dy = mLastY - y;
                smoothScrollBy(0, (int) dy);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                smoothScrollTo(0,0);
                break;
        }
        mLastY = y;
        return true;
    }

    private void smoothScrollTo(int fx, int fy) {
        int dx = fx - mScroller.getFinalX();
        int dy = fy - mScroller.getFinalY();

        smoothScrollBy(dx, dy);
    }

    private void smoothScrollBy(int dx, int dy) {
        mScroller.startScroll(mScroller.getFinalX(), mScroller.getFinalY(), dx, dy);
        invalidate();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        Log.d(TAG, "onInterceptTouchEvent() called with: event = " + event);
        boolean intercept = false;
        // 记录此次触摸事件的y坐标
        float y = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                mLastY = y;
                intercept = false;
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                event.getRawY();
                if (y > mLastY) { // 下滑操作
                    View child = getChildAt(0);
                    if (child instanceof AdapterView) {
                        AdapterView adapterChild = (AdapterView) child;
                        // 判断AbsListView是否已经到达内容最顶部(如果已经到达最顶部，就拦截事件，自己处理滑动)
                        if (adapterChild.getFirstVisiblePosition() == 0
                                || adapterChild.getChildAt(0).getTop() == 0) {
                            intercept = true;
                        }
                    }
                    float getX = child.getX();
                    float translationX = child.getTranslationX();
                    int scrollX = child.getScrollX();
                    Log.d(TAG, "getX :" + getX + ", translationX :" + translationX + ", scrollX :" + scrollX);
                    intercept = true;
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

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        return super.dispatchTouchEvent(event);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int size = getChildCount();
        final int parentWidthSize = MeasureSpec.getSize(widthMeasureSpec);
        final int paretnHeightSize = MeasureSpec.getSize(heightMeasureSpec);
        for (int i = 0; i < size; ++i) {
            final View child = getChildAt(i);
            if (child.getVisibility() != GONE) {
                LayoutParams childLp = child.getLayoutParams();
                final boolean childWidthWC = childLp.width == LayoutParams.WRAP_CONTENT;
                final boolean childHeightWC = childLp.height == LayoutParams.WRAP_CONTENT;
                int childWidthMeasureSpec;
                int childHeightMeasureSpec;
                if (child.getLayoutParams() instanceof MarginLayoutParams) {
                    MarginLayoutParams childMarginLp = (MarginLayoutParams) childLp;
                    childWidthMeasureSpec = childWidthWC ? MeasureSpec.makeMeasureSpec(parentWidthSize, MeasureSpec.UNSPECIFIED)
                            : getChildMeasureSpec(widthMeasureSpec,
                            getPaddingLeft() + getPaddingRight() + childMarginLp.leftMargin + childMarginLp.rightMargin, childLp.width);
                    childHeightMeasureSpec = childHeightWC ? MeasureSpec
                            .makeMeasureSpec(paretnHeightSize,
                                    MeasureSpec.UNSPECIFIED)
                            : getChildMeasureSpec(heightMeasureSpec,
                            getPaddingTop() + getPaddingBottom()
                                    + childMarginLp.topMargin
                                    + childMarginLp.bottomMargin,
                            childMarginLp.height);
                } else {
                    childWidthMeasureSpec = childWidthWC ? MeasureSpec
                            .makeMeasureSpec(parentWidthSize,
                                    MeasureSpec.UNSPECIFIED)
                            : getChildMeasureSpec(widthMeasureSpec,
                            getPaddingLeft() + getPaddingRight(),
                            childLp.width);
                    childHeightMeasureSpec = childHeightWC ? MeasureSpec
                            .makeMeasureSpec(paretnHeightSize,
                                    MeasureSpec.UNSPECIFIED)
                            : getChildMeasureSpec(heightMeasureSpec,
                            getPaddingTop() + getPaddingBottom(),
                            childLp.height);
                }
                child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
            }
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

    }

    float mTotalLength;

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int childStartPostion = 0;
        mTotalLength = 0;
        final int count = getChildCount();
        if (count == 0) {
            return;
        }
        childStartPostion = getPaddingTop();
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (child != null && child.getVisibility() != View.GONE) {
                LayoutParams lp = child.getLayoutParams();
                final int childHeight = child.getMeasuredHeight();
                int leftMargin = 0;
                int rightMargin = 0;
                int topMargin = 0;
                int bottomMargin = 0;
                if (lp instanceof MarginLayoutParams) {
                    MarginLayoutParams mlp = (MarginLayoutParams) lp;
                    leftMargin = mlp.leftMargin;
                    rightMargin = mlp.rightMargin;
                    topMargin = mlp.topMargin;
                    bottomMargin = mlp.bottomMargin;
                }

                childStartPostion += topMargin;
                int startX = (getWidth() - leftMargin - rightMargin - child
                        .getMeasuredWidth()) / 2 + leftMargin;
                child.layout(startX, childStartPostion,
                        startX + child.getMeasuredWidth(), childStartPostion
                                + childHeight);
                childStartPostion += (childHeight + bottomMargin);
            }
        }
        childStartPostion += getPaddingBottom();
        mTotalLength = childStartPostion;
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            System.out.println("computeScroll()---> " +
                    "mScroller.getCurrX()=" + mScroller.getCurrX() + "," +
                    "mScroller.getCurrY()=" + mScroller.getCurrY());
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            postInvalidate();
        }
        super.computeScroll();
    }
}
