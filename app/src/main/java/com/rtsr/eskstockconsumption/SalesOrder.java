package com.rtsr.eskstockconsumption;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Handler;
import android.os.SystemClock;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SalesOrder extends AppCompatActivity {
    public static Integer OrderId = null;
    public static String OrderNum = null;
    public static String WHNum = null;
    public static Boolean my_view;
    public static String service_url;
    public static String my_login;
    public static String my_pass;
    public static Boolean my_ssl_check;

    public static SurfaceView cameraView;
    public static SurfaceView transparentView;
    public static CameraSource cameraSource;
    public static Camera main_Camera;
    public static SurfaceHolder transparent_holder;
    public static SurfaceHolder.Callback CameraCallBack;
    public static int my_camera;

    public static Button btnstopscan;
    public static ListView listView1 = null;

    @Override
    public void onBackPressed() {
        Toast toast = Toast.makeText(SalesOrder.this, "Выход из окна только по крестику вверху.", Toast.LENGTH_LONG);
        toast.show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        TextView mt = null;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sales_order);

        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayShowHomeEnabled(true);
        actionbar.setIcon(R.mipmap.esk);
        actionbar.setTitle("Электроскандия");
        actionbar.setSubtitle("отгрузка");

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        service_url = sharedPref.getString("service_url", "");
        my_login = sharedPref.getString("my_login", "");
        my_pass = sharedPref.getString("my_password", "");
        my_ssl_check = sharedPref.getBoolean("check_certificat", true);
        my_view = sharedPref.getBoolean("screen_orientation", false);
        if (my_view == false) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }

        Bundle order_param = getIntent().getExtras();
        if(order_param != null){
            OrderId = order_param.getInt("RecordID");
            OrderNum = order_param.getString("OrderNum");
            WHNum = order_param.getString("WHNum");
            mt = (TextView) findViewById(R.id.sales_order_item_order_id);
            mt.setText(OrderId.toString());
            mt = (TextView) findViewById(R.id.sales_order_item_order_num);
            mt.setText(OrderNum);
        }

        Button sales_order_item_btnscan = (Button)findViewById(R.id.sales_order_item_btnscan); //Старт чтения штрихкода
        sales_order_item_btnscan.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                my_camera = ContextCompat.checkSelfPermission(SalesOrder.this, android.Manifest.permission.CAMERA);
                if (my_camera != PackageManager.PERMISSION_GRANTED) {
                    List<String> listPermissionsNeeded = new ArrayList<>();
                    listPermissionsNeeded.add(android.Manifest.permission.CAMERA);
                    SalesOrder.this.requestPermissions(listPermissionsNeeded.toArray
                            (new String[listPermissionsNeeded.size()]), 1);
                    return;
                }
                StartBarcodeReading(1);
            }
        });

        Button sales_order_item_btnstopscan = (Button)findViewById(R.id.sales_order_item_btnstopscan); //Стоп чтения штрихкода
        sales_order_item_btnstopscan.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                my_camera = ContextCompat.checkSelfPermission(SalesOrder.this, android.Manifest.permission.CAMERA);
                if (my_camera != PackageManager.PERMISSION_GRANTED) {
                    List<String> listPermissionsNeeded = new ArrayList<>();
                    listPermissionsNeeded.add(android.Manifest.permission.CAMERA);
                    SalesOrder.this.requestPermissions(listPermissionsNeeded.toArray
                            (new String[listPermissionsNeeded.size()]), 1);
                    return;
                }
                StopBarcodeReading();
            }
        });

        Button btn_write = (Button)findViewById(R.id.sales_order_item_btn_write); //запись ввода расхода запасов в Scala
        btn_write.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(SalesOrder.this);
                alertDialogBuilder.setTitle("Обновить?");
                alertDialogBuilder
                        .setMessage("Внимание! будут обновлены отгрузки, помеченные галочками!")
                        .setCancelable(false)
                        .setPositiveButton("Да",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        record_Shipments();
                                    }
                                })

                        .setNegativeButton("Нет", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();

            }
        });

        Button btn_close = (Button)findViewById(R.id.sales_order_item_btn_close); //закрытие заказа (отгрузки) в Scala
        btn_close.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(SalesOrder.this);
                alertDialogBuilder.setTitle("Закрыть?");
                alertDialogBuilder
                        .setMessage("Внимание! Отгрузка по данному заказу будет закрыта. Открыть сможет только начальник склада.")
                        .setCancelable(false)
                        .setPositiveButton("Да",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        close_Order();
                                    }
                                })

                        .setNegativeButton("Нет", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();

            }
        });

        Button btn_selectall = (Button)findViewById(R.id.sales_order_item_btn_selall); //выбрать все записи
        btn_selectall.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                mark_all_items();
            }
        });

        Button btn_unselectall = (Button)findViewById(R.id.sales_order_item_btn_deselectall); //снять выбор всех записей
        btn_unselectall.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                unmark_all_items();
            }
        });

        Button btn_refresh = (Button)findViewById(R.id.sales_order_item_btn_refresh); //обновить
        btn_refresh.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(SalesOrder.this);
                alertDialogBuilder.setTitle("Обновить?");
                alertDialogBuilder
                        .setMessage("Внимание! При обновлении пропадут все внесенные количества по отгрузке!")
                        .setCancelable(false)
                        .setPositiveButton("Да",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        GetOrderInfo();
                                    }
                                })

                        .setNegativeButton("Нет", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
            }
        });

        GetOrderInfo();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        ////////////////////////////////////////////////////////////////////////////////////////////
        //
        // отрисовка полученных из подборки партиий результатов
        //
        ////////////////////////////////////////////////////////////////////////////////////////////
        Double old_Order_QTY = 0.0;
        Double old_ToAssemble_QTY = 0.0;

        super.onActivityResult(requestCode, resultCode, data);
        if (data != null) {
            Bundle order_param = data.getExtras();
            String StrNum = order_param.getString("StrNum");
            String new_ToAssembleQTY = order_param.getString("new_ToAssembleQTY");
            Map<String,String> MyItem;
            OrderItemsListAdapter sa = (OrderItemsListAdapter)listView1.getAdapter();
            for (Integer i = 0; i < sa.getCount(); i++) {
                MyItem = (Map<String,String>)sa.getItem(i);
                if(MyItem.get("StrNum").equals(StrNum)){
                    View v;
                    TextView tv;
                    ArrayList<Map<String,String>> MyArray = (ArrayList<Map<String,String>>)sa.MyArray;
                    old_Order_QTY = Double.parseDouble(MyArray.get(i).get("OrderQTY").toString());
                    old_ToAssemble_QTY = Double.parseDouble(MyArray.get(i).get("ToAssembleQTY").toString());
                    MyArray.get(i).put("ToAssembleQTY", new_ToAssembleQTY);
                    MyArray.get(i).put("AvlQTY", new_ToAssembleQTY);
                    MyArray.get(i).put("OrderQTY",
                            String.valueOf(old_Order_QTY - (old_ToAssemble_QTY - Double.parseDouble(new_ToAssembleQTY))));
                    //v = listView1.getChildAt(i);
                    //tv = v.findViewById(R.id.sales_order_detail_strnum);
                    //tv.setText(new_ToAssembleQTY);
                    listView1.invalidateViews();
                    return;
                }
            }
        }
    }

    private void mark_all_items(){
        ////////////////////////////////////////////////////////////////////////////////////////////
        //
        // пометка всех возможных элементов
        //
        ////////////////////////////////////////////////////////////////////////////////////////////

        LinearLayout ll;
        ListView lv;
        CheckBox cb;
        ArrayList<Map<String,String>> MyArray;
        OrderItemsListAdapter oila;

        lv = (ListView) findViewById(R.id.activity_sales_order_item_list);
        oila = (OrderItemsListAdapter)lv.getAdapter();
        MyArray = oila.MyArray;
        for (Integer cc = 0; cc < lv.getAdapter().getCount(); cc++) {
            ll = (LinearLayout)lv.getAdapter().getView(cc, null, lv);
            cb = ll.findViewById(R.id.sales_order_detail_marktowrite);
            if (cb.isEnabled()) {
                if (MyArray.get(cc).get("MarkToWrite").equals("0")) {
                    cb.performClick();
                }
             }
        }
        lv.invalidateViews();
     }

    private void unmark_all_items(){
        ////////////////////////////////////////////////////////////////////////////////////////////
        //
        // снятие пометки всех возможных элементов
        //
        ////////////////////////////////////////////////////////////////////////////////////////////

        LinearLayout ll;
        ListView lv;
        CheckBox cb;
        ArrayList<Map<String,String>> MyArray;
        OrderItemsListAdapter oila;

        lv = (ListView) findViewById(R.id.activity_sales_order_item_list);
        oila = (OrderItemsListAdapter)lv.getAdapter();
        MyArray = oila.MyArray;
        for (Integer cc = 0; cc < lv.getAdapter().getCount(); cc++) {
            ll = (LinearLayout)lv.getAdapter().getView(cc, null, lv);
            cb = ll.findViewById(R.id.sales_order_detail_marktowrite);
            if (cb.isEnabled()) {
                if (MyArray.get(cc).get("MarkToWrite").equals("1")) {
                    cb.performClick();
                }
            }
        }
        lv.invalidateViews();
    }

    public void GetOrderInfo(){
        ////////////////////////////////////////////////////////////////////////////////////////////
        //
        // получение информации о заказе
        //
        ////////////////////////////////////////////////////////////////////////////////////////////
        JSONArray jsarr = null;
        JSONObject jsel = null;

        if (my_login.equals("") || my_pass.equals("")) {           //-----не заполнен логин или пароль
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setTitle("Внимание!");
            alertDialogBuilder
                    .setMessage("Не заполнен логин или пароль")
                    .setCancelable(false)
                    .setPositiveButton("OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        } else {
            try {
                //---------------------список заказов---------------------------------------------------
                String my_str = new String("");
                HttpWorkAsync my_httpAs = new HttpWorkAsync();
                my_str = my_httpAs.execute(service_url + "GetOrderForAssemblingJson/?order=" + OrderNum,
                        my_login, my_pass, my_ssl_check).get().toString();
                if (my_httpAs.get_my_error() !=  ""){
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                    alertDialogBuilder.setTitle("Ошибка!");
                    alertDialogBuilder
                            .setMessage(my_httpAs.my_error)
                            .setCancelable(false)
                            .setPositiveButton("OK",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.cancel();
                                        }
                                    });
                    AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();
                } else {
                    //-----отрисовка значений
                    if (my_str != "") {

                        jsarr = new JSONArray(my_str);
                        if (jsarr.length() > 0) {
                            jsel = jsarr.getJSONObject(0);
                            if (jsel.getString("ErrorNum").equals("0") == false) {
                                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                                alertDialogBuilder.setTitle("Ошибка!");
                                alertDialogBuilder
                                        .setMessage(jsel.getString("ErrorValue"))
                                        .setCancelable(false)
                                        .setPositiveButton("OK",
                                                new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int id) {
                                                        dialog.cancel();
                                                    }
                                                });
                                AlertDialog alertDialog = alertDialogBuilder.create();
                                alertDialog.show();
                            } else {
                                ArrayList<Map<String, String>> listItems = getArrayListFromJSONArray(jsarr);
                                listView1 = (ListView) findViewById(R.id.activity_sales_order_item_list);
                                String[] from = {"StrNum", "ItemCode", "ItemName", "ItemType", "IsCabel",
                                        "RequestedCabelParts", "OrderQTY", "AvlQTY", "ToAssembleQTY", "ItemCell", "BarCode"};
                                int[] to = {R.id.sales_order_detail_strnum, R.id.sales_order_detail_itemcode, R.id.sales_order_detail_itemname,
                                        R.id.sales_order_detail_itemtype, R.id.sales_order_detail_iscabel, R.id.sales_order_detail_requestedcabelparts,
                                        R.id.sales_order_detail_orderqty, R.id.sales_order_detail_avlqty, R.id.sales_order_detail_toassembleqty,
                                        R.id.sales_order_detail_itemcell, R.id.sales_order_detail_barcode};
                                //listView1.setAdapter(new SimpleAdapter(this, listItems, R.layout.sales_order_detail, from, to));
                                listView1.setAdapter(new OrderItemsListAdapter(this, listItems, R.layout.sales_order_detail, from, to));
                             }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                alertDialogBuilder.setTitle("Ошибка!");
                alertDialogBuilder
                        .setMessage(e.getLocalizedMessage())
                        .setCancelable(false)
                        .setPositiveButton("OK",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });
                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
            }
        }
    }

    private ArrayList<Map<String,String>> getArrayListFromJSONArray(JSONArray jsonArray){
        ////////////////////////////////////////////////////////////////////////////////////////////
        //
        // получение листа значений из массива JSON
        //
        ////////////////////////////////////////////////////////////////////////////////////////////
        ArrayList<Map<String,String>> aList = new ArrayList<Map<String,String>>();
        JSONObject jsel = null;

        try{
            if (jsonArray != null) {
                for (int i = 0; i < jsonArray.length(); i++) {
                    jsel = jsonArray.getJSONObject(i);
                    Map<String, String> datum = new HashMap<String, String>();
                    datum.put("StrNum", jsel.getString("StrNum"));
                    datum.put("ItemCode", jsel.getString("ItemCode"));
                    datum.put("ItemName", jsel.getString("ItemName"));
                    datum.put("ItemType", jsel.getString("ItemType"));
                    datum.put("IsCabel", jsel.getString("IsCabel"));
                    datum.put("RequestedCabelParts", jsel.getString("RequestedCabelParts"));
                    datum.put("OrderQTY", String.valueOf(Float.parseFloat(jsel.getString("RequestedQTY"))
                            - Float.parseFloat(jsel.getString("AssembledQTY"))));
                    datum.put("AvlQTY", jsel.getString("ToAssembleQTY"));
                    datum.put("ToAssembleQTY", jsel.getString("ToAssembleQTY"));
                    datum.put("ItemCell", jsel.getString("ItemCell"));
                    datum.put("BarCode", jsel.getString("BarCode"));
                    datum.put("MarkToWrite", jsel.getString("MarkToWrite"));
                    aList.add(datum);
                }
            }
        } catch (JSONException je){
            je.printStackTrace();
        }
        return  aList;
    }

    public void StartBarcodeReading(Integer my_flag){
        ////////////////////////////////////////////////////////////////////////////////////////////
        //
        // запуск камеры на чтение штрихкодов
        //
        ////////////////////////////////////////////////////////////////////////////////////////////
        cameraView = (SurfaceView) findViewById(R.id.camera_view);

        if (my_flag == 1) {
            RelativeLayout my_rl = (RelativeLayout) findViewById(R.id.scaner_layout);
            my_rl.setVisibility(View.VISIBLE);
        }

        BarcodeDetector barcodeDetector =
                new BarcodeDetector.Builder(this)
                        .setBarcodeFormats(Barcode.EAN_13) //что сканируем
                        .build();

        cameraSource = new CameraSource
                .Builder(this, barcodeDetector)
                .setRequestedPreviewSize(1920, 1080)
                .build();

        if (my_camera == PackageManager.PERMISSION_GRANTED) {
            try {
                cameraSource.start(cameraView.getHolder());
                CameraCallBack = new SurfaceHolder.Callback() {

                    @Override
                    public void surfaceCreated(SurfaceHolder holder) {
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

                };
                cameraView.getHolder().addCallback(CameraCallBack);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {
            }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {

                final SparseArray<Barcode> barcodes = detections.getDetectedItems();

                if (barcodes.size() != 0) {
                    btnstopscan = (Button)findViewById(R.id.sales_order_item_btnstopscan);
                    //btnstopscan.performClick();
                    btnstopscan.post(new Runnable() {    // Use the post method of the TextView
                        public void run() {
                            /*barcodeInfo.setText(    // Update the TextView
                                    barcodes.valueAt(0).displayValue
                            );*/
                            //-------------нахождение элемента и установка его текущим
                            Integer my_flag = 0;

                            String my_barcode = barcodes.valueAt(0).displayValue;
                            Map<String,String> MyItem;
                            SimpleAdapter sa = (SimpleAdapter)listView1.getAdapter();
                            for (Integer i = 0; i < sa.getCount(); i++) {
                                MyItem = (Map<String,String>)sa.getItem(i);
                                if(MyItem.get("BarCode").equals(my_barcode)){
                                    my_flag = 1;
                                    //listView1.smoothScrollToPositionFromTop(i, 0, 0);
                                    listView1.setSelection(i);
                                    //-------------сигнализация о нахождении
                                    Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                                    // Vibrate for 500 milliseconds
                                    v.vibrate(500);
                                    ToneGenerator toneG = new ToneGenerator(AudioManager.STREAM_ALARM, 100);
                                    toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 200);
                                    listView1.getAdapter().getView(i, null, null).performClick();

                                    break;
                                }
                            }
                            StopBarcodeReading();
                            if (my_flag == 0) {
                                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(SalesOrder.this);
                                alertDialogBuilder.setTitle("Ошибка!");
                                alertDialogBuilder
                                        .setMessage("Прочитанный штрихкод не найден в списке на подборку")
                                        .setCancelable(false)
                                        .setPositiveButton("OK",
                                                new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int id) {
                                                        dialog.cancel();
                                                    }
                                                });
                                AlertDialog alertDialog = alertDialogBuilder.create();
                                alertDialog.show();
                            }
                        }
                    });
                 }
                SystemClock.sleep(500);
            }
        });

        transparentView = (SurfaceView) findViewById(R.id.transparent_view);
        transparentView.setZOrderMediaOverlay(true);

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

        transparent_holder = transparentView.getHolder();
        transparent_holder.setFormat(PixelFormat.TRANSPARENT);
        if (my_flag == 1) {
            Draw();
        }
        cameraView.layout(0,0,cameraView.getWidth() - 1,cameraView.getHeight() - 1);
    }

    public void StopBarcodeReading(){
        ////////////////////////////////////////////////////////////////////////////////////////////
        //
        // стоп чтения штрихкодов с камеры
        //
        ////////////////////////////////////////////////////////////////////////////////////////////

        if (cameraView != null) {
            cameraView.getHolder().removeCallback(CameraCallBack);
        }
        if (cameraSource != null) {
            cameraSource.stop();
        }
        RelativeLayout my_rl = (RelativeLayout) findViewById(R.id.scaner_layout);
        my_rl.setVisibility(View.INVISIBLE);
    }

    public void Draw() {
        if(transparent_holder != null) {
            Paint paint;
            Canvas canvas;
            float RectLeft = transparentView.getWidth() * 5 / 16;
            float RectTop = transparentView.getHeight() *12 / 28;
            float RectRight = transparentView.getWidth() * 11 / 16;
            float RectBottom = transparentView.getHeight() *16 / 28;

            float StartX = transparentView.getWidth() / 6;
            float StartY = transparentView.getHeight() / 2;
            float StopX = transparentView.getWidth() * 5 / 6;
            float StopY = transparentView.getHeight() / 2;

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
                Intent intent = new Intent();
                intent.putExtra("OrderNum", OrderNum);
                setResult(RESULT_OK, intent);
                finish();
                return true;
            default:
                return false;
        }
    }

    public class OrderItemsListAdapter extends SimpleAdapter{
        private Context mContext;
        public ArrayList<Map<String,String>> MyArray;
        private LayoutInflater mInflater;
        private Integer SelElement;

        public OrderItemsListAdapter(Context context, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to) {
            ////////////////////////////////////////////////////////////////////////////////////////////
            //
            // конструктор
            //
            ////////////////////////////////////////////////////////////////////////////////////////////
            super(context, data, resource, from, to);
            mInflater = LayoutInflater.from( context );
            mContext = context;
            MyArray = (ArrayList<Map<String,String>>)data;
            SelElement = -1;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent){
            ////////////////////////////////////////////////////////////////////////////////////////////
            //
            // подсветка и обработка
            //
            ////////////////////////////////////////////////////////////////////////////////////////////
            TextView ic;
            Integer IsCabel;
            String HasRequest;
            Integer IsComplex;
            CheckBox cb;
            TextView tv;
            LinearLayout ll;

            final View view = super.getView(position,convertView,parent);
            ic = view.findViewById(R.id.sales_order_detail_iscabel);
            IsCabel = Integer.parseInt(ic.getText().toString());
            ic = view.findViewById(R.id.sales_order_detail_requestedcabelparts);
            HasRequest = ic.getText().toString();
            ic = view.findViewById(R.id.sales_order_detail_itemtype);
            IsComplex = Integer.parseInt(ic.getText().toString());
            tv = view.findViewById(R.id.sales_order_detail_toassembleqty);
            cb = view.findViewById(R.id.sales_order_detail_marktowrite);

            if(MyArray.get(position).get("MarkToWrite").equals("0")){
                cb.setChecked(false);
            } else {
                cb.setChecked(true);
            }

            view.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    listView1.post(new Runnable() {
                        @Override
                        public void run() {
                            Handler handler = new Handler();
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    listView1.setSelection(position);
                                    listView1.setItemChecked(position,true);
                                    listView1.smoothScrollToPositionFromTop(position, 0, 0);
                                }
                            });
                            SelElement = position;
                        }
                    });
                }
            });

            view.setOnLongClickListener(new View.OnLongClickListener() {

                @Override
                public boolean onLongClick(final View view) {
                    listView1.post(new Runnable() {
                        @Override
                        public void run() {
                            if (!MyArray.get(position).get("ItemType").equals("8")) {
                                if (MyArray.get(position).get("IsCabel").equals("1")) {
                                    //-----переход к подбору кабеля
                                    go_to_lots_of_goods(OrderNum, MyArray.get(position).get("StrNum"),
                                            MyArray.get(position).get("ItemCode"), MyArray.get(position).get("AvlQTY"),
                                            MyArray.get(position).get("RequestedCabelParts"), MyArray.get(position).get("ToAssembleQTY"));
                                } else {
                                    //-----ввод отгруженного количества
                                    final TextView tv1 = (TextView)view.findViewById(R.id.sales_order_detail_toassembleqty);
                                    tv1.post(new Runnable(){

                                        @Override
                                        public void run() {
                                            //---ввод количества
                                            AlertDialog.Builder alertDialog = new AlertDialog.Builder(SalesOrder.this);
                                            alertDialog.setTitle("Отгружено");
                                            alertDialog.setMessage("Введите количество отгруженного товара");
                                            final EditText txt_input = new EditText(SalesOrder.this);
                                            txt_input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                                            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                                    LinearLayout.LayoutParams.MATCH_PARENT);
                                            txt_input.setLayoutParams(lp);
                                            alertDialog.setView(txt_input);
                                            txt_input.setText(tv1.getText());
                                            alertDialog.setPositiveButton("Ввод",
                                                    new DialogInterface.OnClickListener() {
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            String EnteredQTY = "";
                                                            EnteredQTY = txt_input.getText().toString();
                                                            if (EnteredQTY.equals("")) {
                                                                //-----кол-во не введено
                                                                Toast toast = Toast.makeText(SalesOrder.this, "Необходимо ввести отгружаемое количество.", Toast.LENGTH_LONG);
                                                                toast.show();
                                                            } else {
                                                                if (Double.parseDouble(txt_input.getText().toString())
                                                                        > Double.parseDouble(MyArray.get(position).get("AvlQTY"))) {
                                                                    //-----кол-во больше, чем доступно к отгрузке
                                                                    Toast toast = Toast.makeText(SalesOrder.this, "Количество не может быть больше доступного", Toast.LENGTH_LONG);
                                                                    toast.show();
                                                                } else {
                                                                    //CheckBox cb;
                                                                    MyArray.get(position).put("ToAssembleQTY", txt_input.getText().toString());
                                                                    MyArray.get(position).put("MarkToWrite", "1");
                                                                    listView1.invalidateViews();
                                                                }
                                                            }
                                                        }
                                                    });
                                            alertDialog.setNegativeButton("Отмена",
                                                    new DialogInterface.OnClickListener() {
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            dialog.cancel();
                                                        }
                                                    });
                                            txt_input.requestFocus();
                                            txt_input.selectAll();

                                            alertDialog.show();
                                            tv1.post(new Runnable() {
                                                @Override
                                                public void run() {
                                                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                                    imm.showSoftInput(txt_input, InputMethodManager.SHOW_IMPLICIT);
                                                }
                                            });
                                        }
                                    });
                                }
                            }
                        }
                    });
                    return true;
                }
            });

            if (SelElement == position) {
                view.setBackgroundColor(Color.rgb(177, 228, 226));
            } else {
                view.setBackgroundColor(Color.WHITE);
            }

            if (IsComplex == 8) {    //---составной продукт
                ic = view.findViewById(R.id.sales_order_detail_requestedcabelparts_lbl);
                LinearLayout.LayoutParams params10 = (LinearLayout.LayoutParams) ic.getLayoutParams();
                params10.height = 0;
                params10.setMargins(0, 0, 0, 0);
                ic.setLayoutParams(params10);
                ic.setPadding(0, 0, 0, 0);

                ic = view.findViewById(R.id.sales_order_detail_requestedcabelparts);
                LinearLayout.LayoutParams params11 = (LinearLayout.LayoutParams) ic.getLayoutParams();
                params11.height = 0;
                params11.setMargins(0, 0, 0, 0);
                ic.setLayoutParams(params11);
                ic.setPadding(0, 0, 0, 0);

                ll = view.findViewById(R.id.sales_order_detail_requested_size_layout);
                ll.setPadding(0, 0, 0, 0);
                ViewGroup.LayoutParams params12 = ll.getLayoutParams();
                params12.height = 0;
                ll.setLayoutParams(params12);

                tv.setBackgroundColor(Color.RED);
                 cb.setEnabled(false);
            } else {
                if (IsCabel == 1) {                     //---кабельная продукция
                    if (HasRequest.equals("")) {        //---размер кусков кабеля не запрошен
                        ic = view.findViewById(R.id.sales_order_detail_requestedcabelparts_lbl);
                        LinearLayout.LayoutParams params1 = (LinearLayout.LayoutParams) ic.getLayoutParams();
                        params1.height = 0;
                        params1.setMargins(0, 0, 0, 0);
                        ic.setLayoutParams(params1);
                        ic.setPadding(0, 0, 0, 0);

                        ic = view.findViewById(R.id.sales_order_detail_requestedcabelparts);
                        LinearLayout.LayoutParams params2 = (LinearLayout.LayoutParams) ic.getLayoutParams();
                        params2.height = 0;
                        params2.setMargins(0, 0, 0, 0);
                        ic.setLayoutParams(params2);
                        ic.setPadding(0, 0, 0, 0);

                        ll = view.findViewById(R.id.sales_order_detail_requested_size_layout);
                        ll.setPadding(0, 0, 0, 0);
                        ViewGroup.LayoutParams params3 = ll.getLayoutParams();
                        params3.height = 0;
                        ll.setLayoutParams(params3);

                        tv.setBackgroundColor(Color.YELLOW);
                        cb.setEnabled(false);
                    } else {                           //---запрошены размеры кусков кабеля
                        ic = view.findViewById(R.id.sales_order_detail_requestedcabelparts_lbl);
                        LinearLayout.LayoutParams params4 = (LinearLayout.LayoutParams) ic.getLayoutParams();
                        params4.height = -1;
                        params4.setMargins(2, 2, 2, 2);
                        ic.setLayoutParams(params4);
                        ic.setPadding(3, 0, 0, 0);

                        ic = view.findViewById(R.id.sales_order_detail_requestedcabelparts);
                        LinearLayout.LayoutParams params5 = (LinearLayout.LayoutParams) ic.getLayoutParams();
                        params5.height = -1;
                        params5.setMargins(2, 2, 2, 2);
                        ic.setLayoutParams(params5);
                        ic.setPadding(3, 0, 0, 0);

                        ll = view.findViewById(R.id.sales_order_detail_requested_size_layout);
                        ll.setPadding(0, 0, 0, 0);
                        ViewGroup.LayoutParams params6 = ll.getLayoutParams();
                        params6.height = -1;
                        ll.setLayoutParams(params6);

                        tv.setBackgroundColor(Color.rgb(252, 149, 128));
                        cb.setEnabled(false);
                    }
                } else {
                    ic = view.findViewById(R.id.sales_order_detail_requestedcabelparts_lbl);
                    LinearLayout.LayoutParams params7 = (LinearLayout.LayoutParams) ic.getLayoutParams();
                    params7.height = 0;
                    params7.setMargins(0, 0, 0, 0);
                    ic.setLayoutParams(params7);
                    ic.setPadding(0, 0, 0, 0);

                    ic = view.findViewById(R.id.sales_order_detail_requestedcabelparts);
                    LinearLayout.LayoutParams params8 = (LinearLayout.LayoutParams) ic.getLayoutParams();
                    params8.height = 0;
                    params8.setMargins(0, 0, 0, 0);
                    ic.setLayoutParams(params8);
                    ic.setPadding(0, 0, 0, 0);

                    ll = view.findViewById(R.id.sales_order_detail_requested_size_layout);
                    ll.setPadding(0, 0, 0, 0);
                    ViewGroup.LayoutParams params9 = ll.getLayoutParams();
                    params9.height = 0;
                    ll.setLayoutParams(params9);

                    tv.setBackgroundColor(Color.WHITE);
                    cb.setEnabled(true);

                    cb.setOnClickListener(new View.OnClickListener(){

                        @Override
                        public void onClick(View v) {
                            CheckBox chbx = (CheckBox) v ;
                            if (chbx.isChecked() == true){
                                MyArray.get(position).put("MarkToWrite", "1");
                            } else {
                                MyArray.get(position).put("MarkToWrite", "0");
                            }
                        }
                    });
                }
            }


            return view;
        }

    }

    /*
    public class OrderItemsListAdapter extends SimpleAdapter{
        private Context mContext;
        public ArrayList<Map<String,String>> MyArray;
        private LayoutInflater mInflater;
        private Integer SelElement;

        public OrderItemsListAdapter(Context context, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to) {
            ////////////////////////////////////////////////////////////////////////////////////////////
            //
            // конструктор
            //
            ////////////////////////////////////////////////////////////////////////////////////////////
            super(context, data, resource, from, to);
            mInflater = LayoutInflater.from( context );
            mContext = context;
            MyArray = (ArrayList<Map<String,String>>)data;
            SelElement = -1;
         }

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            if(MyArray != null && MyArray.size() != 0){
                return MyArray.size();
            }
            return 0;
        }

        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return MyArray.get(position);
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent){
            ////////////////////////////////////////////////////////////////////////////////////////////
            //
            // подсветка
            //
            ////////////////////////////////////////////////////////////////////////////////////////////
            TextView ic;
            LinearLayout ll;
            Integer IsCabel;
            String HasRequest;
            Integer IsComplex;
            CheckBox cb;
            EditText et;

            final View view = super.getView(position,convertView,parent);

            ic = view.findViewById(R.id.sales_order_detail_iscabel);
            IsCabel = Integer.parseInt(ic.getText().toString());
            ic = view.findViewById(R.id.sales_order_detail_requestedcabelparts);
            HasRequest = ic.getText().toString();
            ic = view.findViewById(R.id.sales_order_detail_itemtype);
            IsComplex = Integer.parseInt(ic.getText().toString());

            et = view.findViewById(R.id.sales_order_detail_toassembleqty);
            cb = view.findViewById(R.id.sales_order_detail_marktowrite);

            if(MyArray.get(position).get("MarkToWrite").equals("0")){
                cb.setChecked(false);
            } else {
                cb.setChecked(true);
            }

            view.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    listView1.post(new Runnable() {
                        @Override
                        public void run() {
                            listView1.setSelection(position);
                            listView1.setItemChecked(position,true);
                            listView1.smoothScrollToPositionFromTop(position, 0, 0);
                            SelElement = position;
                            if (!MyArray.get(position).get("ItemType").equals("8")) {
                                if (MyArray.get(position).get("IsCabel").equals("1")) {
                                    go_to_lots_of_goods(OrderNum, MyArray.get(position).get("StrNum"),
                                            MyArray.get(position).get("ItemCode"), MyArray.get(position).get("OrderQTY"),
                                            MyArray.get(position).get("RequestedCabelParts"), MyArray.get(position).get("ToAssembleQTY"));
                                } else {
                                    final EditText et1 = (EditText)view.findViewById(R.id.sales_order_detail_toassembleqty);
                                    et1.post(new Runnable(){
                                        @Override
                                        public void run() {
                                            et1.requestFocus();
                                            et1.selectAll();
                                            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                            imm.showSoftInput(et1, InputMethodManager.SHOW_IMPLICIT);
                                        }
                                    });
                                }
                            }
                        }
                    });
                }
            });

            if (SelElement == position) {
                view.setBackgroundColor(Color.rgb(177, 228, 226));
             } else {
                view.setBackgroundColor(Color.WHITE);
            }

            if (IsComplex == 8){    //---составной продукт
                ic = view.findViewById(R.id.sales_order_detail_requestedcabelparts_lbl);
                LinearLayout.LayoutParams params10 = (LinearLayout.LayoutParams) ic.getLayoutParams();
                params10.height = 0;
                params10.setMargins(0, 0, 0, 0);
                ic.setLayoutParams(params10);
                ic.setPadding(0, 0, 0, 0);

                ic = view.findViewById(R.id.sales_order_detail_requestedcabelparts);
                LinearLayout.LayoutParams params11 = (LinearLayout.LayoutParams) ic.getLayoutParams();
                params11.height = 0;
                params11.setMargins(0, 0, 0, 0);
                ic.setLayoutParams(params11);
                ic.setPadding(0, 0, 0, 0);

                ll = view.findViewById(R.id.sales_order_detail_requested_size_layout);
                ll.setPadding(0, 0, 0, 0);
                ViewGroup.LayoutParams params12 = ll.getLayoutParams();
                params12.height = 0;
                ll.setLayoutParams(params12);

                et.setBackgroundColor(Color.RED);
                et.setEnabled(false);
                cb.setEnabled(false);
            } else {
                if (IsCabel == 1) {                     //---кабельная продукция
                    if (HasRequest.equals("")) {        //---размер кусков кабеля не запрошен
                        ic = view.findViewById(R.id.sales_order_detail_requestedcabelparts_lbl);
                        LinearLayout.LayoutParams params1 = (LinearLayout.LayoutParams) ic.getLayoutParams();
                        params1.height = 0;
                        params1.setMargins(0, 0, 0, 0);
                        ic.setLayoutParams(params1);
                        ic.setPadding(0, 0, 0, 0);

                        ic = view.findViewById(R.id.sales_order_detail_requestedcabelparts);
                        LinearLayout.LayoutParams params2 = (LinearLayout.LayoutParams) ic.getLayoutParams();
                        params2.height = 0;
                        params2.setMargins(0, 0, 0, 0);
                        ic.setLayoutParams(params2);
                        ic.setPadding(0, 0, 0, 0);

                        ll = view.findViewById(R.id.sales_order_detail_requested_size_layout);
                        ll.setPadding(0, 0, 0, 0);
                        ViewGroup.LayoutParams params3 = ll.getLayoutParams();
                        params3.height = 0;
                        ll.setLayoutParams(params3);

                        et.setBackgroundColor(Color.YELLOW);
                        et.setEnabled(false);
                        cb.setEnabled(false);
                    } else {                            //---запрошены размеры кусков кабеля
                        ic = view.findViewById(R.id.sales_order_detail_requestedcabelparts_lbl);
                        LinearLayout.LayoutParams params4 = (LinearLayout.LayoutParams) ic.getLayoutParams();
                        params4.height = -1;
                        params4.setMargins(2, 2, 2, 2);
                        ic.setLayoutParams(params4);
                        ic.setPadding(3, 0, 0, 0);

                        ic = view.findViewById(R.id.sales_order_detail_requestedcabelparts);
                        LinearLayout.LayoutParams params5 = (LinearLayout.LayoutParams) ic.getLayoutParams();
                        params5.height = -1;
                        params5.setMargins(2, 2, 2, 2);
                        ic.setLayoutParams(params5);
                        ic.setPadding(3, 0, 0, 0);

                        ll = view.findViewById(R.id.sales_order_detail_requested_size_layout);
                        ll.setPadding(0, 0, 0, 0);
                        ViewGroup.LayoutParams params6 = ll.getLayoutParams();
                        params6.height = -1;
                        ll.setLayoutParams(params6);

                        et.setBackgroundColor(Color.rgb(252, 149, 128));
                        et.setEnabled(false);
                        cb.setEnabled(false);
                    }
                } else {
                    ic = view.findViewById(R.id.sales_order_detail_requestedcabelparts_lbl);
                    LinearLayout.LayoutParams params7 = (LinearLayout.LayoutParams) ic.getLayoutParams();
                    params7.height = 0;
                    params7.setMargins(0, 0, 0, 0);
                    ic.setLayoutParams(params7);
                    ic.setPadding(0, 0, 0, 0);

                    ic = view.findViewById(R.id.sales_order_detail_requestedcabelparts);
                    LinearLayout.LayoutParams params8 = (LinearLayout.LayoutParams) ic.getLayoutParams();
                    params8.height = 0;
                    params8.setMargins(0, 0, 0, 0);
                    ic.setLayoutParams(params8);
                    ic.setPadding(0, 0, 0, 0);

                    ll = view.findViewById(R.id.sales_order_detail_requested_size_layout);
                    ll.setPadding(0, 0, 0, 0);
                    ViewGroup.LayoutParams params9 = ll.getLayoutParams();
                    params9.height = 0;
                    ll.setLayoutParams(params9);

                    et.setBackgroundColor(Color.WHITE);
                    et.setEnabled(true);
                    cb.setEnabled(true);

                    et.setOnFocusChangeListener(new View.OnFocusChangeListener() {

                        @Override
                        public void onFocusChange(View v, boolean hasFocus) {
                            if (!hasFocus) {
                                //final int my_position = v.getId();
                                EditText Caption = (EditText) v;
                                if (Caption.getText().toString().equals("")) {

                                } else {
                                    if (Double.parseDouble(Caption.getText().toString()) < 0) {
                                        Caption.setText("0");
                                        Toast toast = Toast.makeText(SalesOrder.this, "Количество не может быть меньше 0", Toast.LENGTH_LONG);
                                        toast.show();
                                        MyArray.get(position).put("ToAssembleQTY", "0");
                                        listView1.invalidateViews();
                                    } else {
                                        if (Double.parseDouble(Caption.getText().toString())
                                                > Double.parseDouble(MyArray.get(position).get("OrderQTY"))) {
                                            Caption.setText(Caption.getText().toString());
                                            Toast toast = Toast.makeText(SalesOrder.this, "Количество не может быть больше запрошенного в заказе", Toast.LENGTH_LONG);
                                            toast.show();
                                            MyArray.get(position).put("ToAssembleQTY", MyArray.get(position).get("OrderQTY"));
                                            listView1.invalidateViews();
                                        } else {
                                            MyArray.get(position).put("ToAssembleQTY", Caption.getText().toString());
                                        }
                                    }
                                }
                            }
                        }
                    });

                    cb.setOnClickListener(new View.OnClickListener(){

                        @Override
                        public void onClick(View v) {
                            CheckBox chbx = (CheckBox) v ;
                            if (chbx.isChecked() == true){
                                MyArray.get(position).put("MarkToWrite", "1");
                            } else {
                                MyArray.get(position).put("MarkToWrite", "0");
                            }
                        }
                    });
                }
            }

            return view;
        }
    }
    */

    public void go_to_lots_of_goods(String OrderNum, String StrNum, String ItemCode,
                                    String AvlQTY, String RequestedCabelParts,
                                    String ToAssembleQTY){
        ////////////////////////////////////////////////////////////////////////////////////////////
        //
        // Открытие активности с выбором партий товара
        //
        ////////////////////////////////////////////////////////////////////////////////////////////

        Intent my_intent = new Intent(SalesOrder.this, ItemPartions.class);
        my_intent.putExtra("OrderNum", OrderNum);
        my_intent.putExtra("WHNum", WHNum);
        my_intent.putExtra("StrNum", StrNum);
        my_intent.putExtra("ItemCode", ItemCode);
        my_intent.putExtra("AvlQTY", AvlQTY);
        my_intent.putExtra("RequestedCabelParts", RequestedCabelParts);
        my_intent.putExtra("ToAssembleQTY", ToAssembleQTY);

        startActivityForResult(my_intent, 0);
    }

    public void record_Shipments(){
        ////////////////////////////////////////////////////////////////////////////////////////////
        //
        // Запись  помеченных галочкой  оттгрузок
        //
        ////////////////////////////////////////////////////////////////////////////////////////////

        final RelativeLayout my_rl = (RelativeLayout) findViewById(R.id.progress_order_sh);
        my_rl.setVisibility(View.VISIBLE);

        new Thread(new Runnable() {
            public void run() {
                try {
                    synchronized (this) {
                        runOnUiThread(new Runnable() {
                            public void run() {
                                OrderItemsListAdapter sa = (OrderItemsListAdapter)listView1.getAdapter();
                                ArrayList<Map<String,String>> MyArray = (ArrayList<Map<String,String>>)sa.MyArray;
                                for (Integer i = 0; i < sa.getCount(); i++) {
                                    if(MyArray.get(i).get("MarkToWrite").equals("1")){ //-----если помечено на запись
                                        String my_return = write_one_string_sh(OrderNum, WHNum,
                                                MyArray.get(i).get("ItemCode").toString(),
                                                MyArray.get(i).get("StrNum").toString(),
                                                MyArray.get(i).get("ToAssembleQTY").toString());
                                        if (my_return.equals("")) { //-----Запись OK
                                        }else {                     //-----Ошибка записи
                                            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(SalesOrder.this);
                                            alertDialogBuilder.setTitle("Ошибка!");
                                            alertDialogBuilder
                                                    .setMessage(my_return)
                                                    .setCancelable(false)
                                                    .setPositiveButton("OK",
                                                            new DialogInterface.OnClickListener() {
                                                                public void onClick(DialogInterface dialog, int id) {
                                                                    dialog.cancel();
                                                                }
                                                            });
                                            AlertDialog alertDialog = alertDialogBuilder.create();
                                            alertDialog.show();
                                        }
                                    }
                                }
                                GetOrderInfo();
                                my_rl.setVisibility(View.INVISIBLE);
                                Toast toast = Toast.makeText(SalesOrder.this, "Завершена запись об отгрузках", Toast.LENGTH_LONG);
                                toast.show();
                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public String write_one_string_sh(String OrderNum, String WHNum, String ItemCode, String StrNum, String QTY){
        ////////////////////////////////////////////////////////////////////////////////////////////
        //
        // Запись  об отгрузке одной строки заказа
        //
        ////////////////////////////////////////////////////////////////////////////////////////////
        JSONArray jsarr = null;
        JSONObject jsel = null;
        String my_err_message = "";
        String my_err_code = "0";

        if (my_login.equals("") || my_pass.equals("")) {           //-----не заполнен логин или пароль
            return "Не заполнен логин или пароль.";
        } else {
            try {
                String my_str = new String("");
                HttpWorkAsync my_httpAs = new HttpWorkAsync();
                my_str = my_httpAs.execute(service_url + "GetShipmentFromItemRezJson/?OrderNum="
                                + OrderNum + "&WHNum=" + WHNum + "&ItemCode=" + ItemCode
                                + "&StrNum=" + StrNum + "&QTY=" + QTY,
                        my_login, my_pass, my_ssl_check).get().toString();
                if (my_httpAs.get_my_error() !=  ""){
                    return my_httpAs.my_error;
                } else {
                    jsarr = new JSONArray(my_str);
                    if (jsarr.length() > 0) {
                        for (int i = 0; i < jsarr.length(); i++) {
                            jsel = jsarr.getJSONObject(i);
                            my_err_code = jsel.getString("ErrorNum");
                            my_err_message = jsel.getString("ErrorValue");
                        }
                        if (my_err_code.equals("0") == false) { //-----ошибка отгрузки
                            return my_err_message;
                        } else {                                //-----все OK
                            return "";
                        }
                    } else {
                        return "Нет ответа от сервера";
                    }
                }
            }catch (Exception e) {
                return e.getMessage();
            }
        }
    }

    public void close_Order(){
        ////////////////////////////////////////////////////////////////////////////////////////////
        //
        // Закрытие заказа и закрытие данной активности
        //
        ////////////////////////////////////////////////////////////////////////////////////////////

        final RelativeLayout my_rl = (RelativeLayout) findViewById(R.id.progress_order_sh);
        my_rl.setVisibility(View.VISIBLE);

        new Thread(new Runnable() {
            public void run() {
                try {
                    synchronized (this) {
                        runOnUiThread(new Runnable() {
                            public void run() {
                                JSONArray jsarr = null;
                                JSONObject jsel = null;

                                if (my_login.equals("") || my_pass.equals("")) {           //-----не заполнен логин или пароль
                                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(SalesOrder.this);
                                    alertDialogBuilder.setTitle("Внимание!");
                                    alertDialogBuilder
                                            .setMessage("Не заполнен логин или пароль")
                                            .setCancelable(false)
                                            .setPositiveButton("OK",
                                                    new DialogInterface.OnClickListener() {
                                                        public void onClick(DialogInterface dialog, int id) {
                                                            dialog.cancel();
                                                        }
                                                    });
                                    AlertDialog alertDialog = alertDialogBuilder.create();
                                    alertDialog.show();
                                } else {
                                    try {
                                        String my_str = new String("");
                                        HttpWorkAsync my_httpAs = new HttpWorkAsync();
                                        my_str = my_httpAs.execute(service_url + "GetOrderShCloseRezJson/?order="
                                                        + OrderId,
                                                my_login, my_pass, my_ssl_check).get().toString();
                                        if (my_httpAs.get_my_error() != "") {
                                            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(SalesOrder.this);
                                            alertDialogBuilder.setTitle("Ошибка!");
                                            alertDialogBuilder
                                                    .setMessage(my_httpAs.my_error)
                                                    .setCancelable(false)
                                                    .setPositiveButton("OK",
                                                            new DialogInterface.OnClickListener() {
                                                                public void onClick(DialogInterface dialog, int id) {
                                                                    dialog.cancel();
                                                                }
                                                            });
                                            AlertDialog alertDialog = alertDialogBuilder.create();
                                            alertDialog.show();
                                        } else {
                                            jsarr = new JSONArray(my_str);
                                            if (jsarr.length() > 0) {
                                                //-----заказ успешно закрыт
                                                Intent intent = new Intent();
                                                intent.putExtra("OrderNum", OrderNum);
                                                setResult(RESULT_OK, intent);
                                                Toast toast = Toast.makeText(SalesOrder.this, "Заказ закрыт", Toast.LENGTH_LONG);
                                                toast.show();
                                                my_rl.post(new Runnable(){
                                                    @Override
                                                    public void run() {
                                                        finish();
                                                    }
                                                });
                                            }else {
                                                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(SalesOrder.this);
                                                alertDialogBuilder.setTitle("Ошибка!");
                                                alertDialogBuilder
                                                        .setMessage("Сервер не вернул данные")
                                                        .setCancelable(false)
                                                        .setPositiveButton("OK",
                                                                new DialogInterface.OnClickListener() {
                                                                    public void onClick(DialogInterface dialog, int id) {
                                                                        dialog.cancel();
                                                                    }
                                                                });
                                                AlertDialog alertDialog = alertDialogBuilder.create();
                                                alertDialog.show();
                                            }
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(SalesOrder.this);
                                        alertDialogBuilder.setTitle("Ошибка!");
                                        alertDialogBuilder
                                                .setMessage(e.getLocalizedMessage())
                                                .setCancelable(false)
                                                .setPositiveButton("OK",
                                                        new DialogInterface.OnClickListener() {
                                                            public void onClick(DialogInterface dialog, int id) {
                                                                dialog.cancel();
                                                            }
                                                        });
                                        AlertDialog alertDialog = alertDialogBuilder.create();
                                        alertDialog.show();
                                    }
                                }
                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
