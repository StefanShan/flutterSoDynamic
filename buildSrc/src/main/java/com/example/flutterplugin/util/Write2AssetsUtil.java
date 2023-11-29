package com.example.flutterplugin.util;

import com.android.build.api.dsl.AndroidSourceSet;
import com.android.build.gradle.AppExtension;

import java.io.File;
import java.io.IOException;

public class Write2AssetsUtil {

    private StringBuilder stringBuilder = new StringBuilder("{");
    private AppExtension appExtension;
    private File flutterSOConfigFile;

    private Write2AssetsUtil(){}

    private static volatile Write2AssetsUtil singleton;

    public static Write2AssetsUtil getInstance(){
        if(singleton == null){
            synchronized (Write2AssetsUtil.class){
                if(singleton == null){
                    singleton = new Write2AssetsUtil();
                }
            }
        }
        return singleton;
    }

    public void init(AppExtension appExtension, String parentPath){
        if(flutterSOConfigFile != null && flutterSOConfigFile.exists()) return;
        this.appExtension = appExtension;
        flutterSOConfigFile = new File(parentPath + File.separator + "soConfig", "flutterso.json");
    }

    public Write2AssetsUtil writeContent(String content){
        stringBuilder.append(content).append(",");
        return this;
    }

    public void endWrite(){
        int lastCharIndex = stringBuilder.length() - 1;
        stringBuilder.replace(lastCharIndex, stringBuilder.length(), "}");
        try {
            FileUtil.writeStringToFile(flutterSOConfigFile, stringBuilder.toString());
            // 动态添加asset目录
            AndroidSourceSet mainSourceSet = appExtension.getSourceSets().getByName("main");
            mainSourceSet.getAssets().srcDirs(flutterSOConfigFile.getParent());
            LogUtil.log("写入成功= " + flutterSOConfigFile.getAbsolutePath());
        } catch (IOException e) {
            LogUtil.log("文件写入失败= " + e.getLocalizedMessage());
        }
    }
}
