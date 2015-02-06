package com.john.util;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.john.groupbuy.R;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

//google location manager
public enum GoogleLocationManager implements
        LocationListener,
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener {
    INSTANCE;
    // Handle to SharedPreferences for this app
    SharedPreferences mPrefs;
    // Handle to a SharedPreferences editor
    SharedPreferences.Editor mEditor;

    // A request to connect to Location Services
    private LocationRequest mLocationRequest;
    // Stores the current instantiation of the location client in this object
    private LocationClient mLocationClient;
    private Context context;
    private Activity activity;
    private boolean isConnected = false;
    private GetAddressTask addressTask;
    private OnGoogleLocationListener locationListener;

    public void setLocationListener(OnGoogleLocationListener locationListener) {
        this.locationListener = locationListener;
    }

    public void initManager(Context context) {
        this.context = context;
        // Create a new global location parameters object
        mLocationRequest = LocationRequest.create();
        /*
         * Set the update interval
         */
        mLocationRequest.setInterval(LocationUtils.UPDATE_INTERVAL_IN_MILLISECONDS);

        // Use high accuracy
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        // Set the interval ceiling to one minute
        mLocationRequest.setFastestInterval(LocationUtils.FAST_INTERVAL_CEILING_IN_MILLISECONDS);

        // Open Shared Preferences
        mPrefs = context.getSharedPreferences(LocationUtils.SHARED_PREFERENCES, Context.MODE_PRIVATE);

        // Get an editor
        mEditor = mPrefs.edit();
        /*
         * Create a new location client, using the enclosing class to
         * handle callbacks.
         */
        mLocationClient = new LocationClient(context, this, this);
    }

    public boolean startLocation(Activity activity) {
        if (!servicesConnected())
            return false;
        this.activity = activity;
        mLocationClient.connect();
        startPeriodicUpdates();
        return true;
    }

    public void stopLocation() {
        this.activity = null;
        stopPeriodicUpdates();
        if (mLocationClient.isConnecting())
            mLocationClient.disconnect();
        isConnected = false;
    }

    @Override
    public void onConnected(Bundle bundle) {
        isConnected = true;
        startPeriodicUpdates();
    }

    /**
     * Verify that Google Play services is available before making a request.
     *
     * @return true if Google Play services is available, otherwise false
     */
    public boolean servicesConnected() {
        // Check that Google Play services is available
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(context);
        // If Google Play services is available
        if (ConnectionResult.SUCCESS == resultCode) {
            return true;
        } else {
            // Display an error dialog
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(resultCode, activity, 0);
            dialog.show();
            return false;
        }
    }

    /**
     * In response to a request to start updates, send a request
     * to Location Services
     */
    private void startPeriodicUpdates() {
        if (isConnected && activity != null)
            mLocationClient.requestLocationUpdates(mLocationRequest, this);
    }

    /**
     * In response to a request to stop updates, send a request to
     * Location Services
     */
    private void stopPeriodicUpdates() {
        mLocationClient.removeLocationUpdates(this);
    }

    @Override
    public void onDisconnected() {
        isConnected = false;
        stopPeriodicUpdates();
    }

    /**
     * Report location updates to the UI.
     *
     * @param location The updated location.
     */
    @Override
    public void onLocationChanged(Location location) {
        if (location == null)
            return;

        if (locationListener != null)
            locationListener.onLocationChanged(location);

        stopTask(addressTask);
        addressTask = new GetAddressTask(context);
        addressTask.execute(location);
    }

    private void stopTask(GetAddressTask task) {
        if (task == null)
            return;
        if (task.getStatus() == AsyncTask.Status.RUNNING)
            task.cancel(true);
        task = null;
    }

    /*
         * Called by Location Services if the attempt to
         * Location Services fails.
         */
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

        /*
         * Google Play services can resolve some errors it detects.
         * If the error has a resolution, try sending an Intent to
         * start a Google Play services activity that can resolve
         * error.
         */
        if (connectionResult.hasResolution()) {
            try {

                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(
                        activity,
                        LocationUtils.CONNECTION_FAILURE_RESOLUTION_REQUEST);

                /*
                * Thrown if Google Play services canceled the original
                * PendingIntent
                */

            } catch (IntentSender.SendIntentException e) {

                // Log the error
                e.printStackTrace();
            }
        } else {

            // If no resolution is available, display a dialog to the user with the error.
            showErrorDialog(connectionResult.getErrorCode());
        }
    }

    /**
     * Show a dialog returned by Google Play services for the
     * connection error code
     *
     * @param errorCode An error code returned from onConnectionFailed
     */
    private void showErrorDialog(int errorCode) {

        // Get the error dialog from Google Play services
        Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(
                errorCode,
                activity,
                LocationUtils.CONNECTION_FAILURE_RESOLUTION_REQUEST);

        // If Google Play services can provide an error dialog
        if (errorDialog != null) {
            errorDialog.setCanceledOnTouchOutside(true);
            errorDialog.show();
        }
    }

    public interface OnGoogleLocationListener {
        public void onLocationChanged(Location location);
        public void onAddressChanged(String address);
    }

    /**
     * An AsyncTask that calls getFromLocation() in the background.
     * The class uses the following generic types:
     * Location - A {@link android.location.Location} object containing the current location,
     * passed as the input parameter to doInBackground()
     * Void     - indicates that progress units are not used by this subclass
     * String   - An address passed to onPostExecute()
     */
    protected class GetAddressTask extends AsyncTask<Location, Void, String> {

        // Store the context passed to the AsyncTask when the system instantiates it.
        Context localContext;
        boolean addressAvailable;

        // Constructor called by the system to instantiate the task
        public GetAddressTask(Context context) {
            // Required by the semantics of AsyncTask
            super();
            // Set a Context for the background task
            localContext = context;
        }

        /**
         * Get a geocoding service instance, pass latitude and longitude to it, format the returned
         * address, and return the address to the UI thread.
         */
        @Override
        protected String doInBackground(Location... params) {
            Geocoder geocoder = new Geocoder(localContext, Locale.getDefault());
            Location location = params[0];
            // Create a list to contain the result address
            List<Address> addresses = null;

            // Try to get an address for the current location. Catch IO or network problems.
            try {

                /*
                 * Call the synchronous getFromLocation() method with the latitude and
                 * longitude of the current location. Return at most 1 address.
                 */
                addresses = geocoder.getFromLocation(location.getLatitude(),
                        location.getLongitude(), 1
                );
                // Catch network or other I/O problems.
            } catch (IOException exception1) {
                exception1.printStackTrace();
                return "";
            } catch (IllegalArgumentException exception2) {
                exception2.printStackTrace();
                return "";
            }
            // If the reverse geocode returned an address
            if (addresses != null && addresses.size() > 0) {

                // Get the first address
                Address address = addresses.get(0);

                // Format the first line of address
                String addressText = context.getString(R.string.address_output_string,
                        // If there's a street address, add it
                        address.getMaxAddressLineIndex() > 0 ?
                                address.getAddressLine(0) : "",
                        // Locality is usually a city
                        address.getLocality(),
                        // The country of the address
                        address.getCountryName()
                );

                addressAvailable = true;
                // Return the text
                return addressText;

                // If there aren't any addresses, post a message
            } else {
                return "";
            }
        }

        /**
         * A method that's called once doInBackground() completes. Set the text of the
         * UI element that displays the address. This method runs on the UI thread.
         */
        @Override
        protected void onPostExecute(String address) {
            if (locationListener != null)
                locationListener.onAddressChanged(address);
        }
    }
}
