package com.example.contextualtrigger.DataSources;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.contextualtrigger.Database.LocationTable;
import com.example.contextualtrigger.Database.TriggerDatabase;
import com.example.contextualtrigger.Database.WeatherTable;
import com.example.contextualtrigger.MainActivity;
import com.example.contextualtrigger.Triggers.GoodWeatherTrigger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class WeatherAPIinfo extends BroadcastReceiver {

    Context MainContext;
    TriggerDatabase triggerDatabase;

    private static int CITYID = 0; //ID of the city glasgow

    public WeatherAPIinfo(Context mainContext) {
        MainContext = mainContext;

    }

    public WeatherAPIinfo(){

    }

    //Uses the current known location stored and send that to the API
    //to get the ID to use in order to get the weather for the user's
    //current location.
    private synchronized void fetchLocationIDFromApi(){
        triggerDatabase = TriggerDatabase.getInstance(MainContext);
        List<LocationTable> locations = triggerDatabase.locationDao().getTodayLocations(getDate());

        if(locations.size() == 0){
            fetchWeatherFromApi();
            System.out.println("No locations, will try later....");
        } else {
            if(isOnline()){
                RequestQueue queue = Volley.newRequestQueue(MainContext);
                String url = "https://www.metaweather.com/api/location/search/?lattlong=" + locations.get(0).getLat() +","+ locations.get(0).getLng();
                JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url,null, new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            JSONObject cityInfo = response.getJSONObject(0);
                            CITYID = Integer.valueOf(cityInfo.getString("woeid"));
                        } catch (JSONException E){
                            E.printStackTrace();
                        }
                        fetchWeatherFromApi();
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                });
                queue.add(request);
            }
        }
    }

    //Gets the current weather from an api
    //Current hard coded to glasgow (will change to use lat and long)
    private  synchronized void fetchWeatherFromApi(){
        if (isOnline() && CITYID != 0) {
            RequestQueue queue = Volley.newRequestQueue(MainContext);
            String url = "https://www.metaweather.com/api/location/" + CITYID;
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                public void onResponse(JSONObject response) {
                    try {
                        JSONArray weather = response.getJSONArray("consolidated_weather");

                        JSONObject day1 = (JSONObject) weather.get(0);

                        storeWeatherData(day1, MainContext);
                    } catch (JSONException E) {
                        E.printStackTrace();
                    }

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                }
            });
            queue.add(request);
        }else {

        }
    }


    //Stores the weather info into the database
    private synchronized void storeWeatherData(JSONObject weatherJSON, Context context){
        triggerDatabase = TriggerDatabase.getInstance(context);
        List<WeatherTable> weather = triggerDatabase.weatherDao().getWeatherByDate(getDate());

        String desc = "";
        double minTemp = 0.0;
        double maxTemp = 0.0;
        double currentTemp = 0.0;
        int humidity = 0;
        double visibility = 0.0;
        String date = "";

        try {
            desc = weatherJSON.getString("weather_state_name");
            minTemp = weatherJSON.getDouble("min_temp");
            maxTemp = weatherJSON.getDouble("max_temp");
            currentTemp = weatherJSON.getDouble("the_temp");
            humidity = weatherJSON.getInt("humidity");
            visibility = weatherJSON.getDouble("visibility");
            date = getDate();

        }catch (JSONException E){
            E.printStackTrace();
        }


        if(findDate(weather,getDate())){
            triggerDatabase.weatherDao().updateCurrentWeather(desc,minTemp,maxTemp,currentTemp,humidity,visibility,date);
        } else {
            WeatherTable newEntry = new WeatherTable(desc,minTemp,maxTemp,currentTemp,humidity,visibility,date);
            triggerDatabase.weatherDao().insertWeather(newEntry);
        }
    }

    private boolean findDate(List<WeatherTable> weather, String current_date){
        if(weather.size() == 0){ return false;}
        for(int i = 0; i < weather.size(); i++){
                if(weather.get(i).getDate().equals(current_date)){
                    return true;
                }
        }
        return false;
    }

    //Checks to see if the device has an internet connection
    private boolean isOnline(){
        ConnectivityManager connMgr = (ConnectivityManager) MainContext.getSystemService(MainContext.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    @Override
    //This is called by the alarm manager when the time has been reached to execute this (i.e an hour has gone by)
    public void onReceive(Context context, Intent intent) {
        MainContext = context;
        fetchLocationIDFromApi();
    }

    //Gets the Current date and returns it
    public String getDate(){
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDateTime now = LocalDateTime.now();
        String date = dtf.format(now);

        return date;
    }
}
