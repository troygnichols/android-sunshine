package io.tgn.sunshine.app;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class ForecastFragment extends Fragment {
    public static String LOG_TAG = ForecastFragment.class.getSimpleName();

    public ForecastFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // ArrayAdapter<String> listItemAdapter = new ArrayAdapter<>(
        //         getActivity(),
        //         R.layout.list_item_forecast,
        //         R.id.list_item_forecast_textview,
        //         getWeekForecast());

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        // ListView forecastListView = (ListView) rootView.findViewById(R.id.listview_forecast);
        // forecastListView.setAdapter(listItemAdapter);

        // new FetchWeatherTask().execute();

        return rootView;
    }

    protected class FetchWeatherTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String postalCode = params[0];
            String daysToFetch = params[1];

            String jsonString = fetchForecastData(postalCode, daysToFetch);
            Log.v(LOG_TAG, "Forecast JSON string: " + jsonString);

            return jsonString;
        }

        private String fetchForecastData(String postalCode, String days) {
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            try {
                Uri.Builder uriBuilder = new Uri.Builder();
                URL url = new URL(uriBuilder
                        .scheme("http")
                        .authority("api.openweathermap.org")
                        .appendPath("data")
                        .appendPath("2.5")
                        .appendPath("forecast")
                        .appendPath("daily")
                        .appendQueryParameter("mode", "json")
                        .appendQueryParameter("units", "metric")
                        .appendQueryParameter("cnt", days)
                        .appendQueryParameter("q", postalCode)
                        .toString());

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();

                if (inputStream == null) {
                    return null;
                }

                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // extra newline makes this easier to debug
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    return null;
                }

                return buffer.toString();
            } catch (IOException e) {
                Log.e(ForecastFragment.LOG_TAG, "Error communicating with weather API", e);
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) { // ??? why is this final?
                        Log.e(ForecastFragment.LOG_TAG, "Error closing stream for connection to Weather API", e);
                    }
                }
            }

            return null;
        }
    }
}
