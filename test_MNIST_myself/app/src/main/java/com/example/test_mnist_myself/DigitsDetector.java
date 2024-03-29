package com.example.test_mnist_myself;

import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;

import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

import static com.example.test_mnist_myself.MnistModelConfig.CLASSIFICATION_THRESHOLD;
import static com.example.test_mnist_myself.MnistModelConfig.MAX_CLASSIFICATION_RESULTS;

public class DigitsDetector {
    private final Interpreter interpreter;

    public DigitsDetector(Interpreter interpreter) {
        this.interpreter = interpreter;
    }

    public static DigitsDetector classifier(AssetManager assetManager, String modelPath) throws IOException {
        ByteBuffer byteBuffer = loadModelFile(assetManager, modelPath);



// =============================Don't know why we need MappedByteBuffer rather than byteMap ===================================================================================================================
        Interpreter interpreter = new Interpreter((MappedByteBuffer) byteBuffer);
        return new DigitsDetector(interpreter);
    }

    private static ByteBuffer loadModelFile(AssetManager assetManager, String modelPath) throws IOException {
        AssetFileDescriptor fileDescriptor = assetManager.openFd(modelPath);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    public List<Classification> recognizeImage(Bitmap bitmap) {
        ByteBuffer byteBuffer = convertBitmapToByteBuffer(bitmap);
        float[][] result = new float[1][MnistModelConfig.OUTPUT_LABELS.size()];
        interpreter.run(byteBuffer, result);
        return getSortedResult(result);
    }

    private ByteBuffer convertBitmapToByteBuffer(Bitmap bitmap) {
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(MnistModelConfig.MODEL_INPUT_SIZE);
        byteBuffer.order(ByteOrder.nativeOrder());
        int[] pixels = new int[MnistModelConfig.INPUT_IMG_SIZE_WIDTH * MnistModelConfig.INPUT_IMG_SIZE_HEIGHT];
        bitmap.getPixels(pixels, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
        for (int pixel : pixels) {
            float rChannel = (pixel >> 16) & 0xFF;
            float gChannel = (pixel >> 8) & 0xFF;
            float bChannel = (pixel) & 0xFF;
            float pixelValue = (rChannel + gChannel + bChannel) / 3 / 255.f;
            byteBuffer.putFloat(pixelValue);
        }
        return byteBuffer;
    }

    private List<Classification> getSortedResult(float[][] resultsArray) {
        PriorityQueue<Classification> sortedResults = new PriorityQueue<>(
                MAX_CLASSIFICATION_RESULTS,
                (lhs, rhs) -> Float.compare(rhs.confidence, lhs.confidence)
        );

        for (int i = 0; i < MnistModelConfig.OUTPUT_LABELS.size(); ++i) {
            float confidence = resultsArray[0][i];
            if (confidence > CLASSIFICATION_THRESHOLD) {
                MnistModelConfig.OUTPUT_LABELS.size();
                sortedResults.add(new Classification(MnistModelConfig.OUTPUT_LABELS.get(i), confidence));
            }
        }

        return new ArrayList<>(sortedResults);
    }
}
