package com.example.a10017078.aangescape;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.constraint.solver.widgets.Rectangle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

public class MainActivity extends AppCompatActivity {
    //Code from this program has been used from Beginning Android Games
    //Review SurfaceView, Canvas, continue

    GameSurface gameSurface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        gameSurface = new GameSurface(this);
        setContentView(gameSurface);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.d("YEOO", "TIME UP");
                Intent intent = new Intent(MainActivity.this, EndActivity.class);
                startActivity(intent);
            }

        }, 40000);

    }

    @Override
    protected void onPause(){
        super.onPause();
        gameSurface.pause();
    }

    @Override
    protected void onResume(){
        super.onResume();
        gameSurface.resume();
    }

    //----------------------------GameSurface Below This Line--------------------------
    public class GameSurface extends SurfaceView implements Runnable,SensorEventListener{


        Thread gameThread;
        int sensorX;
        SurfaceHolder holder;
        volatile boolean running = false;
        Bitmap glider, fireball;
        int gliderX=0;
        int fireballX = 0;
        int x=200;
        int count = 0;
        int gposX, gposY, fposY;
        String sensorOutput="";
        Paint paintProperty;
        int saved;
        int score = 0;
        int affect = 0;
        int time = 40;
        MediaPlayer mp, mp2;
        boolean hit = false;

        int screenWidth;
        int screenHeight;

        public GameSurface(Context context) {
            super(context);
            holder=getHolder();
            glider= BitmapFactory.decodeResource(getResources(),R.drawable.glider);
            fireball= BitmapFactory.decodeResource(getResources(),R.drawable.fireball);

            Display screenDisplay = getWindowManager().getDefaultDisplay();
            Point sizeOfScreen = new Point();
            screenDisplay.getSize(sizeOfScreen);
            screenWidth=sizeOfScreen.x;
            screenHeight=sizeOfScreen.y;

            SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
            Sensor accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            sensorManager.registerListener(this,accelerometerSensor,sensorManager.SENSOR_DELAY_NORMAL);

            paintProperty= new Paint();
            paintProperty.setTextSize(100);

        }

        @Override
        public void run() {

            mp= MediaPlayer.create(getApplicationContext(),R.raw.agnikai);
            mp.start();

            mp2 = MediaPlayer.create(getApplicationContext(),R.raw.blast);

            while (running == true){
                if (holder.getSurface().isValid() == false)
                    continue;
                Canvas canvas= holder.lockCanvas();

                canvas.drawRGB(0,204,204);

                canvas.drawText(sensorOutput,x,200,paintProperty);

                gameSurface.setOnTouchListener(new OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        if(event.getAction() == MotionEvent.ACTION_DOWN)
                            affect = 250;
                        if(event.getAction() == MotionEvent.ACTION_UP)
                            affect = -250;
                        return true;
                    }
                });

                Paint painty = new Paint();
                painty.setColor(Color.BLACK);
                painty.setStyle(Paint.Style.FILL);
                painty.setTextSize(30);
                canvas.drawText("Score: " + score, 10, 25, painty);
                canvas.drawText("Time: " + time, 10, 45, painty);

                canvas.drawBitmap( glider,(screenWidth/2) - glider.getWidth()/2 +gliderX ,(screenHeight-100) - 2*glider.getHeight(),null);
                gposY = (screenHeight-100) - 2*glider.getHeight();
                gposX = (screenWidth/2) - glider.getWidth()/2 +gliderX;

                int rand = (int)(Math.random()*600) - 300;

                if(count<600) {

                    if(count<5) {
                        canvas.drawBitmap(fireball, (screenWidth / 2) - fireball.getWidth() / 2 + fireballX + rand, (screenHeight / 6) - fireball.getHeight() + (2 * count) + affect, null);
                        saved = (screenWidth / 2) - fireball.getWidth() / 2 + fireballX + rand;
                    }

                    else {
                        canvas.drawBitmap(fireball, saved, (screenHeight / 6) - fireball.getHeight() + (2 * count) + affect, null);
                        fposY = (screenHeight / 6) - fireball.getHeight() + (2 * count);
                    }

                    count += 5;
//                    Log.d("PLS", "" + saved);
//                    Log.d("PLS", "" + gposX);
//                    Log.d("PLS", "" +  (gposY+300));
//                    Log.d("PLS", "" + fposY);
                    contact();
                }

                else{
                    if(!hit)
                        score++;
                    else
                        score--;
                    count = 0;
                    hit = false;
                }

                if(sensorX<0) {
                    if (((screenWidth / 2) + glider.getWidth() / 2 + gliderX) < screenWidth)
                        gliderX -= sensorX;
                    else
                        glider= BitmapFactory.decodeResource(getResources(),R.drawable.glider);
                }

                if(sensorX>0) {
                    if (((screenWidth / 2) + glider.getWidth() / 2 - gliderX) < screenWidth)
                        gliderX -= sensorX;
                    else
                        glider= BitmapFactory.decodeResource(getResources(),R.drawable.glider);
                }

                holder.unlockCanvasAndPost(canvas);
            }
        }

        public boolean contact(){
            if(saved>=gposX) {
                if (saved <= (gposX + 50) && ((fposY <= (gposY + 300) && fposY >= (gposY + 300 - 40)) || ((fposY >= (gposY + 300) && fposY <= (gposY + 300 + 40))))) {
                    hit = true;
//                    Log.d("YEEOO", "it works!");
                    mp2.start();
                    glider= BitmapFactory.decodeResource(getResources(),R.drawable.explosion);
                    return true;
                }
            }
            else if (saved<=gposX) {
                if (saved >= (gposX - 50) && ((fposY <= (gposY + 300) && fposY >= (gposY + 300 - 40)) || ((fposY >= (gposY + 300) && fposY <= (gposY + 300 + 40))))) {
                    hit = true;
//                    Log.d("YEEOO", "it works!");
                    mp2.start();
                    glider= BitmapFactory.decodeResource(getResources(),R.drawable.explosion);
                    return true;
                }
            }
            return false;
        }

        public void resume(){
            running=true;
            gameThread=new Thread(this);
            gameThread.start();
        }

        public void pause() {
            running = false;
            while (true) {
                try {
                    gameThread.join();
                } catch (InterruptedException e) {
                }
            }
        }

        @Override
        public void onSensorChanged(SensorEvent event) {
            sensorX = (int)event.values[0];
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }

    }//GameSurface
}//Activity
