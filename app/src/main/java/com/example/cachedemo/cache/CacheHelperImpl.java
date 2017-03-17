package com.example.cachedemo.cache;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.example.cachedemo.cache.disc.impl.LruDiskCache;
import com.example.cachedemo.cache.memory.WeakMemoryCache;

import java.io.IOException;
import java.io.Serializable;

/**
 * Created by caocong on 3/14/17.
 */

public class CacheHelperImpl implements ICacheHelper {
    private static final String TAG = CacheHelperImpl.class.getSimpleName();
    private ICacheHelper mDiskCache;
    private ICacheHelper mMemoryCache;

    public CacheHelperImpl(Context context, long diskMaxSize) {
        try {
            mDiskCache = new LruDiskCache(context.getCacheDir(), diskMaxSize);
            mMemoryCache = new WeakMemoryCache();
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, e.getMessage());
        }
    }

    @Override
    public boolean put(String key, String value) {
        mDiskCache.put(key, value);
        mMemoryCache.put(key, value);
        return true;
    }

    @Override
    public boolean put(String key, byte[] value) {
        mDiskCache.put(key, value);
        mMemoryCache.put(key, value);
        return true;
    }

    @Override
    public boolean put(String key, Serializable value) {
        mDiskCache.put(key, value);
        mMemoryCache.put(key, value);
        return true;
    }

    @Override
    public boolean put(String key, Bitmap value) {
        mDiskCache.put(key, value);
        mMemoryCache.put(key, value);
        return true;
    }

    @Override
    public String getString(String key) {
        String value = mMemoryCache.getString(key);
        if (value != null) {
            return value;
        }
        value = mDiskCache.getString(key);
        if (value != null) {
            mMemoryCache.put(key, value);
        }
        return value;
    }

    @Override
    public byte[] getByteArray(String key) {
        byte[] value = mMemoryCache.getByteArray(key);
        if (value != null) {
            return value;
        }
        value = mDiskCache.getByteArray(key);
        if (value != null) {
            mMemoryCache.put(key, value);
        }
        return value;
    }

    @Override
    public Serializable getSerializable(String key) {
        Serializable value = mMemoryCache.getSerializable(key);
        if (value != null) {
            return value;
        }
        value = mDiskCache.getSerializable(key);
        if (value != null) {
            mMemoryCache.put(key, value);
        }
        return value;
    }

    @Override
    public Bitmap getBitmap(String key) {
        Bitmap value = mMemoryCache.getBitmap(key);
        if (value != null) {
            return value;
        }
        value = mDiskCache.getBitmap(key);
        if (value != null) {
            mMemoryCache.put(key, value);
        }
        return value;
    }

    @Override
    public boolean remove(String key) {
        return mMemoryCache.remove(key) && mDiskCache.remove(key);
    }

    @Override
    public boolean isExpired(String key, IExpiredStrategy expiredStrategy) {
        return mDiskCache.isExpired(key, expiredStrategy);
    }

    @Override
    public void clear() {
        mMemoryCache.clear();
        mDiskCache.clear();
    }
}
