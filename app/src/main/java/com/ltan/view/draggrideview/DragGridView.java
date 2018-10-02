package com.ltan.view.draggrideview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Desc:
 * <p>
 * Modified list:
 * <p>
 * created by ltanc on 2018/10/1
 */
public class DragGridView extends FrameLayout {
    private static final String TAG = "ltan/DragGridView";

    private static final boolean DEBUG_CIRCLE = false;
    private ImageView mDragImg;

    private GridView mDragGridView;
    private ListAdapter mGridAdapter;

    public DragGridView(Context context) {
        this(context, null);
    }

    public DragGridView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DragGridView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mDragGridView = new GridView(context);
        LayoutParams gridLP = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        mDragGridView.setLayoutParams(gridLP);

        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.DragGridView, defStyleAttr, 0);
        int columns = ta.getInt(R.styleable.DragGridView_android_numColumns, 2);
        // for press state
        final Drawable selector = ta.getDrawable(R.styleable.DragGridView_android_listSelector);
        ta.recycle();

        if (selector != null) {
            mDragGridView.setSelector(selector);
        }
        mDragGridView.setNumColumns(columns);
        addView(mDragGridView);

        init(context, columns);
        initDragViews();
    }

    private void init(Context context, int columns) {
        mDragViewShowPoint = new Point();
    }

    private void initDragViews() {
        mDragImg = new ImageView(getContext());
        LayoutParams lp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        mDragImg.setLayoutParams(lp);
        mDragImg.setVisibility(INVISIBLE);
        addView(mDragImg);
    }

    public void setAdapter(ListAdapter adapter) {
        mGridAdapter = adapter;
        if (mDragGridView != null) {
            mDragGridView.setAdapter(adapter);
        }
    }

    private Rect getRectInParent(View view) {
        int[] rootLocation = new int[2];
        getLocationInWindow(rootLocation);
        int[] viewLocation = new int[2];
        view.getLocationInWindow(viewLocation);

        Rect rectInParent = new Rect();
        rectInParent.left = viewLocation[0] - rootLocation[0];
        rectInParent.top = viewLocation[1] - rootLocation[1];
        rectInParent.right = rectInParent.left + view.getWidth();
        rectInParent.bottom = rectInParent.top + view.getHeight();
        return rectInParent;
    }

    private void generateDragWH() {
        if (mDragImg.getMeasuredWidth() <= 0 || mDragImg.getMeasuredHeight() <= 0) {
            int wSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
            int hSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
            mDragImg.measure(wSpec, hSpec);
            Log.d(TAG, "generateDragWH: width:" + mDragImg.getMeasuredWidth() + ", height:" + mDragImg.getMeasuredHeight());
        }
    }

    private Point mDragViewShowPoint;

    @Override
    public boolean onTouchEvent(MotionEvent ev) {

        return super.onTouchEvent(ev);
    }

    private Point updateDragStartPoint(int touchIndex, int touchX, int touchY) {
        Point p = mDragViewShowPoint;
        View touchView = mDragGridView.getChildAt(touchIndex);
        //int dragWidth = mDragImg.getWidth() < 0 ? mDragImg.getMeasuredWidth() : mDragImg.getHeight();
        int dragWidth = mDragImg.getMeasuredWidth();
        int dragHeight = mDragImg.getMeasuredHeight();
        p.x = touchView.getLeft() + (touchView.getWidth() - dragWidth) / 2;
        //p.y = touchY - dragHeight; // show view at touch point
        p.y = touchView.getTop() + (touchView.getHeight() - dragHeight) / 2;// child center...
        if (p.y <= 0) {
            p.y = 0;
        }
        dragViewStartX = p.x;
        dragViewStartY = p.y;
        Log.d(TAG, "updateDragStartPoint: down x:" + touchX + ", y:" + touchY + "| position x:" + p.x + ", y:" + p.y);
        Log.d(TAG, "updateDragStartPoint: l:" + mDragImg.getLeft() + ", t:" + mDragImg.getTop());
        return p;
    }

    private int getChildIndex(int targetX, int targetY) {
        for (int i = 0; i < mDragGridView.getChildCount(); i++) {
            View child = mDragGridView.getChildAt(i);
            if ((targetX > child.getLeft() && targetY > child.getTop())
                    && (targetX < child.getRight() && targetY < child.getBottom())) {
                return i;
            }
        }
        return -1;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        int dragViewL = mDragViewShowPoint.x;
        int dragViewT = mDragViewShowPoint.y;
        if (DEBUG_CIRCLE) {
            Log.d(TAG, "onLayout: ");
        }
        mDragImg.layout(dragViewL, dragViewT, mDragImg.getWidth() + dragViewL, mDragImg.getHeight() + dragViewT);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (DEBUG_CIRCLE) {
            Log.d(TAG, "onMeasure: ");
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if(mIsAnimPlaying) {
            return false;
        }
        //Log.d(TAG, "dispatchTouchEvent: ev:" + ev);
        int currentTouchX = (int) ev.getX();
        int currentTouchY = (int) ev.getY();

        switch (ev.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                downX = (int) ev.getX();
                downY = (int) ev.getY();
                int index = getChildIndex(downX, downY);
                mDragIndex = index;
                if (index > -1) {
                    Object itemData = mGridAdapter.getItem(index);
                    if (itemData instanceof DataObject) {
                        mDragImg.setImageDrawable(((DataObject) itemData).icon);
                    }
                    generateDragWH();
                    updateDragStartPoint(index, downX, downY);
                    mDragImg.setAlpha(1.0f);
                    mDragImg.setVisibility(VISIBLE);
                    requestLayout();
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if(mDragIndex == -1) {
                    break;
                }
                onDragEnd(currentTouchX, currentTouchY);
                //return true;
                break;
            case MotionEvent.ACTION_MOVE:
                if(mDragIndex == -1) {
                    break;
                }
                onDraging(currentTouchX, currentTouchY);
                lastTouchX = currentTouchX;
                lastTouchY = currentTouchY;
                //return true;
                break;
        }
        return super.dispatchTouchEvent(ev);
    }

    private int downX, downY;
    private int lastTouchX, lastTouchY;
    private int dragViewStartX, dragViewStartY;
    private int moveDx, moveDy;
    private boolean mIsAnimPlaying;
    private int mDragIndex = -1;

    private void calculateDxDy(int touchX, int touchY) {
        moveDx = touchX - downX;
        moveDy = touchY - downY;
    }

    private void onDraging(int toX, int toY) {
        calculateDxDy(toX, toY);
        mDragViewShowPoint.x = dragViewStartX + moveDx;
        mDragViewShowPoint.y = dragViewStartY + moveDy;
        requestLayout();
    }

    @SuppressLint("ObjectAnimatorBinding")
    private void onDragEnd(int nowX, int nowY) {
        // moveDx = (nowX - downX)
        ObjectAnimator dragViewAnimX = ObjectAnimator.ofFloat(mDragImg, "translationX", -(moveDx));
        ObjectAnimator dragViewAnimY = ObjectAnimator.ofFloat(mDragImg, "translationY", -(moveDy));
        ObjectAnimator dragViewAnimAlpha = ObjectAnimator.ofFloat(mDragImg, "alpha", 1.f, 0.5f);

        List<Animator> animatorList = new ArrayList<Animator>();
        animatorList.add(dragViewAnimX);
        animatorList.add(dragViewAnimY);
        animatorList.add(dragViewAnimAlpha);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.setInterpolator(new DecelerateInterpolator(2.5f));
        animatorSet.playTogether(animatorList);
        animatorSet.setDuration(600);
        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                mIsAnimPlaying = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mIsAnimPlaying = false;
                mDragImg.setVisibility(INVISIBLE);
                mDragImg.setTranslationX(0);
                mDragImg.setTranslationY(0);
                requestLayout();
            }
        });
        animatorSet.start();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return super.onInterceptTouchEvent(ev);
        // to prevent child press state
        //return true;
    }

    public static class DataObject {
        private String label;
        private Drawable icon;

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public Drawable getIcon() {
            return icon;
        }

        public void setIcon(Drawable icon) {
            this.icon = icon;
        }
    }
}
