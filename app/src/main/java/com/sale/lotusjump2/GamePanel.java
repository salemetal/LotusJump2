package com.sale.lotusjump2;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.Random;


/**
 * Created by Sale on 3.10.2016..
 */
public class GamePanel extends SurfaceView implements SurfaceHolder.Callback
{
    public static final int WIDTH = 1280;
    public static final int HEIGHT = 720;
    private MainThread thread;
    private Background bg;
    private Player player;
    private ArrayList<Lotus> lotuses;
    private Clock clock = null;
    private Random rand = new Random();
    private boolean newGameCreated;

    private int steps;
    private long startReset;
    private boolean reset;
    private boolean dissapear;
    private boolean started;
    private int best;

    private long timer = Settings.TIMER;
    private long startTime;

    public static final String MyPREFERENCES = "MyPrefs" ;
    SharedPreferences sharedpreferences;

    final MediaPlayer mpJump = MediaPlayer.create(this.getContext(), R.raw.jump_sound);
    final MediaPlayer mpDrop = MediaPlayer.create(this.getContext(), R.raw.drop_sound);
    final MediaPlayer mpTime = MediaPlayer.create(this.getContext(), R.raw.time_sound);
    final MediaPlayer mpClock = MediaPlayer.create(this.getContext(), R.raw.clock_sound);
    public GamePanel(Context context) {
        super(context);

        //add the callback to the surfaceholder to intercept events
        getHolder().addCallback(this);

        //make gamePanel focusable so it can handle events
        setFocusable(true);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

        boolean retry = true;
        int counter = 0;

        while (retry && counter <1000) {
            counter++;
            try {
                thread.setRunning(false);
                thread.join();
                retry = false;
                thread = null;
                thread = null;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

        bg = new Background(BitmapFactory.decodeResource(getResources(), R.drawable.background));
        player = new Player(BitmapFactory.decodeResource(getResources(), R.drawable.elephant));

        lotuses = new ArrayList<Lotus>();

        thread = new MainThread(getHolder(), this);
        //we can safely start the game loop
        thread.setRunning(true);
        thread.start();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        if(event.getAction() == MotionEvent.ACTION_DOWN)
        {
            if (!player.getMoving())
            {
                if (event.getX() < getWidth()/2) {
                    steps = 2;
                }
                else
                {
                    steps = 1;
                }

                if(!player.getPlaying() && newGameCreated && reset)
                {
                    player.setPlaying(true);
                    player.setUp(true);
                    startTime = System.nanoTime();
                }
                if(player.getPlaying())
                {
                    mpJump.start();
                    if(!started) started = true;
                    reset = false;
                    player.setUp(true);
                    player.setScore(player.getScore() + steps);
                }

            }
        }
        return true;
    }

    public void update()
    {
        if (player.getPlaying()) {

            long elapsed = (System.nanoTime() - startTime)/1000000;
            startTime = System.nanoTime();

            timer -= elapsed;

            if (timer <= 0)
            {
                timer = 0;
                mpTime.start();
                player.setPlaying(false);
            }

            //ckeck clock collison
            if (clock != null)
            {
                if (collision(player, clock))
                {
                    mpClock.start();
                    clock = null;
                    timer += Settings.TIMER_ADDON;
                    player.setScore(player.getScore() + 1);
                }
            }

            if (player.getMoving())
            {
                bg.update(steps);
                this.updateLotuses(steps);
                player.update();

                if (clock != null && clock.getX() < -200)
                {
                    clock = null;
                }
                if (clock != null)
                {
                    clock.update(steps);
                }
            }
            else
            {
                for(int i= 0; i<lotuses.size(); i++)
                {
                    if(lotuses.get(i).getWater())
                    {
                        if (this.collision(player, lotuses.get(i)))
                        {
                            mpDrop.start();
                            player.setPlaying(false);
                        }
                    }
                }
            }
        }
        else
        {
            if(!reset)
            {
                newGameCreated = false;
                startReset = System.nanoTime();
                dissapear = true;
                reset = true;
            }

            long resetElapsed = (System.nanoTime()-startReset)/1000000;
            if (resetElapsed > 1500 && !newGameCreated)
            {
                newGame();
            }
        }

    }

    @Override
    public void draw(Canvas canvas)
    {
        final float scaleFactorX = getWidth()/(WIDTH*1.f);
        final float scaleFactorY = getHeight()/(HEIGHT*1.f);

        if (canvas != null) {
            final int savedState = canvas.save();
            canvas.scale(scaleFactorX, scaleFactorY);
            bg.draw(canvas);

            //draw lotuses
            for(Lotus l : lotuses)
            {
                l.draw(canvas);
            }

            if(!dissapear)
            {
                player.draw(canvas);
            }

            this.drawText(canvas);

            if(clock != null)
            {
                clock.draw(canvas);
            }

            canvas.restoreToCount(savedState);

        }
    }

    public void updateLotuses(int steps)
    {
        for (int i = 0; i < lotuses.size(); i++)
        {
            lotuses.get(i).update(steps);

            //if lotus is moving off screen remove it and add a new one
            if(lotuses.get(i).getX() < -300)
            {
                lotuses.remove(i);

                if (lotuses.get((lotuses.size()-1)).getWater())
                {
                    this.addLotus();
                }
                else
                {
                    addRandom();
                }
            }
        }
    }

    public void newGame()
    {
        if(player.getScore() > best)
        {
            setBest(this.getContext(), player.getScore());
        }

        dissapear = false;
        player.resetScore();

        lotuses.clear();
        clock = null;



        //initial lotuses
        for(int i=0; i*150<WIDTH + 300; i++)
        {
            //first lotus
            if(i==0)
            {
                lotuses.add(new Lotus(BitmapFactory.decodeResource(getResources(), R.drawable.leaf), Settings.LOTUS_W, false));
            }
            else
            {
                if (lotuses.get((lotuses.size()-1)).getWater())
                {
                    this.addLotus();
                }
                else
                {
                    addRandom();
                }
            }
        }

        timer = Settings.TIMER;
        newGameCreated = true;
    }

    public boolean collision(GameObject a, GameObject b)
    {
        if(Rect.intersects(a.getRectangle(), b.getRectangle()))
        {
            return true;
        }
        return false;
    }

    //1 for lotus, 2 for water
    public int getLotusPattern()
    {
        int randInt = rand.nextInt(3-1+1) + 1;
        if (randInt == 2)
        return 2;
        else return 1;
    }

    public void addLotus()
    {
        lotuses.add(new Lotus(BitmapFactory.decodeResource(getResources(), R.drawable.leaf),
                lotuses.get((lotuses.size() - 1)).getX() + Settings.LOTUS_W + Settings.LOTUS_GAP, false));

        //add clock
        if (player.getScore() > 20 && player.getScore() % 7 == 0 && clock == null)
        {
            if(!lotuses.get((lotuses.size()-2)).getWater())
            {
                clock = new Clock(BitmapFactory.decodeResource(getResources(), R.drawable.clock), lotuses.get((lotuses.size()-1)).getX() + 50);
            }
        }

    }

    public void addWater()
    {
        lotuses.add(new Lotus(BitmapFactory.decodeResource(getResources(), R.drawable.leaf_transparent),
                lotuses.get((lotuses.size()-1)).getX() + Settings.LOTUS_W + Settings.LOTUS_GAP, true));
    }

    public void addRandom()
    {
        int randInt = this.getLotusPattern();
        if (randInt == 1)
        {
            this.addLotus();
        }
        else
        {
            this.addWater();
        }
    }

    public void drawText(Canvas canvas)
    {
        best = this.getBest(this.getContext());

        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setTextSize(50);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        canvas.drawText("SCORE: " + (player.getScore()), 30, HEIGHT - 660, paint);
        canvas.drawText("BEST: " + best , WIDTH/2-100, HEIGHT - 660, paint);

        paint.setTextSize(50);
        canvas.drawText("TIME: " + timer/1000, 1000, HEIGHT -660, paint);

        if (!player.getPlaying() && newGameCreated && reset)
        {
            Paint paint2 = new Paint();
            paint2.setTextSize(50);
            paint2.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
            canvas.drawText("PRESS TO START", WIDTH / 2 - 50, HEIGHT / 2, paint2);

            paint2.setTextSize(30);
            canvas.drawText("PRESS LEFT FOR 2 JUMPS", WIDTH / 2 - 50, HEIGHT / 2 + 20, paint2);
            canvas.drawText("PRESS RIGHT FOR 1 JUMP", WIDTH / 2 - 50, HEIGHT / 2 + 40, paint2);

        }

        if(!player.getPlaying() && player.getScore()>0)
        {
            Paint paint3 = new Paint();
            paint3.setTextSize(100);
            paint3.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
            canvas.drawText("SCORE: " + player.getScore(), WIDTH / 2 - 200, HEIGHT / 2, paint3);
        }
    }

    private int getBest(Context context)
    {
        sharedpreferences = context.getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        int best = sharedpreferences.getInt(context.getString(R.string.saved_best), 0);
        return best;
    }

    private void setBest(Context context, int best)
    {
        sharedpreferences = context.getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putInt(context.getString(R.string.saved_best), best);
        editor.commit();
    }



}
