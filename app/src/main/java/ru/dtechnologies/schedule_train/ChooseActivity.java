package ru.dtechnologies.schedule_train;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
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
    TextView tvCountry, tvRegion, tvDiscrict, tvSity, tvName;
    Button btnCancle, btnChoose;
    RelativeLayout layoutInfo;
    ProgressDialog pDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose);

        // находим элементы экрана
        lvSchedule = (ListView) findViewById(R.id.lvSchedule);
        layoutInfo = (RelativeLayout) findViewById(R.id.layout_details_item);
        tvName = (TextView) findViewById(R.id.tvNameStation);
        tvCountry = (TextView) findViewById(R.id.tvCountry_toast);
        tvRegion = (TextView) findViewById(R.id.tvRegion_toast);
        tvDiscrict = (TextView) findViewById(R.id.tvDistrict_toast);
        tvSity = (TextView) findViewById(R.id.tvSity_toast);
        btnChoose = (Button) findViewById(R.id.btnChoose_toast);
        btnCancle = (Button) findViewById(R.id.btnCancle_toast);
        //
        btnChoose.setOnClickListener(this);
        btnCancle.setOnClickListener(this);
        lvSchedule.setOnItemClickListener(this);

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
                intent.putExtra(TAG_NAME_STATION, tvName.getText());
                if (tvRegion.length() == 0){
                    intent.putExtra(TAG_LOCATION_1, tvCountry.getText());
                }else {
                    intent.putExtra(TAG_LOCATION_1, tvCountry.getText() + ", " + tvRegion.getText());
                }

                if (tvDiscrict.length() == 0){
                    intent.putExtra(TAG_LOCATION_2, tvSity.getText());
                }else{
                    intent.putExtra(TAG_LOCATION_2, tvSity.getText()+", "+tvDiscrict.getText());
                }

                intent.putExtra(TAG_DATA, scheduleTable);
                Log.d(LOG_TAG,scheduleTable);
                Log.d(LOG_TAG, tvCountry.getText()+", "+tvRegion.getText()+",\n"+
                        tvDiscrict.getText()+", "+tvSity.getText());
                startActivity(intent);
                break;

            // закрытие окна доп. информации
            case R.id.btnCancle_toast:
                layoutInfo.setVisibility(View.INVISIBLE);
                break;
        }
    }

    // поток чтения json
    class LoadSchedule extends AsyncTask<String, String, String> {

        /**
         * Перед началом фонового потока Show Progress Dialog
         * */
        String arg = "null";
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(ChooseActivity.this);
            pDialog.setMessage("Загрузка рассписания. Подождите...");
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

           /* HashMap<String, String> map = new HashMap<String, String>();

            //map.put(TAG_ID, p.getString(TAG_ID));
            map.put(TAG_NAME_STATION, "Werty");
            map.put(TAG_COUNTRY, "Россия");
            map.put(TAG_REGION, "Центральная россия");
            map.put(TAG_SITY, "Савёлки");
            map.put(TAG_DISTRICT, "Московская область");

            listStation.add(map);

            map = new HashMap<String, String>();

            //map.put(TAG_ID, p.getString(TAG_ID));
            map.put(TAG_NAME_STATION, "erty");
            map.put(TAG_COUNTRY, "Россия");
            map.put(TAG_REGION, "Центральная россия");
            map.put(TAG_SITY, "Кимовск");
            map.put(TAG_DISTRICT, "Тульская область");

            listStation.add(map);*/

            return null;
        }

        /**
         * После завершения фоновой задачи закрываем прогрес диалог
         * **/
        protected void onPostExecute(String arg) {
            // закрываем прогресс диалог
            pDialog.dismiss();

            Toast.makeText(ChooseActivity.this, "Рассписание обновлено!", Toast.LENGTH_SHORT).show();

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

        String str = String.valueOf((parent.getAdapter().getItem(position)));

        layoutInfo.setVisibility(View.VISIBLE);
        tvName.setText(search(TAG_NAME_STATION, str));
        tvCountry.setText(search(TAG_COUNTRY, str));
        tvRegion.setText(search(TAG_REGION, str));
        tvDiscrict.setText(search(TAG_DISTRICT, str));
        tvSity.setText(search(TAG_SITY, str));

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
            case R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    //--------------------------------------------------------------------------------------------<<

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        finish();
    }
}
