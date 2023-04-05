package com.mygdx.hattrick2023;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;
import com.mygdx.hattrick2023.DeviceAPI;
import com.mygdx.hattrick2023.GameClientInterface;
import android.os.Handler;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

public class AndroidLauncher extends AndroidApplication implements DeviceAPI {

	@Override
	public void setCallback(GameClientInterface callback) {
		this.callback = callback;
		Log.d(TAG, "Callback set");
	}

	private com.mygdx.hattrick2023.GameClientInterface callback;

	private AudioTrack speaker;
	private AudioRecord recorder;
	private static final String TAG = "AndroidLauncher";
	private int minBufferSize = 0;
	private final int maxBufferSize = 4096;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();

		Log.d(TAG, "API Level: " + Build.VERSION.SDK_INT);

		initialize(new com.mygdx.hattrick2023.MyGdxGame(this), config);
	}

	@Override
	public void showNotification(final String message) {
		handler.post(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
			}
		});
	}

	public String getIpAddress() {
		WifiManager wifiMan =
				(WifiManager) getContext().getApplicationContext().getSystemService(getContext().WIFI_SERVICE);
		int ip = wifiMan.getConnectionInfo().getIpAddress();

		if (ByteOrder.nativeOrder().equals(ByteOrder.LITTLE_ENDIAN)) {
			ip = Integer.reverseBytes(ip);
		}

		byte[] ipByteArray = BigInteger.valueOf(ip).toByteArray();

		String ipAddress;
		try {
			ipAddress = InetAddress.getByAddress(ipByteArray).getHostAddress();
		} catch (UnknownHostException e) {
			ipAddress = "Unable to get host address";
		}

		return ipAddress;
	}

	public boolean isConnectedToLocalNetwork() {
		ConnectivityManager connManager =
				(ConnectivityManager) getContext().getSystemService(getContext().CONNECTIVITY_SERVICE);
		NetworkInfo info = connManager.getActiveNetworkInfo();
		return info != null && info.isConnected();
	}
	@Override
	public int getBufferSize() {
		return Math.max(minBufferSize, maxBufferSize);
	}

//	public List<Integer> getValidSampleRates() {
//		List<Integer> list = new ArrayList<>();
//		for (int rate : new int[] {8000, 11025, 16000, 22050, 44100}) {  // add the rates you wish to check against
//			int bufferSize = AudioRecord.getMinBufferSize(rate, recordChannelConfig, audioFormat);
//			if (bufferSize > 0) {
//				Log.d(TAG, rate  + " is supported");
//				list.add(rate);
//			}
//		}

//		return list;
//	}

	private boolean updated = false;
	private byte[] message;


	}

