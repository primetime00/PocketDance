package com.kegel.pocketdance.ui.watch;

import android.view.GestureDetector;
import android.view.MotionEvent;

public abstract class DanceVideoGestureListener extends GestureDetector.SimpleOnGestureListener {
    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        if (velocityX <= -4000) {
            onFlingBack();
        } else if (velocityX >= 4000)
        {
            onFlingForward();
        }
        return super.onFling(e1, e2, velocityX, velocityY);
    }

    @Override
    public boolean onDoubleTap(MotionEvent e) {
        onDoublePress(e);
        return super.onDoubleTap(e);
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        onTap();
        return super.onSingleTapConfirmed(e);
    }

    public abstract boolean onDoublePress(MotionEvent e);
    public abstract boolean onFlingBack();
    public abstract boolean onFlingForward();
    public abstract boolean onTap();
}
