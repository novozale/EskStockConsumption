package com.rtsr.eskstockconsumption;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SalesOrderList extends AppCompatActivity {
    public static String service_url;
    public static String my_login;
    public static String my_pass;
    public static Boolean my_ssl_check;
    public static ListView listView1 = null;
    public static Boolean my_view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sales_order_list);

        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayShowHomeEnabled(true);
        actionbar.setIcon(R.mipmap.esk);
        actionbar.setTitle("Электроскандия");
        actionbar.setSubtitle("заказы на отгрузку");

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

        Button ButtonInfo = (Button) findViewById(R.id.sales_button_refresh);         //обновление информации
        ButtonInfo.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                GetOrderList();
            }
        });

        GetOrderList();
    }

    @Override
    public void onBackPressed() {
        Toast toast = Toast.makeText(SalesOrderList.this, "Выход из окна только по крестику вверху.", Toast.LENGTH_LONG);
        toast.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        GetOrderList();
        if (data != null) {
            Bundle order_param = data.getExtras();
            String OrderNum = order_param.getString("OrderNum");
            Map<String,String> MyItem;
            SimpleAdapter sa = (SimpleAdapter)listView1.getAdapter();
            for (Integer i = 0; i < sa.getCount(); i++) {
                MyItem = (Map<String,String>)sa.getItem(i);
                if(MyItem.get("OrderNum").equals(OrderNum)){
                    listView1.smoothScrollToPositionFromTop(i, 0, 100);
                    listView1.setSelection(i);
                    listView1.requestFocusFromTouch();
                    return;
                }
            }
        }
    }

    public void GetOrderList(){
        ////////////////////////////////////////////////////////////////////////////////////////////
        //
        // вывод списка заказов на подборку
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
                my_str = my_httpAs.execute(service_url + "GetOrderListForCollectionJson/?user=" + my_login,
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
                            try {
                                ArrayList<Map<String, String>> listItems = getArrayListFromJSONArray(jsarr);
                                listView1 = (ListView) findViewById(R.id.sales_order_list);
                                String[] from = {"RecordID", "OrderNum", "WHNum", "RequestedDate", "Customer", "Delivery"};
                                int[] to = {R.id.sales_order_item_id, R.id.sales_order_item_order_num,
                                        R.id.sales_order_item_whnum, R.id.sales_order_item_date,
                                        R.id.sales_order_item_customer, R.id.sales_order_item_delivery};
                                listView1.setAdapter(new SimpleAdapter(this, listItems, R.layout.sales_order_item, from, to));

                                listView1.setClickable(true);
                                listView1.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                                    @Override
                                    public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                                        listView1.setSelection(position);
                                        Object o = listView1.getItemAtPosition(position);
                                        Intent my_intent = new Intent(SalesOrderList.this, SalesOrder.class);
                                        HashMap hm = (HashMap) o;
                                        my_intent.putExtra("RecordID", Integer.parseInt(hm.get("RecordID").toString()));
                                        my_intent.putExtra("OrderNum", hm.get("OrderNum").toString());
                                        my_intent.putExtra("WHNum", hm.get("WHNum").toString());
                                        startActivity(my_intent);
                                        //startActivityForResult(my_intent, 0);
                                    }
                                });
                            } catch (Exception e) {
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
                    datum.put("RecordID", jsel.getString("RecordID"));
                    datum.put("OrderNum", jsel.getString("OrderNum"));
                    datum.put("RequestedDate", jsel.getString("RequestedDate"));
                    datum.put("Customer", jsel.getString("Customer"));
                    datum.put("Delivery", jsel.getString("Delivery"));
                    datum.put("WHNum", jsel.getString("WHNum"));
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
                finish();
                return true;
            default:
                return false;
        }
    }
}
