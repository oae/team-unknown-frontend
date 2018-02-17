package com.teamunknown.paranbende.util;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.teamunknown.paranbende.controller.BaseApplication;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by halitogunc on 17.02.2018.
 */

public class PreferencesPB {
    private static final String TAG=PreferencesPB.class.getSimpleName();

    public static void setValue(String key, String value) {
        try {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(BaseApplication.getAppContext());
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(key, value);
            editor.commit();
            Log.d(TAG," setValue(String key, String value) - Newly Setted Key/Value : " + key + "/" + value);
        }catch (Exception e) {
            Log.e(TAG," setValue(String key, String value) : " + e.toString());
        }
    }

    public static void setValue(String key, Set<String> value) {
        try {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(BaseApplication.getAppContext());
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putStringSet(key, value);
            editor.commit();
            Log.d(TAG," setValue(String key, HashSet<String> value) - Newly Setted Key/Value : " + key + "/" + value);
        } catch (Exception e) {
            Log.e(TAG," setValue(String key, HashSet<String> value) : " + e.toString());
        }
    }

    public static void setValue(String key, int value) {
        try{
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(BaseApplication.getAppContext());
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt(key, value);
            editor.commit();
            Log.d(TAG," setValue(String key, int value) - Newly Setted Key/Value : " + key + "/" + value);
        }catch(Exception e){
            Log.e(TAG," setValue(String key, int value) : " + e.toString());
        }
    }

    public static String getValue(String key) {
        SharedPreferences mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(BaseApplication.getAppContext());
        SharedPreferences.Editor mPrefsEditor = mSharedPrefs.edit();
        String value = mSharedPrefs.getString(key, "");
        mPrefsEditor.commit();
        Log.d(TAG,"String getValue(String key) - Getted Key/Value : " + key + "/" + value);
        return value;
    }

    public static Set<String> getListValue(String key) {
        SharedPreferences mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(BaseApplication.getAppContext());
        SharedPreferences.Editor mPrefsEditor = mSharedPrefs.edit();
        Set<String> value = mSharedPrefs.getStringSet(key, new HashSet<String>());
        mPrefsEditor.commit();
        Log.d(TAG,"HashSet<String> getValue(String key) - Getted Key/Value : " + key + "/" + value);
        return value;
    }

    public static int getValueInt(String key) {
        int value = 0;
        try{
            SharedPreferences mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(BaseApplication.getAppContext());
            SharedPreferences.Editor mPrefsEditor = mSharedPrefs.edit();
            value = mSharedPrefs.getInt(key, 0);
            mPrefsEditor.commit();
            Log.d(TAG,"int getValue(String key) - Getted Key/Value : " + key + "/" + value);
        }catch(Exception e){
            Log.e(TAG,"int getValue(String key) : " + e.toString());
        }
        return value;
    }

    public static void removeValue(String key) {
        try{
            SharedPreferences mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(BaseApplication.getAppContext());
            mSharedPrefs.edit().remove(key).commit();
            Log.d(TAG,"removeValue(String key) - Removed Key : " + key);
        }catch(Exception e){
            Log.e(TAG,"removeValue() : " + e.toString());
        }
    }

    public static void setScreenWidthSize(int screenWidth) {
        try{
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(BaseApplication.getAppContext());
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt("screenWidth", screenWidth);
            editor.commit();
        }catch(Exception e){
            Log.e(TAG,"Preferences - setScreenWidthSize : " + e.toString());
        }
    }

    public static int getScreenWidthSize() {
        int screenWidth = 0;
        try {
            SharedPreferences mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(BaseApplication.getAppContext());
            SharedPreferences.Editor mEditor = mSharedPreferences.edit();
            screenWidth = mSharedPreferences.getInt("screenWidth",0);
            mEditor.commit();
        } catch (Exception e) {
            Log.e(TAG,"Preferences - getScreenWidthSize : " + e.toString());
        }
        return screenWidth;
    }

    public static boolean checkPreferencesWhetherTheValueisExistorNot(String value){
        try{
            SharedPreferences mSharedPreferences= PreferenceManager.getDefaultSharedPreferences(BaseApplication.getAppContext());
            if (mSharedPreferences.contains(value))
                return true;
            else
                return false;

        }
        catch (Exception e){
            return false;
        }

    }

}
