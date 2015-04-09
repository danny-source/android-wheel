/*
 *  Android Wheel Control.
 *  https://code.google.com/p/android-wheel/
 *  
 *  Copyright 2011 Yuri Kanivets
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package kankan.wheel.widget;

import java.util.LinkedList;
import java.util.List;

import kankan.wheel.widget.adapters.WheelViewAdapter;

import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.GradientDrawable.Orientation;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Interpolator;
import android.widget.Button;
import android.widget.LinearLayout;

/*
 * android-wheel for Android Studio
 * http://cms.35g.tw/coding
 *
 * based on
 *
 * Android Wheel Control.
 * https://code.google.com/p/android-wheel/
 *
 * Copyright 2011 Yuri Kanivets
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

public class WheelView extends View {

    /**
     * Top and bottom shadows colors
     */
    private static final int[] SHADOWS_COLORS = new int[]{0xFF111111,
            0x00AAAAAA, 0x00AAAAAA};

    /**
     * Top and bottom items offset (to hide that)
     */
    private static final int ITEM_OFFSET_PERCENT = 10;

    /**
     * Left and right padding value
     */
    private static final int PADDING = 10;

    /**
     * Default count of visible items
     */
    private static final int DEF_VISIBLE_ITEMS = 5;

    // Wheel Values
    private int currentItem = 0;

    // Count of visible items
    private int visibleItems = DEF_VISIBLE_ITEMS;

    // Item height
    private int itemHeight = 0;

    // Center Line
    private Drawable centerDrawable;

    // Shadows drawables
    private GradientDrawable topShadow;
    private GradientDrawable bottomShadow;

    // Scrolling
    private WheelScroller scroller;
    private boolean isScrollingPerformed;
    private int scrollingOffset;

    // Cyclic
    boolean isCyclic = false;

    // Items layout
    private LinearLayout itemsLayout;

    // The number of first item in layout
    private int firstItem;

    // View adapter
    private WheelViewAdapter viewAdapter;

    // Recycle
    private WheelRecycle recycle = new WheelRecycle(this);

    // Listeners
    private List<OnWheelChangedListener> changingListeners = new LinkedList<OnWheelChangedListener>();
    private List<OnWheelScrollListener> scrollingListeners = new LinkedList<OnWheelScrollListener>();
    private List<OnWheelClickedListener> clickingListeners = new LinkedList<OnWheelClickedListener>();
    // Swipe
    float downX;
    float downY;
    float upX;
    float upY;
    //delete button
    private Button deleteButton;
    private LinearLayout buttonLayout;
    private int deleteButtonIsViaiable = INVISIBLE;
    private float deleteButtonFontSize = 20;
    private int deleteButtonFontWidth = 128;
    private int deleteButtonFontHeight = 65;
    private int deleteButtonColor = Color.GREEN;
    private String deleteButtonText = "Delete";
    private boolean isSwipeLeft = false;
    private boolean isSwipeRight = false;
    //action button
    private Button actionButton;
    private int actionButtonIsViaiable = INVISIBLE;
    private float actionButtonFontSize = 20;
    private int actionButtonFontWidth = 65;
    private int actionButtonFontHeight = 65;
    private int actionButtonColor = Color.GREEN;
    private String actionButtonText = ">";
    private boolean isActionDeleteButtonTouched = false;
    private int pastCurrentItem = -1;
    /**
     * Constructor
     */
    public WheelView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initData(context);
        initActionButton(context);
        initDeleteButton(context);
    }

    /**
     * Constructor
     */
    public WheelView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initData(context);
        initActionButton(context);
        initDeleteButton(context);
    }

    /**
     * Constructor
     */
    public WheelView(Context context) {
        super(context);
        initData(context);
        initActionButton(context);
        initDeleteButton(context);
    }

    /**
     * Initializes class data
     *
     * @param context the context
     */
    private void initData(Context context) {
        scroller = new WheelScroller(getContext(), scrollingListener);
    }

    private void initDeleteButton(Context context) {
        //
        buttonLayout = new LinearLayout(getContext());
        buttonLayout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1); // , 1是可選寫的
        lp.setMargins(0, 0, 0, 0);
        buttonLayout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        //
        deleteButton = new Button(getContext());
        lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT, 1); // , 1是可選寫的
        lp.setMargins(0, 0, 0, 0);
        deleteButton.setLayoutParams(lp);
        deleteButton.setFocusable(false);
        deleteButton.setClickable(true);
        deleteButton.setTextColor(deleteButtonColor);
        deleteButton.setBackgroundResource(R.drawable.wheel_delete);
        deleteButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                isActionDeleteButtonTouched = false;
                notifyClickListenersAboutDeleteClick(currentItem);
                invalidate();
            }
        });
        deleteButton.setVisibility(deleteButtonIsViaiable);
        deleteButton.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                isActionDeleteButtonTouched = true;
                invalidate();
                return false;
            }
        });
        //
    }

    private void initActionButton(Context context) {
        //
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1); // , 1是可選寫的
        lp.setMargins(0, 0, 0, 0);
        actionButton = new Button(getContext());
        actionButton.setLayoutParams(lp);
        actionButton.setFocusable(false);
        actionButton.setClickable(true);
        actionButton.setTextColor(actionButtonColor);
        actionButton.setBackgroundResource(R.drawable.wheel_action);
        actionButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                isActionDeleteButtonTouched = false;
                notifyClickListenersAboutActionClick(currentItem);
                invalidate();
            }
        });
        actionButton.setVisibility(actionButtonIsViaiable);
        actionButton.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                isActionDeleteButtonTouched = true;
                invalidate();
                return false;
            }
        });
        //
    }

    // Scrolling listener
    WheelScroller.ScrollingListener scrollingListener = new WheelScroller.ScrollingListener() {
        public void onStarted() {
            if (deleteButtonIsViaiable == VISIBLE) {
                deleteButton.setVisibility(INVISIBLE);
                isSwipeLeft = false;
                invalidate();
            }
            if (actionButtonIsViaiable == VISIBLE) {
                actionButton.setVisibility(INVISIBLE);
                isSwipeRight = false;
                invalidate();
            }
            isScrollingPerformed = true;
            notifyScrollingListenersAboutStart();
        }

        public void onScroll(int distance) {
            doScroll(distance);
            int height = getHeight();
            if (scrollingOffset > height) {
                scrollingOffset = height;
                scroller.stopScrolling();
            } else if (scrollingOffset < -height) {
                scrollingOffset = -height;
                scroller.stopScrolling();
            }
        }

        public void onFinished() {
            if (isScrollingPerformed) {
                if (actionButtonIsViaiable == VISIBLE) {
                    actionButton.setVisibility(VISIBLE);
                    invalidate();
                }
                notifyScrollingListenersAboutEnd();
                isScrollingPerformed = false;
                notifyClickListenersSelected(currentItem);
            }

            scrollingOffset = 0;
            invalidate();
        }

        public void onJustify() {
            if (Math.abs(scrollingOffset) > WheelScroller.MIN_DELTA_FOR_SCROLLING) {
                scroller.scroll(scrollingOffset, 0);
            }
        }
    };

    /**
     * Set the the specified scrolling interpolator
     *
     * @param interpolator the interpolator
     */
    public void setInterpolator(Interpolator interpolator) {
        scroller.setInterpolator(interpolator);
    }

    /**
     * Gets count of visible items
     *
     * @return the count of visible items
     */
    public int getVisibleItems() {
        return visibleItems;
    }

    /**
     * Sets the desired count of visible items.
     * Actual amount of visible items depends on wheel layout parameters.
     * To apply changes and rebuild view call measure().
     *
     * @param count the desired count for visible items
     */
    public void setVisibleItems(int count) {
        visibleItems = count;
    }

    /**
     * Gets view adapter
     *
     * @return the view adapter
     */
    public WheelViewAdapter getViewAdapter() {
        return viewAdapter;
    }

    // Adapter listener
    private DataSetObserver dataObserver = new DataSetObserver() {
        @Override
        public void onChanged() {
            invalidateWheel(false);
        }

        @Override
        public void onInvalidated() {
            invalidateWheel(true);
        }
    };

    /**
     * Sets view adapter. Usually new adapters contain different views, so
     * it needs to rebuild view by calling measure().
     *
     * @param viewAdapter the view adapter
     */
    public void setViewAdapter(WheelViewAdapter viewAdapter) {
        if (this.viewAdapter != null) {
            this.viewAdapter.unregisterDataSetObserver(dataObserver);
        }
        this.viewAdapter = viewAdapter;
        if (this.viewAdapter != null) {
            this.viewAdapter.registerDataSetObserver(dataObserver);
        }

        invalidateWheel(true);
    }

    /*
    action button set
     */
    public void setActionButtonEnabled(boolean flag) {
        actionButtonIsViaiable = (flag == true ? VISIBLE : INVISIBLE);
    }

    public boolean isActionButtonEnabled() {
        return (actionButtonIsViaiable == VISIBLE ? true : false);
    }

    public void setActionButtonFontSize(float size) {
        actionButtonFontSize = size;
    }

    public void setActionButtonText(String text) {
        actionButtonText = text;
    }

    public void setActionButtonTextColor(int color) {
        actionButtonColor = color;
    }

    /*
    delete button set
    */
    public void setDeleteButtonEnabled(boolean flag) {
        deleteButtonIsViaiable = (flag == true ? VISIBLE : INVISIBLE);
    }

    public boolean isDeleteButtonEnabled() {
        return (deleteButtonIsViaiable == VISIBLE ? true : false);
    }

    public void setDeleteButtonFontSize(float size) {
        deleteButtonFontSize = size;
    }

    public void setDeleteButtonText(String text) {
        deleteButtonText = text;
    }

    public void setDeleteButtonTextColor(int color) {
        deleteButtonColor = color;
    }


    /**
     * Adds wheel changing listener
     *
     * @param listener the listener
     */
    public void addChangingListener(OnWheelChangedListener listener) {
        changingListeners.add(listener);
    }

    /**
     * Removes wheel changing listener
     *
     * @param listener the listener
     */
    public void removeChangingListener(OnWheelChangedListener listener) {
        changingListeners.remove(listener);
    }

    /**
     * Notifies changing listeners
     *
     * @param oldValue the old wheel value
     * @param newValue the new wheel value
     */
    protected void notifyChangingListeners(int oldValue, int newValue) {
        for (OnWheelChangedListener listener : changingListeners) {
            listener.onChanged(this, oldValue, newValue);
        }
    }

    /**
     * Adds wheel scrolling listener
     *
     * @param listener the listener
     */
    public void addScrollingListener(OnWheelScrollListener listener) {
        scrollingListeners.add(listener);
    }

    /**
     * Removes wheel scrolling listener
     *
     * @param listener the listener
     */
    public void removeScrollingListener(OnWheelScrollListener listener) {
        scrollingListeners.remove(listener);
    }

    /**
     * Notifies listeners about starting scrolling
     */
    protected void notifyScrollingListenersAboutStart() {
        for (OnWheelScrollListener listener : scrollingListeners) {
            listener.onScrollingStarted(this);
        }
    }

    /**
     * Notifies listeners about ending scrolling
     */
    protected void notifyScrollingListenersAboutEnd() {
        for (OnWheelScrollListener listener : scrollingListeners) {
            listener.onScrollingFinished(this);
        }
    }

    /**
     * Adds wheel clicking listener
     *
     * @param listener the listener
     */
    public void addClickingListener(OnWheelClickedListener listener) {
        clickingListeners.add(listener);
    }

    /**
     * Removes wheel clicking listener
     *
     * @param listener the listener
     */
    public void removeClickingListener(OnWheelClickedListener listener) {
        clickingListeners.remove(listener);
    }

    /**
     * Notifies listeners about clicking
     */
    protected void notifyClickListenersAboutClick(int item) {
        for (OnWheelClickedListener listener : clickingListeners) {
            listener.onItemClicked(this, item);
        }
    }

    protected void notifyClickListenersAboutSwipeRight(int item) {
        actionButton.setVisibility(INVISIBLE);
        for (OnWheelClickedListener listener : clickingListeners) {
            boolean v = listener.onItemSwipRight(this, item);
            if (v) {
                deleteButton.setVisibility(deleteButtonIsViaiable);
                isSwipeLeft = true;
                invalidate();
            }
        }
    }

    //
    protected void notifyClickListenersSelected(int item) {
        if (pastCurrentItem == item) {
            return;
        }
        pastCurrentItem = item;
        for (OnWheelClickedListener listener : clickingListeners) {
            listener.onItemSelected(this, item);
        }
    }

    protected void notifyClickListenersAboutSwipeLeft(int item) {
        isSwipeRight = true;
        deleteButton.setVisibility(INVISIBLE);
        invalidate();
        for (OnWheelClickedListener listener : clickingListeners) {
            boolean v = listener.onItemSwipLeft(this, item);
            //if (v) {
            actionButton.setVisibility(actionButtonIsViaiable);
            invalidate();
            //}

        }
    }

    protected void notifyClickListenersAboutActionClick(int item) {
        for (OnWheelClickedListener listener : clickingListeners) {
            listener.onActionClicked(this, item);
        }
    }

    protected void notifyClickListenersAboutDeleteClick(int item) {
        for (OnWheelClickedListener listener : clickingListeners) {
            listener.onDeleteClicked(this, item);
        }
    }
    protected void notifyClickListenersAboutUnderSelectBarClick(int item) {
        for (OnWheelClickedListener listener : clickingListeners) {
            listener.onItemUnderClicked(this, item);
        }
    }
    protected void notifyClickListenersAboutAboveSelectBarClick(int item) {
        for (OnWheelClickedListener listener : clickingListeners) {
            listener.onItemAboveClicked(this, item);
        }
    }
    /**
     * Gets current value
     *
     * @return the current value
     */
    public int getCurrentItem() {
        return currentItem;
    }

    /**
     * Sets the current item. Does nothing when index is wrong.
     *
     * @param index    the item index
     * @param animated the animation flag
     */
    public void setCurrentItem(int index, boolean animated) {
        if (viewAdapter == null || viewAdapter.getItemsCount() == 0) {
            return; // throw?
        }

        int itemCount = viewAdapter.getItemsCount();
        if (index < 0 || index >= itemCount) {
            if (isCyclic) {
                while (index < 0) {
                    index += itemCount;
                }
                index %= itemCount;
            } else {
                return; // throw?
            }
        }
        if (index != currentItem) {
            if (animated) {
                int itemsToScroll = index - currentItem;
                if (isCyclic) {
                    int scroll = itemCount + Math.min(index, currentItem) - Math.max(index, currentItem);
                    if (scroll < Math.abs(itemsToScroll)) {
                        itemsToScroll = itemsToScroll < 0 ? scroll : -scroll;
                    }
                }
                scroll(itemsToScroll, 0);
            } else {
                scrollingOffset = 0;

                int old = currentItem;
                currentItem = index;

                notifyChangingListeners(old, currentItem);

                invalidate();
            }
            if (!isScrollingPerformed) {
                notifyClickListenersSelected(currentItem);
            }
        }
    }

    /**
     * Sets the current item w/o animation. Does nothing when index is wrong.
     *
     * @param index the item index
     */
    public void setCurrentItem(int index) {
        setCurrentItem(index, false);
    }

    /**
     * Tests if wheel is cyclic. That means before the 1st item there is shown the last one
     *
     * @return true if wheel is cyclic
     */
    public boolean isCyclic() {
        return isCyclic;
    }

    /**
     * Set wheel cyclic flag
     *
     * @param isCyclic the flag to set
     */
    public void setCyclic(boolean isCyclic) {
        this.isCyclic = isCyclic;
        invalidateWheel(false);
    }

    /**
     * Invalidates wheel
     *
     * @param clearCaches if true then cached views will be clear
     */
    public void invalidateWheel(boolean clearCaches) {
        if (clearCaches) {
            recycle.clearAll();
            if (itemsLayout != null) {
                itemsLayout.removeAllViews();
            }
            scrollingOffset = 0;
        } else if (itemsLayout != null) {
            // cache all items
            recycle.recycleItems(itemsLayout, firstItem, new ItemsRange());
        }

        invalidate();
    }

    /**
     * Initializes resources
     */
    private void initResourcesIfNecessary() {
        if (centerDrawable == null) {
            centerDrawable = getContext().getResources().getDrawable(R.drawable.wheel_val);
        }

        if (topShadow == null) {
            topShadow = new GradientDrawable(Orientation.TOP_BOTTOM, SHADOWS_COLORS);
        }

        if (bottomShadow == null) {
            bottomShadow = new GradientDrawable(Orientation.BOTTOM_TOP, SHADOWS_COLORS);
        }

        setBackgroundResource(R.drawable.wheel_bg);
    }

    /**
     * Calculates desired height for layout
     *
     * @param layout the source layout
     * @return the desired layout height
     */
    private int getDesiredHeight(LinearLayout layout) {
        if (layout != null && layout.getChildAt(0) != null) {
            itemHeight = layout.getChildAt(0).getMeasuredHeight();
        }

        int desired = itemHeight * visibleItems - itemHeight * ITEM_OFFSET_PERCENT / 50;

        return Math.max(desired, getSuggestedMinimumHeight());
    }

    /**
     * Returns height of wheel item
     *
     * @return the item height
     */
    private int getItemHeight() {
        if (itemHeight != 0) {
            return itemHeight;
        }

        if (itemsLayout != null && itemsLayout.getChildAt(0) != null) {
            itemHeight = itemsLayout.getChildAt(0).getHeight();
            return itemHeight;
        }

        return getHeight() / visibleItems;
    }

    private void calculateButtonLayout(int widthSize, int heightSize) {
        buttonLayout.measure(MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.UNSPECIFIED),
                MeasureSpec.makeMeasureSpec(heightSize, MeasureSpec.UNSPECIFIED));
    }

    /**
     * Calculates control width and creates text layouts
     *
     * @param widthSize the input layout width
     * @param mode      the layout mode
     * @return the calculated control width
     */
    private int calculateLayoutWidth(int widthSize, int mode) {
        initResourcesIfNecessary();

        // TODO: make it static
        itemsLayout.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        itemsLayout.measure(MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.UNSPECIFIED),
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
        int width = itemsLayout.getMeasuredWidth();

        if (mode == MeasureSpec.EXACTLY) {
            width = widthSize;
        } else {
            width += 2 * PADDING;

            // Check against our minimum width
            width = Math.max(width, getSuggestedMinimumWidth());

            if (mode == MeasureSpec.AT_MOST && widthSize < width) {
                width = widthSize;
            }
        }

        itemsLayout.measure(MeasureSpec.makeMeasureSpec(width - 2 * PADDING, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));

        return width;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        buildViewForMeasuring();
        buildButtonLayoutForMeasuring();

        int width = calculateLayoutWidth(widthSize, widthMode);
        calculateButtonLayout(widthSize, heightSize);

        int height;
        if (heightMode == MeasureSpec.EXACTLY) {
            height = heightSize;
        } else {
            height = getDesiredHeight(itemsLayout);

            if (heightMode == MeasureSpec.AT_MOST) {
                height = Math.min(height, heightSize);
            }
        }
//
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        layout(r - l, b - t);
        layoutDeleteButton(r - l, b - t);
        layoutActionButton(r - l, b - t);
    }

    /**
     * Sets layouts width and height
     *
     * @param width  the layout width
     * @param height the layout height
     */
    private void layout(int width, int height) {
        int itemsWidth = width - 2 * PADDING;

        itemsLayout.layout(0, 0, itemsWidth, height);
        //
    }

    private void layoutDeleteButton(int width, int height) {
        //result/metrics.scaledDensity = value
        //float value = getItemHeight() / getScaleDensity(getContext());
        //deleteButton.setTextSize(value - 12);
        deleteButton.setTextSize(deleteButtonFontSize);
        deleteButton.setText(deleteButtonText);
        deleteButton.setLines(1);
        deleteButton.setGravity(Gravity.CENTER);
        deleteButton.setTextColor(deleteButtonColor);
        //auto adjust width and height by fontsize
        deleteButtonFontWidth = deleteButton.getMeasuredWidth();
        deleteButtonFontHeight = deleteButton.getMeasuredHeight();
        //
        int center = height / 2;
        int offset = deleteButtonFontHeight / 2;
        int centerBarHeight = (int) (getItemHeight() / 2 * 1.2);
        int padding = Math.abs(centerBarHeight - offset);

        deleteButton.setTop(center - offset);
        deleteButton.setBottom(center + offset);
        deleteButton.setLeft(width - deleteButtonFontWidth - padding);
        deleteButton.setRight(width - padding);

    }

    private void layoutActionButton(int width, int height) {
        //result/metrics.scaledDensity = value
        //float value = getItemHeight() / getScaleDensity(getContext());
        //deleteButton.setTextSize(value - 12);
        actionButton.setTextSize(actionButtonFontSize);
        actionButton.setText(actionButtonText);
        actionButton.setLines(1);
        actionButton.setGravity(Gravity.CENTER);
        actionButton.setTextColor(actionButtonColor);
        //auto adjust width and height by fontsize
        actionButtonFontWidth = actionButton.getMeasuredWidth();
        actionButtonFontHeight = actionButton.getMeasuredHeight();
        //
        int center = height / 2;
        int offset = actionButtonFontHeight / 2;
        int centerBarHeight = (int) (getItemHeight() / 2 * 1.2);
        int padding = 14;//centerBarHeight - offset;

        actionButton.setTop(center - offset);
        actionButton.setBottom(center + offset);
        actionButton.setLeft(width - actionButtonFontHeight - padding);
        actionButton.setRight(width - padding);
        if (!isSwipeLeft) {
            if (actionButtonIsViaiable == VISIBLE) {
                actionButton.setVisibility(VISIBLE);
                invalidate();
            }
        }

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (viewAdapter != null && viewAdapter.getItemsCount() > 0) {
            updateView();

            drawItems(canvas);
            drawButtonLayout(canvas);
            drawCenterRect(canvas);


        }

        //drawShadows(canvas);
    }

    private void drawButtonLayout(Canvas canvas) {
        //deleteButton.setHeight(getItemHeight());
        if ((deleteButtonIsViaiable == VISIBLE) && (deleteButton.getVisibility() == VISIBLE)) {
            buttonLayout.draw(canvas);
        } else if ((actionButtonIsViaiable == VISIBLE) && (actionButton.getVisibility() == VISIBLE)) {
            buttonLayout.draw(canvas);
        }
    }

    /**
     * Draws shadows on top and bottom of control
     *
     * @param canvas the canvas for drawing
     */
    private void drawShadows(Canvas canvas) {
        int height = (int) (1.5 * getItemHeight());
        topShadow.setBounds(0, 0, getWidth(), height);
        topShadow.draw(canvas);

        bottomShadow.setBounds(0, getHeight() - height, getWidth(), getHeight());
        bottomShadow.draw(canvas);
    }

    /**
     * Draws items
     *
     * @param canvas the canvas for drawing
     */
    private void drawItems(Canvas canvas) {
        canvas.save();

        int top = (currentItem - firstItem) * getItemHeight() + (getItemHeight() - getHeight()) / 2;
        //smooth scrolling
        canvas.translate(PADDING, -top + scrollingOffset);
        itemsLayout.draw(canvas);

        canvas.restore();
    }

    /**
     * Draws rect for current value
     *
     * @param canvas the canvas for drawing
     */
    private void drawCenterRect(Canvas canvas) {
        int center = getHeight() / 2;
        int offset = (int) (getItemHeight() / 2 * 1.2);
        centerDrawable.setBounds(0, center - offset, getWidth(), center + offset);
        centerDrawable.draw(canvas);

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isEnabled() || getViewAdapter() == null) {
            return true;
        }
        float deltaX;
        float deltaY;
        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:
                downX = event.getX();
                downY = event.getY();
                break;

            case MotionEvent.ACTION_MOVE:

                if (getParent() != null) {
                    getParent().requestDisallowInterceptTouchEvent(true);
                }

                break;

            case MotionEvent.ACTION_UP:
                if (isScrollingPerformed) {
                    break;
                }
                // when scrolling ,skip it
                upX = event.getX();
                upY = event.getY();
                deltaX = downX - upX;
                deltaY = downY - upY;
                //
                if ((Math.abs(deltaX) > 40) && (Math.abs(deltaY) < getItemHeight())
                        ) {
                    // left or right
                    //Log.i("TAG", "Swipe in downX:" + downX + " downY:" + downY);
                    if ((deltaX < 0) && isSelectBarArea(downX,downY)) {
                        //Log.i("TAG", "Swipe contains currentItem:" + currentItem);
                        downX = event.getX();
                        downY = event.getY();
                        if (isValidItemIndex(currentItem)) {
                            notifyClickListenersAboutSwipeRight(currentItem);
                        }
                        return true;
                    }
                    if ((deltaX > 0) && isSelectBarArea(downX,downY)) {
                        //Log.i("TAG", "Swipe contains currentItem:" + currentItem);
                        downX = event.getX();
                        downY = event.getY();
                        if (isValidItemIndex(currentItem)) {
                            notifyClickListenersAboutSwipeLeft(currentItem);
                        }
                        return true;
                    }
                } else {
                    int distance = (int) event.getY() - getHeight() / 2;
                    if (distance > 0) {
                        distance += getItemHeight() / 2;
                    } else {
                        distance -= getItemHeight() / 2;
                    }
                    int items = distance / getItemHeight();
                    if (isValidItemIndex(currentItem + items)) {
                        // Swipe Left don't callback Click
                        if (isSelectBarClicked(items,downX,downY)) {
                            notifyClickListenersAboutClick(currentItem + items);
                        }

                    }
                    if ((items >0) && (!(isSelectBarArea(downX, downY)))) {
                        // clicked under SelectBar
                        notifyClickListenersAboutUnderSelectBarClick(currentItem);
                    }
                    if ((items <0) && (!(isSelectBarArea(downX,downY)))){
                        // clicked above SelectBar
                        notifyClickListenersAboutAboveSelectBarClick(currentItem);
                    }
                    Log.i("TAG", "items:" + items + " currentItem:" + currentItem);

                }
                break;

        }
        return scroller.onTouchEvent(event);

    }

    private boolean isSelectBarArea(float x, float y) {
        return (centerDrawable.getBounds().contains((int)x,(int)y));
    }
/*
check if Action/Delete button are clicked or SelectBar
when ACTION_UP is on SelectBar , item == 0
 */
    private boolean isSelectBarClicked(int item,float x, float y) {
        boolean result = false;
        if (isSelectBarArea(downX,downY) && (item == 0)) {
            if ((deleteButton.getVisibility() == VISIBLE) && (x < deleteButton.getLeft())){
                result = true;
            } else if ((actionButton.getVisibility() == VISIBLE)  && (x < actionButton.getLeft())){
                result = true;
            } else {
                result = true;
            }
        }
        return result;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if ((deleteButtonIsViaiable == VISIBLE) && (deleteButton.getVisibility() == VISIBLE)) {
            buttonLayout.dispatchTouchEvent(event);
        } else if ((actionButtonIsViaiable == VISIBLE) && (actionButton.getVisibility() == VISIBLE)) {
            buttonLayout.dispatchTouchEvent(event);
        }
        super.dispatchTouchEvent(event);
        return true;
    }

    /**
     * Scrolls the wheel
     *
     * @param delta the scrolling value
     */
    private void doScroll(int delta) {
        scrollingOffset += delta;


        int itemHeight = getItemHeight();
        int count = scrollingOffset / itemHeight;

        int pos = currentItem - count;
        int itemCount = viewAdapter.getItemsCount();

        int fixPos = scrollingOffset % itemHeight;
        //Log.i("TAG","scrollingOffset:" + scrollingOffset + " count:" + count + " fixPos:" + fixPos + " pos:" + pos + " itemCount:" + itemCount);
        if (Math.abs(fixPos) <= itemHeight / 2) {
            fixPos = 0;
        }
        if (isCyclic && itemCount > 0) {
            if (fixPos > 0) {
                pos--;
                count++;
            } else if (fixPos < 0) {
                pos++;
                count--;
            }
            // fix position by rotating
            while (pos < 0) {
                pos += itemCount;
            }
            pos %= itemCount;
        } else {
            //
            if (pos < 0) {
                count = currentItem;
                pos = 0;
            } else if (pos >= itemCount) {
                count = currentItem - itemCount + 1;
                pos = itemCount - 1;
            } else if (pos > 0 && fixPos > 0) {
                pos--;
                count++;
            } else if (pos < itemCount - 1 && fixPos < 0) {
                pos++;
                count--;
            }
        }

        int offset = scrollingOffset;
        if (pos != currentItem) {
            setCurrentItem(pos, false);
        } else {
            invalidate();
        }

        // update offset
        scrollingOffset = offset - count * itemHeight;
        if (scrollingOffset > getHeight()) {
            scrollingOffset = scrollingOffset % getHeight() + getHeight();
        }
    }

    /**
     * Scroll the wheel
     *
     * @param itemsToSkip items to scroll
     * @param time        scrolling duration
     */
    public void scroll(int itemsToScroll, int time) {
        int distance = itemsToScroll * getItemHeight() - scrollingOffset;
        scroller.scroll(distance, time);
    }

    /**
     * Calculates range for wheel items
     *
     * @return the items range
     */
    private ItemsRange getItemsRange() {
        if (getItemHeight() == 0) {
            return null;
        }

        int first = currentItem;
        int count = 1;

        while (count * getItemHeight() < getHeight()) {
            first--;
            count += 2; // top + bottom items
        }

        if (scrollingOffset != 0) {
            if (scrollingOffset > 0) {
                first--;
            }
            count++;

            // process empty items above the first or below the second
            int emptyItems = scrollingOffset / getItemHeight();
            first -= emptyItems;
            count += Math.asin(emptyItems);
        }
        return new ItemsRange(first, count);
    }

    /**
     * Rebuilds wheel items if necessary. Caches all unused items.
     *
     * @return true if items are rebuilt
     */
    private boolean rebuildItems() {
        boolean updated = false;
        ItemsRange range = getItemsRange();
        if (itemsLayout != null) {
            int first = recycle.recycleItems(itemsLayout, firstItem, range);
            updated = firstItem != first;
            firstItem = first;
        } else {
            createItemsLayout();
            updated = true;
        }

        if (!updated) {
            updated = firstItem != range.getFirst() || itemsLayout.getChildCount() != range.getCount();
        }

        if (firstItem > range.getFirst() && firstItem <= range.getLast()) {
            for (int i = firstItem - 1; i >= range.getFirst(); i--) {
                if (!addViewItem(i, true)) {
                    break;
                }
                firstItem = i;
            }
        } else {
            firstItem = range.getFirst();
        }

        int first = firstItem;
        for (int i = itemsLayout.getChildCount(); i < range.getCount(); i++) {
            if (!addViewItem(firstItem + i, false) && itemsLayout.getChildCount() == 0) {
                first++;
            }
        }
        firstItem = first;

        return updated;
    }

    /**
     * Updates view. Rebuilds items and label if necessary, recalculate items sizes.
     */
    private void updateView() {
        if (rebuildItems()) {
            calculateLayoutWidth(getWidth(), MeasureSpec.EXACTLY);
            layout(getWidth(), getHeight());
        }
    }

    /**
     * Creates item layouts if necessary
     */
    private void createItemsLayout() {
        if (itemsLayout == null) {
            itemsLayout = new LinearLayout(getContext());
            itemsLayout.setOrientation(LinearLayout.VERTICAL);
        }
    }

    /**
     * Builds view for measuring
     */
    private void buildViewForMeasuring() {
        // clear all items
        if (itemsLayout != null) {
            recycle.recycleItems(itemsLayout, firstItem, new ItemsRange());
        } else {
            createItemsLayout();
        }

        // add views
        int addItems = visibleItems / 2;
        for (int i = currentItem + addItems; i >= currentItem - addItems; i--) {
            if (addViewItem(i, true)) {
                firstItem = i;
            }
        }
    }

    private void buildButtonLayoutForMeasuring() {
        buttonLayout.removeAllViews();
        buttonLayout.addView(deleteButton);
        buttonLayout.addView(actionButton);
        invalidate();
    }

    /**
     * Adds view for item to items layout
     *
     * @param index the item index
     * @param first the flag indicates if view should be first
     * @return true if corresponding item exists and is added
     */
    private boolean addViewItem(int index, boolean first) {
        View view = getItemView(index);
        if (view != null) {
            if (first) {
                itemsLayout.addView(view, 0);
            } else {
                itemsLayout.addView(view);
            }

            return true;
        }

        return false;
    }

    /**
     * Checks whether intem index is valid
     *
     * @param index the item index
     * @return true if item index is not out of bounds or the wheel is cyclic
     */
    private boolean isValidItemIndex(int index) {
        return viewAdapter != null && viewAdapter.getItemsCount() > 0 &&
                (isCyclic || index >= 0 && index < viewAdapter.getItemsCount());
    }

    /**
     * Returns view for specified item
     *
     * @param index the item index
     * @return item view or empty view if index is out of bounds
     */
    private View getItemView(int index) {
        if (viewAdapter == null || viewAdapter.getItemsCount() == 0) {
            return null;
        }
        int count = viewAdapter.getItemsCount();
        if (!isValidItemIndex(index)) {
            return viewAdapter.getEmptyItem(recycle.getEmptyItem(), itemsLayout);
        } else {
            while (index < 0) {
                index = count + index;
            }
        }

        index %= count;
        return viewAdapter.getItem(index, recycle.getItem(), itemsLayout);
    }

    /**
     * Stops scrolling
     */
    public void stopScrolling() {
        scroller.stopScrolling();
    }
    //Tool

    /**
     * Covert dp to px
     *
     * @param dp
     * @param context
     * @return pixel
     */
    public static float convertDpToPixel(float dp, Context context) {
        float px = dp * getDensity(context);
        return px;
    }

    /**
     * Covert px to dp
     *
     * @param px
     * @param context
     * @return dp
     */
    public static float convertPixelToDp(float px, Context context) {
        float dp = px / getDensity(context);
        return dp;
    }

    /**
     * 取得螢幕密度
     * 120dpi = 0.75
     * 160dpi = 1 (default)
     * 240dpi = 1.5
     *
     * @param context
     * @return
     */
    public static float getDensity(Context context) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return metrics.density;
    }

    public static float getScaleDensity(Context context) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return metrics.scaledDensity;
    }
}
