package com.baviux.homeassistant.util;

import android.content.Context;

import org.apache.commons.io.IOUtils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class FileUtils {

    public static String getRawFileContents(Context context, int rawResId){
        String fileContents = null;

        InputStream is = context.getResources().openRawResource(rawResId);

        try {
            fileContents = IOUtils.toString(is);
        }catch(Exception e){
            e.printStackTrace();
        }
        finally {
            IOUtils.closeQuietly(is);
        }

        return fileContents;
    }

}