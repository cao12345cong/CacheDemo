package com.example.cachedemo.cache.memory;

import android.graphics.Bitmap;

import com.example.cachedemo.cache.ICacheHelper;

import java.io.Serializable;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by caocong on 3/9/17.
 */

public class WeakMemoryCache implements ICacheHelper {
    private final Map<String, Reference<byte[]>> mByteMap;

    public WeakMemoryCache() {
        mByteMap = new ConcurrentHashMap<>();
    }

    @Override
    public boolean put(String key, String value) {
        return put(key, value.getBytes(Charset.defaultCharset()));
    }

    @Override
    public boolean put(String key, byte[] bytes) {
        this.mByteMap.put(key, createReference(bytes));
        return true;
    }

    @Override
    public boolean put(String key, Serializable value) {
        SerializableByteMapper mapper = new SerializableByteMapper();
        return put(key, mapper.getBytes(value));
    }

    @Override
    public boolean put(String key, Bitmap value) {
        BitmapByteMapper mapper = new BitmapByteMapper();
        return put(key, mapper.getBytes(value));
    }

    @Override
    public String getString(String key) {
        checkNotNull(key);
        Reference<byte[]> reference = mByteMap.get(key);
        if (reference == null) {
            mByteMap.remove(key);
            return null;
        }
        return new String(reference.get(), Charset.defaultCharset());
    }

    @Override
    public byte[] getByteArray(String key) {
        checkNotNull(key);
        Reference<byte[]> reference = mByteMap.get(key);
        if (reference == null) {
            mByteMap.remove(key);
            return null;
        }
        return reference.get();
    }

    @Override
    public Serializable getSerializable(String key) {
        checkNotNull(key);
        Reference<byte[]> reference = mByteMap.get(key);
        if (reference == null) {
            mByteMap.remove(key);
            return null;
        }
        return new SerializableByteMapper().getObject(reference.get());
    }

    @Override
    public Bitmap getBitmap(String key) {
        checkNotNull(key);
        Reference<byte[]> reference = mByteMap.get(key);
        if (reference == null) {
            mByteMap.remove(key);
            return null;
        }
        return new BitmapByteMapper().getObject(reference.get());
    }

    @Override
    public boolean remove(String key) {
        checkNotNull(key);
        mByteMap.remove(key);
        return true;
    }

    @Override
    public boolean isExpired(String key, IExpiredStrategy expiredStrategy) {
        return false;
    }

    @Override
    public void clear() {
        this.mByteMap.clear();
    }

    private void checkNotNull(Object object) {
        if (object == null) {
            throw new NullPointerException("object is null");
        }
    }

    private Reference<byte[]> createReference(byte[] bytes) {
        return new WeakReference<>(bytes);
    }
}
