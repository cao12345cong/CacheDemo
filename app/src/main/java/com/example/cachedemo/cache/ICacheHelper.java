package com.example.cachedemo.cache;

import android.graphics.Bitmap;

import java.io.Serializable;

/**
 * Created by caocong on 1/5/17.
 */
public interface ICacheHelper {
    boolean put(String key, String value);

    boolean put(String key, byte[] value);

    boolean put(String key, Serializable value);

    boolean put(String key, Bitmap value);


    String getString(String key);

    byte[] getByteArray(String key);

    Object getObject(String key);

    Bitmap getBitmap(String key);


    boolean remove(String key);

    boolean isExpired(String key, IExpiredStrategy expiredStrategy);

    void clear();

    public interface IExpiredStrategy {

        boolean isExpired(long lastSaveTimeMills);
    }

}
