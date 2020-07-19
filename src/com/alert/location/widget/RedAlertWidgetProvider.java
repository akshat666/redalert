package com.alert.location.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.RemoteViews;

import com.alert.location.R;
import com.alert.location.SettingActivity;
import com.alert.location.service.SendAlertService;

public class RedAlertWidgetProvider extends AppWidgetProvider 
{
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) 
	{

		final int N = appWidgetIds.length;

		for (int i=0; i<N; i++) {
			int appWidgetId = appWidgetIds[i];


			//If no settings found, direct to settings page
			SharedPreferences settingsPreference = context.getSharedPreferences(SettingActivity.RED_ALERT_SETTING_PREF, Context.MODE_WORLD_READABLE);
			String number = settingsPreference.getString(SettingActivity.CONTACT_NUMBER, "NA");
			String number2 = settingsPreference.getString(SettingActivity.CONTACT_NUMBER_2, "NA");
			if(number.equals("NA") || number2.equals("NA"))
			{
				Intent intent = new Intent(context, SettingActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				context.startActivity(intent);
				return;
			}

			

			// Create an Intent to launch service
			Intent alertServiceIntent = new Intent(context, SendAlertService.class);
			//alertServiceIntent.setData(Uri.parse("uri::"+new Date().getSeconds()));
			PendingIntent pendingIntent = PendingIntent.getService(context, 0, alertServiceIntent, 0);

			// Get the layout for the App Widget and attach an on-click listener
			// to the button
			RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
			views.setOnClickPendingIntent(R.id.widgetBtn, pendingIntent);

			// Tell the AppWidgetManager to perform an update on the current app widget
			appWidgetManager.updateAppWidget(appWidgetId, views);

		}




	}

}
