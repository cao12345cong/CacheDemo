package com.example.cachedemo;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.example.cachedemo.cache.CacheHelperImpl;
import com.example.cachedemo.cache.ICacheHelper;
import com.example.cachedemo.cache.disc.impl.LruDiskCache;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by caocong on 3/3/17.
 */

public class CacheDemoActivity extends Activity {
    private static final String TAG = "congcao";
    ICacheHelper cacheHelper;
    Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cache);
        bitmap = BitmapFactory.decodeResource(getResources(),
                R.drawable.uc_bg_user_center_unlogin_diamond);

        cacheHelper = new CacheHelperImpl(this, bitmap.getByteCount() * 5);
        //cacheHelper = new SimpleLruDiskCache(getCacheDir(), bitmap.getByteCount() * 5);
    }


    public void doClick(View v) {
        if (cacheHelper == null) {
            Log.d(TAG, "cachehelper is null");
            return;
        }
        boolean result = false;
        switch (v.getId()) {
            case R.id.btn_save_bitmap:
                result = cacheHelper.put("bitmap", BitmapFactory.decodeResource(getResources(),
                        R.drawable.uc_bg_user_center_unlogin_diamond));
                Log.d(TAG, "save bitmap result is:" + result);
                break;
            case R.id.btn_save_string:
                result = cacheHelper.put("string",
                        "2017年3月1日，遵义市红花岗区一名7岁小学生小况，在回家途中被火车压断双脚。事件回顾3月2"
                                + "日凌晨，经手术后，小况转入医院重症监护室。据医生介绍，目前孩子生命体征平稳，但他的双脚因受损严重，已无法接上。中金网3月3日"
                                + " ");
                Log.d(TAG, "save string result is:" + result);
                break;
            case R.id.btn_save_object:
                MemberAction action = new MemberAction();
                action.setBitmapUrl("http://www.baidu.com");
                action.setContent("content");
                action.setDescription("desciption");
                action.setType(2);
                action.setBitmap(BitmapFactory.decodeResource(getResources(),
                        R.drawable.uc_bg_user_center_unlogin_diamond));
                result = cacheHelper.put("object", action);
                Log.d(TAG, "save string result is:" + result);
                break;
            case R.id.btn_save_byte:
                byte[] bytes = new byte[2];
                bytes[0] = 1;
                bytes[1] = 2;
                result = cacheHelper.put("byte", bytes);
                Log.d(TAG, "save byte result is:" + result);
                break;
            case R.id.btn_get_bitmap:
                Bitmap bitmap = cacheHelper.getBitmap("bitmap");
                if (bitmap == null) {
                    Log.d(TAG, "get bitmap is null");
                } else {
                    ImageView imageView = (ImageView) findViewById(R.id.image_view);
                    imageView.setImageBitmap(bitmap);
                }
                break;
            case R.id.btn_get_string:
                Log.d(TAG, "get cache string is:" + cacheHelper.getString("string"));
                break;
            case R.id.btn_get_object:
                Log.d(TAG, "get object is:" + cacheHelper.getSerializable("object"));
                break;
            case R.id.btn_get_bytes:
                byte[] byteArr = cacheHelper.getByteArray("byte");
                if (byteArr == null) {
                    Log.d(TAG, "get byte array is null");
                } else {
                    for (int i = 0; i < byteArr.length; i++) {
                        Log.d(TAG, "get byte array is:" + byteArr[i] + ";");
                    }
                }

                break;
            case R.id.btn_expire_bitmap:
                boolean isExpired = cacheHelper.isExpired("bitmap",
                        new ICacheHelper.IExpiredStrategy() {
                            @Override
                            public boolean isExpired(long lastSaveTimeMills) {
                                long currentTime = System.currentTimeMillis();
                                Log.d(TAG, "last time is:" + lastSaveTimeMills + ";current time is:"
                                        + currentTime);
                                return currentTime - lastSaveTimeMills >= 60 * 60 * 1000 * 24;
                            }
                        });
                Log.d(TAG, "is Expired is:" + isExpired);
                if (isExpired) {
                    cacheHelper.remove("bitmap");
                }

                break;
            case R.id.btn_expire_string:
                boolean isExpired2 = cacheHelper.isExpired("string",
                        new ICacheHelper.IExpiredStrategy() {
                            @Override
                            public boolean isExpired(long lastSaveTimeMills) {
                                long currentTime = System.currentTimeMillis();
                                Log.d(TAG, "last time is:" + lastSaveTimeMills + ";current time is:"
                                        + currentTime);
                                if (currentTime < lastSaveTimeMills) {
                                    return true;
                                }
                                Calendar cal1 = Calendar.getInstance();
                                cal1.setTime(new Date(currentTime));

                                Calendar cal2 = Calendar.getInstance();
                                cal2.setTime(new Date(lastSaveTimeMills));

                                if ((cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR))
                                        && cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH)
                                        && cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(
                                        Calendar.DAY_OF_MONTH)) {
                                    return false;
                                }
                                return true;
                            }
                        });
                Log.d(TAG, "is Expired is:" + isExpired2);
                if (isExpired2) {
                    cacheHelper.remove("string");
                }
                break;
            case R.id.btn_expire_object:
                boolean isExpired3 = cacheHelper.isExpired("object",
                        new ICacheHelper.IExpiredStrategy() {
                            @Override
                            public boolean isExpired(long lastSaveTimeMills) {
                                long currentTime = System.currentTimeMillis();
                                Log.d(TAG, "last time is:" + lastSaveTimeMills + ";current time is:"
                                        + currentTime);
                                if (currentTime < lastSaveTimeMills) {
                                    return true;
                                }
                                Calendar cal1 = Calendar.getInstance();
                                cal1.setTime(new Date(currentTime));

                                Calendar cal2 = Calendar.getInstance();
                                cal2.setTime(new Date(lastSaveTimeMills));

                                if ((cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR))
                                        && cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH)
                                        && cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(
                                        Calendar.DAY_OF_MONTH)) {
                                    return false;
                                }
                                return true;
                            }
                        });
                Log.d(TAG, "is Expired is:" + isExpired3);
                if (isExpired3) {
                    cacheHelper.remove("object");
                }
                break;
            case R.id.btn_expire_byte:
                boolean isExpired4 = cacheHelper.isExpired("byte",
                        new ICacheHelper.IExpiredStrategy() {
                            @Override
                            public boolean isExpired(long lastSaveTimeMills) {
                                long currentTime = System.currentTimeMillis();
                                Log.d(TAG, "last time is:" + lastSaveTimeMills + ";current time is:"
                                        + currentTime);
                                if (currentTime < lastSaveTimeMills) {
                                    return true;
                                }
                                Calendar cal1 = Calendar.getInstance();
                                cal1.setTime(new Date(currentTime));

                                Calendar cal2 = Calendar.getInstance();
                                cal2.setTime(new Date(lastSaveTimeMills));

                                if ((cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR))
                                        && cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH)
                                        && cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(
                                        Calendar.DAY_OF_MONTH)) {
                                    return false;
                                }
                                return true;
                            }
                        });
                Log.d(TAG, "is Expired is:" + isExpired4);
                if (isExpired4) {
                    cacheHelper.remove("byte");
                }
                break;
            case R.id.btn_test_lru:
                testLru();
                break;
            case R.id.btn_test_lru_2:
                testLru2();
                break;

            default:
                break;
        }
    }

    private void testLru() {
        new Thread() {
            @Override
            public void run() {
                cacheHelper.clear();
                for (int i = 0; i < 15; i++) {
                    boolean success = cacheHelper.put("bitmap" + i,
                            BitmapFactory.decodeResource(getResources(),
                                    R.drawable.uc_bg_user_center_unlogin_diamond));
                    Log.d(TAG, "save bitmap key is:bitmap" + i + ";result is:" + success);
                }
                Log.d(TAG, "test lru add bitmap done");
            }
        }.start();
    }

    private void testLru2() {
        new Thread() {
            @Override
            public void run() {
                for (int i = 0; i < 14; i++) {
                    Bitmap bitmap = cacheHelper.getBitmap("bitmap" + i);
                    Log.d(TAG, "get bitmap key is:bitmap" + i + ";is null:" + (bitmap == null));
                }
            }
        }.start();
    }
}
