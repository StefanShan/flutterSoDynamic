package com.example.flutterplugin;

import com.android.build.gradle.AppExtension;
import com.android.build.gradle.api.ApplicationVariant;
import com.example.flutterplugin.util.*;

import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ResolvedDependency;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.TaskAction;

import java.io.File;

import javax.annotation.Nullable;

public class EngineSoDynamicTask extends DefaultTask {

    @Internal
    public ApplicationVariant variant;

    @Internal
    AppExtension appExtension;

    @Input
    public String mergeNativeLibsOutputPath;


    public ApplicationVariant getVariant() {
        return variant;
    }

    public AppExtension getAppExtension() {
        return appExtension;
    }

    public String getMergeNativeLibsOutputPath() {
        return mergeNativeLibsOutputPath;
    }


    public EngineSoDynamicTask() {
        setGroup("flutterOpt");
    }

    @TaskAction
    public void optimizeEngineSo() {
        String flutterSDKVersion = findFlutterSDKVersion(getProject(), variant.getName());
        if (flutterSDKVersion == null || flutterSDKVersion.isEmpty()) return;
        LogUtil.log("libflutter.so version is " + flutterSDKVersion);

        File soFile = FileUtil.findSpecificFile(mergeNativeLibsOutputPath, "arm64-v8a", "libflutter.so");
        LogUtil.log("soFile = " + (soFile == null ? "null" : soFile.getAbsolutePath()));

        //检测 libflutter.so 是否需要重新上传
        String flutterSoUrl = checkFlutterSDK(flutterSDKVersion);
        if (flutterSoUrl != null && !flutterSoUrl.isEmpty()) {
            //不需要重新上传，直接写入。并剔除
            write2Assets(flutterSDKVersion, flutterSoUrl);
            if (soFile != null && soFile.exists()) {
                boolean deleteResult = soFile.delete();
                LogUtil.log("删除结果= " + deleteResult);
            }
            return;
        }
        //上传，写入，并从 apk 中剔除
        if (soFile == null || !soFile.exists()) return;
        String url = HttpUtil.getInstance().upload(soFile);
        if (url != null){
            write2Assets(flutterSDKVersion, url);
            boolean deleteResult = soFile.delete();
            LogUtil.log("删除结果= " + deleteResult);
        }
    }
    @Nullable
    private String checkFlutterSDK(String sdkVersion) {
        return HttpUtil.getInstance().check(SoType.LIB_FLUTTER_SO, sdkVersion);
    }

    private void write2Assets(String version, String url) {
        String content = "\"flutterSoUrl\":\"" + url + "\",\"flutterSoVersion\":\"" + version + "\"";
        Write2AssetsUtil.getInstance().writeContent(content);
    }

    private String findFlutterSDKVersion(Project project, String variantName) {
        Configuration configuration = project.getConfigurations().getByName(variantName + "RuntimeClasspath");
        for (ResolvedDependency resolvedDependency : configuration.getResolvedConfiguration().getLenientConfiguration().getAllModuleDependencies()) {
            if (resolvedDependency.getModuleGroup().equals("io.flutter")) {
                return resolvedDependency.getModuleVersion();
            }
        }
        return null;
    }
}
