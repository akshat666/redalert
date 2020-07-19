package com.alert.location;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

public class SettingActivity extends Activity
{
	
	private static final int PICK_CONTACT = 1;
	private static final int PICK_CONTACT_2 = 2;
	public static final String RED_ALERT_SETTING_PREF = "RED_ALERT_SETTING";
	
	public static final String CONTACT_NAME = "CONTACT_NAME";
	public static final String CONTACT_NUMBER= "CONTACT_NUMBER";
	public static final String CONTACT_URI = "CONTACT_URI";
	
	public static final String CONTACT_NAME_2 = "CONTACT_NAME_2";
	public static final String CONTACT_NUMBER_2= "CONTACT_NUMBER_2";
	public static final String CONTACT_URI_2 = "CONTACT_URI_2";
	
	public static final String GAP_TIME = "GAP_TIME";
	public static final String NO_OF_MESSGS = "NO_OF_MESSGS";
	public static final String MESSAGE = "MESSAGE";
	public static final String TAG = "SettingActivity";
	
	public String contactName;
	public String contactNumber;
	public String contactURI;
	
	private Context context = this;
	
	private static SharedPreferences settingsPreference;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings);
	
		Button priContactBtn = (Button)findViewById(R.id.primanyContact);
		priContactBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) 
			{
				//Ask system to find an activity that can perform a PICK action 
				Intent intent = new Intent(Intent.ACTION_GET_CONTENT);//, ContactsContract.Contacts.CONTENT_URI);
				intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
				startActivityForResult(intent, PICK_CONTACT);
			}
		});
		
		Button secContactBtn = (Button)findViewById(R.id.secondContactBtn);
		secContactBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) 
			{
				//Ask system to find an activity that can perform a PICK action 
				Intent intent = new Intent(Intent.ACTION_GET_CONTENT);//, ContactsContract.Contacts.CONTENT_URI);
				intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
				startActivityForResult(intent, PICK_CONTACT_2);
			}
		});
		
		
		
		//Add a listener for gap time spinner
		Spinner gapTimeSpinner = (Spinner)findViewById(R.id.spinnerGapTime);
		gapTimeSpinner.setOnItemSelectedListener(new OnItemSelectedListener(){

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,int pos, long id) 
			{
				String gapTime = parent.getItemAtPosition(pos).toString();
				settingsPreference = getSharedPreferences(SettingActivity.RED_ALERT_SETTING_PREF, Context.MODE_WORLD_READABLE);
				Editor editPref = settingsPreference.edit();
				editPref.putString(SettingActivity.GAP_TIME, gapTime);
				editPref.commit();
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) 
			{
			}
			
		});
		
		
		// Add a listener for no of messgs spinner
		Spinner noOfMessgsSpinner = (Spinner) findViewById(R.id.spinnerNoOfMgs);
		noOfMessgsSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,int pos, long id) 
			{
				String noOfMessgs = parent.getItemAtPosition(pos).toString();
				settingsPreference = getSharedPreferences(SettingActivity.RED_ALERT_SETTING_PREF,Context.MODE_WORLD_READABLE);
				Editor editPref = settingsPreference.edit();
				editPref.putString(SettingActivity.NO_OF_MESSGS, noOfMessgs);
				editPref.commit();
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {

			}

		});
		
		//------------------------ START ------------------------
		//If setting values are present set values into button and spinners 
		settingsPreference = getSharedPreferences(SettingActivity.RED_ALERT_SETTING_PREF,Context.MODE_WORLD_READABLE);
		String name = settingsPreference.getString(SettingActivity.CONTACT_NAME, "NA");
		String numString = settingsPreference.getString(SettingActivity.CONTACT_NUMBER, "NA");
		
		String name2 = settingsPreference.getString(SettingActivity.CONTACT_NAME_2, "NA");
		String numString2 = settingsPreference.getString(SettingActivity.CONTACT_NUMBER_2, "NA");
		
		String noOfMessgsInt = settingsPreference.getString(SettingActivity.NO_OF_MESSGS, "0");
		String gapTimeInt = settingsPreference.getString(SettingActivity.GAP_TIME, "0");
		String message = settingsPreference.getString(SettingActivity.MESSAGE, "Urgent! Help me!");
		
		if(!name.equals("NA") && !numString.equals("NA"))
		{
			priContactBtn.setText(name+" : "+numString);
		}
		if(!name2.equals("NA") && !numString2.equals("NA"))
		{
			secContactBtn.setText(name2+" : "+numString2);
		}
		
		if(!noOfMessgsInt.equals("0") && !gapTimeInt.equals("0"))
		{
			ArrayAdapter<String> messgAdap = (ArrayAdapter<String>) noOfMessgsSpinner.getAdapter();
			int spinnerPosition1 = messgAdap.getPosition(noOfMessgsInt);
			
			ArrayAdapter<String> gapAdap = (ArrayAdapter<String>) gapTimeSpinner.getAdapter();
			int spinnerPosition2 = gapAdap.getPosition(gapTimeInt);
			
			//set the default according to value
			noOfMessgsSpinner.setSelection(spinnerPosition1);
			gapTimeSpinner.setSelection(spinnerPosition2);
		}
		
		//custom message view
		final TextView messgTextView = (TextView)findViewById(R.id.messgText);
		messgTextView.setText(message);
		
		
		messgTextView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) 
			{
				//Get and inflate the promt view
				LayoutInflater li = LayoutInflater.from(context);
				View promptsView = li.inflate(R.layout.message_promt, null);
				
				AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
 
				// set prompt view to alertdialog builder
				alertDialogBuilder.setView(promptsView);
 
				final EditText userInput = (EditText) promptsView.findViewById(R.id.message);
				
				// set dialog message
				alertDialogBuilder
					.setCancelable(false)
					.setPositiveButton("Save",
					  new DialogInterface.OnClickListener() {
					    public void onClick(DialogInterface dialog,int id) {
						// get user input and set it to SharedPref

					    	Editor editor = settingsPreference.edit();
					    	editor.putString(SettingActivity.MESSAGE, userInput.getText().toString());
					    	editor.commit();
					    	messgTextView.setText(userInput.getText());
					    }
					  })
					.setNegativeButton("Cancel",
					  new DialogInterface.OnClickListener() {
					    public void onClick(DialogInterface dialog,int id) {
						dialog.cancel();
					    }
					  });
				
				// create alert dialog
				AlertDialog alertDialog = alertDialogBuilder.create();
 
				// show it
				alertDialog.show();
			}
		});
		
		
		
		//------------------------ END ------------------------
	}
	
	/**
	 *  Called when an activity is returned back with a resultCode
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) 
	{
		super.onActivityResult(requestCode, resultCode, data);
		
		switch (requestCode) 
		{
			case PICK_CONTACT:
				if(resultCode == Activity.RESULT_OK)
				{
					fetchContact(data,PICK_CONTACT);					
				}
				break;
				
			case PICK_CONTACT_2:
				if(resultCode == Activity.RESULT_OK)
				{
					fetchContact(data,PICK_CONTACT_2);					
				}
				break;

		default:
			break;
		}
	}
	
	private void fetchContact(Intent data, int contact)
	{
		Uri contactDataUri = data.getData();
		this.contactURI = contactDataUri.toString();

		//Get contact ID from URI
		String contactID = contactDataUri.getLastPathSegment();

		// query for phone numbers for the selected contact id
		Cursor c = getContentResolver().query(
		    Phone.CONTENT_URI, null,
		    Phone._ID + "=?",
		    new String[]{contactID}, null);

		int displayName = c.getColumnIndex(Phone.DISPLAY_NAME);
		int phoneIdx = c.getColumnIndex(Phone.NUMBER);

		if(c.getCount() == 1) { // contact has a single phone number
		    // get the only phone number
		    if(c.moveToFirst()) {
		        this.contactNumber = c.getString(phoneIdx);
		        this.contactName = c.getString(displayName);
		        if(contact == PICK_CONTACT)
		        {
		        	saveContactOne();
		        }
		        else
		        {
		        	saveContactTwo();
		        }
		        

		    } else {
		        Log.w(SettingActivity.TAG, "No results");
		    }
		}
	}
	
	public void saveContactOne()
	{
		SharedPreferences settingPref = getSharedPreferences(SettingActivity.RED_ALERT_SETTING_PREF, Context.MODE_WORLD_READABLE);
		Editor editPref = settingPref.edit();
		
		editPref.putString(SettingActivity.CONTACT_NAME, this.contactName);
		editPref.putString(SettingActivity.CONTACT_NUMBER, this.contactNumber);
		editPref.putString(SettingActivity.CONTACT_URI, this.contactURI);
		
		editPref.commit();
		
		
		Button contactBtn = (Button)findViewById(R.id.primanyContact);
		contactBtn.setText(this.contactName+" : "+this.contactNumber);
	}
	
	public void saveContactTwo()
	{
		SharedPreferences settingPref = getSharedPreferences(SettingActivity.RED_ALERT_SETTING_PREF, Context.MODE_WORLD_READABLE);
		Editor editPref = settingPref.edit();
		
		editPref.putString(SettingActivity.CONTACT_NAME_2, this.contactName);
		editPref.putString(SettingActivity.CONTACT_NUMBER_2, this.contactNumber);
		editPref.putString(SettingActivity.CONTACT_URI_2, this.contactURI);
		
		editPref.commit();
		
		
		Button contactBtn = (Button)findViewById(R.id.secondContactBtn);
		contactBtn.setText(this.contactName+" : "+this.contactNumber);
	}
}
