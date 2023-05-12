package nat.pink.base.utils;


import android.content.Context;
import android.os.Build;
import android.provider.Settings;

public class UtilsNew {
	public static String LogTag = "henrytest";
	public static String EXTRA_MSG = "extra_msg";


	public static boolean canDrawOverlays(Context context){
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
			return true;
		}else{
			return Settings.canDrawOverlays(context);
		}


	}


}
