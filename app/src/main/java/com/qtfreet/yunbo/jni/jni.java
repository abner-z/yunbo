package com.qtfreet.yunbo.jni;

/**
 * Created by qtfreet on 2016/4/7.
 */
public class jni {
    static {
        System.loadLibrary("qtfreet");
    }

    public native String API();

}
