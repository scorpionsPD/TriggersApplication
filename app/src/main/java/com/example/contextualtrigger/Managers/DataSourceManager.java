package com.example.contextualtrigger.Managers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.example.contextualtrigger.DataSources.StepCountReceiver;
import com.example.contextualtrigger.DataSources.LocationLatLong;
import com.example.contextualtrigger.DataSources.WeatherAPIinfo;
import com.example.contextualtrigger.DataSources.StepCount;

public class DataSourceManager {

    private Context context;

    public DataSourceManager(Context context) {
        this.context = context;
    }

    //Alarm manager to trigger the information sources to execute code at certain times (like every hour even when the application isn't being used)
    public void setAlarmManager(){

        Intent weatherAPIIntent = new Intent(context, WeatherAPIinfo.class); //weather api intent
        PendingIntent sender = PendingIntent.getBroadcast(context, 2,weatherAPIIntent, 0);
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if(am != null) {
            long runAfter = 60 * 60 * 1000; //Every hour
            long runEvery = 60 * 60 * 1000; //Every hour
            am.setRepeating(AlarmManager.RTC_WAKEUP, runAfter, runEvery, sender); //call the weather api intent every hour
        }


        Intent locationIntent = new Intent(context, LocationLatLong.class); //location sensor intent
        PendingIntent sender2 = PendingIntent.getBroadcast(context, 2, locationIntent, 0);
        if(am != null){
            long runAfter2 = 30 * 60 * 1000; //Run after 30 mins has past for testing
            long runEvery2 = 60 * 60 * 1000; //Run every hour after the initial 30 mins
            am.setRepeating(AlarmManager.RTC_WAKEUP, runAfter2, runEvery2,sender2); //call the location sensor every hour
        }

        Intent stepIntent = new Intent(context, StepCountReceiver.class);
        PendingIntent sender3 = PendingIntent.getBroadcast(context, 0,stepIntent,0);
        if(am != null){
            long runAfter3 = 1 * 60 * 1000; //Run after 30 mins has past for testing
            long runEvery3 = 2 * 60 * 1000; //Run every hour after the initial 30 mins
            am.setRepeating(AlarmManager.RTC_WAKEUP,runAfter3, runEvery3,sender3);
       }


       // Intent stepIntent = new Intent(context, StepCount.class);
       // PendingIntent sender3 = PendingIntent.getBroadcast(context,2, stepIntent, 0);
       // AlarmManager am3 = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
       // if(am3 != null){
           // long runAfter3 = 45 * 60 * 1000;// run after the first 45 minutes for testing
           // long runevery3 = 120 * 60 * 1000;// Run evey 2 hours after intial 45 minutes
            //am3.setRepeating(AlarmManager.RTC_WAKEUP, runAfter3, runevery3, sender3);
       // }

        //Intent calorieIntent = new Intent(context, StepCount.class);
       // PendingIntent sender4 = PendingIntent.getBroadcast(context,2, calorieIntent, 0);
        //AlarmManager am4 = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        //if(am4 != null){
         //   long runAfter3 = 180 * 60 * 1000;// run after the first 3 hours for testing
            //long runevery3 = 360 * 60 * 1000;// Run evey 6 hours after intial 45 minutes
           // am4.setRepeating(AlarmManager.RTC_WAKEUP, runAfter3, runevery3, sender4);
        //}

    }

    //used to cancel the background alarms so there is only ever 2 alarms
    public void  cancelAlarmManager(){

        Intent weatherAPIIntent = new Intent(context, WeatherAPIinfo.class);//weather api intent
        PendingIntent sender = PendingIntent.getBroadcast(context, 2,weatherAPIIntent, 0);
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if(am != null) {
            am.cancel(sender);
        }

        Intent locationIntent = new Intent(context, LocationLatLong.class);//location data source intent
        PendingIntent sender2 = PendingIntent.getBroadcast(context, 2, locationIntent, 0);
        if(am != null){
            am.cancel(sender2);
        }

        Intent stepIntent = new Intent(context, StepCount.class);
        PendingIntent sender3 = PendingIntent.getBroadcast(context,2, stepIntent, 0);
        AlarmManager am3 = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if(am3 != null){
            am3.cancel(sender3);
        }

        Intent calorieIntent = new Intent(context, StepCount.class);
        PendingIntent sender4 = PendingIntent.getBroadcast(context,2, calorieIntent, 0);
        AlarmManager am4 = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if(am3 != null){
            am4.cancel(sender4);
        }


    }
}
