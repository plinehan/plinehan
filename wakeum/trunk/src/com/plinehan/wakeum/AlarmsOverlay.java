/*
 * Copyright (C) 2008  Patrick F. Linehan
 * 
 * See the LICENSE file for details.
 */
package com.plinehan.wakeum;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import junit.framework.Assert;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Paint.Style;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;
import com.google.android.maps.Projection;

class AlarmsOverlay extends ItemizedOverlay<OverlayItem>
{
    private final Wakeum wakeum;
    private final DbHelper dbHelper;
    private final Paint fillPaint;
    private final Paint strokePaint;
    private final Drawable upppMarker;
    private final Drawable downMarker;
    private Alarm[] alarms;
    private final GestureDetector gestureDetector;

    public AlarmsOverlay(Wakeum wakeum)
    {
        super(boundCenterBottom(wakeum.getResources().getDrawable(R.drawable.pin)));
        //super.setDrawFocusedItem(false);
        
        this.wakeum = wakeum;
        this.dbHelper = new DbHelper(wakeum);

        this.fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.fillPaint.setARGB(64, 255, 119, 107);
        
        this.strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.strokePaint.setARGB(255, 255, 119, 107);
        this.strokePaint.setStyle(Style.STROKE);
        this.strokePaint.setStrokeWidth(5);

        this.upppMarker = wakeum.getResources().getDrawable(android.R.drawable.arrow_up_float);
        this.downMarker = wakeum.getResources().getDrawable(android.R.drawable.arrow_down_float);
        Assert.assertEquals(this.upppMarker.getIntrinsicWidth(), this.downMarker.getIntrinsicWidth());
        Assert.assertEquals(this.upppMarker.getIntrinsicHeight(), this.downMarker.getIntrinsicHeight());
        
        this.alarms = Alarm.getAll(this.dbHelper);
        
        this.gestureDetector = new GestureDetector(new AlarmOnGestureListener(this));
        
        populate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event, MapView mapView)
    {
        return this.gestureDetector.onTouchEvent(event);
    }
    
    @Override
    protected OverlayItem createItem(int i)
    {
        return this.alarms[i].toOverlayItem();
    }

    @Override
    public void draw(Canvas canvas, MapView mapView, boolean shadow)
    {
        OverlayItem item = getFocus();
        if (item != null)
        {
            Projection projection = mapView.getProjection();
            GeoPoint geoPoint = item.getPoint();
            Point point = projection.toPixels(geoPoint, null);
            
            final int CIRCLE_RADIUS = 50;
            
            drawCircle(canvas, this.fillPaint, point, CIRCLE_RADIUS);
            drawCircle(canvas, this.strokePaint, point, CIRCLE_RADIUS);
            drawCircle(canvas, this.strokePaint, point, 3);
            
            int intrinsicWidth = this.upppMarker.getIntrinsicWidth();
            int intrinsicHeight = this.upppMarker.getIntrinsicHeight();
            
            Rect arrowMarkers = new Rect(point.x, point.y + CIRCLE_RADIUS, point.x + intrinsicWidth, point.y + CIRCLE_RADIUS + intrinsicHeight);
            arrowMarkers.offset(-intrinsicWidth / 2, 0);

            arrowMarkers.offset(0, 1);
            this.downMarker.setBounds(arrowMarkers);
            this.downMarker.draw(canvas);

            arrowMarkers.offset(0, -(intrinsicHeight + (int)this.strokePaint.getStrokeWidth() - 1));
            
            this.upppMarker.setBounds(arrowMarkers);
            this.upppMarker.draw(canvas);
        }   
        super.draw(canvas, mapView, shadow);
    }

    private void drawCircle(Canvas canvas, Paint paint, Point center, int radius)
    {
        RectF circleRect = new RectF(center.x - radius, center.y - radius, center.x + radius, center.y + radius);
        canvas.drawOval(circleRect, paint);
    }
    
    @Override
    protected boolean onTap(int i)
    {
        OverlayItem item = getItem(i);
        setFocus(item);
        MapView mapView = (MapView)this.wakeum.findViewById(R.id.mapview);
        mapView.getController().animateTo(item.getPoint());
        Toast.makeText(
                        this.wakeum,
                        item.getSnippet(),
                        Toast.LENGTH_SHORT).show();
        return true;
    }

    @Override
    public int size()
    {
        return this.alarms.length;
    }

    public void deleteAll()
    {
        this.alarms = Alarm.deleteAll(this.dbHelper);
        populate();
    }

    public void create(GeoPoint mapCenter)
    {
        Geocoder geocoder = new Geocoder(this.wakeum);
        List<Address> addresses;
        try
        {
            addresses = geocoder.getFromLocation(mapCenter.getLatitudeE6() / 1000000.0, mapCenter.getLongitudeE6() / 1000000.0, 1);
        }
        catch (IOException e)
        {
            Log.e(Wakeum.TAG, e.toString());
            addresses = Collections.emptyList();
        }
        final String name;
        if (addresses.size() == 0)
        {
            Log.e(Wakeum.TAG, "No address could be found.");
            name = "Unknown location";
        }
        else
        {
            int size = addresses.get(0).getMaxAddressLineIndex() + 1;
            StringBuilder string = new StringBuilder();
            for (int i = 0; i < size; i++)
            {
                String addressLine = addresses.get(0).getAddressLine(i);
                if (string.length() != 0)
                {
                    string.append(", ");
                }
                string.append(addressLine);
            }
            name = string.toString();
        }
        
        this.alarms = Alarm.create(this.dbHelper, name, mapCenter);
        Toast.makeText(
                        this.wakeum,
                        name,
                        Toast.LENGTH_SHORT).show();
        populate();
    }

    /**
     * In drag alarm mode, the focused alarm is dragged.
     */
    private boolean dragAlarm = false;
    
    public void setDragAlarm(boolean dragAlarm)
    {
        this.dragAlarm = dragAlarm;        
    }
}
