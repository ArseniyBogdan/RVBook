package com.example.recyclerviewtest;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ScrollView;

public final class MultiScrollView extends ScrollView {
    //Координаты текущего касания
    private int origX, origY;

    public MultiScrollView(Context context) {
        super(context);
    }

    public MultiScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MultiScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean canScrollHorizontally(int direction) {
        return true;
    }

    @Override
    public boolean canScrollVertically(int direction) {
        return true;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            origX = (int) ev.getX();
            origY = (int) ev.getY();
        } else if (ev.getAction() == MotionEvent.ACTION_MOVE) {
            float deltaX = Math.abs(ev.getX() - origX);
            float deltaY = Math.abs(ev.getY() - origY);
            //Если палец сдвинулся на величину больше, чем 60 px, то происходит скроллинг
            float THRESHOLD = 60;
            return deltaX >= THRESHOLD || deltaY >= THRESHOLD;
        }
        return false;
    }
}
