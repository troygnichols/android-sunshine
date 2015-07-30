package io.tgn.sunshine.app;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ForecastFragment extends Fragment {

    private final String LOG_TAG = ForecastFragment.class.getSimpleName();

    private ArrayAdapter<String> mForecastAdapter;

    public ForecastFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu m, MenuInflater i) {
        // Inflate the menu; this adds items to the action bar if it is present.
        i.inflate(R.menu.forecastfragment, m);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_refresh) {
            Log.v(LOG_TAG, "Refreshing weather data in background");
            FetchWeatherTask t = new FetchWeatherTask();
            t.execute("75208,US");
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstance) {
        String[] data = {
                "Mon 6/23 - Sunny - 31/17",
                "Tue 6/24 - Foggy - 21/8",
                "Wed 6/25 - Cloudy - 22/17",
                "Thurs 6/26 - Rainy - 18/11",
                "Fri 6/27 - Foggy - 21/10",
                "Sat 6/28 - TRAPPED IN WEATHERSTATION - 23/18",
                "Sun 6/29 - Sunny - 20/7"
        };
        List<String> weekForecast = new ArrayList<>(Arrays.asList(data));

        mForecastAdapter = new ArrayAdapter<>(getActivity(),
                R.layout.list_item_forecast,
                R.id.list_item_forecast_textview, weekForecast);

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        ListView lv = (ListView) rootView.findViewById(R.id.listview_forecast);
        lv.setAdapter(mForecastAdapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> av, View v, int pos, long l) {
                Intent intent = new Intent(v.getContext(), DetailActivity.class)
                        .putExtra("forecast", mForecastAdapter.getItem(pos));
                startActivity(intent);
            }
        });
        return rootView;
    }

    public class FetchWeatherTask extends AsyncTask<String, Void, String[]> {

        private final String LOG_TAG = FetchWeatherTask.class.getSimpleName();

        @Override
        protected String[] doInBackground(String ...params) {
            String postal = params[0];

            HttpURLConnection urlc = null;
            BufferedReader r = null;

            String resp = null;

            try {
                Uri.Builder urib = new Uri.Builder();
                urib.scheme("http")
                    .authority("api.openweathermap.org")
                    .appendPath("data")
                    .appendPath("2.5")
                    .appendPath("forecast")
                    .appendPath("daily")
                    .appendQueryParameter("q", postal)
                    .appendQueryParameter("mode", "json")
                    .appendQueryParameter("units", "metric")
                    .appendQueryParameter("cnt", "7");

                URL url = new URL(urib.toString());

                Log.v(LOG_TAG,  "Requesting URL: " + url);

                urlc = (HttpURLConnection) url.openConnection();
                urlc.setRequestMethod("GET");
                urlc.connect();

                InputStream in = urlc.getInputStream();
                StringBuffer b = new StringBuffer();
                r = new BufferedReader(new InputStreamReader(in));

                String line;
                while((line = r.readLine()) != null) {
                    b.append(line);
                }

                resp = b.toString();

            } catch (IOException e) {
                Log.e(LOG_TAG, "Error", e);
            } finally {
                if (urlc != null) {
                    urlc.disconnect();
                }
                if (r != null) {
                    try {
                        r.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }

            Log.v(LOG_TAG, "Got response: " + resp);


            try {
                return OpenWeatherMap.dailySummaries(resp, 7);
            } catch (JSONException e) {
                Log.e(LOG_TAG, "Could not parse OpenWeatherMap data into daily summaries: " + resp,  e);
                return new String[]{};
            }
        }

        @Override
        protected void onPostExecute(String[] data) {
            mForecastAdapter.clear();
            Log.v(LOG_TAG, "onPostExecute: " + data);
            for (String s : data) {
                mForecastAdapter.add(s);
            }
        }
    }

}