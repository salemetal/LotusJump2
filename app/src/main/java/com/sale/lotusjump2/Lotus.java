package com.sale.lotusjump2;

import android.graphics.Bitmap;
import android.graphics.Canvas;

/**
 * Created by Sale on 4.10.2016..
 */
public class Lotus extends GameObject{

    private Bitmap image;
    private boolean water;

    public Lotus(Bitmap res, int x,  boolean water)
    {
        this.x = x;
        this.y = Settings.LOTUS_Y;
        height = Settings.LOTUS_H;
        width = Settings.LOTUS_W;
        this.water = water;

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

    public boolean getWater()
    {
        return water;
    }
}
