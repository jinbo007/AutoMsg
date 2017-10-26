package com.jinbo.sophix.testmirror.utils;

import android.content.ContentResolver;
import android.database.Cursor;
import android.provider.ContactsContract;


/**
 * ContactUtil
 */
public class ContactUtil {


    /**
     * 获取指定手机号的联系人名称
     *
     * @return
     * @throws Exception
     */
    public static String getContactNameFromPhone(ContentResolver contentResolver, String phone) {
        String displayName = "未知姓名";
        Cursor cursor;
        cursor = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.NUMBER + " = ?",
                new String[]{phone}, null);
        while (cursor.moveToNext()) {
            displayName = cursor.getString(cursor.getColumnIndex(
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
        }

        if (cursor != null) {
            cursor.close();
        }

        return displayName;
    }

}
