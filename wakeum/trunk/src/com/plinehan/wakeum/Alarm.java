/*
 * Copyright (C) 2008  Patrick F. Linehan
 * 
 * See the LICENSE file for details.
 */
package com.plinehan.wakeum;

import java.util.ArrayList;
import java.util.List;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.OverlayItem;

public class Alarm
{
    //private Long id;
    private final String name;
    private final int latitudeE6;
    private final int longitudeE6;

    Alarm(String name, int latitudeE6, int longitudeE6)
    {
        this.name = name;
        this.latitudeE6 = latitudeE6;
        this.longitudeE6 = longitudeE6;
    }

    public static Alarm[] getAll(DbHelper dbHelper)
    {
        List<Alarm> alarms = new ArrayList<Alarm>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor =
                        db.query(
                                        "alarms",
                                        new String[]
                                        {
                                                        "id",
                                                        "name",
                                                        "latitudeE6",
                                                        "longitudeE6" },
                                        null,
                                        null,
                                        null,
                                        null,
                                        null);
        for (int i = 0; i < cursor.getCount(); i++)
        {
            cursor.moveToPosition(i);
            alarms.add(new Alarm(
                            cursor.getString(1),
                            (int)cursor.getLong(2),
                            (int)cursor.getLong(3)));
        }

        cursor.close();
        db.close();

        return alarms.toArray(new Alarm[0]);
    }

    public static Alarm[] deleteAll(DbHelper dbHelper)
    {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        database.delete("alarms", null, null);
        database.close();
        Alarm[] alarms = getAll(dbHelper);
        if (alarms.length != 0)
        {
            throw new IllegalArgumentException();
        }
        return alarms;
    }

    public OverlayItem toOverlayItem()
    {
        return new OverlayItem(
                        new GeoPoint(this.latitudeE6, this.longitudeE6),
                        this.name,
                        this.name);
    }

    public static Alarm[] create(DbHelper dbHelper, String pName, GeoPoint center)
    {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        database
                        .execSQL(
                                        "INSERT INTO alarms (name, latitudeE6, longitudeE6) VALUES (?,?,?)",
                                        new Object[]
                                        {
                                                        pName,
                                                        Long
                                                                        .valueOf(center
                                                                                        .getLatitudeE6()),
                                                        Long
                                                                        .valueOf(center
                                                                                        .getLongitudeE6()) });
        database.close();
        return getAll(dbHelper);
    }
}
