package com.jinbo.sophix.testmirror.wechat;

/**
 * Created by houjinbo on 2017/10/12.
 *
 * 微信发送事件
 */

public class WechatSendEvent {

    private String content;
    public WechatSendEvent(String content){
        this.content = content;

    }

    public String getContent() {
        return content;
    }

}
