/*
 * Copyright 2020 Kay Glahn
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

package com.example.tflitedemo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.tflitedemo.ml.MobilenetV1101601Metadata1;

import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ops.ResizeOp;
import org.tensorflow.lite.support.label.Category;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    static final int REQUEST_IMAGE_CAPTURE = 1;

    ImageView imageView;
    TextView[] textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView = findViewById(R.id.imageView);
        textView = new TextView[]{findViewById(R.id.textView1), findViewById(R.id.textView2), findViewById(R.id.textView3)};
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bitmap imageBitmap = (Bitmap) data.getExtras().get("data");
            imageView.setImageBitmap(imageBitmap);
            recognizeImage(imageBitmap);
        }
    }

    public void takePhoto(View view) {
        Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePhotoIntent.resolveActivity(getPackageManager()) != null)
            startActivityForResult(takePhotoIntent, REQUEST_IMAGE_CAPTURE);
    }

    private void recognizeImage(Bitmap imageBitmap) {

        ImageProcessor imageProcessor = new ImageProcessor.Builder()
                .add(new ResizeOp(160, 160, ResizeOp.ResizeMethod.BILINEAR))
                .build();

        TensorImage tImage = TensorImage.fromBitmap(imageBitmap);

        tImage = imageProcessor.process(tImage);

        try {

            MobilenetV1101601Metadata1 model = MobilenetV1101601Metadata1.newInstance(MainActivity.this);
            MobilenetV1101601Metadata1.Outputs outputs = model.process(tImage);

            List<Category> probability = outputs.getProbabilityAsCategoryList();
            probability.sort(Comparator.comparing(Category::getScore, Comparator.reverseOrder()));

            for (int i=0; i<3; i++)
                textView[i].setText(getString(R.string.result_text, probability.get(i).getLabel(), probability.get(i).getScore()));

            model.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}