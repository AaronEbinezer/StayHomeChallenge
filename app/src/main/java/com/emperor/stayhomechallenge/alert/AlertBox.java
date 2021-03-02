package com.emperor.stayhomechallenge.alert;



import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.emperor.stayhomechallenge.R;


/**
 * Created by Systems02 on 22-May-17.
 */

public class AlertBox {

    public Context context;
    public AlertDialog alertDialog;
    private OnPositiveClickListener onPositiveClickListener;
    private OnNegativeClickListener onNegativeClickListener;
    private OnCustomClickListener onCustomClickListener;
    private OnMessagePassClickListener onMessagePassClickListener;

    public AlertBox(Context context) {
        this.context = context;
//        onClickListener= (Alert.onClickListener) context;
    }

    public void showAlertBox(String msg) {
        alertDialog = new AlertDialog.Builder(
                context).create();

        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.alert_box_msg, null);
        TextView alert = dialogView.findViewById(R.id.alert_msg);
        alert.setText(msg);
        Button okayBt = (Button)  dialogView.findViewById(R.id.okay_bt);
        alertDialog.setView(dialogView);
        okayBt.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                alertDialog.dismiss();

            }
        });
        alertDialog.show();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.setCancelable(false);
    }
    public void showAlertBoxWithEt(String msg, int isVisible) {
        alertDialog = new AlertDialog.Builder(
                context).create();

        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.alert_box_msg, null);
        TextView alert = dialogView.findViewById(R.id.alert_msg);
        alert.setText(msg);
        Button okayBt = (Button)  dialogView.findViewById(R.id.okay_bt);
        final EditText remarkEt =  dialogView.findViewById(R.id.remark_et);
//        Button cancelBt = (Button)  dialogView.findViewById(R.id.cancel_bt);
//        LinearLayout cancelBtLayout =  dialogView.findViewById(R.id.cancel_layout);
        remarkEt.setVisibility(isVisible);
        alertDialog.setView(dialogView);
        okayBt.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                alertDialog.dismiss();
                onMessagePassClickListener.onClick(remarkEt.getText().toString());

            }
        });
        //alertDialog.setMessage(msg);
        //alertDialog.setTitle("Turbo Energy Limited");
       /* alertDialog.setIcon(ContextCompat.getDrawable(context,R.mipmap.ic_launcher_square));
        alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

            }
        });*/
        //alertDialog.setIcon(R.drawable.logo);
        alertDialog.show();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.setCancelable(false);
        /*alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {

            }
        })*/
    }

    public void showAlertBoxWithListener(String msg, int isVisible) {
        alertDialog = new AlertDialog.Builder(
                context).create();

        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.alert_box_msg, null);
        TextView alert = dialogView.findViewById(R.id.alert_msg);
        alert.setText(msg);
        Button okayBt = (Button)  dialogView.findViewById(R.id.okay_bt);
        Button cancelBt = (Button)  dialogView.findViewById(R.id.cancel_bt);
        LinearLayout cancelLayout = dialogView.findViewById(R.id.cancel_layout);
        alertDialog.setView(dialogView);
        cancelLayout.setVisibility(isVisible);
        okayBt.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                alertDialog.dismiss();
//                ((Activity) context).finish();

                onPositiveClickListener.onPositiveClick();
            }
        });

        cancelBt.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                alertDialog.dismiss();
//                ((Activity) context).finish();
                onNegativeClickListener.onPositiveClick();
            }
        });
        alertDialog.show();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.setCancelable(false);
    }




    public void setOnMessageClickListener(OnMessagePassClickListener onClickListener){
        this.onMessagePassClickListener=onClickListener;
    }

    public void setOnPositiveClickListener(OnPositiveClickListener onClickListener){
        this.onPositiveClickListener=onClickListener;
    }

    public void setOnNegativeClickListener(OnNegativeClickListener onClickListener){
        this.onNegativeClickListener=onClickListener;
    }

    public void setOnCustomClickListener(OnCustomClickListener onClickListener){
        this.onCustomClickListener=onClickListener;
    }

    public interface OnPositiveClickListener{
        public void onPositiveClick();
    }

    public interface OnNegativeClickListener{
        public void onPositiveClick();
    }

    public interface OnCustomClickListener{
        public void onClick();
    }

    public interface OnMessagePassClickListener{
        public void onClick(String msg);
    }
}
