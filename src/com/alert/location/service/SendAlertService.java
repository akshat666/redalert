package com.alert.location.service;

import java.util.ArrayList;
import java.util.List;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.SmsManager;
import android.widget.Toast;

import com.alert.location.RedAlertActivity;
import com.alert.location.SettingActivity;

public class SendAlertService extends IntentService {

	private static final String SEND_ALERT_THREAD = "SendAlertService";
	private LocationManager locationManager;
	private LocationListener locationListener;

	
	private Criteria myCriteria ;
	private String bestProvider;
	private Location newLocation;
	private Location oldLocation;
	private Location  bestLocation;
	private String priNumber;
	private String secNumber;
	private String message;
	private String mapURL = "http://maps.google.com/maps?q=loc:";
	public static boolean pauseService = false;
	public static boolean isRunningService = false;
	
	private static final int TWO_MINUTES = 1000 * 60 * 2;
	private int NO_OF_MESSGS = 0;
	private int GAP_TIME = 0;
	private Handler handler;
	
	/**
	 * Thread class to display a toast
	 * @author akshat
	 *
	 */
	private class DisplayToast implements Runnable {
		String text;

		public DisplayToast(String text) {
			this.text = text;
		}

		public void run() {
			Toast.makeText(getApplicationContext(), this.text, Toast.LENGTH_SHORT).show();
		}
	}
	
	
	public SendAlertService() {
		super(SendAlertService.SEND_ALERT_THREAD);
	
		myCriteria = new Criteria();
		myCriteria.setAccuracy(Criteria.ACCURACY_FINE);
		myCriteria.setPowerRequirement(Criteria.POWER_LOW);
		
		handler = new Handler() 
		{
			@Override
			public void handleMessage(Message msg) 
			{
				//
			}
		};
	}

	@Override
	protected void onHandleIntent(Intent intent) {

		if(pauseService)
		{
			isRunningService = false;
			stopSelf();
			return;
		}
		
		// Fetch the settings
		SharedPreferences settingsPref = getSharedPreferences(SettingActivity.RED_ALERT_SETTING_PREF,Context.MODE_WORLD_READABLE);
		this.NO_OF_MESSGS = Integer.parseInt(settingsPref.getString(SettingActivity.NO_OF_MESSGS, "0"));
		this.GAP_TIME = Integer.parseInt(settingsPref.getString(SettingActivity.GAP_TIME, "0"));
		
		//Get contact number
		priNumber = settingsPref.getString(SettingActivity.CONTACT_NUMBER, "NA");
		secNumber = settingsPref.getString(SettingActivity.CONTACT_NUMBER_2, "NA");
		message = settingsPref.getString(SettingActivity.MESSAGE, "Urgent! Help me!");
		
		if(priNumber.equals("NA") || secNumber.equals("NA"))
		{
			this.handler.post(new DisplayToast("No contacts found"));
			isRunningService = false;
			return;
		}
		
		// Acquire a reference to the system Location Manager
		locationManager = (LocationManager) this
				.getSystemService(Context.LOCATION_SERVICE);

		//find and set the locationProvider
		findProvider();
		
		//send alerts
		sendAlertMessages();

	}
	
	private void findProvider() {
		
		// let Android select the right location provider for you
		bestProvider = locationManager.getBestProvider(myCriteria, true);

		//If no provider set as network provider
		if (bestProvider == null) {
			bestProvider = locationManager.getProvider(
					LocationManager.NETWORK_PROVIDER).getName();
		}

	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) 
	{
		pauseService = intent.getBooleanExtra("pause", false);
		if(pauseService && isRunningService == true)
		{
			//Service is now running
			isRunningService = false;
		}
		else 
		{
			if(isRunningService == true)
			{
				this.handler.post(new DisplayToast("Alert service already running!"));
				return super.onStartCommand(intent, flags, startId);
			}
						
		}
		return super.onStartCommand(intent, flags, startId);
	}

	private void sendAlertMessages() 
	{
		
		// If no setting values found, or pause is true,  return
		if (pauseService == true || this.NO_OF_MESSGS == 0 || this.GAP_TIME == 0) 
		{
			isRunningService = false;
			stopSelf();
			return;
		}
		
		
		// Define a listener that responds to location updates
		locationListener = new LocationListener() {

			// Called when a new location is found by the network location provider.
			public void onLocationChanged(Location location) 
			{
				setBestLocation(location);
			}

			public void onStatusChanged(String provider, int status,Bundle extras) {}

			public void onProviderEnabled(String provider) {}

			public void onProviderDisabled(String provider) {}
			
		};

		
		//---------------------------------------- START LISTENING FOR LOCATION AND SEND ALERT MESSAGES ----------------------------//
		
		// Register the listener with the Location Manager to receive location updates
		locationManager.requestLocationUpdates(bestProvider, 0, 0,locationListener);
		
		//Set old location first
		this.bestLocation = this.oldLocation = this.newLocation = locationManager.getLastKnownLocation(bestProvider);
		
		// if Location not present in the history, set network last location
		if(this.bestLocation == null)
		{
			this.bestLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		}
		
		// Send the messages
		for(int i=0; i<this.NO_OF_MESSGS;i++)
		{
			
			if(this.bestLocation != null)
			{
				
				List<String> matchingProviders = locationManager.getAllProviders();
				ArrayList<Location> lastLocations = new ArrayList<Location>();
				
				for (String provider: matchingProviders) 
				{
				  Location location = locationManager.getLastKnownLocation(provider);
				  if (location != null) 
				  {
				    lastLocations.add(location);
				  }
				}
				findBestLocation(lastLocations);			
				
			}
			
			if(pauseService)
			{
				isRunningService = false;
				stopSelf();
				return;
			}
			
			String locationURL = null;
			
			//create and send SMS
			SmsManager smsManager = SmsManager.getDefault();
			if(bestLocation != null)
			{
				locationURL = mapURL+bestLocation.getLatitude()+"+"+bestLocation.getLongitude();
			}
			//If the location is still null, which can be because
			// GPS is still starting, just use the network location
			else
			{
				String provider = locationManager.getProvider(LocationManager.NETWORK_PROVIDER).getName();
				this.bestLocation = locationManager.getLastKnownLocation(provider);
				if(this.bestLocation == null)
				{
					// Got to use the handler to display the text
					this.handler.post(new DisplayToast("Location data unavailable.\n Check location settings!")); 
					SendAlertService.isRunningService = false;
					stopSelf();
					return;
				}
				locationURL = mapURL+bestLocation.getLatitude()+"+"+bestLocation.getLongitude();
			}
			smsManager.sendTextMessage(priNumber, null, message+" My location "+locationURL, null, null);
			smsManager.sendTextMessage(secNumber, null, message+" My location "+locationURL, null, null);
			
			//---------------------------------------------Send a notification to user - START------------------------------------------
			NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
			Notification notification = new Notification(com.alert.location.R.drawable.icon,
					"Red alert notification", System.currentTimeMillis());
			// Hide the notification after its selected
			notification.flags |= Notification.FLAG_AUTO_CANCEL;

			int j = i+1;
			Intent intent = new Intent(this, RedAlertActivity.class);
			PendingIntent activity = PendingIntent.getActivity(this, 0, intent,0);
			notification.setLatestEventInfo(this, "Red Alert", "Alert set "+j+" out of "+NO_OF_MESSGS+" sent", activity);
			notification.number = j;
			notificationManager.notify(0, notification);
			//---------------------------------------------Send a notification to user - END------------------------------------------
			

			//if more than 1 messgs and if not the last message..do the wait
			if(NO_OF_MESSGS > 1 && (i+1) != this.NO_OF_MESSGS)
			{
				// Do nothing for the gap_time between messages
				long endTime = System.currentTimeMillis() + this.GAP_TIME * 1000 * 60;
				while (System.currentTimeMillis() < endTime) 
				{
					if(pauseService)
					{
						isRunningService=false;
						stopSelf();
						return;
					}
				}
			}
		}
		
		locationManager.removeUpdates(locationListener);
		
		//---------------------------------------- STOP LISTENING FOR LOCATION ----------------------------//
		
		isRunningService = false;
	}
	
	
	private void findBestLocation(ArrayList<Location> lastLocations) 
	{
		if(this.bestLocation == null)
		{
			this.bestLocation = lastLocations.get(0);
		}
		for(Location loc : lastLocations)
		{
			if(isBetterLocation(loc, bestLocation))
			{
				bestLocation=loc;
			}
		}
		
	}

	/** Determines whether one Location reading is better than the current Location fix
	  * @param location  The new Location that you want to evaluate
	  * @param currentBestLocation  The current Location fix, to which you want to compare the new one
	  */
	protected boolean isBetterLocation(Location location, Location currentBestLocation) {
	    if (currentBestLocation == null) {
	        // A new location is always better than no location
	        return true;
	    }

	    // Check whether the new location fix is newer or older
	    long timeDelta = location.getTime() - currentBestLocation.getTime();
	    boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
	    boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
	    boolean isNewer = timeDelta > 0;

	    // If it's been more than two minutes since the current location, use the new location
	    // because the user has likely moved
	    if (isSignificantlyNewer) {
	        return true;
	    // If the new location is more than two minutes older, it must be worse
	    } else if (isSignificantlyOlder) {
	        return false;
	    }

	    // Check whether the new location fix is more or less accurate
	    int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
	    boolean isLessAccurate = accuracyDelta > 0;
	    boolean isMoreAccurate = accuracyDelta < 0;
	    boolean isSignificantlyLessAccurate = accuracyDelta > 200;

	    // Check if the old and new location are from the same provider
	    boolean isFromSameProvider = isSameProvider(location.getProvider(),
	            currentBestLocation.getProvider());

	    // Determine location quality using a combination of timeliness and accuracy
	    if (isMoreAccurate) {
	        return true;
	    } else if (isNewer && !isLessAccurate) {
	        return true;
	    } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
	        return true;
	    }
	    return false;
	}
	
	/** Checks whether two providers are the same */
	private boolean isSameProvider(String provider1, String provider2) {
	    if (provider1 == null) {
	      return provider2 == null;
	    }
	    return provider1.equals(provider2);
	}
	
	
	/**
	 *  From the new location obtained, select the best location and set it
	 * @param location
	 */
	private void setBestLocation(Location location)
	{
		oldLocation = newLocation;
		newLocation = location;
		
		if(isBetterLocation(newLocation, oldLocation))
		{
			bestLocation = newLocation;
		}
		else
		{
			bestLocation = oldLocation;
		}
	}

}
