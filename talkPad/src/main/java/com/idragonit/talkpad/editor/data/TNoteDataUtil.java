package com.idragonit.talkpad.editor.data;

import android.content.Context;
import android.os.Environment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

public final class TNoteDataUtil { //c

    public static String mStoragePath;
    public static String mTalkPadPath;
    public static String mNoteDirPath;
    private static String mTypeList[][];

    public static String genRandomUUID() { //a
        return UUID.randomUUID().toString();
    }

    public static String getDefaultTitle(Context context) { //a
        return "No title";
    }

    public static boolean externalEnabled() { //b
        return Environment.getExternalStorageState().equals("mounted");
    }

    public static String getNotePath(String uuid) { //b.a
        return (mNoteDirPath + uuid + "/");
    }

    public static String getFileType(File file) { //a

        String type = "*/*";
        String filename = file.getName();
        int lastIndex = filename.lastIndexOf(".");

        if (lastIndex >= 0) {

            String lowerCase = filename.substring(lastIndex, filename.length()).toLowerCase();

            if (!lowerCase.equals("")) {

                for (int i = 0; i < mTypeList.length; ++ i) {

                    if (lowerCase.equals(mTypeList[i][0])) {
                        type = mTypeList[i][1];
                    }
                }
            }
        }

        return type;
    }

    public static String assetToExternal(Context context, String asset, String toPath) { //a.a

        FileOutputStream fos = null;
        InputStream is = null;
        byte buf[] = new byte[1024];
        String filename;
        int nRead = 1;

        filename = toPath + getFileNameFromPath(asset);

        if ((new File(filename)).exists())
            return filename;

        try {

            is = context.getAssets().open(asset);
            fos = new FileOutputStream(filename);

            while (nRead > 0) {

                nRead = is.read(buf);
                if (nRead > 0) {
                    fos.write(buf, 0, nRead);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {

            if (is != null) {
                is.close();
            }

            if (fos != null) {
                fos.close();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        return filename;
    }

    public static String copyFile(String srcFile, String toPath) { //a.a

        String filename;
        FileInputStream fis;
        FileOutputStream fos;
        byte buf[];
        int nRead = 1;

        filename = toPath + getFileNameFromPath(srcFile);

        if ((new File(filename)).exists())
            return filename;

        try {
            buf = new byte[1024];
            fis = new FileInputStream(srcFile);
            fos = new FileOutputStream(filename);

            while (nRead > 0) {
                nRead = fis.read(buf);

                if (nRead > 0) {
                    fos.write(buf, 0, nRead);
                }
            }

            fis.close();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return filename;
    }

    public static String getFileContent(String filename) { //a.c

        StringBuilder sb = new StringBuilder();

        try {

            BufferedReader br = new BufferedReader(new FileReader(filename));

            while (true) {

                String line = br.readLine();

                if (line == null) {
                    break;
                }

                sb.append(line);
            }
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        return sb.toString();
    }

    public static boolean removeDir(String dirPath) { //a.d

        boolean result = true;
        File dir = new File(dirPath);

        if (dir.exists()) {
            File[] listFiles = dir.listFiles();

            if (listFiles != null) {

                for (int i = 0; i < listFiles.length; ++ i) {

                    if (listFiles[i].isDirectory()) {
                        removeDir(listFiles[i].getPath());
                    }
                    else if (!listFiles[i].delete()) {
                        result = false;
                    }
                }
            }
        }
        else {
            result = false;
        }

        if (result) {
            dir.delete();
        }

        return result;
    }

    public static boolean makeDir(String dirPath) {

        if (TNoteDataUtil.externalEnabled()) {

            File file = new File(dirPath);

            if (!file.exists())
                return file.mkdirs();
        }

        return false;
    }

    public static String getFileNameFromPath(String path) { //a.a

        int i = path.lastIndexOf('/');

        if (i >= 0)
            path = path.substring(i + 1);

        return path;
    }

    static {

        mStoragePath = Environment.getExternalStorageDirectory().toString();
        mTalkPadPath = mStoragePath + "/TalkPad/";
        mNoteDirPath = mTalkPadPath + "notes/";

        mTypeList = new String[][]{
                {".3gp", "video/3gpp"},
                {".apk", "application/vnd.android.package-archive"},
                {".asf", "video/x-ms-asf"},
                {".avi", "video/x-msvideo"},
                {".bin", "application/octet-stream"},
                {".bmp", "image/bmp"},
                {".c", "text/plain"},
                {".class", "application/octet-stream"},
                {".conf", "text/plain"},
                {".cpp", "text/plain"},
                {".doc", "application/msword"},
                {".exe", "application/octet-stream"},
                {".gif", "image/gif"},
                {".gtar", "application/x-gtar"},
                {".gz", "application/x-gzip"},
                {".h", "text/plain"},
                {".htm", "text/html"},
                {".html", "text/html"},
                {".jar", "application/java-archive"},
                {".java", "text/plain"},
                {".jpeg", "image/jpeg"},
                {".jpg", "image/jpeg"},
                {".js", "application/x-javascript"},
                {".log", "text/plain"},
                {".amr", "audio/amr"},
                {".m3u", "audio/x-mpegurl"},
                {".m4a", "audio/mp4a-latm"},
                {".m4b", "audio/mp4a-latm"},
                {".m4p", "audio/mp4a-latm"},
                {".m4u", "video/vnd.mpegurl"},
                {".m4v", "video/x-m4v"},
                {".mov", "video/quicktime"},
                {".mp2", "audio/x-mpeg"},
                {".mp3", "audio/x-mpeg"},
                {".mp4", "video/mp4"},
                {".mpc", "application/vnd.mpohun.certificate"},
                {".mpe", "video/mpeg"},
                {".mpeg", "video/mpeg"},
                {".mpg", "video/mpeg"},
                {".mpg4", "video/mp4"},
                {".mpga", "audio/mpeg"},
                {".msg", "application/vnd.ms-outlook"},
                {".ogg", "audio/ogg"},
                {".pdf", "application/pdf"},
                {".png", "image/png"},
                {".pps", "application/vnd.ms-powerpoint"},
                {".ppt", "application/vnd.ms-powerpoint"},
                {".prop", "text/plain"},
                {".rar", "application/x-rar-compressed"},
                {".rc", "text/plain"},
                {".rmvb", "audio/x-pn-realaudio"},
                {".rtf", "application/rtf"},
                {".sh", "text/plain"},
                {".tar", "application/x-tar"},
                {".tgz", "application/x-compressed"},
                {".txt", "text/plain"},
                {".wav", "audio/x-wav"},
                {".wma", "audio/x-ms-wma"},
                {".wmv", "audio/x-ms-wmv"},
                {".wps", "application/vnd.ms-works"},
                {".xml", "text/plain"},
                {".z", "application/x-compress"},
                {".zip", "application/zip"},
                {"", "*/*"}
        };
    }

}
