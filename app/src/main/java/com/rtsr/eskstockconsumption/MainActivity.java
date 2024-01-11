package com.rtsr.eskstockconsumption;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    public static Boolean my_view;
    public static int my_camera;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ////////////////////////////////////////////////////////////////////////////////////////////
        //
        // старт
        //
        ////////////////////////////////////////////////////////////////////////////////////////////
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayShowHomeEnabled(true);
        actionbar.setIcon(R.mipmap.esk);
        actionbar.setTitle("Электроскандия");
        actionbar.setSubtitle("работа с запасами");

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        my_view = sharedPref.getBoolean("screen_orientation", false);
        if (my_view == false) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }

        Button Button1Type = (Button)findViewById(R.id.button);         //отгрузка товаров
        Button1Type.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                Intent my_intent = new Intent(MainActivity.this, SalesOrderList.class);
                startActivity(my_intent);
            }
        });

        Button Button1Type1 = (Button)findViewById(R.id.button2);         //приемка товаров
        Button1Type1.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                Intent my_intent = new Intent(MainActivity.this, PurchaseOrderList.class);
                startActivity(my_intent);
            }
        });

        my_camera = ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA);

        if (my_camera != PackageManager.PERMISSION_GRANTED) {
            List<String> listPermissionsNeeded = new ArrayList<>();
            listPermissionsNeeded.add(android.Manifest.permission.CAMERA);
            //ActivityCompat.requestPermissions(this,listPermissionsNeeded.toArray
            //        (new String[listPermissionsNeeded.size()]), 1);
            this.requestPermissions(listPermissionsNeeded.toArray
                    (new String[listPermissionsNeeded.size()]), 1);
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
        inflater.inflate(R.menu.menu_items, menu);
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
            case R.id.main_settings:    //основные настройки
                Intent my_intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(my_intent);
                return true;
            case R.id.main_exit:        //выход из приложенния
                exit_my_app();
                return true;
            default:
                return false;
        }
    }

    public void exit_my_app() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("Выйти из приложения?");
        alertDialogBuilder
                .setMessage("Нажмите \"Да\" для выхода!")
                .setCancelable(false)
                .setPositiveButton("Да",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                moveTaskToBack(true);
                                android.os.Process.killProcess(android.os.Process.myPid());
                                System.exit(1);
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
}
