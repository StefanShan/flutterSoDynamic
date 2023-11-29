package com.example.flutterdynamic.util

import android.app.ActivityManager
import android.content.Context
import android.os.Process

object ProcessUtil {

    /**
     * 是否为主进程
     */
    fun isMainProcess(context: Context): Boolean {
        return context.packageName == getCurrentProcessName(context)
    }

    /**
     * 获取当前进程名
     */
    fun getCurrentProcessName(context: Context): String? {
        try {
            val am: ActivityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager ?: return ""
            val info: List<ActivityManager.RunningAppProcessInfo> = am.runningAppProcesses
            if (info.isEmpty()) return ""
            val pid = Process.myPid()
            for (aInfo in info) {
                if (aInfo.pid == pid) {
                    if (aInfo.processName != null) {
                        return aInfo.processName
                    }
                }
            }
        } catch (e: Exception) {
            return ""
        }
        return ""
    }


}