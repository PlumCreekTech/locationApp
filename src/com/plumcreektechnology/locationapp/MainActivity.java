package com.plumcreektechnology.locationapp;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.Menu;
import android.widget.Toast;

@SuppressLint("NewApi")
public class MainActivity extends FragmentActivity implements GooglePlayServicesClient.ConnectionCallbacks, GooglePlayServicesClient.OnConnectionFailedListener {
	private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
	private LocationClient locClient;
	private Location currentLoc;

	 /**
	  * creates dialog fragment to display our error message.
	  * unlike other widgets, you can't instantiate DialogFragments on
	  * their own.
	  * @author norahayes
	  *
	  */
	@SuppressLint("NewApi")
	public static class ErrorDialogFragment extends DialogFragment{
		//global field containing actual error dialog
		private Dialog die;
		
		/**
		 * default constructor; sets dialog to null
		 */
		public ErrorDialogFragment(){
			super();
			die = null;
		}

		/**
		 * sets dialog
		 * @param dialog
		 */
		public void setDialog(Dialog dialog){
			die = dialog;
		}
		
		/**
		 * Return dialog to DialogFragment (tho not sure exactly why it works...)
		 * @param savedInstanceState
		 * @return dialog
		 */
		public Dialog onCreateDialog(Bundle savedInstanceState){
			return die;
		}
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		//Create new location client
		locClient = new LocationClient(this, this, this);
	}

	protected void onStart(){
		super.onStart();
		//connect client
		locClient.connect();
	}
	
	protected void onStop(){
		super.onStop();
		//Disconnect client
		locClient.disconnect();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	/**
	 * handles OnConnectionFailed error calls from LocationUpdateRemover 
	 * & LocationUpdateRequester because they call startResolutionForResult()
	 * and it calls onActivityResult() which comes here
	 */
	public void onActivityResult(int request, int result, Intent data) {
		switch (request) {
		case CONNECTION_FAILURE_RESOLUTION_REQUEST:
			switch (result) {
			case Activity.RESULT_OK:
				// TODO display positive result to user with a textview object

				break;
			default:
				// TODO display negative result
				break;
			}
			break;
		}
	}

	/**
	 * checks whether the device is connected to Google Play Services and
	 * displays an error message if not
	 * @return
	 */
	private boolean servicesConnected() {
		// check for Google Play
		int result = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
		//it's available
		if(ConnectionResult.SUCCESS == result) {
			Log.d("Location Updates", "Google Play services is available.");
			return true;
		} else {
			//not available
			showErrorDialog(result);
			return false;
		}
	}
	
	/**
	 * Displays an error dialog
	 * @param errorCode
	 */
	private void showErrorDialog(int errorCode) {
		Dialog dialog = GooglePlayServicesUtil.getErrorDialog(errorCode, this, 0);
		if(dialog != null){
			ErrorDialogFragment errorFrag = new ErrorDialogFragment();
			errorFrag.setDialog(dialog);
			errorFrag.show(getFragmentManager(), "Location Updates");
		}
	}
	
	/**
	 * Resolves connection error if possible; otherwise displays error dialog
	 */
	@Override
	public void onConnectionFailed(ConnectionResult connectResult) {
		// TODO Auto-generated method stub
		/*
		 * Google Play Services can resolve some connection errors
		 * If the error has a resolution, try to send an Intent to
		 * start a Google Play services activity that can fix error
		 */
		if(connectResult.hasResolution()) {
			try{
				connectResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
			} catch(IntentSender.SendIntentException e) {
				e.printStackTrace(); // log the error
			}
		} else {
			showErrorDialog(connectResult.getErrorCode()); // display a dialog
		}
	}

	/**
	 * Displays a toast when Location Services is connected
	 * Get most recent location
	 */
	@Override
	public void onConnected(Bundle bundle) {
		//Display connection status
		Toast.makeText(this, "Connected! Go you!", Toast.LENGTH_SHORT).show();
		//get location
		currentLoc = locClient.getLastLocation();
	}

	/**
	 * Displays a toast when Location Services is disconnected
	 */
	@Override
	public void onDisconnected() {
		// Display sad connection status
		Toast.makeText(this, "Disconnected. Don't beat yourself up about it.", Toast.LENGTH_SHORT).show();	
	}
}
