package com.example.smartberry;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

final class ChartDrawable extends Drawable {
    private static final int TEMPERATURE_COLOR = Color.rgb(53, 167, 54);
    private static final int HUMIDITY_COLOR = Color.rgb(139, 53, 200);
    private final JSONArray points;
    private final boolean temperature;
    private final boolean humidity;
    private final long start, end;
    private final long[] timestamps;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    ChartDrawable(JSONArray points, boolean temperature, boolean humidity) {
        this(points, temperature, humidity, 0, 0);
    }

    ChartDrawable(JSONArray points, boolean temperature, boolean humidity, long start, long end) {
        this.points = points;
        timestamps = new long[points.length()];
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ROOT);
        for (int i = 0; i < points.length(); i++) {
            JSONObject item = points.optJSONObject(i);
            if (item == null) continue;
            Date date = format.parse(item.optString("periodo"), new ParsePosition(0));
            if (date != null) timestamps[i] = date.getTime();
        }
        this.start = start != 0 || timestamps.length == 0 ? start : timestamps[0];
        this.end = end != 0 || timestamps.length == 0 ? end : timestamps[timestamps.length - 1];
        this.temperature = temperature;
        this.humidity = humidity;
    }

    @Override
    public void draw(Canvas canvas) {
        if (temperature) drawSeries(canvas, "temperaturaMedia", TEMPERATURE_COLOR);
        if (humidity) drawSeries(canvas, "umidadeMedia", HUMIDITY_COLOR);
    }

    private void drawSeries(Canvas canvas, String field, int color) {
        double min = Double.POSITIVE_INFINITY;
        double max = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < points.length(); i++) {
            double value = valueAt(i, field);
            if (Double.isFinite(value)) {
                min = Math.min(min, value);
                max = Math.max(max, value);
            }
        }

        if (temperature && humidity) {
            min = Math.min(min, 0);
            max = Math.max(max, 100);
        }
        paint.setColor(color);
        paint.setStrokeWidth(3);
        float previousX = 0;
        float previousY = Float.NaN;
        for (int i = 0; i < points.length(); i++) {
            double value = valueAt(i, field);
            if (!Double.isFinite(value)) {
                previousY = Float.NaN;
                continue;
            }
            float fraction = end <= start || timestamps[i] == 0 ? 0.5f
                    : Math.max(0, Math.min(1, (timestamps[i] - start) / (float) (end - start)));
            float x = getBounds().left + 6 + (getBounds().width() - 12) * fraction;
            float y = getBounds().bottom - 8 - (getBounds().height() - 16)
                    * (float) (min == max ? 0.5 : (value - min) / (max - min));
            if (Float.isFinite(previousY)) canvas.drawLine(previousX, previousY, x, y, paint);
            canvas.drawCircle(x, y, 4, paint);
            previousX = x;
            previousY = y;
        }
    }

    private double valueAt(int index, String field) {
        JSONObject point = points.optJSONObject(index);
        return point == null ? Double.NaN : point.optDouble(field, Double.NaN);
    }

    @Override public void setAlpha(int alpha) { paint.setAlpha(alpha); }
    @Override public void setColorFilter(ColorFilter filter) { paint.setColorFilter(filter); }
    @Override public int getOpacity() { return PixelFormat.TRANSLUCENT; }
}
