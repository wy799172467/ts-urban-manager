package com.geone.inspect.threepart_ts.util;

import com.geone.inspect.threepart_ts.R;

import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;

public class GPSTracker extends Service implements LocationListener {

	private final Context mContext;

	/** GPS是否打开默认false */
	boolean isGPSEnabled = false;

	// flag for network status
	boolean isNetworkEnabled = false;

	// flag for GPS status
	boolean canGetGPSLocation = false;

	Location mLocation; // location
	double latitude; // latitude
	double longitude; // longitude

	// The minimum distance to change Updates in meters
	private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 meters

	// The minimum time between updates in milliseconds
	private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1; // 1 minute

	// Declaring a Location Manager
	protected LocationManager locationManager;

	public GPSTracker(Context context) {
		this.mContext = context;
		// startGPSLocation();
	}

	/**
	 * liyl 2015-3-17，只获取GPS位置，同时更新isGPSEnabled状态
	 * 必须放在isGPSEnabled()前，否则可能导致isGPSEnabled返回值错误
	 */

	public void startGPSLocation() {
		try {
			if (locationManager == null) {
				locationManager = (LocationManager) mContext
						.getSystemService(LOCATION_SERVICE);
			}

			// getting GPS status,检测GPS是否打开
			isGPSEnabled = locationManager
					.isProviderEnabled(LocationManager.GPS_PROVIDER);

			// getting network status
			// isNetworkEnabled = locationManager
			// .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

			if (isGPSEnabled) {
				this.canGetGPSLocation = true;

				if (mLocation == null) {
					locationManager.requestLocationUpdates(
							LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES,
							MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
					LogUtils.d("GPSTracker", "GPS Enabled");
					if (locationManager != null) {
						mLocation = locationManager
								.getLastKnownLocation(LocationManager.GPS_PROVIDER);
						if (mLocation != null) {
							latitude = mLocation.getLatitude();
							longitude = mLocation.getLongitude();
						}
					}
				}

			} else {
				this.canGetGPSLocation = false;
				// return null;
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		// return mLocation;
	}

	/**
	 * Stop using GPS listener Calling this function will stop using GPS in your
	 * app
	 * */
	public void stopUsingGPS() {
		if (locationManager != null) {
			locationManager.removeUpdates(GPSTracker.this);
		}
	}

	/**
	 * Function to get latitude
	 * */
	public double getLatitude() {
		if (mLocation != null) {
			latitude = mLocation.getLatitude();
		} // return latitude
		return latitude;

	}

	/**
	 * Function to get longitude
	 * */
	public double getLongitude() {
		if (mLocation != null) {
			longitude = mLocation.getLongitude();
		}

		// return longitude
		return longitude;
	}

	/**
	 * Function to check GPS enabled
	 * 
	 * @return boolean
	 * */
	public boolean canGetGPSLocation() {
		return this.canGetGPSLocation;
	}

	/**
	 * 
	 * @return GPS是否打开，默认false
	 */
	public boolean isGPSEnabled() {
		return this.isGPSEnabled;
	}

	/**
	 * Function to show settings alert dialog On pressing Settings button will
	 * lauch Settings Options
	 * */
	public void showSettingsAlert() {
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);

		// Setting Dialog Title
		alertDialog.setTitle(R.string.setting);

		// Setting Dialog Message
		alertDialog.setMessage(R.string.open_gps);

		// On pressing Settings button
		alertDialog.setPositiveButton(R.string.setting,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						Intent intent = new Intent(
								Settings.ACTION_LOCATION_SOURCE_SETTINGS);
						intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
						mContext.startActivity(intent);
					}
				});

		// on pressing cancel button
		// alertDialog.setNegativeButton(android.R.string.cancel,
		// new DialogInterface.OnClickListener() {
		// public void onClick(DialogInterface dialog, int which) {
		// dialog.cancel();
		// Toast.makeText(mContext, R.string.open_gps_later,
		// Toast.LENGTH_SHORT).show();
		// }
		// });
		// Showing Alert Message
		alertDialog.show();
	}

	@Override
	public void onLocationChanged(Location location) {
		this.mLocation = location;
	}

	@Override
	public void onProviderDisabled(String provider) {
	}

	@Override
	public void onProviderEnabled(String provider) {
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

}
