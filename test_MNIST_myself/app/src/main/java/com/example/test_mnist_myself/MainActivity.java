package com.example.test_mnist_myself;


import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.os.Bundle;
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
    Button btn1, btn2;
    ImageView mResultImage;
    TextView tV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tV = findViewById(R.id.tV);
        btn1 = findViewById(R.id.button1);
        btn2 = findViewById(R.id.button2);
        paintView = findViewById(R.id.paintView);
        mResultImage = findViewById(R.id.iV);

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        paintView.init(metrics);
        loadMnistClassifier();
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
                paintView.clear();
                mResultImage.setVisibility(View.GONE);
                tV.setVisibility(View.GONE);
                break;
            case R.id.button2:
                onDetectclicked();
                mResultImage.setVisibility(View.VISIBLE);
                tV.setVisibility(View.VISIBLE);




                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mResultImage.setVisibility(View.GONE);
        tV.setVisibility(View.GONE);
    }

    private void onDetectclicked() {
        Bitmap bitmap = Bitmap.createScaledBitmap(paintView.getBitmap(), PIXEL_WIDTH, PIXEL_WIDTH, false);
        Bitmap squareBitmap = ThumbnailUtils.extractThumbnail(bitmap, getScreenWidth(), getScreenWidth());

        Bitmap preprocessedImage = ImageUtils.prepareImageForClassification(squareBitmap);
        mResultImage.setImageBitmap(preprocessedImage);

        List<Classification> recognitions = mnistClassifier.recognizeImage(preprocessedImage);
        tV.setText(recognitions.toString());
        Log.d("NKO", "onDetectclicked: "+recognitions.toString());
    }

    private int getScreenWidth() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.widthPixels;
    }
}
//        Bitmap scaledBitmap = Bitmap.createScaledBitmap(paintView.getBitmap(), PIXEL_WIDTH, PIXEL_WIDTH, false);
//        mResultImage.setImageBitmap(scaledBitmap);
//
//
//        int digit = mnistClassifier.classify(scaledBitmap);
//        mResultImage.setImageBitmap(scaledBitmap);
//        if (digit >= 0) {
//            Log.d("nima", "Found Digit = " + digit);
//            String result = getString(R.string.found_digits) + digit;
//            Log.d("Nima", ""+result);
//        }
//    }
//}