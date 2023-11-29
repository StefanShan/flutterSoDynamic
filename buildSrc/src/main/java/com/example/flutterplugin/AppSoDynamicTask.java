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

public class AppSoDynamicTask extends DefaultTask {

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


    public AppSoDynamicTask() {
        setGroup("flutterOpt");
    }

    @TaskAction
    public void optimizeEngineSo() {
        String appSOVersion = findAppSOVersion(getProject(), variant.getName());
        if (appSOVersion == null || appSOVersion.isEmpty()) return;
        LogUtil.log("libapp.so version is " + appSOVersion);

        //检测 libflutter.so 是否需要重新上传
        String appSoUrl = checkFlutterSDK(appSOVersion);
        File soFile = FileUtil.findSpecificFile(mergeNativeLibsOutputPath, "arm64-v8a", "libapp.so");
        LogUtil.log("soFile = " + (soFile == null ? "null" : soFile.getAbsolutePath()));

        if (appSoUrl != null && !appSoUrl.isEmpty()) {
            //不需要重新上传，直接写入。并剔除
            write2Assets(appSOVersion, appSoUrl);
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
            write2Assets(appSOVersion, url);
            boolean deleteResult = soFile.delete();
            LogUtil.log("删除结果= " + deleteResult);
        }
    }

    private String checkFlutterSDK(String sdkVersion) {
        return HttpUtil.getInstance().check(SoType.LIB_APP_SO, sdkVersion);
    }

    private void write2Assets(String version, String url) {
        String content = "\"appSoUrl\":\"" + url + "\",\"appSoVersion\":\"" + version + "\"";
        Write2AssetsUtil.getInstance().writeContent(content).endWrite();
    }

    private String findAppSOVersion(Project project, String variantName) {
        Configuration configuration = project.getConfigurations().getByName(variantName + "RuntimeClasspath");
        for (ResolvedDependency resolvedDependency : configuration.getResolvedConfiguration().getLenientConfiguration().getAllModuleDependencies()) {
            //TODO: 修改成自己 flutter aar 的 ModuleGroup
            if (resolvedDependency.getModuleGroup().equals("com.stefan.flutter_module")) {
                return resolvedDependency.getModuleVersion();
            }
        }
        return null;
    }
}
