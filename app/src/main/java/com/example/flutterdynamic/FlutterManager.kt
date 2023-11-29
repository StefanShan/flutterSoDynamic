package com.example.flutterdynamic

import android.content.Context
import android.net.Uri
import android.text.TextUtils
import android.util.Log
import com.example.flutterdynamic.download.DownloadConfig
import com.example.flutterdynamic.download.DownloadManager
import com.example.flutterdynamic.download.IDownloadListener
import com.example.flutterdynamic.mode.FlutterConfig
import com.example.flutterdynamic.util.fromJsonProxy
import com.google.gson.Gson
import io.flutter.FlutterInjector
import io.flutter.embedding.engine.FlutterEngineGroup
import io.flutter.embedding.engine.FlutterJNI
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

object FlutterManager {

    private var engineGroup: FlutterEngineGroup? = null

    fun init(context: Context) {
        val flutterSoUrl = context.assets.open("flutterso.json").readBytes().decodeToString()
        val flutterConfig = Gson().fromJsonProxy(flutterSoUrl, FlutterConfig::class.java) ?: return
        //TODO: 根据版本检测是否需要下载
        MainScope().launch {
            val libFlutterSOSaveDir = context.getDir("libflutter", Context.MODE_PRIVATE)
            val libFlutterResult = downloadDynamicSO(context, DownloadConfig(
                flutterConfig.flutterSoUrl,
                libFlutterSOSaveDir.absolutePath
            ).apply {
                fileName = "libflutter.so"
            })
            val libAppResult = downloadDynamicSO(context, DownloadConfig(
                flutterConfig.appSoUrl,
                context.getDir("libflutter", Context.MODE_PRIVATE).absolutePath
            ).apply {
                fileName = "libapp.so"
            })

            //下载完成，动态加载，并初始化 FlutterEngineGroup
            if (!TextUtils.isEmpty(libFlutterResult) && !TextUtils.isEmpty(libAppResult)){
                loadAndInitFlutter(context, libFlutterSOSaveDir, libAppResult!!)
                return@launch
            }
        }
    }

    private suspend fun downloadDynamicSO(context: Context, downloadConfig: DownloadConfig): String? {
        return suspendCoroutine {
            //TODO: 自己实现下载
            var startTime = System.currentTimeMillis()
            DownloadManager.instance.start(
                context,
                downloadConfig,
                object : IDownloadListener {
                    override fun onStart(url: String?, contentLength: Long) {
                        super.onStart(url, contentLength)
                        startTime = System.currentTimeMillis()
                    }

                    override fun onSuccess(url: String?, savePath: Uri?) {
                        super.onSuccess(url, savePath)
                        Log.e(
                            "FlutterManager",
                            "下载成功[$url] -> ${downloadConfig.fileName} & 耗时-> ${System.currentTimeMillis() - startTime}"
                        )
                        it.resume(savePath?.path)
                    }

                    override fun onFailed(url: String?, throwable: Throwable) {
                        super.onFailed(url, throwable)
                        Log.e(
                            "FlutterManager",
                            "下载失败[${downloadConfig.fileName}] -> $throwable & 耗时-> ${System.currentTimeMillis() - startTime}"
                        )
                        it.resume(throwable.message)
                    }
                })
        }
    }

    private fun loadAndInitFlutter(context: Context, flutterSOSaveDir: File, appSOSavePath: String) {
        TinkerLoadLibrary.installNativeLibraryPath(context.classLoader, flutterSOSaveDir)
        FlutterInjector.setInstance(
            FlutterInjector.Builder()
            .setFlutterJNIFactory(CustomFlutterJNI.CustomFactory(appSOSavePath))
            .build())
        engineGroup = FlutterEngineGroup(context)
    }
}

class CustomFlutterJNI(private val appSOSavePath: String) : FlutterJNI(){
    override fun init(
        context: Context,
        args: Array<out String>,
        bundlePath: String?,
        appStoragePath: String,
        engineCachesPath: String,
        initTimeMillis: Long
    ) {
        val hookArgs = args.toMutableList().run {
            add("--aot-shared-library-name=$appSOSavePath")
            toTypedArray()
        }
        super.init(context, hookArgs, bundlePath, appStoragePath, engineCachesPath, initTimeMillis)
    }

    class CustomFactory(private val appSOSavePath: String) : Factory(){
        override fun provideFlutterJNI(): FlutterJNI {
            return CustomFlutterJNI(appSOSavePath)
        }
    }
}