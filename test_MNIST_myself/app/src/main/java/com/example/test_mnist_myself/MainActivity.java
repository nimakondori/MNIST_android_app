package com.example.test_mnist_myself;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final int PIXEL_WIDTH =  28;
    private DigitsDetector mnistClassifier;
    private PaintView paintView;
    Button btn1, btn2, btn3;
    ImageView mResultImage;
    TextView tV;
    Thread mThread;
    Handler mHandler;
    boolean stopThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tV = findViewById(R.id.tV);
        btn1 = findViewById(R.id.button1);
        btn2 = findViewById(R.id.button2);
        btn3 = findViewById(R.id.button3);
        paintView = findViewById(R.id.paintView);
        mResultImage = findViewById(R.id.iV);
        stopThread = false;
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        paintView.init(metrics);
        loadMnistClassifier();
        mThread= new Thread(new onDetectclickedThread());
    }

    private void loadMnistClassifier() {
        try {
            mnistClassifier = mnistClassifier.classifier(getAssets(), MnistModelConfig.MODEL_FILENAME);
        } catch (IOException e) {
            Toast.makeText(this, "MNIST model couldn't be loaded. Check logs for details.", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.normal:
                paintView.normal();
                return true;
            case R.id.emboss:
                paintView.emboss();
                return true;
            case R.id.blur:
                paintView.blur();
                return true;
            case R.id.clear:
                paintView.clear();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void doSomething(View v) {
        switch (v.getId()) {
            case R.id.button1:
               // mThread.interrupt();
                stopThread = true;
                mResultImage.setVisibility(View.GONE);
                tV.setVisibility(View.GONE);
                paintView.clear();

                break;

            case R.id.button2:
                mThread= new Thread(new onDetectclickedThread());
                stopThread = false;
                mThread.start();
                mResultImage.setVisibility(View.VISIBLE);
                tV.setVisibility(View.VISIBLE);
                break;

            case R.id.button3:
                paintView.clear();
                //mResultImage.setVisibility(View.GONE);
                //tV.setVisibility(View.GONE);
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mResultImage.setVisibility(View.GONE);
        tV.setVisibility(View.GONE);
    }

    private class onDetectclickedThread implements Runnable
    {

        @Override
        public void run() {
            Bitmap bitmap = null;
            Bitmap preprocessedImage = null;
            List<Classification> recognitions = null;
            String finalRecognitions;

            while(!Thread.currentThread().isInterrupted()&& !stopThread) {
                try
                {
                    bitmap = Bitmap.createScaledBitmap(paintView.getBitmap(), PIXEL_WIDTH, PIXEL_WIDTH, false);
                    // squareBitmap = ThumbnailUtils.extractThumbnail(bitmap, getScreenWidth(), getScreenWidth());
                    preprocessedImage = ImageUtils.prepareImageForClassification(bitmap);
                    recognitions = mnistClassifier.recognizeImage(preprocessedImage);
                    Log.d("NKO", "onDetectclicked: " + recognitions.toString());
                    finalRecognitions = recognitions.toString();
//                  ===================================================Are these really necessary??======================================================
                    Bitmap finalPreprocessedImage = preprocessedImage;
                    String finalRecognitions1 = finalRecognitions;
//                  ===================================================Are these really necessary??======================================================
//                  the run on UI thread requires this copying to temp files to execute. Still not sure why it's needed though??

                    Thread.sleep(30);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                mResultImage.setImageBitmap(finalPreprocessedImage);
                                tV.setText(finalRecognitions1);
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }
                    });
                }catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }
    }
    // This was supposed to be used for thumbnail utilities which was replaced by scaled bitmap function
    // Results are quite similar
    private int getScreenWidth() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.widthPixels;
    }
}
