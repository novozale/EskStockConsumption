package com.rtsr.eskstockconsumption;

import android.util.Base64;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * Created by Novozhilov on 16/02/2018.
 */

public class HttpWork {
    ////////////////////////////////////////////////////////////////////////////////////////////
    //
    // класс https- запроса к rest api Elektroskandia services
    //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public static String my_httpsurl;
    public static String my_login;
    public static String my_password;
    public static Boolean my_checkssl;
    public static String my_error;

    public HttpWork(String base_url, String Login, String Password, Boolean CheckSSL){
        ////////////////////////////////////////////////////////////////////////////////////////////
        //
        // конструктор
        //
        ////////////////////////////////////////////////////////////////////////////////////////////

        my_httpsurl = base_url;
        my_login = Login;
        my_password = Password;
        my_checkssl = CheckSSL;
        my_error = "";
    }

    public String get_my_error(){
        return my_error;
    }

    public void set_my_error(String mystr){
        my_error = mystr;
    }

    public String get_data(){
        ////////////////////////////////////////////////////////////////////////////////////////////
        //
        // получение данных по https - запросу с кастомной авторизацией из rest api на базе WCF
        //
        ////////////////////////////////////////////////////////////////////////////////////////////
        StringBuilder sb = new StringBuilder();             //построитель строки
        URL my_url = null;                                  //URL, с которого получаются данные
        URLConnection urlConnection = null;                 //соединение
        InputStream is = null;                              //входной поток
        BufferedReader in = null;                           //ридер
        String authString;                                  //строка авторизации (логин:пароль)

        my_error = "";
        try {
            my_url = new URL(my_httpsurl);
            urlConnection = my_url.openConnection();
            authString = my_login + ":" + my_password;
            String authStringEnc = Base64.encodeToString(authString.getBytes(), 16);
            urlConnection.setRequestProperty("Authorization", "Basic " + authStringEnc);
            if (my_checkssl == false) {
                //-------------------кастомная проверка сертификата
                HttpsURLConnection conHttps = (HttpsURLConnection) urlConnection;
                TrustManager[] trustAllCerts = new TrustManager[] {
                        new X509TrustManager() {
                            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                                //return new X509Certificate[0];
                                return null;
                            }
                            public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
                            }
                            public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
                            }
                        }
                };
                SSLContext sc = SSLContext.getInstance("TLSv1.2");
                sc.init(null, trustAllCerts, new java.security.SecureRandom());
                conHttps.setSSLSocketFactory(sc.getSocketFactory());
                HostnameVerifier allHostsValid = new HostnameVerifier() {
                    public boolean verify(String hostname, SSLSession session) {
                        return true;
                    }
                };
                conHttps.setHostnameVerifier(allHostsValid);
                //-------------------конец кастомной проверки сертификата
            }

            is = urlConnection.getInputStream();
            in = new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line = in.readLine()) != null)
                sb.append(line);
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
            if ((e.getMessage() == null) || (e.getMessage() == "")) {
                set_my_error(e.getClass().getName());
            } else {
                set_my_error(e.getMessage() + " " + e.getClass().getName());
            }
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        };
        return sb.toString();
    }
}