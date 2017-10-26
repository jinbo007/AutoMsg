package com.jinbo.sophix.testmirror.service;

/**
 * Created by houjinbo on 2017/10/11.
 * 暂时不用，可以借鉴多另外一种监听电话短信实现方法
 */

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.text.TextUtils;
import android.util.Log;

import com.jinbo.sophix.testmirror.wechat.WechatMsgHelper;

public class SmsMessageReceiver extends BroadcastReceiver {

    private static final String SMS_RECEIVED_ACTION = "android.provider.Telephony.SMS_RECEIVED";
    private static final String SMS_DELIVER_ACTION = "android.provider.Telephony.SMS_DELIVER";
    private static final String SMS_SIM_ACTION = "android.intent.action.SERVICE_STATE";
    private String TAG = "SmsMessageReceiver";

    ContentResolver resolver;

    @Override
    public void onReceive(Context context, Intent intent) {

        resolver = context.getContentResolver();
        switch (intent.getAction()) {
            case SMS_RECEIVED_ACTION:
            case SMS_DELIVER_ACTION:
            case SMS_SIM_ACTION:

                handleMsg(context,intent);

                break;
        }


    }

    String msg = "";

    /**
     * 处理短信息
     *
     * @param intent
     */
    private void handleMsg(Context context,Intent intent) {

        log("开始接收短信.....");
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            Object[] pdus = (Object[]) bundle.get("pdus");
            if (pdus == null) {
                return;
            }
            SmsMessage[] messages = new SmsMessage[pdus.length];
            for (int i = 0; i < pdus.length; i++) {
                messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
            }

            for (SmsMessage message : messages) {
                String msg = message.getMessageBody();
                long when = message.getTimestampMillis();
                String from = message.getOriginatingAddress();

                String content = WechatMsgHelper.getMsgInfo(context, when, from, msg);
                WechatMsgHelper.sendWechatMsg(content);
                Log.d(TAG, content);
            }
        }
    }


    private void log(String msg) {
        if (!TextUtils.isEmpty(msg)) {
            Log.v(TAG, msg);
        }
    }


}