package com.example.cachedemo.cache;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;


import com.example.cachedemo.cache.ext.DiskLruCache;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;

/**
 * Created by caocong on 3/3/17.
 */

public class CustomBaseDiskCache implements ICacheHelper {
    private static final String TAG = CustomBaseDiskCache.class.getSimpleName();
    protected DiskLruCache cache;

    public CustomBaseDiskCache(File cacheDir, long cacheMaxSize) throws IOException {
        this(cacheDir, cacheMaxSize, 0);
    }

    public CustomBaseDiskCache(File cacheDir, long cacheMaxSize,
                               int cacheMaxFileCount) throws IOException {
        if (cacheMaxSize < 0) {
            throw new IllegalArgumentException("cacheMaxSize value set error");
        }
        if (cacheMaxFileCount < 0) {
            throw new IllegalArgumentException("cacheMaxFileCount value set error");
        }
        if (cacheMaxSize == 0) {
            cacheMaxSize = Long.MAX_VALUE;
        }
        if (cacheMaxFileCount == 0) {
            cacheMaxFileCount = Integer.MAX_VALUE;
        }

        initCache(cacheDir, cacheMaxSize, cacheMaxFileCount);
    }

    private void initCache(File cacheDir, long cacheMaxSize, int cacheMaxFileCount)
            throws IOException {
        try {
            cache = DiskLruCache.open(cacheDir, 1, 1, cacheMaxSize, cacheMaxFileCount);
            if (cache == null) {
                throw new RuntimeException("Can't initialize disk cache");
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, e.getMessage());
        }
    }


    private String generatorKey(String key) {
        return key;
    }


    @Override
    public boolean put(String key, String value) {
        try {
            return put(key, value.getBytes("utf-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean put(String key, byte[] data) {
        OutputStream os = null;
        boolean success = false;
        DiskLruCache.Editor editor = null;
        try {
            editor = getDiskLruCacheEditor(key);
            if (editor == null) {
                return false;
            }
            os = new BufferedOutputStream(editor.newOutputStream(0));
            os.write(data);
            os.flush();
            success = true;
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, e.getMessage());
        } finally {
            closeIO(os);
            commitDiskLruCacheEditor(success, editor);
        }
        return success;
    }

    @Override
    public boolean put(String key, Serializable value) {
        ObjectOutputStream os = null;
        boolean success = false;
        DiskLruCache.Editor editor = null;
        try {
            editor = getDiskLruCacheEditor(key);
            if (editor == null) {
                return false;
            }
            os = new ObjectOutputStream(editor.newOutputStream(0));
            os.writeObject(value);
            os.flush();
            success = true;
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, e.getMessage());
        } finally {
            closeIO(os);
            commitDiskLruCacheEditor(success, editor);
        }
        return success;
    }

    @Override
    public boolean put(String key, Bitmap bitmap) {
        return put(key, bitmap2Bytes(bitmap));
    }

    private byte[] bitmap2Bytes(Bitmap bm) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }

    private Bitmap bytes2Bitmap(byte[] b) {
        if (b != null && b.length != 0) {
            return BitmapFactory.decodeByteArray(b, 0, b.length);
        } else {
            return null;
        }
    }

    @Override
    public String getString(String key) {
        try {
            byte[] bytes = getByteArray(generatorKey(key));
            if (bytes == null || bytes.length == 0) {
                return "";
            }
            return new String(bytes, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            Log.e(TAG, e.getMessage());
            return "";
        }
    }

    @Override
    public byte[] getByteArray(String key) {
        BufferedInputStream bis = null;
        try {
            File cacheFile = getCacheFile(generatorKey(key));
            if (cacheFile == null || !cacheFile.exists()) {
                return null;
            }
            bis = new BufferedInputStream(new FileInputStream(cacheFile));
            byte[] byteArray = new byte[(int) cacheFile.length()];
            int result = bis.read(byteArray);
            Log.d(TAG, "get byte array result is:" + result);
            return (result == -1) ? null : byteArray;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            closeIO(bis);
        }
    }

    @Override
    public Object getObject(String key) {
        ObjectInputStream ois = null;
        try {
            File cacheFile = getCacheFile(generatorKey(key));
            if (cacheFile == null || !cacheFile.exists()) {
                return null;
            }
            ois = new ObjectInputStream(new FileInputStream(cacheFile));
            return ois.readObject();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            closeIO(ois);
        }
    }

    @Override
    public Bitmap getBitmap(String key) {
        byte[] bytes = getByteArray(generatorKey(key));
        return bytes2Bitmap(bytes);
    }

    @Override
    public boolean isExpired(String key, IExpiredStrategy expiredStrategy) {
        File cacheFile = getCacheFile(key);
        if (cacheFile == null) {
            return true;
        }
        boolean isExpired = expiredStrategy.isExpired(cacheFile.lastModified());
        Log.d(TAG, "get cache key is:" + key + "; is expired is:" + isExpired);
        return isExpired;
    }

    @SuppressWarnings("unused")
    private File getDirectory() {
        return cache.getDirectory();
    }

    private File getCacheFile(String key) {
        DiskLruCache.Snapshot snapshot = null;
        try {
            snapshot = cache.get(generatorKey(key));
            return snapshot == null ? null : snapshot.getFile(0);
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
            return null;
        } finally {
            if (snapshot != null) {
                snapshot.close();
            }
        }
    }


    private DiskLruCache.Editor getDiskLruCacheEditor(String key) {
        try {
            return cache.edit(generatorKey(key));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void commitDiskLruCacheEditor(boolean success, DiskLruCache.Editor editor) {
        if (editor != null) {
            try {
                if (success) {
                    editor.commit();
                } else {
                    editor.abort();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private void closeIO(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public boolean remove(String imageUri) {
        try {
            return cache.remove(generatorKey(imageUri));
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
            return false;
        }
    }

    @SuppressWarnings("unused")
    public void close() {
        try {
            cache.close();
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }
        cache = null;
    }

    @Override
    public void clear() {
        try {
            cache.delete();
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }
        try {
            initCache(cache.getDirectory(), cache.getMaxSize(), cache.getMaxFileCount());
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }
    }

}
