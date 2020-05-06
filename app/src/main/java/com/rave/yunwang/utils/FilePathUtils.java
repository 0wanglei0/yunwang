package com.rave.yunwang.utils;

import android.graphics.Bitmap;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class FilePathUtils {
    private static String TAG = FilePathUtils.class.getSimpleName();
    private static String mAppRoot;
    private final static String mThumbPath = ".Thumb" + File.separator;
    public final static int PATH_TYPE_DOCUMENT = 0;
    public final static int PATH_TYPE_AUDIO = 1;
    public final static int PATH_TYPE_VIDEO = 2;
    public final static int PATH_TYPE_IMAGE = 3;
    public final static int PATH_TYPE_STICKER = 4;
    public final static int PATH_TYPE_HEAD_IMAGE = 5;
    public final static int PATH_TYPE_FILE = 6;
    public final static int PATH_TYPE_LINK_CACHE = 7;
    public final static int PATH_TYPE_LOG_CACHE = 8;
    public final static int PATH_TYPE_CAMERA_CACHE = 9;
    public final static int PATH_TYPE_ALBUM_CACHE = 10;

    public final static int PATH_TYPE_IS_THUMB = 0x10;
    private final static int PATH_TYPE_THUMB_MASK = 0x0F;

    public static String mPathDirs[];

    public static void init() {
        String appName = getAppName();

        mAppRoot = Environment.getExternalStorageDirectory() + File.separator + appName;
        mPathDirs = new String[PATH_TYPE_ALBUM_CACHE + 1];
        mPathDirs[PATH_TYPE_DOCUMENT] = mAppRoot + File.separator + ".Doc" + File.separator;
        mPathDirs[PATH_TYPE_AUDIO] = mAppRoot + File.separator + ".Audio" + File.separator;
        mPathDirs[PATH_TYPE_VIDEO] = mAppRoot + File.separator + ".Video" + File.separator;
        mPathDirs[PATH_TYPE_IMAGE] = mAppRoot + File.separator + ".Image" + File.separator;
        mPathDirs[PATH_TYPE_STICKER] = mAppRoot + File.separator + ".Sticker" + File.separator;
        mPathDirs[PATH_TYPE_HEAD_IMAGE] = mAppRoot + File.separator + ".HeadImage" + File.separator;
        mPathDirs[PATH_TYPE_FILE] = mAppRoot + File.separator + ".File" + File.separator;
        mPathDirs[PATH_TYPE_LINK_CACHE] = mAppRoot + File.separator + ".LinkCache" + File.separator;
        mPathDirs[PATH_TYPE_LOG_CACHE] = mAppRoot + File.separator + ".Log" + File.separator;
        mPathDirs[PATH_TYPE_CAMERA_CACHE] = mAppRoot + File.separator + ".CameraCache" + File.separator;
        mPathDirs[PATH_TYPE_ALBUM_CACHE] = mAppRoot + File.separator + ".AlbumCache" + File.separator;
    }

    private static void createDir(String path, boolean forbiddenScan) {
        if (!TextUtils.isEmpty(path)) {
            try {
                File file = new File(path);
                if (!file.exists()) {
                    synchronized (FilePathUtils.class) {
                        file.mkdirs();
                        if (forbiddenScan) {
                            File fileMedia = new File(path + ".nomedia");
                            if (!fileMedia.exists()) {
                                fileMedia.createNewFile();
                            }
                        }
                    }
                }
            } catch (Exception e) {
                Log.e("FilePathUtils", e.getMessage());
            }
        }
    }

    public static String getAppRootDir() {
        return mAppRoot;
    }

    // EX: getDir(PATH_TYPE_DOCUMENT | PATH_TYPE_IS_THUMB)
    public static String getDir(int type) {
        int pathType = type & PATH_TYPE_THUMB_MASK;
        if (pathType < 0 || pathType > PATH_TYPE_ALBUM_CACHE) {
            return null;
        }
        String forFix = (type & PATH_TYPE_IS_THUMB) != 0 ? mThumbPath : "";
        StringBuilder stringBuilder = new StringBuilder();
        int pos = type & PATH_TYPE_THUMB_MASK;
        if (pos >= mPathDirs.length) {
            Log.d(TAG, "type beyond array");
            return null;
        }

        String dir = stringBuilder.append(mPathDirs[type & PATH_TYPE_THUMB_MASK]).append(forFix).toString();
        createDir(dir, pathType < PATH_TYPE_ALBUM_CACHE);
        return dir;
    }

    public static String getAlbumDir() {
        File storageDir = null;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            storageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), getAppName());
            if (!storageDir.mkdirs()) {
                if (!storageDir.exists()) {
                    Log.d(TAG, "failed to create directory");
                    return getDir(PATH_TYPE_ALBUM_CACHE);
                }
            }
        } else {
            Log.d(TAG, "External storage is not mounted READ/WRITE.");
            return getDir(PATH_TYPE_ALBUM_CACHE);
        }

        if (storageDir != null) {
            return storageDir.getAbsolutePath() + File.separator;
        }

        return getDir(PATH_TYPE_ALBUM_CACHE);
    }

    private static String getAppName() {
        return "yunwang";
    }

    public static String getDir(int type, boolean needThumb) {
        return getDir(type | (needThumb ? PATH_TYPE_IS_THUMB : 0));
    }

    public static boolean delFile(File file) {
        if (!file.exists()) {
            return false;
        }
        if (file.isFile()) {
            return file.delete();
        } else {
            File[] files = file.listFiles();
            for (File f : files) {
                delFile(f);
            }
            return file.delete();
        }
    }

    public static void saveImage2(Bitmap bmp) {
        if (bmp == null) {
            return;
        }
        File appDir = new File(Environment.getExternalStorageDirectory(), "Boohee");
        if (!appDir.exists()) {
            boolean isMkdir = appDir.mkdir();
            if (!isMkdir) {
                Log.e("File Path: ", "创建Boohee文件夹失败");
            }
        }
        String fileName = System.currentTimeMillis() + "xxx_detected.jpg";
        File file = new File(appDir, fileName);
        try {
            FileOutputStream fos = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Environment.getExternalStorageDirectory();
    }

}
