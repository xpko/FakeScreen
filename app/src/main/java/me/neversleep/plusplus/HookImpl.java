package me.neversleep.plusplus;

import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

public class HookImpl {
     public static final String TAG = "neversleep";

     public static void main(final ClassLoader classLoader) {
          try {
               final XSharedPreferences xSharedPreferences = new XSharedPreferences(BuildConfig.APPLICATION_ID, "x_conf");
               xSharedPreferences.makeWorldReadable();
               xSharedPreferences.reload();
               XposedBridge.hookAllMethods(XposedHelpers.findClass("com.android.server.policy.PhoneWindowManager", classLoader), "powerPress", new XC_MethodHook() { // from class: me.neversleep.plusplus.HookImpl.1
                    int mode = 0;

                    protected void beforeHookedMethod(XC_MethodHook.MethodHookParam methodHookParam) throws Throwable {
                         super.beforeHookedMethod(methodHookParam);
                         try {
                              XUtils.xLog("neversleep", "beforeHookedMethod: start");
                              xSharedPreferences.reload();
                              int i = 0;
                              if (!xSharedPreferences.getBoolean("power", false)) {
                                   Log.e("neversleep", "beforeHookedMethod: power is false");
                                   return;
                              }
                              XUtils.xLog("neversleep", "beforeHookedMethod: power is true");
                              Class<?> cls = Class.forName("android.view.SurfaceControl", false, classLoader);
                              IBinder iBinder = Build.VERSION.SDK_INT < 29 ? (IBinder) XposedHelpers.callStaticMethod(cls, "getBuiltInDisplay", 0) : (IBinder) XposedHelpers.callStaticMethod(cls, "getInternalDisplayToken");
                              if (iBinder != null) {
                                   XposedHelpers.callStaticMethod(cls, "setDisplayPowerMode", iBinder, this.mode);
                                   if (this.mode == 0) {
                                        i = 2;
                                   }
                                   this.mode = i;
                              }
                              methodHookParam.setResult(null);
                              XUtils.xLog("neversleep", "replace success");
                         } catch (Throwable th) {
                              XUtils.xLog("neversleep", "beforeHookedMethod: error:", th);
                         }
                    }
               });
               XUtils.xLog("neversleep", "main: Hook success");
          } catch (Throwable th) {
               th.printStackTrace();
               XUtils.xLog("neversleep", "main: error:" + th.getMessage(), th);
          }
     }
}
