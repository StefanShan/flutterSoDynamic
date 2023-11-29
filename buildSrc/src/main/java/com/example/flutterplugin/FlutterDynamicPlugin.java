package com.example.flutterplugin;

import com.android.build.gradle.AppExtension;
import com.example.flutterplugin.util.*;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;

public class FlutterDynamicPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        if (project.getPlugins().hasPlugin("com.android.application")) {
            LogUtil.init(project);
            project.afterEvaluate(project1 -> {
                AppExtension appExtension = project.getExtensions().getByType(AppExtension.class);
                appExtension.getApplicationVariants().all(variant -> {
                    String variantName = StringUtil.capitalize(variant.getName());
                    if (!variantName.equalsIgnoreCase("release")) return;

                    Write2AssetsUtil.getInstance().init(appExtension, project.getBuildDir().getAbsolutePath());

                    //处理 libflutter.so
                    EngineSoDynamicTask engineSoDynamicTask = project.getTasks().create("flutterSoDynamic" + variantName, EngineSoDynamicTask.class);
                    //处理 libapp.so
                    AppSoDynamicTask appSoDynamicTask = project.getTasks().create("appSoDynamic" + variantName, AppSoDynamicTask.class);

                    // mergeReleaseNativeLibs -> flutterSoDynamicRelease -> appSoDynamicRelease -> mergeReleaseAssets
                    Task mergeSOTask = project.getTasks().findByName("merge" + variantName + "NativeLibs");
                    mergeSOTask.finalizedBy(engineSoDynamicTask, appSoDynamicTask);
                    appSoDynamicTask.mustRunAfter(engineSoDynamicTask);
                    Task mergeAssetsTask = project.getTasks().findByName("merge" + variantName + "Assets");
                    mergeAssetsTask.dependsOn(appSoDynamicTask);

                    engineSoDynamicTask.variant = variant;
                    engineSoDynamicTask.appExtension = appExtension;
                    engineSoDynamicTask.mergeNativeLibsOutputPath = mergeSOTask.getOutputs().getFiles().getAsPath();

                    appSoDynamicTask.variant = variant;
                    appSoDynamicTask.appExtension = appExtension;
                    appSoDynamicTask.mergeNativeLibsOutputPath = mergeSOTask.getOutputs().getFiles().getAsPath();
                });
            });
        }
    }
}
