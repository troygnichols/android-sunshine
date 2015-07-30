package io.tgn.sunshine.app;

import android.text.format.Time;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;

public class OpenWeatherMap {
    public static final String LOG_TAG = OpenWeatherMap.class.getSimpleName();

    public static  String dateFromUnix(long t) {
       return  new SimpleDateFormat("EEE MMM dd").format(t);
    }

    public static String prettyHighLow(double high, double low) {
        long roundedHigh = Math.round(high);
        long roundedLow = Math.round(low);

        return roundedHigh + "/" + roundedLow;
    }

    public static String[] dailySummaries(String json, int numDays) throws JSONException {
        JSONObject weatherData = new JSONObject(json);
        JSONArray dailySummaries = weatherData.getJSONArray("list");

        Time day = new Time();

        int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), day.gmtoff);

        day = new Time();

        String[] results = new String[numDays];

        for (int i = 0; i < dailySummaries.length(); i++) {
            JSONObject forecast = dailySummaries.getJSONObject(i);
            long datetime = day.setJulianDay(julianStartDay + i);
            String humanDate = dateFromUnix(datetime);

            JSONObject weather = forecast.getJSONArray("weather").getJSONObject(0);
            String weatherDesc = weather.getString("description");

            JSONObject temp = forecast.getJSONObject("temp");
            double high = temp.getDouble("max");
            double low = temp.getDouble("min");

            results[i] = humanDate + " - " + weatherDesc + " - " + prettyHighLow(high, low);
        }

        for (String s : results) {
            Log.v(LOG_TAG, "Forecast entry: " + s);
        }

        return results;
    }
}
