package com.plinehan.wakeum;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.OverlayItem;

public class AlarmItem extends OverlayItem {
	final long id;
	
	public AlarmItem(long id, GeoPoint point, String title, String snippet) {
		super(point, title, snippet);
		this.id = id;
	}
}
