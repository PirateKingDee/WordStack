/* Copyright 2016 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.engedu.wordstack;

import android.content.res.AssetManager;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Random;
import java.util.Stack;

public class MainActivity extends AppCompatActivity {

    private static final int WORD_LENGTH = 5;
    public static final int LIGHT_BLUE = Color.rgb(176, 200, 255);
    public static final int LIGHT_GREEN = Color.rgb(200, 255, 200);
    private ArrayList<String> words = new ArrayList<>();
    private Random random = new Random();
    private StackedLayout stackedLayout;
    private String word1, word2;
    private StringBuilder word1Builder = new StringBuilder();
    private StringBuilder word2Builder = new StringBuilder();
    private LinearLayout verticalLayout;
    private Stack<LetterTile> placedTile = new Stack<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AssetManager assetManager = getAssets();
        try {
            InputStream inputStream = assetManager.open("words.txt");
            BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
            String line = null;
            while((line = in.readLine()) != null) {

                String word = line.trim();
                if(word.length()==WORD_LENGTH){
                    words.add(word);
                }
            }
        } catch (IOException e) {
            Toast toast = Toast.makeText(this, "Could not load dictionary", Toast.LENGTH_LONG);
            toast.show();
        }
        verticalLayout = (LinearLayout) findViewById(R.id.vertical_layout);
        stackedLayout = new StackedLayout(this);
        verticalLayout.addView(stackedLayout, 3);
        View word1LinearLayout = findViewById(R.id.word1);

        word1LinearLayout.setOnTouchListener(new TouchListener());
        word1LinearLayout.setOnDragListener(new DragListener());
        View word2LinearLayout = findViewById(R.id.word2);
        word2LinearLayout.setOnTouchListener(new TouchListener());
        word2LinearLayout.setOnDragListener(new DragListener());
    }

    private class TouchListener implements View.OnTouchListener {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN && !stackedLayout.empty()) {
                LetterTile tile = (LetterTile) stackedLayout.peek();
                tile.moveToViewGroup((ViewGroup) v);
                if(v.equals(findViewById(R.id.word1))){
                    word1Builder.append(tile.getLetter());
                }
                else{
                    word2Builder.append(tile.getLetter());
                }
                if (stackedLayout.empty()) {
                    TextView messageBox = (TextView) findViewById(R.id.message_box);
                    messageBox.setText(word1 + " " + word2);
                    if(word1Builder.toString().equals(word1)||word2Builder.toString().equals(word1)){

                        //resultText.setText("Its correct");
                        Toast.makeText(getApplicationContext(),"true", Toast.LENGTH_LONG).show();
                    }
                    else{
                        //resultText.setText("Its wrong");
                        Toast.makeText(getApplicationContext(),"false", Toast.LENGTH_LONG).show();
                    }
                }
                placedTile.push(tile);
                return true;
            }
            return false;
        }
    }

    private class DragListener implements View.OnDragListener {

        public boolean onDrag(View v, DragEvent event) {
            int action = event.getAction();
            switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED:
                    v.setBackgroundColor(LIGHT_BLUE);
                    v.invalidate();
                    return true;
                case DragEvent.ACTION_DRAG_ENTERED:
                    v.setBackgroundColor(LIGHT_GREEN);
                    v.invalidate();
                    return true;
                case DragEvent.ACTION_DRAG_EXITED:
                    v.setBackgroundColor(LIGHT_BLUE);
                    v.invalidate();
                    return true;
                case DragEvent.ACTION_DRAG_ENDED:
                    v.setBackgroundColor(Color.WHITE);
                    v.invalidate();
                    return true;
                case DragEvent.ACTION_DROP:
                    // Dropped, reassign Tile to the target Layout
                    LetterTile tile = (LetterTile) event.getLocalState();
                    tile.moveToViewGroup((ViewGroup) v);
                    placedTile.push(tile);
                    if(v.equals(findViewById(R.id.word1))){
                        word1Builder.append(tile.getLetter());
                    }
                    else{
                        word2Builder.append(tile.getLetter());
                    }
                    if (stackedLayout.empty()) {
                        TextView messageBox = (TextView) findViewById(R.id.message_box);
                        messageBox.setText(word1 + " " + word2);
                        if(word1Builder.equals(word1)||word2Builder.equals(word1)){

                            //resultText.setText("Its correct");
                            Toast.makeText(getApplicationContext(),"true", Toast.LENGTH_LONG).show();
                        }
                        else{
                            //resultText.setText("Its wrong");
                            Toast.makeText(getApplicationContext(),"false", Toast.LENGTH_LONG).show();
                        }
                    }

//                    if(stackedLayout.empty()){
//                        //TextView resultText = (TextView)findViewById(R.id.result);
//
//                    }
                    return true;
            }

            return false;
        }
    }

    protected boolean onStartGame(View view) {
        TextView messageBox = (TextView) findViewById(R.id.message_box);
        messageBox.setText("Game started");
        LinearLayout word1LinearLayout = (LinearLayout)findViewById(R.id.word1);
        LinearLayout word2LinearLayout = (LinearLayout)findViewById(R.id.word2);
        word1LinearLayout.removeAllViews();
        word2LinearLayout.removeAllViews();
        stackedLayout.clear();

        word1 = words.get(random.nextInt(words.size()));
        word2 = words.get(random.nextInt(words.size()));
        while(word1.equals(word2)){
            word2 = words.get(random.nextInt(words.size()));
        }

        int word1Counter = word1.length()-1;
        int word2Counter = word2.length()-1;
        while(word1Counter >= 0 && word2Counter >= 0){
            Boolean pickWord1 = random.nextBoolean();
            if(pickWord1){
                LetterTile letterTile = new LetterTile(this,word1.charAt(word1Counter));
                stackedLayout.push(letterTile);
                word1Counter--;
            }
            else{
                LetterTile letterTile = new LetterTile(this,word2.charAt(word2Counter));
                stackedLayout.push(letterTile);
                word2Counter--;
            }
        }
        while(word1Counter >= 0){
            LetterTile letterTile = new LetterTile(this,word1.charAt(word1Counter));
            stackedLayout.push(letterTile);
            word1Counter--;
        }
        while(word2Counter >= 0){
            LetterTile letterTile = new LetterTile(this,word2.charAt(word2Counter));
            stackedLayout.push(letterTile);
            word2Counter--;
        }
        Log.d("word1",word1);
        Log.d("word2",word2);
        return true;
    }

    protected boolean onUndo(View view) {
        if(!placedTile.empty()){
            LetterTile tile = placedTile.pop();
            tile.moveToViewGroup(stackedLayout);
        }

        return true;
    }
}
