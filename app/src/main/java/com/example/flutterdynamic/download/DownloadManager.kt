package com.example.flutterdynamic.download

import android.content.Context
import android.net.Uri
import android.text.TextUtils
import com.download.library.DownloadImpl
import com.download.library.DownloadListenerAdapter
import com.download.library.DownloadTask
import com.download.library.DownloadingListener.MainThread
import com.download.library.Extra
import java.io.File
import java.lang.ref.WeakReference
import java.util.concurrent.ConcurrentHashMap


class DownloadManager private constructor() {

    private val downloadStateMap = ConcurrentHashMap<String, DownloadState>()
    private var mContext: WeakReference<Context>? = null

    companion object {
        @JvmStatic
        val instance by lazy {
            DownloadManager()
        }
    }

    //开始
    @JvmOverloads
    fun start(context: Context, config: DownloadConfig, listener: IDownloadListener? = null) {
        mContext = WeakReference(context)
        val resourceRequest = DownloadImpl.getInstance(context)
            .url(config.url)
            .setEnableIndicator(config.isShowNotification)

        if (TextUtils.isEmpty(config.fileName)) {
            resourceRequest.targetDir(config.path)
        } else {
            resourceRequest.target(File(config.path, config.fileName!!))
        }

        if (config.isShowNotification) {
            resourceRequest.setIcon(config.notificationIcon ?: 0)
        }

        resourceRequest.enqueue(object : DownloadListenerAdapter() {
            override fun onStart(
                url: String?,
                userAgent: String?,
                contentDisposition: String?,
                mimetype: String?,
                contentLength: Long,
                extra: Extra?
            ) {
                super.onStart(url, userAgent, contentDisposition, mimetype, contentLength, extra)
                listener?.onStart(url, contentLength)
            }

            override fun onResult(throwable: Throwable?, path: Uri?, url: String?, extra: Extra?): Boolean {
                if (throwable == null) {
                    listener?.onSuccess(url, path)
                } else {
                    listener?.onFailed(url, throwable)
                }
                return false
            }

            @MainThread
            override fun onProgress(url: String?, downloaded: Long, length: Long, usedTime: Long) {
                super.onProgress(url, downloaded, length, usedTime)
                listener?.onProgress(url, downloaded, length)
            }

            override fun onDownloadStatusChanged(extra: Extra?, status: Int) {
                super.onDownloadStatusChanged(extra, status)
                if (extra != null) {
                    downloadStateMap[extra.url] = statusCover(status)
                }
            }
        })
    }

    private fun statusCover(status: Int): DownloadState {
        return when (status) {
            DownloadTask.STATUS_PENDDING -> DownloadState.PEND
            DownloadTask.STATUS_DOWNLOADING -> DownloadState.DOWNLOADING
            DownloadTask.STATUS_PAUSED -> DownloadState.PAUSE
            DownloadTask.STATUS_SUCCESSFUL -> DownloadState.SUCCESS
            DownloadTask.STATUS_ERROR -> DownloadState.FAIL
            DownloadTask.STATUS_CANCELED -> DownloadState.CANCEL
            else -> DownloadState.UNKNOW
        }
    }
}