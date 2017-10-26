package com.jinbo.sophix.testmirror;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.jinbo.sophix.testmirror.service.PhoneService;
import com.jinbo.sophix.testmirror.wechat.WechatMsgHelper;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.btn_send_test).setOnClickListener(this);
        startService(new Intent(this, PhoneService.class));

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_send_test:
                if (isAccessibilitySettingsOn(this)) {
                    startTest();
                } else {
                    Toast.makeText(this, R.string.please_open_phone_service, Toast.LENGTH_SHORT).show();
                    gotoAccessibilitySetting();
                }
                break;
        }
    }

    /**
     * 发送测试信息
     */
    private void startTest() {

        WechatMsgHelper.sendWechatMsg("这是一个自动发送的消息");

    }


    /**
     * 是否开启了辅助功能
     *
     * @param mContext
     * @return
     */
    private boolean isAccessibilitySettingsOn(Context mContext) {
        if (mContext == null) {
            return false;
        }
        int accessibilityEnabled = 0;
        final String service = mContext.getPackageName() + "/" + PhoneService.class.getCanonicalName();
        try {
            accessibilityEnabled = Settings.Secure.getInt(mContext.getApplicationContext().getContentResolver(), Settings.Secure.ACCESSIBILITY_ENABLED);
        } catch (Exception e) {
            return false;
        }

        TextUtils.SimpleStringSplitter mStringColonSplitter = new TextUtils.SimpleStringSplitter(':');
        if (accessibilityEnabled == 1) {
            String settingValue = Settings.Secure.getString(mContext.getApplicationContext().getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (settingValue != null) {
                mStringColonSplitter.setString(settingValue);
                while ((mStringColonSplitter.hasNext())) {
                    String accessibiibiyService = mStringColonSplitter.next();
                    if (accessibiibiyService.equalsIgnoreCase(service)) {
                        return true;
                    }
                }
            }

        }

        return false;

    }

    /**
     * 跳转开启辅助功能界面
     */
    private void gotoAccessibilitySetting() {
        final String mAction = Settings.ACTION_ACCESSIBILITY_SETTINGS;
        Intent intent = new Intent(mAction);
        startActivity(intent);
    }
}
