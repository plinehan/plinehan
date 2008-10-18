/*
 * Copyright (C) 2008  Patrick F. Linehan
 * 
 * See the LICENSE file for details.
 */
package com.plinehan.wakeum;

import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;

class AlarmOnGestureListener extends GestureDetector.SimpleOnGestureListener
{
    private final AlarmsOverlay alarmsOverlay;
    
    public AlarmOnGestureListener(AlarmsOverlay alarmsOverlay)
    {
        this.alarmsOverlay = alarmsOverlay;
    }
    
    @Override
    public void onLongPress(MotionEvent e)
    {
        Log.w(Wakeum.TAG, "onLongPress");
        this.alarmsOverlay.setLongPress(e);
    }
}
