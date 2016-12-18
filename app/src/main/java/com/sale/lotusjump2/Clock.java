package com.sale.lotusjump2;

import android.graphics.Bitmap;
import android.graphics.Canvas;

/**
 * Created by Sale on 5.10.2016..
 */
public class Clock extends GameObject{

    private Bitmap image;

    public Clock(Bitmap res, int x)
    {
        this.x = x;
        y = Settings.CLOCK_Y;
        height = Settings.CLOCK_H;
        width = Settings.CLOCK_W;

        image = res;

    }

    public void draw(Canvas canvas)
    {
        canvas.drawBitmap(image, x, y, null);
    }

    public void update(int steps)
    {
        x -= Settings.BGR_SPEED * steps;
    }

    public int getX()
    {
        return x;
    }
}
