package com.example.a8bittetris;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;

class GameView extends View {

    private GameLoop mainLoop;
    Bitmap gameBitmap;
    Canvas gameCanvas;

    public GameView(Context context) {
        super(context);
        this.setDrawingCacheEnabled(true);

        gameCanvas = new Canvas();

        mainLoop = new GameLoop(getResources(), gameCanvas);
        mainLoop.start();
    }

    @SuppressLint("DrawAllocation") @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int w = MeasureSpec.getSize(widthMeasureSpec);
        int h = MeasureSpec.getSize(heightMeasureSpec);

        gameBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);

        gameCanvas.setBitmap(gameBitmap);

        setMeasuredDimension(w, h);
    }

    @Override
    public void onDraw(Canvas canvas) {
        canvas.drawBitmap(gameBitmap, 0, 0, new Paint());
        invalidate();
    }

}

class GameLoop extends Thread {

    private float frameRate = 60;
    private float frameTime = 1000 / frameRate;

    private Game logicGame;
    private Resources gameResources;
    private Canvas gameCanvas;

    public GameLoop(Resources res, Canvas canvas) {
        logicGame = new Game(res, canvas);
    }

    @Override
    public void run()
    {
        while (true) {
            float startTime = System.currentTimeMillis();

            logicGame.Update();
            logicGame.Draw();

            float endTime = System.currentTimeMillis();
            long deltaTime = (long) (frameTime - (endTime - startTime));
            try {
                Thread.sleep(deltaTime);
            } catch (InterruptedException e) {
            }
        }
    }

}

class Game {
    private Resources resources;
    private Canvas canvas;

    private int x = 0;
    private int y = 0;
    private Paint paint;
    private int size = 50;
    private long speed = 1000;
    private long lastUpdate = 0;
    ArrayList<Rect> blocks = new ArrayList<Rect>();
    boolean placed = false;

    public Game(Resources res, Canvas cas) {
        resources = res;
        canvas = cas;

        paint = new Paint();
        paint.setTextSize(size);
    }

    public void Draw() {
        canvas.drawColor(Color.WHITE);
        for(Rect r : blocks){
            canvas.drawRect(r, paint);
        }
        Log.d("DEBUG","blocks size "+blocks.size());
    }

    public void Update() {
        if(lastUpdate == 0 ) {
            lastUpdate=System.currentTimeMillis();
            if (blocks.isEmpty() | placed) {
                blocks.add(createNewBlock());
                placed = false;
            }
            Rect r = blocks.get(blocks.size() - 1);
            r.set(r.left, r.top + size, r.right, r.bottom + size);
            if ((r.bottom + (size * 2)) >= canvas.getHeight()) {
                placed = true;
            }
            blocks.set(blocks.size() - 1, r);
        }
        if((System.currentTimeMillis()-lastUpdate)>speed)
            lastUpdate=0;
    }

    public Rect createNewBlock(){
        return new Rect(x,0,x+size,y+size);
    }
}