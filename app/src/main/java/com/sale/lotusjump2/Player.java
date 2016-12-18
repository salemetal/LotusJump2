package com.sale.lotusjump2;

import android.graphics.Bitmap;
import android.graphics.Canvas;

/**
 * Created by Sale on 4.10.2016..
 */
public class Player extends GameObject{

    private Bitmap image;
    private int score;
    private boolean playing;
    private boolean up;
    private boolean down;

    public Player(Bitmap res)
    {
        x = Settings.PLAYER_X;
        y = Settings.PLAYER_Y;
        dy = 0;
        score = 0;
        height = Settings.PLAYER_H;
        width = Settings.PLAYER_W;

        image = res;

    }

    public void update()
    {
        if (up)
        {
            dy -= Settings.PLR_JUMP_ACLR;

            if(y < Settings.JUMP_HEIGHT)
            {
                up = false;
                down = true;
            }


        }
        if(down)
        {
            dy += Settings.PLR_JUMP_ACLR;
            if (y == Settings.PLAYER_Y)
            {
                down = false;
                dy = 0;
                return;
            }
        }

        y += dy * 2;
        dy = 0;
    }

    public void draw(Canvas canvas)
    {
        canvas.drawBitmap(image, x, y, null);
    }

    public int getScore(){return score;}

    public boolean getPlaying(){return playing;}

    public void setPlaying(boolean b)
    {
        playing = b;
    }

    public boolean getUp()
    {
       return up;
    }

    public void setUp(boolean b)
    {
        up = b;
    }

    public boolean getDown()
    {
        return down;
    }

    public void setDown(boolean b)
    {
        down = b;
    }

    public boolean getMoving()
    {
        if (this.getUp() || this.getDown()) return true;
        return false;
    }

    public void setScore(int score)
    {
        this.score = score;
    }

    public void resetScore()
    {
        score = 0;
    }
}
