package com.jinbo.sophix.testmirror.wechat;

import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;

import com.jinbo.sophix.testmirror.utils.ContactUtil;
import com.jinbo.sophix.testmirror.utils.DateUtils;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

/**
 * Created by houjinbo on 2017/10/12.
 * 微信消息
 */

public class WechatMsgHelper {

    /**
     * 发起发送微信消息事件
     *
     * @param content
     */
    public static void sendWechatMsg(String content) {

        if (!TextUtils.isEmpty(content)) {
            EventBus.getDefault().post(new WechatSendEvent(content));
        }
    }

    /**
     * 是否安裝了微信
     *
     * @param context
     * @return
     */
    public static boolean isWeixinAvilible(Context context) {
        final PackageManager packageManager = context.getPackageManager();// 获取packagemanager
        List<PackageInfo> pinfo = packageManager.getInstalledPackages(0);// 获取所有已安装程序的包信息
        if (pinfo != null) {
            for (int i = 0; i < pinfo.size(); i++) {
                String pn = pinfo.get(i).packageName;
                if (pn.equals("com.tencent.mm")) {
                    return true;
                }
            }
        }
        return false;
    }


    /**
     * 拼装来电字符串
     *
     * @param resover
     * @param phoneNumber
     * @return
     */
    public static String getPhoneInfo(ContentResolver resover, String phoneNumber) {
        String msg = "";
        if (resover != null && !TextUtils.isEmpty(phoneNumber)) {

            msg = "亲，您在" + DateUtils.getTimeNow() + "收到了来自 (" + ContactUtil.getContactNameFromPhone(resover, phoneNumber) + ") 的电话，来电号码是：" + phoneNumber;
        }
        return msg;
    }


    /**
     * 拼装短信字符串
     *
     * @param context
     * @param phoneNumber
     * @param content
     * @return
     */
    public static String getMsgInfo(Context context ,long date, String phoneNumber, String content) {

        String msg = "";
        if (context != null && !TextUtils.isEmpty(phoneNumber)) {
            msg = "亲，您在" + DateUtils.getTime(date) + "收到了来自 （" + ContactUtil.getContactNameFromPhone(context.getContentResolver(), phoneNumber) + "） 号码为:" + phoneNumber + " 的短信:\n" + content;
        }
        return msg;


    }


}
