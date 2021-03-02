package com.emperor.stayhomechallenge.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.sql.Connection;

public class NetworkConnection {
	private Boolean checkingInternet;
	private Connection connection;
	private Context context;



	public NetworkConnection(Context context) {
		this.checkingInternet = Boolean.valueOf(false);
		this.context = context;
	}

	

	public boolean checkInternet() {
		ConnectivityManager connec = (ConnectivityManager) this.context.getSystemService("connectivity");
		NetworkInfo wifi = connec.getNetworkInfo(1);
		NetworkInfo mobile = connec.getNetworkInfo(0);
		if (wifi.isConnected()) {
			this.checkingInternet = Boolean.valueOf(true);
		} else if (mobile.isConnected()) {
			this.checkingInternet = Boolean.valueOf(true);
		} else {
			this.checkingInternet = Boolean.valueOf(false);
		}
		return this.checkingInternet.booleanValue();
	}

//	public void alertDialogShow() {
//		final AlertDialog alertDialog = new Builder(
//				context).create();
//
//		LayoutInflater inflater =((Activity) context).getLayoutInflater();
//		View dialogView = inflater.inflate(R.layout.alertbox, null);
//		alertDialog.setView(dialogView);
//
//		TextView log = (TextView) dialogView.findViewById(R.id.textView1);
//		Button okay = (Button)  dialogView.findViewById(R.id.okay);
//		log.setText("Unable to connect please check your internet connection !");
//		okay.setOnClickListener(new OnClickListener() {
//
//			@Override
//			public void onClick(View arg0) {
//				// TODO Auto-generated method stub
//				alertDialog.dismiss();
//			}
//		});alertDialog.show();
//
//	}
}
