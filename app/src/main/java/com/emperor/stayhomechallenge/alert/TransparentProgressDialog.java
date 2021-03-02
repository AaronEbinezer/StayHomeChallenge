package com.emperor.stayhomechallenge.alert;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.emperor.stayhomechallenge.R;


public class TransparentProgressDialog extends Dialog {

    private ImageView iv;

    public TransparentProgressDialog(Context context, int progressLayout) {
        super(context, R.style.CustomTransparentDialog);
        WindowManager.LayoutParams wlmp = getWindow().getAttributes();
        wlmp.gravity = Gravity.CENTER_HORIZONTAL;
        getWindow().setAttributes(wlmp);
        setTitle(null);
        setCancelable(false);
        setOnCancelListener(null);
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
//        iv = new ImageView(context);
//        iv.setImageResource(resourceIdOfImage);
//        layout.addView(resourceIdOfImage, params);
        View view = LayoutInflater.from(context).inflate(progressLayout,null, false);
        addContentView(view, params);
    }



    @Override
    public void show() {
        super.show();
//        RotateAnimation anim = new RotateAnimation(0.0f, 360.0f , Animation.RELATIVE_TO_SELF, .5f, Animation.RELATIVE_TO_SELF, .5f);
//        anim.setInterpolator(new LinearInterpolator());
//        anim.setRepeatCount(Animation.INFINITE);
//        anim.setDuration(3000);
//        iv.setAnimation(anim);
//        iv.startAnimation(anim);
    }
}
