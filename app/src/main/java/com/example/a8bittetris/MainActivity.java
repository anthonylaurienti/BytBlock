package com.example.a8bittetris;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    boolean[][] field = new boolean[10][20];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        GridView pixelGrid = new GridView(this);
        pixelGrid.setNumColumns(4);
        pixelGrid.setNumRows(6);

        //setContentView(pixelGrid);
        setContentView(new GameView(this));
    }
}