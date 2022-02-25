package org.tensorflow.lite.examples.detection;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import org.tensorflow.lite.examples.detection.tflite.Classifier;
import org.tensorflow.lite.examples.detection.tflite.DetectorFactory;
import org.tensorflow.lite.examples.detection.tflite.YoloV5Classifier;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class PhotoActivity extends AppCompatActivity {

    private ImageView image;

    private String imagePath;
    private Bitmap bitmap = null;

    private YoloV5Classifier detector;

    private String ASSET_PATH = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);
        image = findViewById(R.id.imageView);

        Intent intent = getIntent();
        imagePath = intent.getStringExtra("imagePath");
        imagePath += "/image.jpg";

        System.out.println("TRYING BITMAP");

        try {
            detector = DetectorFactory.getDetector(getAssets(), "best-fp16.tflite");
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            bitmap = BitmapFactory.decodeFile(imagePath);
            detect(bitmap);
            // image.setImageBitmap(bitmap);
            System.out.printf("IMAGE DIMS: %d, %d%n", bitmap.getWidth(), bitmap.getHeight());
        } catch (Exception e) {
            System.out.println("ERROR ERROR");
            e.printStackTrace();
        }
    }

    public void detect(Bitmap b) {
        final long startTime = SystemClock.uptimeMillis();
        final List<Classifier.Recognition> results = detector.recognizeImage(b);

        Log.e("CHECK", "run: " + results.size());

        Bitmap copyBitmap = Bitmap.createBitmap(b);
        Bitmap mutableBitmap = copyBitmap.copy(Bitmap.Config.ARGB_8888, true);
        final Canvas canvas = new Canvas(mutableBitmap);
        final Paint paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2.0f);

        float minimumConfidence = 0.2f;
        final List<Classifier.Recognition> mappedRecognitions =
                new LinkedList<Classifier.Recognition>();

        for (final Classifier.Recognition result : results) {
            final RectF location = result.getLocation();
            if (location != null && result.getConfidence() >= minimumConfidence) {
                canvas.drawRect(location, paint);

                // cropToFrameTransform.mapRect(location);

                result.setLocation(location);
                mappedRecognitions.add(result);
                System.out.println("DETECTED ONE");
            }
        }

        image.setImageBitmap(mutableBitmap);
    }

    public void onBack(View view) {
        Intent intent = new Intent(this, DetectorActivity.class);
        startActivity(intent);
    }
}