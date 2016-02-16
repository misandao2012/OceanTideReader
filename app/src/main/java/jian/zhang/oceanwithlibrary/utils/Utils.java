package jian.zhang.oceanwithlibrary.utils;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by jian on 12/15/2015.
 */
public class Utils {

    private static final String TAG = "OceanTide";

    //Change the time format
    public static String parseTideTime(String original) {
        SimpleDateFormat dateFormat;
        dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z", Locale.US);
        Date date = null;
        Calendar calendar = Calendar.getInstance();
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        try {
            date = dateFormat.parse(original);
        } catch (ParseException e) {
            Log.e(TAG, e.getMessage());
        }
        dateFormat = new SimpleDateFormat("MM/dd/yy, h:mm a", Locale.US);
        dateFormat.setTimeZone(calendar.getTimeZone());
        return dateFormat.format(date);
    }

    // Locks the device window in portrait mode
    public static void lockOrientationPortrait(Activity activity) {
        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    // Allows user to freely use portrait or landscape mode
    public static void unlockOrientation(Activity activity) {
        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
    }
}
