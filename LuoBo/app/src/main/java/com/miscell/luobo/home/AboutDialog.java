package com.miscell.luobo.home;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import com.miscell.luobo.R;
import com.miscell.luobo.utils.Utils;

/**
 * Created by chenjishi on 14-1-10.
 */
public class AboutDialog extends Dialog implements View.OnClickListener {

    public AboutDialog(Context context) {
        super(context, R.style.FullHeightDialog);

        setCanceledOnTouchOutside(true);
        View view = LayoutInflater.from(context).inflate(R.layout.about_view, null);
        setContentView(view);

        Button versionBtn = (Button) view.findViewById(R.id.btn_version);
        String versionName = Utils.getVersionName(context);
        if (null != versionName) {
            versionBtn.setText(versionName);
        }

        findViewById(R.id.btn_feedback).setOnClickListener(this);
    }

    @Override
    public void show() {
        WindowManager windowManager = getWindow().getWindowManager();
        DisplayMetrics metrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(metrics);

        WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
        layoutParams.width = (int) (metrics.widthPixels * 0.8f);
        getWindow().setAttributes(layoutParams);

        super.show();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_feedback) {
            feedBack();
            dismiss();
        }
    }

    private void feedBack() {
        Context context = getContext();
        String text = context.getString(R.string.phone_info,
                Utils.getVersionName(context),
                Utils.getDeviceName(),
                Build.VERSION.RELEASE);

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"chenjishi313@gmail.com"});
        intent.putExtra(Intent.EXTRA_SUBJECT, text);

        context.startActivity(Intent.createChooser(intent, context.getString(R.string.email_choose)));
    }
}
