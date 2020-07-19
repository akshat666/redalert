package com.alert.location;





import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.alert.location.service.SendAlertService;

public class RedAlertActivity extends Activity 
{
	
	private static final int menu_option_settings = 0;
	private static final int menu_option_cancel_service = 1;
	private static final int menu_option_about = 2;
	
    
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        new SimpleEula(this).show();
        
        final Button alertBtn = (Button) findViewById(R.id.alertBtn);
        alertBtn.setOnClickListener(new OnClickListener() {
        	
			@Override
			public void onClick(View v) 
			{
				Vibrator myVib = (Vibrator) getSystemService(VIBRATOR_SERVICE);
				myVib.vibrate(60);
				myVib=null;
				
				if(SendAlertService.isRunningService)
				{
					Toast toast = Toast.makeText(RedAlertActivity.this, R.string.alertServiceIsRunning, Toast.LENGTH_SHORT);
					toast.show();
				}
				else
				{
					//If no settings found, direct to settings page
					SharedPreferences settingsPreference = getSharedPreferences(SettingActivity.RED_ALERT_SETTING_PREF, Context.MODE_WORLD_READABLE);
					
					String number = settingsPreference.getString(SettingActivity.CONTACT_NUMBER, "NA");
					String secNumber = settingsPreference.getString(SettingActivity.CONTACT_NUMBER_2, "NA");
					
					//If no contact is added in settings, go to settings page
					if(number.equals("NA") || secNumber.endsWith("NA"))
					{
						Intent intent = new Intent(RedAlertActivity.this, SettingActivity.class);
						startActivity(intent);
						return;
					}
					
					Intent alertServiceIntent = new Intent(RedAlertActivity.this, SendAlertService.class);
					alertServiceIntent.putExtra("pause", false);
					startService(alertServiceIntent);

				}
				
			}
		});
        
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) 
    {
    	super.onCreateOptionsMenu(menu);
    	menu.add(0, menu_option_settings, menu_option_settings, R.string.settings);
    	menu.add(0, menu_option_cancel_service, menu_option_cancel_service, R.string.cancelAlerts);
    	menu.add(0, menu_option_about, menu_option_about, R.string.about);
    	return true;
    }
    
    
    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) 
    {
    
    	
    	switch(item.getItemId())
    	{
    		case menu_option_settings: 
    			openSettings();
    			return true;
    		case menu_option_cancel_service:
    			cancelService();
    			return true;
    		case menu_option_about:
    			about();
    			return true;
    	}
    	return super.onMenuItemSelected(featureId, item);
    }

	private void about() {
		Intent intent = new Intent(RedAlertActivity.this, AboutActivity.class);
		startActivity(intent);		
	}

	private void cancelService() {
		
		Intent alertServiceIntent = new Intent(RedAlertActivity.this, SendAlertService.class);
		alertServiceIntent.putExtra("pause", true);
		startService(alertServiceIntent);
		
	}

	private void openSettings() 
	{
		Intent intent = new Intent(RedAlertActivity.this, SettingActivity.class);
		startActivity(intent);
	}
}