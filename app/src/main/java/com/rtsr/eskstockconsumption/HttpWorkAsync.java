package com.rtsr.eskstockconsumption;

import android.os.AsyncTask;

/**
 * Created by Novozhilov on 16/02/2018.
 */

public class HttpWorkAsync extends AsyncTask {
    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // класс асинхронного выполнения https - запроса
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////
    public static String my_str;
    public static String my_error;
    public static HttpWork my_http;

    public HttpWorkAsync () {
        ////////////////////////////////////////////////////////////////////////////////////////////
        //
        // конструктор
        //
        ////////////////////////////////////////////////////////////////////////////////////////////
        my_str = "";
        my_error = "";
        my_http = null;
    }

    public String get_my_error(){
        return my_error;
    }

    public void set_my_error(String mystr){
        my_error = mystr;
    }

    @Override
    protected Object doInBackground(Object[] objects) {
        ////////////////////////////////////////////////////////////////////////////////////////////
        //
        // асинронный https - запрос
        //
        ////////////////////////////////////////////////////////////////////////////////////////////

        my_error = "";
        try {
            my_http = new HttpWork((String)objects[0],
                    (String)objects[1], (String)objects[2], (Boolean) objects[3]);
            my_str = my_http.get_data();
            if (my_http.get_my_error() != ""){
                set_my_error(my_http.get_my_error());
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (my_http.get_my_error() == "") {
                if ((e.getMessage() == null) || (e.getMessage() == "")) {
                    set_my_error(e.getClass().getName());
                } else {
                    set_my_error(e.getMessage());
                }
            } else {
                set_my_error(my_http.get_my_error());
            }
        }
        return my_str;
    }
}