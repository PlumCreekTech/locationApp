package com.plumcreektechnology.locationapp;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationRequest;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint("NewApi")
public class MainActivity extends FragmentActivity implements
		GooglePlayServicesClient.ConnectionCallbacks,
		GooglePlayServicesClient.OnConnectionFailedListener,
		com.google.android.gms.location.LocationListener {
	
	private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
	private LocationClient locClient;
	private Location currentLoc;
	private LocationRequest locRequest;
	
	private static final long UPDATE_INTERVAL_MS = 5000;
	private static final long FASTEST_INTERVAL_MS = 2000;
	
	private TextView addressText;
	private ProgressBar progress;

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
	
	/**
	 * a class to handle address finding in the background
	 * extends AsyncTask<Locaiton, Void, String>
	 * @author devinfrenze
	 *
	 */
	private class GetAddressTask extends AsyncTask<Location, Void, String> {

		Context context;
		
		/**
		 * construct an instance of GetAddressTask
		 * @param context
		 */
		public GetAddressTask(Context context) {
			super();
			this.context = context;
		}
		
		/**
		 * finds address in background and returns as string
		 * takes any number of locations as arguments but 
		 * our implementation handles only the first
		 */
		@Override
		protected String doInBackground(Location... locArg) {
			Geocoder geocoder = new Geocoder(context, Locale.getDefault());
			Location loc = locArg[0];
			List<Address> addresses = null;
			try {
				addresses = geocoder.getFromLocation(loc.getLatitude(),loc.getLongitude(), 1);
			} catch (IOException e1) {
				Log.e("LocationAddress", "IO Exception in getFromLocaiton()");
				e1.printStackTrace();
				return ("IO Exception trying to get address");
			} catch (IllegalArgumentException e2) {
				String errorString = "Illegal arguments "
						+ Double.toString(loc.getLatitude()) + ", "
						+ Double.toString(loc.getLongitude())
						+ " passed to address service";
				Log.e("LocationSampleActivity", errorString);
				e2.printStackTrace();
				return errorString;
			}
			// if there is an address
			if(addresses != null && addresses.size()>0) {
				Address add = addresses.get(0);
				String addText = String.format("%s, %s, %s",
						add.getMaxAddressLineIndex() > 0 ? add.getAddressLine(0) : "",
								add.getLocality(),
								add.getCountryName());
				return addText;
			} else return "No address found";
		}
		
		/**
		 * displays the result of an address lookup when called
		 * hides the progress indicator
		 */
		protected void onPostExecute(String address) {
			progress.setVisibility(View.GONE);
			addressText.setText(address);
		}
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		addressText = (TextView) findViewById(R.id.location_adddress);
		progress = (ProgressBar) findViewById(R.id.address_progress);

		if (servicesConnected()) {
			// Create new location client
			locClient = new LocationClient(this, this, this);
		}
		
		// do all of the right things to make a location request for repeated updates
		locRequest = LocationRequest.create();
		locRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
		locRequest.setInterval(UPDATE_INTERVAL_MS);
		locRequest.setFastestInterval(FASTEST_INTERVAL_MS);
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
		String message = "First Location: "
				+ Double.toString(currentLoc.getLatitude()) + ","
				+ Double.toString(currentLoc.getLongitude());
		TextView viewLocation = (TextView) findViewById(R.id.locationtext);
		viewLocation.setText(message);
		locClient.requestLocationUpdates(locRequest, this);
	}

	/**
	 * Displays a toast when Location Services is disconnected
	 */
	@Override
	public void onDisconnected() {
		// Display sad connection status
		Toast.makeText(this, "Disconnected. Don't beat yourself up about it.", Toast.LENGTH_SHORT).show();	
	}

	/**
	 * every time the location changes make a toast
	 * and update the address in the textview
	 */
	@Override
	public void onLocationChanged(Location location) {
		currentLoc = location;
		String message = "Updated Location: "
				+ Double.toString(currentLoc.getLatitude()) + ","
				+ Double.toString(currentLoc.getLongitude());
		Toast.makeText(this, message, Toast.LENGTH_LONG).show();
		// check if Geocoder services is available
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD && Geocoder.isPresent()) {
			progress.setVisibility(View.VISIBLE);
			(new GetAddressTask(this)).execute(currentLoc);
		} else Toast.makeText(this, "geocoder unavailable", Toast.LENGTH_SHORT);
	}
}
