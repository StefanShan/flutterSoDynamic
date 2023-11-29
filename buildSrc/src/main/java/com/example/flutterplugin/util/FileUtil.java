package com.example.flutterplugin.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

import javax.annotation.Nullable;

public class FileUtil {
    @Nullable
    public static File findSpecificFile(String findFolderPath, String fileName) {
        File findFolder = new File(findFolderPath);
        targetFile = null;
        return findSpecificFile(findFolder, null, fileName);
    }

    @Nullable
    public static File findSpecificFile(String findFolderPath, String fileFolder, String fileName) {
        File findFolder = new File(findFolderPath);
        targetFile = null;
        return findSpecificFile(findFolder, fileFolder, fileName);
    }

    private static File targetFile = null;
    @Nullable
    private static File findSpecificFile(File findFolder, String fileFolder, String fileName) {
        if (!findFolder.exists() || !findFolder.isDirectory()) return null;
        File[] files = findFolder.listFiles();
        if (files != null && targetFile == null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    findSpecificFile(file, fileFolder, fileName);
                } else {
                    if (file.getAbsolutePath().contains(fileFolder) && file.getAbsolutePath().contains(fileName)) {
                        targetFile = file;
                        return targetFile;
                    }
                }
            }
        }
        return targetFile;
    }

    public static void writeStringToFile(File file, String data) throws IOException {
        writeStringToFile(file, data, Charset.forName("utf-8"), false);
    }

    public static void writeStringToFile(File file, String data, boolean append) throws IOException {
        writeStringToFile(file, data, Charset.forName("utf-8"), append);
    }

    public static void writeStringToFile(File file, String data, Charset encoding, boolean append) throws IOException {
        OutputStream out = openOutputStream(file, append);
        Throwable var5 = null;

        try {
            if (data != null) {
                out.write(data.getBytes(encoding));
            }
        } catch (Throwable var14) {
            var5 = var14;
            throw var14;
        } finally {
            if (out != null) {
                if (var5 != null) {
                    try {
                        out.close();
                    } catch (Throwable var13) {
                        var5.addSuppressed(var13);
                    }
                } else {
                    out.close();
                }
            }

        }

    }

    private static FileOutputStream openOutputStream(File file, boolean append) throws IOException {
        if (file.exists()) {
            if (file.isDirectory()) {
                throw new IOException("File '" + file + "' exists but is a directory");
            }

            if (!file.canWrite()) {
                throw new IOException("File '" + file + "' cannot be written to");
            }
        } else {
            File parent = file.getParentFile();
            if (parent != null && !parent.mkdirs() && !parent.isDirectory()) {
                throw new IOException("Directory '" + parent + "' could not be created");
            }
        }

        return new FileOutputStream(file, append);
    }
}
