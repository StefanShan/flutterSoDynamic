package com.example.flutterplugin.util;

import org.gradle.api.Project;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;

public class LogUtil {
    
    private static File logFile;

    public static void init(Project project){
        if (logFile != null && logFile.exists()) {
            log("\n   --------------  \n");
            return;
        }
        logFile =  new File(project.getBuildDir(), "log.txt");
    }

    public static void log(String msg) {
        try {
            String time = new SimpleDateFormat("HH:mm:ss").format(System.currentTimeMillis());
            FileUtil.writeStringToFile(logFile, time + " --- " + msg + "\n", true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
