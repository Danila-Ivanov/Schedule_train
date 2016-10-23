package ru.dtechnologies.schedule_train;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, AdapterView.OnItemClickListener,
        View.OnClickListener{

    private static final String LOG_TAG = "myLogs";
    private static final String TAG_DATA = "extra_data";

    private static final String TAG_LOCATION_1 = "location_1";
    private static final String TAG_LOCATION_2 = "location_2";
    private static final String TAG_NAME_STATION = "stationTitle";
    private static final String TAG_IMAGE = "image";
    final int station_d = R.drawable.icon_station_from;
    final int station_a = R.drawable.icon_station_in;

    final String SAVED_STATION_A = "saved_station_a";
    final String SAVED_STATION_D = "saved_station_d";
    final String SAVED_LOCATION_A1 = "saved_location_a1";
    final String SAVED_LOCATION_A2 = "saved_location_a2";
    final String SAVED_LOCATION_D1 = "saved_location_d1";
    final String SAVED_LOCATION_D2 = "saved_location_d2";
    final String SAVED_DATE = "saved_date";
    boolean networkState = true;

    DrawerLayout drawer;
    ListView lvStation;
    RelativeLayout layoutDate;

    SimpleAdapter adapter;
    TextView tvDate;

    SharedPreferences sPref;

    ArrayList<HashMap<String, Object>> listStation = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        lvStation = (ListView) findViewById(R.id.lvStations);
        init_list(listStation, 2);
        adapter = new SimpleAdapter(MainActivity.this, listStation, R.layout.item_schedule ,
                new String[]{ TAG_NAME_STATION, TAG_LOCATION_1, TAG_LOCATION_2, TAG_IMAGE},
                new int[]{R.id.tvStation, R.id.tvCountry_Region, R.id.tvSity_District, R.id.image_station});
        lvStation.setAdapter(adapter);
        lvStation.setOnItemClickListener(this);

        tvDate = (TextView) findViewById(R.id.tvDate);
        layoutDate = (RelativeLayout) findViewById(R.id.layout_calendar);
        layoutDate.setOnClickListener(this);
    }

    public void init_list(ArrayList<HashMap<String, Object>> arrayList, int length){
        HashMap<String, Object> map = null;
        for (int i = 0; i < length; i++) {
            map = new HashMap<>();
            map.put(TAG_NAME_STATION, "");
            map.put(TAG_LOCATION_1, "");
            map.put(TAG_LOCATION_2, "");
            if (i == 0){
                map.put(TAG_IMAGE, station_d);
            }else{
                map.put(TAG_IMAGE, station_a);
            }

            arrayList.add(i, map);
        }
    }

    @Override
    public void onStart() {
        super.onStart();


        if(getIntent().hasExtra(TAG_DATA)) {
            String str = getIntent().getStringExtra(TAG_DATA);
            loadSetting();

            if (tvDate.getText().length() == 0) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
                tvDate.setHint(sdf.format(new Date(System.currentTimeMillis())));
            }

            if (str.equals("from")) {
                HashMap<String, Object> map = new HashMap<>();
                map.put(TAG_NAME_STATION, getIntent().getStringExtra(TAG_NAME_STATION));
                map.put(TAG_LOCATION_1, getIntent().getStringExtra(TAG_LOCATION_1));
                map.put(TAG_LOCATION_2, getIntent().getStringExtra(TAG_LOCATION_2));
                map.put(TAG_IMAGE, station_d);

                listStation.set(0, map);
            }
            if (str.equals("to")) {
                HashMap<String, Object> map = new HashMap<>();
                map.put(TAG_NAME_STATION, getIntent().getStringExtra(TAG_NAME_STATION));
                map.put(TAG_LOCATION_1, getIntent().getStringExtra(TAG_LOCATION_1));
                map.put(TAG_LOCATION_2, getIntent().getStringExtra(TAG_LOCATION_2));
                map.put(TAG_IMAGE, station_a);

                listStation.set(1, map);
            }
        }else{
            drawer.openDrawer(Gravity.LEFT);
        }

        if (isNetworkAvailable()){
            networkState = true;
        }else{
            networkState = false;
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null;
    }

    @Override
    public void onBackPressed() {
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_schedule) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (id == R.id.nav_about) {
            // показ диалога с информацией о копирайте и версии
            String versionName = BuildConfig.VERSION_NAME;
            int versionCode = BuildConfig.VERSION_CODE;

            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle(R.string.title_dialog)
                    .setMessage("© Д. А. Иванов, 2016" + "\n\nВерсия " + versionName + " (сборка " + versionCode + ")")
                    .setCancelable(false)
                    .setNegativeButton(R.string.btn_OK,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });
            AlertDialog alert = builder.create();
            alert.show();
        }
        return true;
    }

    public void saveSetting(){
        sPref = getSharedPreferences("SCHEDULE_SETTING", MODE_PRIVATE);
        SharedPreferences.Editor edHelp = sPref.edit();
        edHelp.putString(SAVED_STATION_D, String.valueOf(listStation.get(0).get(TAG_NAME_STATION)));
        edHelp.putString(SAVED_STATION_A, String.valueOf(listStation.get(1).get(TAG_NAME_STATION)));
        //
        edHelp.putString(SAVED_LOCATION_D1, String.valueOf(listStation.get(0).get(TAG_LOCATION_1)));
        edHelp.putString(SAVED_LOCATION_D2, String.valueOf(listStation.get(0).get(TAG_LOCATION_2)));
        //
        edHelp.putString(SAVED_LOCATION_A1, String.valueOf(listStation.get(1).get(TAG_LOCATION_1)));
        edHelp.putString(SAVED_LOCATION_A2, String.valueOf(listStation.get(1).get(TAG_LOCATION_2)));

        edHelp.putString(SAVED_DATE, tvDate.getText().toString());
        edHelp.commit();
    }

    public void loadSetting(){
        sPref = getSharedPreferences("SCHEDULE_SETTING", MODE_PRIVATE);

        HashMap<String, Object> map = null;
        // загрузка данных отправления
        map = new HashMap<>();
            map.put(TAG_NAME_STATION, sPref.getString(SAVED_STATION_D, ""));
            map.put(TAG_LOCATION_1, sPref.getString(SAVED_LOCATION_D1, ""));
            map.put(TAG_LOCATION_2, sPref.getString(SAVED_LOCATION_D2, ""));
            map.put(TAG_IMAGE, station_d);

        listStation.set(0, map);

        // загрузка данных прибытия
        map = new HashMap<>();
            map.put(TAG_NAME_STATION, sPref.getString(SAVED_STATION_A, ""));
            map.put(TAG_LOCATION_1, sPref.getString(SAVED_LOCATION_A1, ""));
            map.put(TAG_LOCATION_2, sPref.getString(SAVED_LOCATION_A2, ""));
            map.put(TAG_IMAGE, station_a);

        listStation.set(1, map);

        tvDate.setText(sPref.getString(SAVED_DATE, ""));
    }

    @Override
    public void onStop() {
        super.onStop();

        saveSetting();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = null;
        if (networkState) {
            switch (position) {
                case 0:
                    intent = new Intent(MainActivity.this, ChooseActivity.class);
                    intent.putExtra(TAG_DATA, "from");
                    startActivity(intent);
                    break;

                case 1:
                    intent = new Intent(MainActivity.this, ChooseActivity.class);
                    intent.putExtra(TAG_DATA, "to");
                    startActivity(intent);
                    break;
            }
        }else{
            Toast.makeText(this, "Нет подключения к интернету!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.layout_calendar:
                callDatePicker();
                break;
        }
    }

    private void callDatePicker() {
        // получаем текущую дату
        final Calendar cal = Calendar.getInstance();
        int mYear = cal.get(Calendar.YEAR);
        int mMonth = cal.get(Calendar.MONTH);
        int mDay = cal.get(Calendar.DAY_OF_MONTH);

        // инициализируем диалог выбора даты текущими значениями
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        String editTextDateParam = dayOfMonth + "." + (monthOfYear + 1) + "." + year;
                        tvDate.setText(editTextDateParam);
                    }
                }, mYear, mMonth, mDay);
        datePickerDialog.show();
    }

}
