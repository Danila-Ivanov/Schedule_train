package ru.dtechnologies.schedule_train;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import ru.dtechnologies.schedule_train.parser.JSONParser;

/**
 * Created by Admin on 24.10.2016.
 */

public class SplashScreen extends AppCompatActivity {

    // url получения списка всех станций отправления/прибытия
    private static String url_get_schedule = "https://raw.githubusercontent.com/tutu-ru/hire_android_test/master/allStations.json";
    // Создаем JSON парсер
    JSONParser jParser = new JSONParser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splashscreen);

        if (isNetworkAvailable()){
            new LoadSchedule().execute();
        }else {
            Toast.makeText(this, "Нет подключения к интернету!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(SplashScreen.this, MainActivity.class);
            startActivity(intent);
        }

    }

    // поток чтения json
    class LoadSchedule extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        protected String doInBackground(String... args) {
            // получаем JSON строк с URL
            List<NameValuePair> params = new ArrayList<NameValuePair>();
           jParser.makeHttpRequest(url_get_schedule, params);
            return null;
        }


        protected void onPostExecute(String arg) {
            Intent intent = new Intent(SplashScreen.this, MainActivity.class);
            startActivity(intent);
        }
    }

    // метод проверки доступности интернета
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null;
    }


    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onStop() {
        super.onStop();
        finish();
    }
}
