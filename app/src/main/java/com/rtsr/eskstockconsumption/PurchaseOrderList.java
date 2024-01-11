package com.rtsr.eskstockconsumption;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class PurchaseOrderList extends AppCompatActivity {
    public static String service_url;
    public static String my_login;
    public static String my_pass;
    public static Boolean my_ssl_check;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_purchase_order_list);

        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayShowHomeEnabled(true);
        actionbar.setIcon(R.mipmap.esk);
        actionbar.setTitle("Электроскандия");
        actionbar.setSubtitle("Приемка счетов фактур");
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
