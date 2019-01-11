package com.prateek.blinklite;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.GradientDrawable;
import android.hardware.Camera;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.support.constraint.ConstraintLayout;
import android.os.Bundle;
import android.support.v4.view.GestureDetectorCompat;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.SeekBar;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.prateek.blinklite.utility.Brightness;

import static android.content.ContentValues.TAG;
import static com.prateek.blinklite.utility.Cast.FloatToInt;
import static com.prateek.blinklite.utility.Cast.IntToFloat;

public class MainActivity extends Activity implements GestureDetector.OnGestureListener {

    ConstraintLayout cslayout;
    CameraManager camManager;
    Camera mCamera;
    Camera.Parameters mParams;
    ToggleButton tb_switch;
    boolean flashLightStatus = false;
    boolean deviceHasCameraFlash;
    private AdView mAdView;
    GestureDetector gestureScanner;
    float brightLevel;
    SeekBar brightnessBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                //.addTestDevice()
                .addTestDevice("ABAA0D507B8EDF5698AE9A4B620AA2D4")
                .build();
        mAdView.loadAd(adRequest);

        deviceHasCameraFlash = getPackageManager()
                .hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
        camManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);

        cslayout = (ConstraintLayout) findViewById(R.id.cs_layout);
        View myView = findViewById(R.id.cs_layout);
        gestureScanner = new GestureDetector(this, this);
        myView.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                return gestureScanner.onTouchEvent(event);
            }
        });
        brightnessBar = (SeekBar) findViewById(R.id.brightnessBar);
        brightnessBar.setMax(10);
        brightnessBar.setProgress(255 * 10/255);
    }

    @Override
    protected void onStart() {
        super.onStart();

        tb_switch = (ToggleButton) findViewById(R.id.tb_switch);

        tb_switch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(deviceHasCameraFlash){
                    if(flashLightStatus){//when light on
                        FlashLightOff();//we should off
                    }
                    else { //when light off
                        FlashLightOn();//we should on
                    }
                }
                else{
                    Toast.makeText(MainActivity.this, "No flash available on your device.", Toast.LENGTH_SHORT).show();
                }
            }

        });

        brightnessBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int iProgress, boolean b) {
                WindowManager.LayoutParams layoutParams = getWindow().getAttributes(); // Get Params
                float BackLightValue = (float)iProgress;
                layoutParams.screenBrightness = BackLightValue * 255 / 10; // Set Value
                getWindow().setAttributes(layoutParams); // Set params
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(deviceHasCameraFlash){
            if(flashLightStatus == false){//when light on
                FlashLightOn();//we should off
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(deviceHasCameraFlash){
            if(flashLightStatus == true){//when light on
                FlashLightOff();//we should off
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(deviceHasCameraFlash){
            if(flashLightStatus == true){//when light on
                FlashLightOff();//we should off
            }
        }
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
    }

    private void FlashLightOn() {
        try{
            String cameraId = null; // Usually front camera is at 0 position.

            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1) {

                    mCamera = Camera.open();
                    mParams = mCamera.getParameters();
                    mParams.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                    mCamera.setParameters(mParams);
                    SurfaceTexture mPreviewTexture = new SurfaceTexture(0);
                    try {
                        mCamera.setPreviewTexture(mPreviewTexture);
                    } catch (Exception e) {
                    }
                    mCamera.startPreview();

            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                cameraId = camManager.getCameraIdList()[0];
                camManager.setTorchMode(cameraId, true);
            }
            flashLightStatus=true;
            tb_switch.setBackgroundResource(R.mipmap.off);
            tb_switch.setTextColor(Color.WHITE);
            //cslayout.setBackgroundResource(R.color.colorWhite);
            GradientDrawable gd = new GradientDrawable(
                    GradientDrawable.Orientation.TOP_BOTTOM,
                    new int[] {0xFFFFFFFF,0xFFFFFFFF});
            gd.setCornerRadius(0f);
            cslayout.setBackgroundDrawable(gd);
        } catch (Exception e){

        }
    }
    private void FlashLightOff() {
        try{

            String cameraId = null; // Usually front camera is at 0 position.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                cameraId = camManager.getCameraIdList()[0];
                if (mCamera != null) {
                    mParams = mCamera.getParameters();
                    if (mParams.getFlashMode().equals(Camera.Parameters.FLASH_MODE_TORCH)) {
                        mParams.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                        mCamera.setParameters(mParams);
                        mCamera.stopPreview();
                        mCamera.release();
                        mCamera = null;
                        //mCamera.equals(null);
                    }
                }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                camManager.setTorchMode(cameraId, false);
            }
            flashLightStatus=false;
            tb_switch.setBackgroundResource(R.mipmap.on);
            tb_switch.setTextColor(Color.WHITE);
            //cslayout.setBackgroundResource(R.color.colorSilver);
            GradientDrawable gd = new GradientDrawable(
                    GradientDrawable.Orientation.TOP_BOTTOM,
                    new int[] {0xFF616261,0xFF131313});
            gd.setCornerRadius(0f);
            cslayout.setBackgroundDrawable(gd);
        } catch (Exception e){

        }
    }


    @Override
    public boolean onDown(MotionEvent motionEvent) {
        return true;
    }

    @Override
    public void onShowPress(MotionEvent motionEvent) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1, float distanceX, float distanceY) {
        return false;

    }

    @Override
    public void onLongPress(MotionEvent motionEvent) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        WindowManager.LayoutParams layoutParams = getWindow().getAttributes();

        if (Math.abs(velocityX) > Math.abs(velocityY)) {
            if (e1.getX() > e2.getX()) {
                Log.d(TAG, "Right to Left swipe performed");
                layoutParams.screenBrightness = 0; // Set Value
            }else {
                Log.d(TAG, "Left to Rightswipe performed");
                layoutParams.screenBrightness = 255; // Set Value
            }
        }else {
            if (e1.getY() > e2.getY()) {
                Log.d(TAG, "Top to Bottom swipe performed");
                layoutParams.screenBrightness = 255; // Set Value
            }else {
                Log.d(TAG, "Bottom to Top swipe performed");
                layoutParams.screenBrightness = 0; // Set Value
            }
        }
        getWindow().setAttributes(layoutParams); // Set params
        brightnessBar.setProgress((int) layoutParams.screenBrightness * 10/255);

        return true;
    }
}
