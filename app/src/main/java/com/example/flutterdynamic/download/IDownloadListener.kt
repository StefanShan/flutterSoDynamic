package com.example.flutterdynamic.download

import android.net.Uri

interface IDownloadListener{
    /**
     * 下载开始
     * @param url 下载链接
     * @param contentLength 文件长度
     */
    fun onStart(url: String?, contentLength: Long) {}

    /**
     * 下载中
     * @param url 下载链接
     * @param downloaded 已下载的长度
     * @param length 文件总大小
     */
    fun onProgress(url: String?, downloaded: Long, length: Long) {}

    /**
     * 下载成功
     * @param url 下载链接
     * @param savePath 保存路径（绝对路径）
     */
    fun onSuccess(url: String?, savePath: Uri?) {}

    /**
     * 下载失败
     * @param url 下载链接
     * @param throwable 异常信息
     */
    fun onFailed(url: String?, throwable: Throwable) {}
}