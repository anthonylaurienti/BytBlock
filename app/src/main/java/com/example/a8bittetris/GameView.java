package com.example.a8bittetris;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import androidx.annotation.NonNull;

import com.example.a8bittetris.Tetrimino.TetriminoBase;

import java.util.ArrayList;

class GameView extends SurfaceView {
    private String TAG ="GameView";
    private ArrayList<TetriminoBase> blockList;
    private TetriminoBase currentBlock;
    private SurfaceHolder holder;
    GameLoopThread gameLoopThread;
    private int x = 0;
    private int y = 0;
    private int sizeX=0;
    private int sizeY=0;

    private Rect collisionRect = null;


    public GameView(Context context) {
        super(context);
        this.setDrawingCacheEnabled(true);

        gameLoopThread = new GameLoopThread(this);
        holder = getHolder();
        holder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(@NonNull SurfaceHolder surfaceHolder) {
                gameLoopThread.setRunning(true);
                gameLoopThread.start();
            }

            @Override
            public void surfaceChanged(@NonNull SurfaceHolder surfaceHolder, int i, int i1, int i2) {

            }

            @Override
            public void surfaceDestroyed(@NonNull SurfaceHolder surfaceHolder) {
                boolean retry = true;
                gameLoopThread.setRunning(false);
                while (retry) {
                    try {
                        gameLoopThread.join();
                        retry = false;
                    } catch (InterruptedException e) {
                        Log.e(TAG,"Error destroying Game",e);
                    }

                }
            }
        });
        blockList = new ArrayList<TetriminoBase>();
        blockList.add(new TetriminoBase(getContext()));
        currentBlock = blockList.get(blockList.size()-1);
        sizeX=currentBlock.getBmp().getWidth();
        sizeY=currentBlock.getBmp().getHeight();


    }

    @Override
    public void onDraw(Canvas canvas) {
        //Log.d(TAG,"drawing!");
        canvas.drawColor(Color.BLUE);
        if(collisionRect!=null){
            //Log.d(TAG,"collisionRect "+collisionRect.top);
        }
        //Log.d(TAG," Y "+y);
        if ((y < (getHeight() - currentBlock.getBmp().getHeight()))&&(collisionRect==null||y<collisionRect.top)) {
            y=y+sizeY;
            currentBlock.setY(y);
        }else{
            //get a sense for the current static map state? Collision detection generation
            Rect rect = new Rect(getWidth(),getHeight(),getWidth(),getHeight());
            for(TetriminoBase block : blockList){
                //Log.d(TAG,"x "+block.getX()+" y "+block.getY());
                if(rect.right>block.getX())
                    rect.right=block.getX();
                if(rect.bottom>block.getY())
                    rect.bottom=block.getY();
                rect.left=rect.right-block.getBmp().getWidth();
                rect.top=rect.bottom-block.getBmp().getHeight();
                //Log.d(TAG,"rect left "+rect.left+" right "+rect.right+" top "+rect.top+" bot "+rect.bottom);
            }
            collisionRect=rect;
            //create new block
            blockList.add(new TetriminoBase(getContext()));
            currentBlock = blockList.get(blockList.size()-1);
            sizeX=currentBlock.getBmp().getWidth();
            sizeY=currentBlock.getBmp().getHeight();
            y=currentBlock.getY();
        }
        for(TetriminoBase block : blockList) {
            canvas.drawBitmap(block.getBmp(), 10, block.getY(), null);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event){
        Log.d(TAG,"Some tocuhin' goin' on");
        Log.d(TAG,"touch x "+event.getX()+" touch y "+event.getY());
        return true;
    }


}