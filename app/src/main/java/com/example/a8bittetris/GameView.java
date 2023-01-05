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
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import androidx.annotation.NonNull;

import com.example.a8bittetris.Tetrimino.TetriminoBase;

import java.util.ArrayList;

class GameView extends SurfaceView implements SurfaceHolder.Callback {
    private String TAG ="GameView";
    private ArrayList<TetriminoBase> blockList;
    private TetriminoBase currentBlock;
    GameLoopThread gameLoopThread;
    private int boardCols = 10;
    private int boardRows = 20;
    private int blockSize = -1;
    private int x = 0;
    private int y = 0;
    private int sizeX=0;
    private int sizeY=0;
    private int speed=200; //the higher, the slower.
    private long lastMove;
    private boolean[][] mapState = new boolean[boardRows][boardCols];

    private Rect collisionRect = null;

    public GameView(Context context) {
        super(context);
        getHolder().addCallback(this);
        init();
    }

    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        getHolder().addCallback(this);
        init();
    }

    public GameView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        getHolder().addCallback(this);
        init();
    }


    private void init(){
        this.setDrawingCacheEnabled(true);

        gameLoopThread = new GameLoopThread(this);
        blockList = new ArrayList<TetriminoBase>();


        lastMove=System.currentTimeMillis();
    }

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

    @Override
    public void onDraw(Canvas canvas) {
        if(blockSize==-1) {
            blockSize = getHeight() / 20;
            idk(canvas);
            blockList.add(new TetriminoBase(getContext(),blockSize));
            currentBlock = blockList.get(blockList.size()-1);
            sizeX=currentBlock.getBmp().getWidth();
            sizeY=currentBlock.getBmp().getHeight();
        }
        canvas.drawColor(Color.WHITE);
        if(fallingCalc()) {
            if ((y < (getHeight() - currentBlock.getBmp().getHeight())) && !collisionCheck()) {
                y = y + sizeY;
                currentBlock.setY(y);
            } else {
                //get a sense for the current static map state? Collision detection generation
                //create new block
                updateMapstate(currentBlock);
                blockList.add(new TetriminoBase(getContext(), blockSize));
                currentBlock = blockList.get(blockList.size() - 1);
                sizeX = currentBlock.getBmp().getWidth();
                sizeY = currentBlock.getBmp().getHeight();
                y = currentBlock.getY();
            }
        }
        for(TetriminoBase block : blockList) {
            canvas.drawBitmap(block.getBmp(), block.getX(), block.getY(), null);
        }

    }

    @Override
    public boolean onTouchEvent(MotionEvent event){
        Log.d(TAG,"Some tocuhin' goin' on");
        Log.d(TAG,"touch x "+event.getX()+" touch y "+event.getY());
        Log.d(TAG,"Block size and board"+blockSize + " "+boardCols+ " ="+(blockSize*boardCols));
        if(event.getX()>blockSize*boardCols)
            currentBlock.setX(blockSize&boardCols-1);//clip to the edge of the board.
        else
            currentBlock.setX(((int)event.getX()/blockSize)*blockSize);//reduce the resolution by casting.
        return true;
    }

    public boolean fallingCalc(){
        long timestamp = System.currentTimeMillis();
        if(timestamp-lastMove>speed){
            lastMove=timestamp;
            return true;
        }
        return false;
    }

    public void idk(Canvas canvas){
        Rect[][] rectMap = new Rect[boardRows][boardCols];
        for(int row = 0; row < rectMap.length; row++){
            for(int col = 0; col < rectMap[row].length; col++){
                int left = row*blockSize;
                int top = col*blockSize;
                int right = left+blockSize;
                int bottom = top+blockSize;
                rectMap[row][col]= new Rect(left,top,right,bottom);
                Log.d(TAG,"Row "+row+" Col "+ col+"\nLeft "+left+"top "+top+"right "+right+"bottom "+bottom);
            }
        }
    }

    public boolean collisionCheck(){
        Log.d(TAG,"currentBlock x"+((currentBlock.getX()/blockSize)));
        if((currentBlock.getY()/blockSize)+1==mapState.length||mapState[currentBlock.getY()/blockSize+1][(currentBlock.getX()/blockSize)]){
            return true;
        }
        return false;
    }

    //we really don't need idk() at all, but it's fun to keep around while we're doing stuff.
    //This updates a 20x10 grid with whether or not something is present.
    //It will also clear any full rows.
    public void updateMapstate(TetriminoBase block){
        int row = block.getY()/blockSize;
        int col = block.getX()/blockSize;
        Log.d(TAG,"updating row "+row+" col "+ col);
        mapState[row][col]=true;
        //check for clears
        boolean clear = true;
        for(int r = 0; r < mapState.length; r++){
            for(int c = 0; c < mapState[r].length; c++){
                if(!mapState[r][c]){
                    clear = false;
                    break;
                }
            }
            if(clear){
                clearBlocks(r);
            }
            clear=true;
        }
    }

    public void clearBlocks(int row) {
        Log.d(TAG, "Clearing row " + row);
        ArrayList<TetriminoBase> itemsForDelete = new ArrayList<>();
        for (TetriminoBase block : blockList) {
            if (block.getY() == (row * blockSize))
                itemsForDelete.add(block);//avoid concurrentModificationException
        }
        blockList.removeAll(itemsForDelete);
        shiftDown(row);
    }

    public void shiftDown(int row){
        for(TetriminoBase block : blockList){
            if(block.getY()<row*blockSize)
                block.setY(block.getY()+blockSize);
        }
        redrawMapstate();
    }

    public void redrawMapstate(){
        for(int r = 0; r < mapState.length; r++){
            for(int c = 0; c < mapState[r].length; c++){
               mapState[r][c]=false;
            }
        }
        for(TetriminoBase block : blockList){
            int row = block.getY()/blockSize;
            int col = block.getX()/blockSize;
            mapState[row][col]=true;
        }
    }


}