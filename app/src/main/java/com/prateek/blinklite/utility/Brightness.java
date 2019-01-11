package com.prateek.blinklite.utility;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

/**
 * Created by Vault on 15-Oct-17.
 */

public class Brightness {
    public static void setBrightness(int brightness, Context context){

        //constrain the value of brightness
        if(brightness < 0)
            brightness = 0;
        else if(brightness > 255)
            brightness = 255;


        ContentResolver cResolver = context.getContentResolver();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Settings.System.canWrite(context)){
                Settings.System.putInt(cResolver, Settings.System.SCREEN_BRIGHTNESS, brightness);
            }else

            {
                Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_WRITE_SETTINGS);
                intent.setData(Uri.parse("package:" + context.getPackageName()));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        }


    }
}
