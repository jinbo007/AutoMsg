package com.jinbo.sophix.testmirror.service;

import android.accessibilityservice.AccessibilityService;
import android.app.KeyguardManager;
import android.app.Service;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.jinbo.sophix.testmirror.wechat.WechatMsgHelper;
import com.jinbo.sophix.testmirror.wechat.WechatSendEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.List;

/**
 * Created by houjinbo on 2017/10/20.
 * 电话短信自动提醒服务
 */

public class PhoneService extends AccessibilityService {

    /**
     * 策信主界面
     */
    public static final String MAIN_ACT = "com.tencent.mm.ui.LauncherUI";
    /**
     * 搜索界面
     */
    public static final String SEARCH_ACT = "com.tencent.mm.plugin.search.ui.FTSMainUI";

    /**
     * 聊天界面
     */
    public static final String CHATTING_ACT = "com.tencent.mm.ui.chatting";


    //主界面的搜索按鈕
    private static final String MAIN_SEARCH_BTN_CLASSNAME_ = "android.widget.TextView";//搜索按钮是一个textview
    private static final String MAIN_SEARCH_BTN_ID_ = "";//ActionBar的按鈕查不到ID
    private static final String MAIN_SEARCH_BTN_DESC = "搜索";//描述

    //搜索界面的搜索框
    private static final String SEARCH_ACT_SEARCH_DESC = "搜索";//描述
    private static final String SEARCH_ACT_SEARCH_CLASSNAME = "android.widget.EditText";//控件类型
    private static final String SEARCH_ACT_SEARCH_ID = "com.tencent.mm:id/hb";//控件的id

    //聊天界面的输入框
    private static final String CHAT_EDITTEXTT_CLASSNAME = "android.widget.EditText";
    private static final String CHAT_EDITTEXT_ID = "com.tencent.mm:id/a71";
    private static final String CHAT_EDITTEXT_DESC = "";

    //切換到文本輸入的按钮
    private static final String CHANGE_TO_TXT_CLASSNAME = "android.widget.ImageButton";
    private static final String CHANGE_TO_TXT_ID = "com.tencent.mm:id/a6z";
    private static final String CHANGE_TO_TXT_DESC = "切换到键盘";

    //发送按钮
    private static final String SEND_BUTTON_CLASSNAME = "android.widget.Button";
    private static final String SEND_BUTTON_ID = "com.tencent.mm:id/a77";
    private static final String SEND_BUTTON_DESC = "";//空
    private static final String SEND_BUTTON_CONTENT = "发送";//空


    private String msg = "";//需要发送的消息
    private String FRIEND_NICKNAME = "金博";

    //监听短信内容的变化
    private SmsObserver mObserver;


    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {


        String uiName = event.getClassName().toString();
        switch (event.getEventType()) {

            //到了新的界面
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                //如果没有需要发送的信息，则不再进行相应的处理
                if (TextUtils.isEmpty(msg)) {
                    return;
                }
                sleep();
                //如果是主界面，找到搜索按钮，并点击
                if (MAIN_ACT.equals(uiName)) {
                    findSerarchBtnAndClick();
                }
                //搜索界面,找到搜索框，输入要发送的好友名称，并点击，如果未找到指定好友，则退出。
                else if (SEARCH_ACT.equals(uiName)) {
                    inputFriendName(FRIEND_NICKNAME);
                    sleep();
                    openFirstResult();
                }
                //聊天界面，找到聊天文本并写入要发送的内容，并点击，如果没有文本说明在语音模式，点击切换到键盘按钮
                else if (uiName.trim().contains(CHATTING_ACT)) {
                    inputChatContentAndSend();
                }
                //微信的其他界面，不在以上3个界面，而且在微信的app之内，说明是处理未知的界面，直接返回
                else {
                    backPress();
                }
                break;

//            //当前窗口的内容有改变,用于处理将语音模式转换为文本模式的操作
//            case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED:
//                //聊天界面，找到聊天文本并写入要发送的内容，并点击，如果没有文本说明在语音模式，点击切换到键盘按钮
//                if (uiName.trim().contains(CHATTING_ACT)) {
//                    inputChatContentAndSend();
//                }
//
//                //搜索界面,如果查到指定搜索的内容，就点击
//                if (SEARCH_ACT.equals(uiName)) {
//                    openFirstResult();
//                }
//
//                break;
        }


    }


    @Override
    public void onCreate() {
        super.onCreate();

        EventBus.getDefault().register(this);

        registPhone();

//        retistSms();


    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);

//        getContentResolver().unregisterContentObserver(mObserver);
    }

    /**
     * 延时操作
     */
    private void sleep() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 1.找到输入框，如果找到，则点击 切换到输入框按钮
     * 2.如果找到，补充内容
     * 3.发送
     */
    private void inputChatContentAndSend() {

        AccessibilityNodeInfo editNode = findWidgetWithDesc(getRootInActiveWindow(), CHAT_EDITTEXTT_CLASSNAME, null);

        if (editNode == null) {
            sleep();
            AccessibilityNodeInfo node = findWidgetWithDesc(getRootInActiveWindow(), CHANGE_TO_TXT_CLASSNAME, CHANGE_TO_TXT_DESC);
            if (node != null) {
                node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                sleep();
                editNode = findWidgetWithDesc(getRootInActiveWindow(), CHAT_EDITTEXTT_CLASSNAME, null);
            }
        }
        //如果找到，发送内容
        if (editNode != null) {
            //填充內容
            setText(editNode, msg);

            //找到按钮并发送
            List<AccessibilityNodeInfo> nodes = getRootInActiveWindow().findAccessibilityNodeInfosByText(SEND_BUTTON_CONTENT);
            for (AccessibilityNodeInfo node : nodes) {
                if (node == null) {
                    continue;
                }
                if (SEND_BUTTON_CLASSNAME.equals(node.getClassName())) {
                    boolean success = node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    if (success) {
                        backPress();
                        sleep();
                        backPress();
                        sleep();
                        gotoHome();
                        msg = "";
                    }
                }
            }

        }

    }

    /**
     * 找到指定的好友并点击,如果找不到就退出
     */
    private void inputFriendName(String friendName) {

        if (TextUtils.isEmpty(friendName)) {
            return;
        }
        AccessibilityNodeInfo searEditText = findWidgetWithDesc(getRootInActiveWindow(), SEARCH_ACT_SEARCH_CLASSNAME, "");
        setText(searEditText, friendName);
    }


    /**
     * 主界面找到搜索按钮，并点击
     */
    private void findSerarchBtnAndClick() {

        AccessibilityNodeInfo searchNode = findWidgetWithDesc(getRootInActiveWindow(), "", MAIN_SEARCH_BTN_DESC);
        if (searchNode != null) {
            searchNode.performAction(AccessibilityNodeInfo.ACTION_CLICK);
        }

    }


    /**
     * 打开第一个搜索的结果
     */
    private void openFirstResult() {
        AccessibilityNodeInfo rootNode = getRootInActiveWindow();
        AccessibilityNodeInfo info = findWidgetWithDesc(rootNode, "android.widget.ListView", null);
        if (info == null) {
            return;
        }
        if (info.getChildCount() < 2) {
            return;
        }
        info = info.getChild(1);
        info.performAction(AccessibilityNodeInfo.ACTION_CLICK);
        return;
    }

    /**
     * 通过描述找到一个控件
     *
     * @param desc
     * @return
     */
    private AccessibilityNodeInfo findWidgetWithDesc(AccessibilityNodeInfo node, String classname, String desc) {

        if (node == null) {
            return null;
        }
        if (TextUtils.isEmpty(classname) && TextUtils.isEmpty(desc)) {
            return null;
        }
        int childCount = node.getChildCount();
        for (int i = 0; i < childCount; i++) {
            AccessibilityNodeInfo child = node.getChild(i);
            if (child == null) {
                continue;
            }
            boolean isDescMatch = true;
            if (!TextUtils.isEmpty(desc)) {
                isDescMatch = desc.equals(child.getContentDescription());
            }
            boolean isClassNameMatch = true;
            if (!TextUtils.isEmpty(classname)) {
                isClassNameMatch = classname.equals(child.getClassName());
            }
            if ((isDescMatch && isClassNameMatch)) {
                return child;
            }
            //递归查询
            AccessibilityNodeInfo childNote = findWidgetWithDesc(child, classname, desc);
            if (childNote != null) {
                return childNote;
            }

        }
        return null;
    }

    /**
     * 设置文本
     */
    private void setText(AccessibilityNodeInfo node, String content) {

        if (node == null || content == null) {
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Bundle args = new Bundle();
            args.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
                    content);
            node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args);
        } else {

            ClipData data = ClipData.newPlainText("content", content);
            ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            clipboardManager.setPrimaryClip(data);
            node.performAction(AccessibilityNodeInfo.ACTION_FOCUS); // 获取焦点
            node.performAction(AccessibilityNodeInfo.ACTION_PASTE); // 执行粘贴
        }
    }

    /**
     * 通过获取id的方法找到控件
     *
     * @param node
     * @return
     */
    private AccessibilityNodeInfo findWidgetWithId(AccessibilityNodeInfo node, String className, String id) {
        if (node == null) {
            return null;
        }
        if (TextUtils.isEmpty(className) && TextUtils.isEmpty(id)) {
            return null;
        }
        int childCount = node.getChildCount();
        for (int i = 0; i < childCount; i++) {
            AccessibilityNodeInfo child = node.getChild(i);
            if (child == null) {
                continue;
            }
            //如果一个为空不检查规则
            boolean isClassNameMatch = true;
            if (!TextUtils.isEmpty(className)) {
                isClassNameMatch = className.equals(child.getClassName().toString());
            }
            boolean isIdMatch = true;
            if (!TextUtils.isEmpty(id)) {
                isIdMatch = id.equals(child.getViewIdResourceName());
            }
            if ((isIdMatch && isClassNameMatch)) {
                return child;
            }
            //递归查询
            AccessibilityNodeInfo childNode = findWidgetWithId(child, className, id);
            if (childNode != null) {
                return childNode;
            }

        }
        return null;
    }

    /**
     * 监听到需要onEvent过来发送信息
     *
     * @param event
     */
    @Subscribe
    public void onSendEvent(WechatSendEvent event) {
        if (event != null) {
            readyTosendmsg(event.getContent());
        }

    }

    /**
     * 检查锁屏状态
     * 1.如果锁屏先解锁，再发送信息
     * 2.如果未锁屏，直接发送信息
     *
     * @param msg
     */
    private void readyTosendmsg(final String msg) {
        this.msg = msg;
        if (isScreenLocked()) {
            locked = true;
            sleep();
            wakeAndUnlock();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    sendMsgToWechat(msg);
                }
            }, 1000);
        } else {
            sendMsgToWechat(msg);
        }

    }

    /**
     * 发送微信
     */
    private void sendMsgToWechat(String content) {
        msg = content;
        sleep();
        if (!TextUtils.isEmpty(content)) {
            startWeixin();
        }
    }


    /**
     * 开启微信
     */
    private void startWeixin() {
        if (!WechatMsgHelper.isWeixinAvilible(this)) {
            return;
        }
        Intent intent = new Intent();
        ComponentName cmp = new ComponentName("com.tencent.mm", "com.tencent.mm.ui.LauncherUI");
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setComponent(cmp);
        startActivity(intent);

    }


    boolean locked = false;
    KeyguardManager.KeyguardLock kl;

    /**
     * 解锁
     */
    private void wakeAndUnlock() {
//        //获取电源管理器对象
//        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
//        //获取PowerManager.WakeLock对象，后面的参数|表示同时传入两个值，最后的是调试用的Tag
//        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "bright");
//        //点亮屏幕
//        wl.acquire(1000);
//        //得到键盘锁管理器对象
//        KeyguardManager km = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
//        kl = km.newKeyguardLock("unLock");
//        //解锁
//        kl.disableKeyguard();

        KeyguardManager km = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        KeyguardManager.KeyguardLock kl = km.newKeyguardLock("unLock");
        //解锁
        kl.disableKeyguard();
        //获取电源管理器对象
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        //获取PowerManager.WakeLock对象,后面的参数|表示同时传入两个值,最后的是LogCat里用的Tag
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.SCREEN_DIM_WAKE_LOCK, "bright");
        //点亮屏幕
        wl.acquire();
        //释放
        wl.release();
    }


    /**
     * 系统是否在锁屏状态
     *
     * @return
     */
    private boolean isScreenLocked() {
        KeyguardManager keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        return keyguardManager.inKeyguardRestrictedInputMode();
    }

    /**
     * 返回到上一个界面
     */
    private void backPress() {

        performGlobalAction(GLOBAL_ACTION_BACK);
    }

    /**
     * 注册监听
     */
    public void retistSms() {
        ContentResolver resolver = getContentResolver();
        mObserver = new SmsObserver(this, new Handler());
        resolver.registerContentObserver(SmsObserver.MMSSMS_ALL, true, mObserver);

    }

    /**
     * 注册来电监听
     */
    private void registPhone() {
        TelephonyManager tm = (TelephonyManager) getSystemService(
                Service.TELEPHONY_SERVICE);
        tm.listen(listener, PhoneStateListener.LISTEN_CALL_STATE);
    }

    boolean isnewIncoming = false;//是否有未接电话；

    PhoneStateListener listener = new PhoneStateListener() {
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            switch (state) {
                // 电话铃响
                case TelephonyManager.CALL_STATE_RINGING:
//                    msg = "亲，您在" + DateUtils.getTimeNow() + "收到了来自 (" + ContactUtil.getContactNameFromPhone(getContentResolver(), incomingNumber) + ") 的电话，来电号码是：" + incomingNumber;
                    msg = WechatMsgHelper.getPhoneInfo(getContentResolver(), incomingNumber);
                    isnewIncoming = true;
                    break;
                //电话挂断了
                case TelephonyManager.CALL_STATE_IDLE:
                    if (isnewIncoming) {
                        WechatMsgHelper.sendWechatMsg(msg);
                        isnewIncoming = false;
                    }
                    break;
                //电话接通了.如果电话被接通了，就不用再发送微信消息了。
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    isnewIncoming = false;
                    break;
            }
        }
    };


    /**
     * 返回到主界面
     */
    private void gotoHome() {

        performGlobalAction(GLOBAL_ACTION_HOME);

    }

    @Override
    public void onInterrupt() {

    }
}
