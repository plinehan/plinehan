/*
 * Copyright (C) 2008  Patrick F. Linehan
 * 
 * See the LICENSE file for details.
 */
package com.plinehan.wakeum;

import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;

class AlarmsOverlay extends ItemizedOverlay<OverlayItem>
{
    private final Wakeum wakeum;
    private final Drawable marker;
    private final DbHelper dbHelper;

    public AlarmsOverlay(Wakeum wakeum, Drawable marker)
    {
        super(marker);
        this.wakeum = wakeum;
        this.marker = marker;
        this.dbHelper = new DbHelper(wakeum);

        populate();
    }

    void refresh()
    {
        this.populate();
    }

    @Override
    protected OverlayItem createItem(int i)
    {
        return Alarm.getAll(this.dbHelper).get(i).toOverlayItem();
    }

    @Override
    public void draw(Canvas canvas, MapView mapView, boolean shadow)
    {
        super.draw(canvas, mapView, shadow);

        boundCenterBottom(this.marker);
    }

    @Override
    protected boolean onTap(int i)
    {
        Toast.makeText(
                        this.wakeum,
                        Alarm.getAll(this.dbHelper).get(i).toOverlayItem().getSnippet(),
                        Toast.LENGTH_SHORT).show();
        return true;
    }

    @Override
    public int size()
    {
        return Alarm.getAll(this.dbHelper).size();
    }

    public void deleteAll()
    {
        Alarm.deleteAll(this.dbHelper);
    }

    public void create(GeoPoint mapCenter)
    {
        Alarm.create(this.dbHelper, mapCenter);
    }
}
