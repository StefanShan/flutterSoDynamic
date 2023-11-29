package com.example.flutterdynamic

import android.os.Build
import android.util.Log
import java.io.File
import java.io.IOException
import java.lang.reflect.Field
import java.lang.reflect.Method

object TinkerLoadLibrary {

    private val TAG = "TinkerLoadLibrary"

    fun installNativeLibraryPath(classLoader: ClassLoader, folder: File?) {
        if (folder == null || !folder.exists()) {
            Log.e(TAG, "installNativeLibraryPath, folder $folder is illegal")
            return
        }
        if (Build.VERSION.SDK_INT == 25 && Build.VERSION.PREVIEW_SDK_INT != 0
            || Build.VERSION.SDK_INT > 25
        ) {
            try {
                V25.install(classLoader, folder)
            } catch (throwable: Throwable) {
                Log.e(
                    TAG,
                    "installNativeLibraryPath, v25 fail, sdk: ${Build.VERSION.SDK_INT}, error: ${throwable.message}, try to fallback to V23"
                )
                V23.install(classLoader, folder)
            }
        } else if (Build.VERSION.SDK_INT >= 23) {
            try {
                V23.install(classLoader, folder)
            } catch (throwable: Throwable) {
                Log.e(
                    TAG,
                    "installNativeLibraryPath, v23 fail, sdk: ${Build.VERSION.SDK_INT}, error: ${throwable.message}, try to fallback to V14"
                )
                V14.install(classLoader, folder)
            }
        }
    }

    private object V25 {
        fun install(classLoader: ClassLoader, folder: File) {
            val pathListField: Field = ShareReflectUtil.findField(classLoader, "pathList")
            val dexPathList = pathListField[classLoader]
            val nativeLibraryDirectories: Field =
                ShareReflectUtil.findField(dexPathList, "nativeLibraryDirectories")
            var origLibDirs = nativeLibraryDirectories[dexPathList] as MutableList<File>
            if (origLibDirs == null) {
                origLibDirs = ArrayList(2)
            }
            val libDirIt = origLibDirs.iterator()
            while (libDirIt.hasNext()) {
                val libDir = libDirIt.next()
                if (folder == libDir) {
                    libDirIt.remove()
                    break
                }
            }
            origLibDirs.add(0, folder)
            val systemNativeLibraryDirectories: Field =
                ShareReflectUtil.findField(dexPathList, "systemNativeLibraryDirectories")
            var origSystemLibDirs = systemNativeLibraryDirectories[dexPathList] as List<File>
            if (origSystemLibDirs == null) {
                origSystemLibDirs = ArrayList(2)
            }
            val newLibDirs: MutableList<File> =
                ArrayList(origLibDirs.size + origSystemLibDirs.size + 1)
            newLibDirs.addAll(origLibDirs)
            newLibDirs.addAll(origSystemLibDirs)
            val makeElements: Method = ShareReflectUtil.findMethod(
                dexPathList, "makePathElements",
                MutableList::class.java
            )
            val elements = makeElements.invoke(dexPathList, newLibDirs) as Array<Any>
            val nativeLibraryPathElements: Field =
                ShareReflectUtil.findField(dexPathList, "nativeLibraryPathElements")
            nativeLibraryPathElements[dexPathList] = elements
        }
    }

    private object V23 {
        fun install(classLoader: ClassLoader, folder: File) {
            val pathListField: Field =
                ShareReflectUtil.findField(
                    classLoader,
                    "pathList"
                )
            val dexPathList = pathListField[classLoader]
            val nativeLibraryDirectories: Field =
                ShareReflectUtil.findField(
                    dexPathList,
                    "nativeLibraryDirectories"
                )
            var origLibDirs = nativeLibraryDirectories[dexPathList] as MutableList<File>
            if (origLibDirs == null) {
                origLibDirs = java.util.ArrayList(2)
            }
            val libDirIt = origLibDirs.iterator()
            while (libDirIt.hasNext()) {
                val libDir = libDirIt.next()
                if (folder == libDir) {
                    libDirIt.remove()
                    break
                }
            }
            origLibDirs.add(0, folder)
            val systemNativeLibraryDirectories: Field =
                ShareReflectUtil.findField(
                    dexPathList,
                    "systemNativeLibraryDirectories"
                )
            var origSystemLibDirs = systemNativeLibraryDirectories[dexPathList] as List<File>
            if (origSystemLibDirs == null) {
                origSystemLibDirs = java.util.ArrayList(2)
            }
            val newLibDirs: MutableList<File> =
                java.util.ArrayList(origLibDirs.size + origSystemLibDirs.size + 1)
            newLibDirs.addAll(origLibDirs)
            newLibDirs.addAll(origSystemLibDirs)
            val makeElements: Method =
                ShareReflectUtil.findMethod(
                    dexPathList,
                    "makePathElements",
                    MutableList::class.java,
                    File::class.java,
                    MutableList::class.java
                )
            val suppressedExceptions = java.util.ArrayList<IOException>()
            val elements = makeElements.invoke(
                dexPathList,
                newLibDirs,
                null,
                suppressedExceptions
            ) as Array<Any>
            val nativeLibraryPathElements: Field =
                ShareReflectUtil.findField(
                    dexPathList,
                    "nativeLibraryPathElements"
                )
            nativeLibraryPathElements[dexPathList] = elements
        }
    }

    private object V14 {
        fun install(classLoader: ClassLoader, folder: File) {
            val pathListField: Field =
                ShareReflectUtil.findField(
                    classLoader,
                    "pathList"
                )
            val dexPathList = pathListField[classLoader]
            val nativeLibDirField: Field =
                ShareReflectUtil.findField(
                    dexPathList,
                    "nativeLibraryDirectories"
                )
            val origNativeLibDirs = nativeLibDirField[dexPathList] as Array<File>
            val newNativeLibDirList: MutableList<File> =
                java.util.ArrayList(origNativeLibDirs.size + 1)
            newNativeLibDirList.add(folder)
            for (origNativeLibDir in origNativeLibDirs) {
                if (folder != origNativeLibDir) {
                    newNativeLibDirList.add(origNativeLibDir)
                }
            }
            nativeLibDirField[dexPathList] = newNativeLibDirList.toTypedArray()
        }
    }
}