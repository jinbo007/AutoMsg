package com.jinbo.sophix.testmirror.service;

/**
 * Created by houjinbo on 2017/10/11.
 */

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import com.jinbo.sophix.testmirror.wechat.WechatMsgHelper;

import java.util.ArrayList;


/**
 * Created by champion on 17/4/23.
 * Sms短信数据库观察者
 */

public class SmsObserver extends ContentObserver {

    private final static String TAG = "SmsObserver";

    public static final Uri MMSSMS_ALL_MESSAGE_URI = Uri.parse("content://sms/inbox");

    public static final Uri MMSSMS_ALL = Uri.parse("content://sms");


    private static final String DB_FIELD_ID = "_id";
    private static final String DB_FIELD_ADDRESS = "address";
    private static final String DB_FIELD_BODY = "body";
    private static final String DB_FIELD_DATE = "date";

    private static final String DB_SELECTION_WHERE = "  read = 0 ";

    private static final String SORT_FIELD_STRING = "date desc";  // 排序

    public static final String[] ALL_DB_FIELD_NAME = {
            DB_FIELD_ID, DB_FIELD_ADDRESS, DB_FIELD_BODY,
            DB_FIELD_DATE,};


    private ContentResolver mResolver;

    ArrayList<Integer> ids = new ArrayList<>();
    private int MAX_IDS = 10;


    /**
     * Creates a content observer.
     *
     * @param handler The handler to run {@link #onChange} on, or null if none.
     */
    public SmsObserver(Context context, Handler handler) {
        super(handler);
        this.mResolver = context.getContentResolver();
    }

    @Override
    public void onChange(boolean selfChange) {
        Log.v(TAG, "短信内容有改变");
        getSmsFromPhone();
    }


    /**
     * 获取短信内容2
     */
    private void getSmsFromPhone() {
        Cursor cursor = null;
        try {
            cursor = mResolver.query(MMSSMS_ALL_MESSAGE_URI, ALL_DB_FIELD_NAME, DB_SELECTION_WHERE, null, SORT_FIELD_STRING);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    int id = cursor.getInt(cursor.getColumnIndex(DB_FIELD_ID));
                    long date = cursor.getLong(cursor.getColumnIndex(DB_FIELD_DATE));
                    String body = cursor.getString(cursor.getColumnIndex(DB_FIELD_BODY));
                    String address = cursor.getString(cursor.getColumnIndex(DB_FIELD_ADDRESS));
                    //解决重复收到 onChange导致重复发送问题
                    if (!isContain(id)) {
//                        String content = WechatMsgHelper.getMsgInfo(mResolver, date, address, body);
//                        sendToWechat(content);
//                        add(id);
                    }
                }
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "读取短信交易错误");
            if (cursor != null) {
                cursor.close();
            }
        }

    }

    /**
     * 将此内容发送给微信
     *
     * @param content
     */
    private void sendToWechat(String content) {

        if (!TextUtils.isEmpty(content)) {
            Log.d(TAG, "" + content);
            WechatMsgHelper.sendWechatMsg(content);
        }

    }

    /**
     * 将收到的短信添加到已发送的id列表
     *
     * @param id
     */
    private void add(int id) {
        if (ids == null) {
            ids = new ArrayList<>();
        }
        while (ids.size() >= MAX_IDS) {
            ids.remove(0);
        }

        ids.add(id);
    }

    /**
     * 是否已经发送
     *
     * @param id
     * @return
     */
    private boolean isContain(int id) {
        boolean isContain = false;
        if (ids == null) {
            return isContain;
        }

        return ids.contains(id);

    }


}
