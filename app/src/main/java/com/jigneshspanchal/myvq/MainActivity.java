package com.jigneshspanchal.myvq;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private CanvasView originalImageView, compressedImageView;
    private Button captureImageButton, demoButton, compressedImageButton;
    private boolean demoOrigImage=false, capturedImage=false;
    private String mCurrentPhotoPath;

    static final int REQUEST_IMAGE_CAPTURE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        switch (getResources().getConfiguration().orientation) {
            case Configuration.ORIENTATION_PORTRAIT:
                setContentView(R.layout.vq_portrait_layout);
                break;
            case Configuration.ORIENTATION_LANDSCAPE:
                setContentView(R.layout.vq_landscape_layout);
                break;
        }

        originalImageView = (CanvasView)findViewById(R.id.originalImage);
        compressedImageView = (CanvasView)findViewById(R.id.compressedImage);

        demoButton = (Button) findViewById(R.id.demo_button);
        demoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                demoOrigImage = true;
                capturedImage = false;
                originalImageView.clearCanvas();
                //InputStream vinayimg = v.getContext().getResources().openRawResource(R.raw.vinay);
                //originalImageView.drawBitmapImage(BitmapFactory.decodeStream(vinayimg));

                originalImageView.drawBitmapImage(v.getContext().getResources().openRawResource(R.raw.bbnr10));

                if (originalImageView.mBitmap != null) {
                    compressedImageView.clearCanvas();

                    //CompressImage cImg = new CompressImage(originalImageView.origBWBitmap, 4, 4, 256, 1);
                    //compressedImageView.drawBitmapImage(cImg.compressedImageBitmap);

                    CompressImage cImg = new CompressImage(v.getContext().getResources().openRawResource(R.raw.bbnr10), originalImageView.mBitmap, 4, 4, 256, 1);
                    //CompressImage cImg = new CompressImage(originalImageView.mBitmap, 4, 4, 256, 5);

                    compressedImageView.mBitmap = cImg.compressedImageBitmap;

                    //Displaying Toast with message
                    Toast.makeText(getApplicationContext(),"Press Capture Image",Toast.LENGTH_SHORT).show();
                }
            }
        });

        captureImageButton = (Button) findViewById(R.id.capture_image_button);
        captureImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Works with Samsung GS7 API
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                // Ensure that there's a camera activity to handle the intent
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    // Create the File where the photo should go
                    File photoFile = null;
                    try {
                        photoFile = createImageFile();
                    } catch (IOException ex) {
                        // Error occurred while creating the File
                    }
                    // Continue only if the File was successfully created
                    if (photoFile != null) {
                        Uri photoURI = FileProvider.getUriForFile(v.getContext(),
                                "com.jigneshspanchal.myvq.fileprovider",
                                photoFile);
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                    }
                }

                /*
                // Works with Moto Droid API 19
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                }*/
            }
        });

        compressedImageButton = (Button) findViewById(R.id.compressed_image_button);
        compressedImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //originalImageView.clearCanvas();
                //InputStream vinayimg = v.getContext().getResources().openRawResource(R.raw.vinay);
                //originalImageView.drawBitmapImage(BitmapFactory.decodeStream(vinayimg));
                if ((originalImageView.mBitmap != null) && (!demoOrigImage)) {
                    capturedImage=false;
                    demoOrigImage = false;
                    //Displaying Toast with message
                    Toast.makeText(getApplicationContext(),"Image Compression in Progress",Toast.LENGTH_LONG).show();
                    compressedImageView.clearCanvas();
                    CompressImage cImg = new CompressImage(originalImageView.mBitmap, 4, 4, 256, 1);
                    compressedImageView.mBitmap =  cImg.compressedImageBitmap;
                    //Displaying Toast with message
                    Toast.makeText(getApplicationContext(),"Press Demo or Capture New Image",Toast.LENGTH_LONG).show();
                }
                else
                {
                    //Displaying Toast with message
                    Toast.makeText(getApplicationContext(),"Capture Image First",Toast.LENGTH_SHORT).show();
                }
            }
        });

        if(capturedImage){
            //Displaying Toast with message
            Toast.makeText(getApplicationContext(), "Press Compressed Image", Toast.LENGTH_SHORT).show();
        }else {
            //Displaying Toast with message
            Toast.makeText(getApplicationContext(), "Press Demo or Capture Image", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("originalImageViewBitmap", originalImageView.mBitmap);
        outState.putParcelable("compressedImageViewBitmap", compressedImageView.mBitmap);
        outState.putBoolean("demoOrigImage",demoOrigImage);
        outState.putBoolean("capturedImage",capturedImage);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        originalImageView.mBitmap=savedInstanceState.getParcelable("originalImageViewBitmap");
        compressedImageView.mBitmap=savedInstanceState.getParcelable("compressedImageViewBitmap");
        demoOrigImage=savedInstanceState.getBoolean("demoOrigImage");
        capturedImage=savedInstanceState.getBoolean("capturedImage");
    }


    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    // Work with Samsung GS7
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {

            // Get the dimensions of the View
            int targetW = originalImageView.getWidth();
            int targetH = originalImageView.getHeight();

            // Get the dimensions of the bitmap
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
            int photoW = bmOptions.outWidth;
            int photoH = bmOptions.outHeight;

            // Determine how much to scale down the image
            int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

            // Decode the image file into a Bitmap sized to fill the View
            bmOptions.inJustDecodeBounds = false;
            bmOptions.inSampleSize = scaleFactor;
            bmOptions.inPurgeable = true;

            Bitmap origBitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
            //Bitmap origBitmap = BitmapFactory.decodeFile(mCurrentPhotoPath);

            if(origBitmap != null) {
                demoOrigImage = false;
                originalImageView.clearCanvas();
                Bitmap origBWBitmap = originalImageView.convertToBW(origBitmap, 0);
                originalImageView.drawBitmapImage(origBWBitmap);
            } else {
                //Displaying Toast with message
                Toast.makeText(getApplicationContext(), "No Image Available! Try again ...", Toast.LENGTH_SHORT).show();
            }
        }
        else {
            //Displaying Toast with message
            Toast.makeText(getApplicationContext(), "Capturing did not work! Try again ...", Toast.LENGTH_SHORT).show();
        }
    }


    /*
    // Work with Moto Droid API 19
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Bundle extras = data.getExtras();
        Bitmap origBitmap = (Bitmap) extras.get("data");
        if(origBitmap != null) {
            demoOrigImage=false;
            capturedImage=true;
            originalImageView.clearCanvas();
            Bitmap origBWBitmap = originalImageView.convertToBW(origBitmap, 0);
            originalImageView.drawBitmapImage(origBWBitmap);
            //Displaying Toast with message
            Toast.makeText(getApplicationContext(), "Press Compressed Image", Toast.LENGTH_SHORT).show();
        } else {
            //Displaying Toast with message
            Toast.makeText(getApplicationContext(), "No Image Captured! Try again ...", Toast.LENGTH_SHORT).show();
        }
    }*/

}
