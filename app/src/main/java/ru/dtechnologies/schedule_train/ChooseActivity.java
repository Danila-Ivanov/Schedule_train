package ru.dtechnologies.schedule_train;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.ArrayMap;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ru.dtechnologies.schedule_train.parser.JSONParser;


/**
 * Created by Admin on 20.10.2016.
 */

public class ChooseActivity extends AppCompatActivity implements AdapterView.OnItemClickListener,
        View.OnClickListener {

    // различные теги для получения/сохранения значений
    private static final String LOG_TAG = "myLogs";

    // передача данных через intent
    private static final String TAG_DATA = "extra_data";

    // таблицы станций прибытия/отправления
    private static final String TAG_TABLE_FROM = "citiesFrom";
    private static final String TAG_TABLE_TO = "citiesTo";
    // данные станции
    private static final String TAG_COUNTRY = "countryTitle";
    private static final String TAG_REGION = "regionTitle";
    private static final String TAG_DISTRICT = "districtTitle";
    private static final String TAG_SITY = "cityTitle";
    private static final String TAG_LOCATION_1 = "location_1";
    private static final String TAG_LOCATION_2 = "location_2";

    private static final String TAG_TABLE_STATIONS = "stations";
    private static final String TAG_NAME_STATION = "stationTitle";

    private SearchView mSearchView;
    private MenuItem searchMenuItem;

    // url получения списка всех станций отправления/прибытия
    private static String url_get_schedule = "https://raw.githubusercontent.com/tutu-ru/hire_android_test/master/allStations.json";
    // Создаем JSON парсер
    JSONParser jParser = new JSONParser();

    //
    ArrayList<HashMap<String, String>> listStation = new ArrayList<HashMap<String, String>>();
    SimpleAdapter adapter;
    String scheduleTable;

    // элементы экрана
    ListView lvSchedule;
    TextView tvCountry, tvSity, tvName;
    Button btnCancle, btnChoose;
    RelativeLayout layoutInfo;
    ProgressDialog pDialog;
    ImageView secondFon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose);

        // находим элементы экрана
        lvSchedule = (ListView) findViewById(R.id.lvSchedule);
        layoutInfo = (RelativeLayout) findViewById(R.id.layout_details_item);
        tvName = (TextView) findViewById(R.id.tvNameStation);
        tvCountry = (TextView) findViewById(R.id.tvCountry_toast);
        tvSity = (TextView) findViewById(R.id.tvSity_toast);
        btnChoose = (Button) findViewById(R.id.btnChoose_toast);
        btnCancle = (Button) findViewById(R.id.btnCancle_toast);
        secondFon = (ImageView) findViewById(R.id.secondFon);
        //
        btnChoose.setOnClickListener(this);
        btnCancle.setOnClickListener(this);
        secondFon.setOnClickListener(this);
        lvSchedule.setOnItemClickListener(this);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        // получаем данные из intent
        scheduleTable = getIntent().getStringExtra(TAG_DATA);
        // запускаем поток чтения данных
        new LoadSchedule().execute(scheduleTable);
    }


    @Override
    public void onClick(View v) {
        Intent intent = null;
        switch (v.getId()){
            // кнопка выбора данной станции => переход на главное активити
            case R.id.btnChoose_toast:
                intent = new Intent(ChooseActivity.this, MainActivity.class);
                //
                intent.putExtra(TAG_NAME_STATION, tvName.getText());
                intent.putExtra(TAG_LOCATION_1, tvCountry.getText());
                intent.putExtra(TAG_LOCATION_2, tvSity.getText());
                intent.putExtra(TAG_DATA, scheduleTable);
                //
                startActivity(intent);
                finish();
                break;

            // закрытие окна доп. информации
            case R.id.btnCancle_toast:
                layoutInfo.setVisibility(View.INVISIBLE);
                secondFon.setVisibility(View.INVISIBLE);
                break;

            case R.id.secondFon:
                layoutInfo.setVisibility(View.INVISIBLE);
                secondFon.setVisibility(View.INVISIBLE);
                break;
        }
    }

    // поток чтения json
    class LoadSchedule extends AsyncTask<String, String, String> {

        /**
         * Перед началом фонового потока Show Progress Dialog
         * */
        String arg = "null";
        boolean flag_toast = false;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(ChooseActivity.this);
            pDialog.setMessage(getResources().getString(R.string.update_data));
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        /**
         * Получаем станции из url
         * */
        protected String doInBackground(String... args) {

            // получаем JSON строк с URL
            JSONObject json = jParser.getJSON();
            if (json == null) {
                flag_toast = false;
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                json = jParser.makeHttpRequest(url_get_schedule, params);
            }

                try {
                    JSONArray base_table = null;
                    if (args[0] == "from"){
                        // Получаем масив станций отправления
                        base_table = json.getJSONArray(TAG_TABLE_FROM);
                    }else{
                        // Получаем масив станций прибытия
                        base_table = json.getJSONArray(TAG_TABLE_TO);
                    }

                    // перебор всех элементов
                    for (int i = 0; i < base_table.length(); i++) {
                        JSONObject c = base_table.getJSONObject(i);
                        JSONArray table_stations = c.getJSONArray(TAG_TABLE_STATIONS);

                        for (int j = 0; j < table_stations.length(); j++) {
                            JSONObject p = table_stations.getJSONObject(j);

                            HashMap<String, String> map = new HashMap<String, String>();
                                map.put(TAG_NAME_STATION, p.getString(TAG_NAME_STATION));
                                map.put(TAG_COUNTRY, p.getString(TAG_COUNTRY));
                                map.put(TAG_REGION, p.getString(TAG_REGION));
                                map.put(TAG_SITY, p.getString(TAG_SITY));
                                map.put(TAG_DISTRICT, p.getString(TAG_DISTRICT));

                            listStation.add(map);
                        }
                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                }
            return null;
        }

        /**
         * После завершения фоновой задачи закрываем прогрес диалог
         * **/
        protected void onPostExecute(String arg) {
            // закрываем прогресс диалог
            pDialog.dismiss();

            if (flag_toast) {
                Toast.makeText(ChooseActivity.this, getResources().getString(R.string.update_data_good), Toast.LENGTH_SHORT).show();
            }

            // обновление адаптера
            adapter = new SimpleAdapter(ChooseActivity.this, listStation, R.layout.item,
                    new String[] {TAG_NAME_STATION, TAG_COUNTRY, TAG_REGION, TAG_DISTRICT, TAG_SITY},
                    new int[] {R.id.tvStation, R.id.tvCountry, R.id.tvRegion, R.id.tvDistrict, R.id.tvSity});
            lvSchedule.setAdapter(adapter);
        }
    }

    // нажатие на пункт списка
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        String data = String.valueOf((parent.getAdapter().getItem(position)));

        layoutInfo.setVisibility(View.VISIBLE);
        tvName.setText(search(TAG_NAME_STATION, data));
        // location_1
        if (search(TAG_REGION, data).equals("")){
            tvCountry.setText(search(TAG_COUNTRY, data));
        }else{
            tvCountry.setText(search(TAG_COUNTRY, data)+", "+search(TAG_REGION, data));
        }

        // location_2
        if (search(TAG_DISTRICT, data).equals("")){
            tvSity.setText(search(TAG_SITY, data));
        }else{
            tvSity.setText(search(TAG_SITY, data)+", "+search(TAG_DISTRICT, data));
        }

        secondFon.setVisibility(View.VISIBLE);
    }

    // поиск значения по тегу в строке элементов (используется в onItemClick)
    public String search(String key, String string){
        String value = null;
        if (string.contains(key)){
             value = string.substring(string.indexOf(key)+1+key.length(), string.length());
            if (value.indexOf(',') != -1){
                value = value.substring(0, value.indexOf(','));
            }else{
                value = value.substring(0, value.indexOf('}'));
            }

        }
        return value;
    }

    // методы меню  ------------------------------------------------------------------------------>>
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_bar_choose, menu);

        SearchView.OnQueryTextListener listener = new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // поиск по слову в списке
                ChooseActivity.this.adapter.getFilter().filter(newText);
                return false;
            }
        };

        searchMenuItem = menu.findItem(R.id.search);
        mSearchView = (SearchView) searchMenuItem.getActionView();
        mSearchView.setOnQueryTextListener(listener);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            // нажатие кнопки home
            case android.R.id.home:
                Intent intent = new Intent(ChooseActivity.this, MainActivity.class);
                intent.putExtra(TAG_DATA, "null");
                startActivity(intent);
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    //--------------------------------------------------------------------------------------------<<

    // нажатие на back
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(ChooseActivity.this, MainActivity.class);
        intent.putExtra(TAG_DATA, "null");
        startActivity(intent);
        finish();
    }
}
