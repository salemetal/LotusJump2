package com.sale.lotusjump2;

import android.graphics.Bitmap;
import android.graphics.Canvas;

/**
 * Created by Sale on 3.10.2016..
 */
public class Background {

    private Bitmap image;
    private int x, y;

    public  Background(Bitmap res)
    {
        image = res;
    }

    public void update(int steps)
    {

        x -= Settings.BGR_SPEED * steps;
        if (x < -GamePanel.WIDTH)
        {
            x = 0;
        }


    }

    public void draw(Canvas canvas)
    {
        canvas.drawBitmap(image, x, y, null);
        if (x<0)
        {
            canvas.drawBitmap(image, x + GamePanel.WIDTH, y, null);
        }
    }
}
