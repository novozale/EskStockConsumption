package com.rtsr.eskstockconsumption;


import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class StockConsumptionActivity extends AppCompatActivity {
    public static String service_url;
    public static String my_login;
    public static String my_pass;
    public static Boolean my_ssl_check;

    TextView barcodeInfo;
    SurfaceView cameraView;
    SurfaceView transparentView;
    CameraSource cameraSource;
    Camera main_Camera;
    SurfaceHolder transparent_holder;
    int my_camera;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ////////////////////////////////////////////////////////////////////////////////////////////
        //
        // старт
        //
        ////////////////////////////////////////////////////////////////////////////////////////////
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_consumption);

        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayShowHomeEnabled(true);
        actionbar.setIcon(R.mipmap.esk);
        actionbar.setTitle("Электроскандия");
        actionbar.setSubtitle("ввод расхода запасов");

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        service_url = sharedPref.getString("service_url", "");
        my_login = sharedPref.getString("my_login", "");
        my_pass = sharedPref.getString("my_password", "");
        my_ssl_check = sharedPref.getBoolean("check_certificat", true);

        cameraView = (SurfaceView) findViewById(R.id.camera_view);
        transparentView = (SurfaceView) findViewById(R.id.transparent_view);
        transparentView.setZOrderMediaOverlay(true);

        my_camera = ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA);

        if (my_camera != PackageManager.PERMISSION_GRANTED) {
            List<String> listPermissionsNeeded = new ArrayList<>();
            listPermissionsNeeded.add(android.Manifest.permission.CAMERA);
            //ActivityCompat.requestPermissions(this,listPermissionsNeeded.toArray
            //        (new String[listPermissionsNeeded.size()]), 1);
            this.requestPermissions(listPermissionsNeeded.toArray
                    (new String[listPermissionsNeeded.size()]), 1);
        }

        barcodeInfo = (TextView) findViewById(R.id.txtContent);

        BarcodeDetector barcodeDetector =
            new BarcodeDetector.Builder(this)
               .setBarcodeFormats(Barcode.EAN_13)//QR_CODE)
               .build();

        cameraSource = new CameraSource
            .Builder(this, barcodeDetector)
            .setRequestedPreviewSize(3200, 2400)
            .build();

        if (my_camera == PackageManager.PERMISSION_GRANTED) {
            cameraView.getHolder().addCallback(new SurfaceHolder.Callback() {
                @Override
                public void surfaceCreated(SurfaceHolder holder) {


                    try {
                        cameraSource.start(cameraView.getHolder());
                    } catch (IOException ie) {
                        Log.e("CAMERA SOURCE", ie.getMessage());
                    }
                }

                @Override
                public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                    Field[] declaredFields = CameraSource.class.getDeclaredFields();
                    for (Field field : declaredFields) {
                        if (field.getType() == Camera.class) {
                            field.setAccessible(true);
                            try {
                                main_Camera = (Camera) field.get(cameraSource);
                                if (main_Camera != null) {
                                    Camera.Parameters parameters = main_Camera.getParameters();
                                    parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
                                    main_Camera.setParameters(parameters);
                                    main_Camera.startPreview();
                                 }
                            } catch (IllegalAccessException e) {
                                e.printStackTrace();
                            }
                            break;
                        }
                    }
                }

                @Override
                public void surfaceDestroyed(SurfaceHolder holder) {
                    cameraSource.stop();
                }

            });
        }

        transparentView.getHolder().addCallback(new SurfaceHolder.Callback() {

            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                transparent_holder = holder;
                holder.setFormat(PixelFormat.TRANSPARENT);
                Draw();
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }



            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {

            }
        });

        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {
            }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {

                final SparseArray<Barcode> barcodes = detections.getDetectedItems();

                if (barcodes.size() != 0) {
                    barcodeInfo.post(new Runnable() {    // Use the post method of the TextView
                        public void run() {
                            barcodeInfo.setText(    // Update the TextView
                                    barcodes.valueAt(0).displayValue
                            );
                            Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                            // Vibrate for 500 milliseconds
                            v.vibrate(500);
                            ToneGenerator toneG = new ToneGenerator(AudioManager.STREAM_ALARM, 100);
                            toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 200);
                        }
                    });
                }
            }
        });

        Button Button1Type = (Button)findViewById(R.id.button_start);         //начало ввода
        Button1Type.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                //get_barcode_from_item();
            }
        });
    }

    public void Draw() {
        if(transparent_holder != null) {
            Paint paint;
            Canvas canvas;
            float RectLeft = 280;
            float RectTop = 260;
            float RectRight = 680;
            float RectBottom = 460;

            float StartX = 100;
            float StartY = 360;
            float StopX = 820;
            float StopY = 360;

            canvas = transparent_holder.lockCanvas();
            canvas.drawColor(0, PorterDuff.Mode.CLEAR);
            //border's properties
            paint = new Paint();
            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(Color.YELLOW);
            paint.setStrokeWidth(5);
            canvas.drawRect(RectLeft, RectTop, RectRight, RectBottom, paint);

            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(Color.WHITE);
            paint.setStrokeWidth(3);
            canvas.drawLine(StartX, StartY, StopX, StopY, paint);

            transparent_holder.unlockCanvasAndPost(canvas);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        ////////////////////////////////////////////////////////////////////////////////////////////
        //
        // вывод меню
        //
        ////////////////////////////////////////////////////////////////////////////////////////////
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_back, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        ////////////////////////////////////////////////////////////////////////////////////////////
        //
        // Меню
        //
        ////////////////////////////////////////////////////////////////////////////////////////////
        switch(item.getItemId()){
            case R.id.menu_back:    //выход из окна
                finish();
                return true;
            default:
                return false;
        }
    }
}
