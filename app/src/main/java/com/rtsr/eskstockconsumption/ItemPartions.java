package com.rtsr.eskstockconsumption;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemPartions extends AppCompatActivity {
    public static String OrderNum = null;
    public static String WHNum = null;
    public static String StrNum = null;
    public static String ItemCode = null;
    public static String OrderQTY = null;
    public static String RequestedCabelParts = null;
    public static String ToAssembleQTY = null;
    public static String new_ToAssembleQTY = null;
    public static String assembled_QTY = null;

    public static Boolean my_view;
    public static String service_url;
    public static String my_login;
    public static String my_pass;
    public static Boolean my_ssl_check;

    public static ListView listView1 = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        TextView mt = null;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_partions);

        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayShowHomeEnabled(true);
        actionbar.setIcon(R.mipmap.esk);
        actionbar.setTitle("Электроскандия");
        actionbar.setSubtitle("подбор партий");

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
            OrderNum = order_param.getString("OrderNum");
            WHNum = order_param.getString("WHNum");
            StrNum = order_param.getString("StrNum");
            ItemCode = order_param.getString("ItemCode");
            OrderQTY = order_param.getString("AvlQTY");
            RequestedCabelParts = order_param.getString("RequestedCabelParts");
            ToAssembleQTY = order_param.getString("ToAssembleQTY");
            new_ToAssembleQTY = ToAssembleQTY;
            mt = (TextView) findViewById(R.id.activity_item_partions_order_num);
            mt.setText(OrderNum);
            mt = (TextView) findViewById(R.id.activity_item_partions_whnum);
            mt.setText(WHNum);
            mt = (TextView) findViewById(R.id.activity_item_partions_strnum);
            mt.setText(StrNum);
            mt = (TextView) findViewById(R.id.activity_item_partions_item_code);
            mt.setText(ItemCode);
            mt = (TextView) findViewById(R.id.activity_item_partions_requestedqty);
            mt.setText(OrderQTY);
            mt = (TextView) findViewById(R.id.activity_item_partions_assembledqty);
            mt.setText("0");
            mt = (TextView) findViewById(R.id.activity_item_partions_requesteparts);
            mt.setText(RequestedCabelParts);

        }

        Button btn_refresh = (Button)findViewById(R.id.activity_item_partions_btn_refresh); //обновить
        btn_refresh.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ItemPartions.this);
                alertDialogBuilder.setTitle("Обновить?");
                alertDialogBuilder
                        .setMessage("Внимание! При обновлении станут невидимы все ранее внесенные количества по отгрузке!")
                        .setCancelable(false)
                        .setPositiveButton("Да",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        OrderQTY = String.valueOf(Double.parseDouble(OrderQTY)
                                                - (Double.parseDouble(ToAssembleQTY)
                                                - Double.parseDouble(new_ToAssembleQTY)));
                                        ToAssembleQTY = new_ToAssembleQTY;
                                        TextView mt1 = null;
                                        mt1 = (TextView) findViewById(R.id.activity_item_partions_requestedqty);
                                        mt1.setText(OrderQTY);
                                        mt1 = (TextView) findViewById(R.id.activity_item_partions_assembledqty);
                                        mt1.setText("0");
                                        GetPartionsInfo();
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

        GetPartionsInfo();
    }

    @Override
    public void onBackPressed() {
        Toast toast = Toast.makeText(ItemPartions.this, "Выход из окна только по крестику вверху.", Toast.LENGTH_LONG);
        toast.show();
    }

    public void GetPartionsInfo(){
        ////////////////////////////////////////////////////////////////////////////////////////////
        //
        // получение информации о партиях запаса на складе
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
                //---------------------список партий------------------------------------------------
                String my_str = new String("");
                HttpWorkAsync my_httpAs = new HttpWorkAsync();
                my_str = my_httpAs.execute(service_url + "GetPartionsForAssemblingJson/?item="
                                + ItemCode + "&wh=" + WHNum + "&order=" + OrderNum,
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
                                listView1 = (ListView) findViewById(R.id.activity_item_partions_part_list);
                                String[] from = {"PartionID", "AvlQTY", "ToAssemblyQTY"};
                                int[] to = {R.id.partions_list_partionnum,
                                        R.id.partions_list_avlqty,
                                        R.id.partions_list_toassemblyqty};
                                listView1.setAdapter(new PartionsListAdapter(this, listItems,
                                        R.layout.partions_list, from, to));
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
                    datum.put("PartionID", jsel.getString("PartionID"));
                    datum.put("AvlQTY", jsel.getString("AvlQTY"));
                    datum.put("ToAssemblyQTY", "0.0");
                    aList.add(datum);
                }
            }
        } catch (JSONException je){
            je.printStackTrace();
        }
        return  aList;
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
                intent.putExtra("new_ToAssembleQTY", new_ToAssembleQTY);
                intent.putExtra("StrNum", StrNum);
                setResult(RESULT_OK, intent);
                finish();
                return true;
            default:
                return false;
        }
    }

    public String record_Shipments(String OrderNum, String WHNum, String ItemCode, String StrNum,
                                 String PartionNum, String MyQTY){
        ////////////////////////////////////////////////////////////////////////////////////////////
        //
        // Запись  отгрузки из партии
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
                my_str = my_httpAs.execute(service_url + "GetShipmentFromPartionRezJson/?OrderNum="
                                + OrderNum + "&WHNum=" + WHNum + "&ItemCode=" + ItemCode
                        + "&StrNum=" + StrNum + "&PartitionNum=" + PartionNum + "&QTY=" + MyQTY,
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

    public class PartionsListAdapter extends SimpleAdapter{
        private Context mContext;
        public ArrayList<Map<String,String>> MyArray;
        private LayoutInflater mInflater;
        private Integer SelElement;

        public PartionsListAdapter(Context context, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to) {
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
            // подсветка + обработки
            //
            ////////////////////////////////////////////////////////////////////////////////////////////
            final View view = super.getView(position,convertView,parent);

            view.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(final View view) {
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
                            final TextView tv1 = (TextView)view.findViewById(R.id.partions_list_toassemblyqty);
                            tv1.post(new Runnable(){
                                @Override
                                public void run() {
                                    //---ввод количества, отгруженного из партии
                                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(ItemPartions.this);
                                    alertDialog.setTitle("Отгружено");
                                    alertDialog.setMessage("Введите количество отгруженного из этой партии");
                                    final EditText txt_input = new EditText(ItemPartions.this);
                                    txt_input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                                            LinearLayout.LayoutParams.MATCH_PARENT,
                                            LinearLayout.LayoutParams.MATCH_PARENT);
                                    txt_input.setLayoutParams(lp);
                                    alertDialog.setView(txt_input);
                                    //txt_input.setText(((TextView)findViewById(R.id.partions_list_toassemblyqty)).getText().toString());
                                    txt_input.setText(tv1.getText());
                                    alertDialog.setPositiveButton("Ввод",
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int which) {
                                                    String EnteredQTY = "";
                                                    EnteredQTY = txt_input.getText().toString();
                                                    if (EnteredQTY.equals("")) {
                                                        //-----кол-во не введено
                                                        Toast toast = Toast.makeText(ItemPartions.this, "Необходимо ввести отгружаемое количество.", Toast.LENGTH_LONG);
                                                        toast.show();
                                                    } else {
                                                        if (Double.parseDouble(txt_input.getText().toString())
                                                                > Double.parseDouble(MyArray.get(position).get("AvlQTY"))) {
                                                            //-----кол-во больше, чем кол-во в партии
                                                            Toast toast = Toast.makeText(ItemPartions.this, "Количество не может быть больше количества в партии", Toast.LENGTH_LONG);
                                                            toast.show();
                                                        } else {
                                                            //-----кол-во меньше, чем кол-во в партии
                                                            MyArray.get(position).put("ToAssemblyQTY", txt_input.getText().toString());
                                                            //-----расчет тотала по запасу
                                                            final Double entered_qty = Double.parseDouble(txt_input.getText().toString());
                                                            //final Double new_qty = 0.0;
                                                            Double new_qty1 = 0.0;
                                                            final Double old_qty = Double.parseDouble(((TextView)findViewById(R.id.activity_item_partions_assembledqty)).getText().toString());
                                                            final Double requested_qty = Double.parseDouble(((TextView)findViewById(R.id.activity_item_partions_requestedqty)).getText().toString());
                                                            for (Integer i = 0; i < MyArray.size(); i++) {
                                                                if (MyArray.get(i).get("ToAssemblyQTY").equals("")) {

                                                                } else {
                                                                    new_qty1 = new_qty1 + Double.parseDouble(MyArray.get(i).get("ToAssemblyQTY"));
                                                                }
                                                            }
                                                            final Double new_qty = new_qty1;
                                                            if (new_qty <= requested_qty){
                                                                //-----введено нормальное кол-во, не больше чем запрошено
                                                                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                                                imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                                                                //imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                                                                final RelativeLayout my_rl = (RelativeLayout) findViewById(R.id.progress_partions_sh);
                                                                my_rl.setVisibility(View.VISIBLE);
                                                                new Thread(new Runnable() {
                                                                    public void run() {
                                                                        try {
                                                                            synchronized (this) {
                                                                                runOnUiThread(new Runnable() {
                                                                                    public void run() {
                                                                                        String my_return = record_Shipments(OrderNum, WHNum, ItemCode, StrNum,
                                                                                                ((TextView) findViewById(R.id.partions_list_partionnum)).getText().toString(),
                                                                                                String.valueOf(entered_qty));
                                                                                        if (my_return.equals("")) { //-----все OK
                                                                                            ((TextView) findViewById(R.id.activity_item_partions_assembledqty)).setText(new_qty.toString());
                                                                                            ((TextView) findViewById(R.id.partions_list_toassemblyqty)).setText(txt_input.getText().toString());
                                                                                            MyArray.get(position).put("ToAssemblyQTY", String.valueOf(entered_qty));
                                                                                            new_ToAssembleQTY = String.valueOf(Double.parseDouble(ToAssembleQTY) - new_qty);
                                                                                            listView1.invalidateViews();
                                                                                        } else {                    //-----ошибка записи
                                                                                            Toast toast = Toast.makeText(ItemPartions.this, "Произошла ошибка занесения информации: " + my_return, Toast.LENGTH_LONG);
                                                                                            toast.show();
                                                                                        }
                                                                                        my_rl.setVisibility(View.INVISIBLE);
                                                                                    }
                                                                                });
                                                                            }
                                                                        } catch (Exception e) {
                                                                            e.printStackTrace();
                                                                        }
                                                                    }
                                                                }).start();
                                                            } else {
                                                                //-----введенное кол-во в итоге больше, чем запрошено
                                                                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                                                imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                                                                final RelativeLayout my_rl = (RelativeLayout) findViewById(R.id.progress_partions_sh);
                                                                my_rl.setVisibility(View.VISIBLE);
                                                                new Thread(new Runnable() {
                                                                    public void run() {
                                                                        try {
                                                                            synchronized (this) {
                                                                                runOnUiThread(new Runnable() {
                                                                                    public void run() {
                                                                                        String my_return = record_Shipments(OrderNum, WHNum, ItemCode, StrNum,
                                                                                                ((TextView)findViewById(R.id.partions_list_partionnum)).getText().toString(),
                                                                                                String.valueOf(Double.parseDouble(ToAssembleQTY) - entered_qty + requested_qty - new_qty));
                                                                                        if (my_return.equals("")) { //-----все OK
                                                                                            ((TextView)findViewById(R.id.activity_item_partions_assembledqty)).setText(requested_qty.toString());
                                                                                            ((TextView)findViewById(R.id.partions_list_toassemblyqty)).setText(String.valueOf(entered_qty + requested_qty - new_qty));
                                                                                            //((TextView)findViewById(R.id.partions_list_toassemblyqty)).setText("0");
                                                                                            Toast toast = Toast.makeText(ItemPartions.this, "Нельзя отгружать больше, чем указано в заказе", Toast.LENGTH_LONG);
                                                                                            toast.show();
                                                                                            MyArray.get(position).put("ToAssemblyQTY", String.valueOf(entered_qty + requested_qty - new_qty));
                                                                                            //MyArray.get(position).put("ToAssemblyQTY", "0");
                                                                                            new_ToAssembleQTY = "0";
                                                                                            listView1.invalidateViews();
                                                                                        } else {                    //-----ошибка записи
                                                                                            Toast toast = Toast.makeText(ItemPartions.this, "Произошла ошибка занесения информации: " + my_return, Toast.LENGTH_LONG);
                                                                                            toast.show();
                                                                                        }
                                                                                        my_rl.setVisibility(View.INVISIBLE);
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
                    });
                    return true;
                }
            });

            if (SelElement == position) {
                view.setBackgroundColor(Color.rgb(177, 228, 226));
            } else {
                view.setBackgroundColor(Color.WHITE);
            }

            return view;
        }

    }

    /*
    public class PartionsListAdapter extends SimpleAdapter{
        private Context mContext;
        public ArrayList<Map<String,String>> MyArray;
        private LayoutInflater mInflater;
        private Integer SelElement;

        public PartionsListAdapter(Context context, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to) {
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
            // подсветка + обработки
            //
            ////////////////////////////////////////////////////////////////////////////////////////////
            EditText et;

            final View view = super.getView(position,convertView,parent);
            et = view.findViewById(R.id.partions_list_toassemblyqty);

            view.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(final View view) {
                    listView1.post(new Runnable() {

                        @Override
                        public void run() {

                            listView1.setSelection(position);
                            listView1.setItemChecked(position,true);
                            listView1.smoothScrollToPositionFromTop(position, 0, 0);
                            SelElement = position;
                            final EditText et1 = (EditText)view.findViewById(R.id.partions_list_toassemblyqty);
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
                    });
                }
            });

            if (SelElement == position) {
                view.setBackgroundColor(Color.rgb(177, 228, 226));
            } else {
                view.setBackgroundColor(Color.WHITE);
            }

            et.setOnFocusChangeListener(new View.OnFocusChangeListener() {

                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (!hasFocus) {
                        EditText Caption = (EditText) v;
                        if (Caption.getText().toString().equals("")) {

                        } else {
                            if (Double.parseDouble(Caption.getText().toString()) < 0) {
                                Caption.setText("0");
                                Toast toast = Toast.makeText(ItemPartions.this, "Количество не может быть меньше 0", Toast.LENGTH_LONG);
                                toast.show();
                                MyArray.get(position).put("ToAssemblyQTY", "0");
                                listView1.invalidateViews();
                            } else {
                                if (Double.parseDouble(Caption.getText().toString())
                                        > Double.parseDouble(MyArray.get(position).get("AvlQTY"))) {
                                    Caption.setText(MyArray.get(position).get("AvlQTY").toString());
                                    Toast toast = Toast.makeText(ItemPartions.this, "Количество не может быть больше количества в партии", Toast.LENGTH_LONG);
                                    toast.show();
                                    MyArray.get(position).put("ToAssemblyQTY", MyArray.get(position).get("AvlQTY").toString());
                                    listView1.invalidateViews();
                                } else {
                                    MyArray.get(position).put("ToAssemblyQTY", Caption.getText().toString());
                                }
                            }

                            //-----расчет тотала по запасу
                            Double entered_qty = Double.parseDouble(Caption.getText().toString());
                            Double new_qty = 0.0;
                            Double old_qty = Double.parseDouble(((TextView)findViewById(R.id.activity_item_partions_assembledqty)).getText().toString());
                            Double requested_qty = Double.parseDouble(((TextView)findViewById(R.id.activity_item_partions_requestedqty)).getText().toString());
                            for (Integer i = 0; i < MyArray.size(); i++) {
                                if (MyArray.get(i).get("ToAssemblyQTY").equals("")) {

                                } else {
                                    new_qty = new_qty + Double.parseDouble(MyArray.get(i).get("ToAssemblyQTY"));
                                }
                            }
                            if (new_qty <= requested_qty){
                                ((TextView)findViewById(R.id.activity_item_partions_assembledqty)).setText(new_qty.toString());
                                MyArray.get(position).put("ToAssemblyQTY", String.valueOf(entered_qty));
                                new_ToAssembleQTY = String.valueOf(Double.parseDouble(ToAssembleQTY) - new_qty);

                             } else {
                                ((TextView)findViewById(R.id.activity_item_partions_assembledqty)).setText(requested_qty.toString());
                                Caption.setText(String.valueOf(entered_qty + requested_qty - new_qty));
                                Toast toast = Toast.makeText(ItemPartions.this, "Нельзя отгружать больше, чем указано в заказе", Toast.LENGTH_LONG);
                                toast.show();
                                MyArray.get(position).put("ToAssemblyQTY", String.valueOf(entered_qty + requested_qty - new_qty));
                                new_ToAssembleQTY = String.valueOf(Double.parseDouble(ToAssembleQTY) - entered_qty + requested_qty - new_qty);
                                listView1.invalidateViews();
                            }
                        }
                    }
                }
            });

            return view;
        }
    }
    */
}
