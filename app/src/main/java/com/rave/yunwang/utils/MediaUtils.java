package com.rave.yunwang.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.text.TextUtils;
import android.util.Pair;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

public class MediaUtils {


    private static String SEND_SUFFIX = "_send";

    private static String THUMBNAIL_SUFFIX = "_thumbnail";

    public static Bitmap getCompressBitmap(String path) {

        if (TextUtils.isEmpty(path)) return null;

        File file = new File(path);

        if (!file.exists()) return null;

        Bitmap bitmap = BitmapFactory.decodeFile(path);

        return getCompressBitmap(bitmap);
    }

    public static Bitmap getCompressBitmap(Bitmap bitmap) {

        if (bitmap == null) return null;

        Bitmap compressBitmap = null;

        try {

            int sizeLimit = 10;
            int quality = 50;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos);

            while (baos.toByteArray().length / 1024 / 1024 > sizeLimit) {
                baos.reset();
                quality -= 10;
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos);
            }

        } catch (OutOfMemoryError error) {
            error.printStackTrace();
            compressBitmap = bitmap;

        } catch (Exception e) {
            e.printStackTrace();
            compressBitmap = bitmap;
        }

        if (bitmap != compressBitmap)
            bitmap.recycle();

        return compressBitmap;
    }

    public static File transferBitmapToFile(int quality, Bitmap bitmap, String fileName) {
        File file = null;
        if (quality == 0 || TextUtils.isEmpty(fileName)) return file;

        try {

            file = new File(FilePathUtils.getDir(FilePathUtils.PATH_TYPE_CAMERA_CACHE), fileName);

            File parentFile = file.getParentFile();
            if (!parentFile.exists()) {
                parentFile.mkdirs();
            }
            if (!file.exists()) {
                file.createNewFile();
            }

            BufferedOutputStream baos = new BufferedOutputStream(
                    new FileOutputStream(file));

            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos);

            baos.flush();
            baos.close();
        } catch (Exception e) {
            e.printStackTrace();
            file = null;
        }
        return file;
    }

    public static Bitmap getCompressBitmap(String path, int standard) {
        if (TextUtils.isEmpty(path)) return null;

        if (standard == 0) {
            standard = WindowUtils.dip2px(120);
        }

        Bitmap thumbnail = null;
        try {
            int KMaxWidth = standard;
            int KMaxHeight = standard;
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(path, options);
            int originalWidth = options.outWidth;
            int originalHeight = options.outHeight;

            options.inJustDecodeBounds = false;
            options.inMutable = true;
            options.inPreferredConfig = Bitmap.Config.RGB_565;
            float widthScaleRatio = originalWidth / (float) KMaxWidth;
            float heightScaleRatio = originalHeight / (float) KMaxHeight;
            float scaleRatio = widthScaleRatio;
            int sampleSize = 1;
            if (originalWidth > KMaxWidth ||
                    originalHeight > KMaxHeight) {
                if (widthScaleRatio > heightScaleRatio) {
                    scaleRatio = heightScaleRatio;
                }
                float scaleRatioDelta = Math.abs(scaleRatio - 1.0f);
                int scaleRatioDeltaInteger = (int) scaleRatioDelta;
                if (scaleRatioDeltaInteger <= 0 &&
                        scaleRatioDelta >= 1.0E-2) {
                    scaleRatio = 1.0f;
                }
                if ((scaleRatio - 1.0f) < 1.0E-3) {
                    sampleSize = 1;
                } else {
                    sampleSize = (int) scaleRatio;
                }
                options.inSampleSize = sampleSize;
                thumbnail = BitmapFactory.decodeFile(path, options);
            } else {
                thumbnail = BitmapFactory.decodeFile(path, options);
            }
            thumbnail = wtitePictureDegree(thumbnail, readPictureDegree(path));
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
            thumbnail = null;
        } catch (Exception e) {
            e.printStackTrace();
            thumbnail = null;
        }

        return thumbnail;
    }

    public static File getThumbnailFile(String path) {
        if (TextUtils.isEmpty(path)) return null;
        File file = getThumbnailFileByOriginalPath(path);

        if (file == null) return null;
        if (file.exists()) return file;

        Bitmap bitmap = getCompressBitmap(path, WindowUtils.dip2px(120));
        if (bitmap == null) return null;
        return transferBitmapToFile(35, bitmap, file.getName());
    }

    public static File getSendFile(String path) {
        if (TextUtils.isEmpty(path)) return null;
        File file = getSendFileByOriginalPath(path);

        if (file == null) return null;
        if (file.exists()) return file;

        Bitmap bitmap = getCompressBitmap(path, WindowUtils.dip2px(120));
        return transferBitmapToFile(70, bitmap, file.getName());
    }

    private static File getSendFileByOriginalPath(String filePath) {
        if (TextUtils.isEmpty(filePath)) return null;
        String uniquePath = getSendUniquePath(filePath);
        if (TextUtils.isEmpty(uniquePath)) return null;
        return new File(FilePathUtils.getDir(FilePathUtils.PATH_TYPE_CAMERA_CACHE), uniquePath);
    }

    private static File getThumbnailFileByOriginalPath(String filePath) {
        if (TextUtils.isEmpty(filePath)) return null;
        String uniquePath = getThumbnailUniquePath(filePath);
        if (TextUtils.isEmpty(uniquePath)) return null;
        return new File(FilePathUtils.getDir(FilePathUtils.PATH_TYPE_CAMERA_CACHE), uniquePath);
    }

    private static String getSendUniquePath(String filePath) {
        if (TextUtils.isEmpty(filePath)) return null;
        String uniquePath = getUniqueNameByOriginalPath(filePath);
        if (TextUtils.isEmpty(uniquePath)) return null;
        uniquePath += SEND_SUFFIX;
        return uniquePath;
    }

    private static String getThumbnailUniquePath(String filePath) {
        if (TextUtils.isEmpty(filePath)) return null;
        String uniquePath = getUniqueNameByOriginalPath(filePath);
        if (TextUtils.isEmpty(uniquePath)) return null;
        uniquePath += THUMBNAIL_SUFFIX;
        return uniquePath;
    }

    private static String getUniqueNameByOriginalPath(String filePath) {
        String unique = null;
        try {
            File file = new File(filePath);
            File parentFile = file.getParentFile();
            if (!parentFile.exists()) {
                parentFile.mkdirs();
            }
            if (!file.exists()) {
                file.createNewFile();
            }
            unique = String.valueOf((filePath.toLowerCase() + file.lastModified()).hashCode());
        } catch (IOException e) {
            e.printStackTrace();
            unique = null;
        }
        return unique;
    }

    public static int readPictureDegree(String path) {
        int degree = 0;
        try {
            ExifInterface exifInterface = new ExifInterface(path);
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
            degree = 0;
        }
        return degree;
    }

    public static Bitmap wtitePictureDegree(Bitmap bitmap, int degree, boolean isFront) {

        if (bitmap == null) return null;

        if (degree == 0) return bitmap;

        Bitmap returnBitmap = null;

        Matrix matrix = new Matrix();

        matrix.postRotate(degree);

        if (isFront)
            matrix.postScale(-1, 1);

        try {
            returnBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
                    bitmap.getHeight(), matrix, true);

        } catch (OutOfMemoryError e) {
            return bitmap;
        } catch (Exception e) {
            return bitmap;
        }

        if (returnBitmap == null) {
            returnBitmap = bitmap;
        }

        if (bitmap != returnBitmap) {
            bitmap.recycle();
        }
        return returnBitmap;
    }

    public static Bitmap wtitePictureDegree(Bitmap bitmap, int degree) {
        return wtitePictureDegree(bitmap, degree, false);
    }

    public static File saveTakeVideo() {
        File file = null;

        String fileName = UUID.randomUUID().toString() + System.currentTimeMillis() + ".mp4";

        file = new File(FilePathUtils.getDir(FilePathUtils.PATH_TYPE_CAMERA_CACHE)
                , fileName);

        try {
            if (!file.exists()) {
                file.createNewFile();
            }
        } catch (Exception e) {
            e.printStackTrace();
            file = null;
        }
        return file;
    }

    public static File saveTakePicture(Bitmap bitmap) {
        return saveTakePicture(bitmap, null);
    }

    public static Pair<Integer, Integer> getImageWidthAndHeight(String filePath) {
        if (new File(filePath).exists()) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(filePath, options);
            return new Pair(options.outWidth, options.outHeight);
        }
        return new Pair(0, 0);
    }


    public static File saveTakePicture(Bitmap bitmap, String fileName) {

        File file = null;
        if (TextUtils.isEmpty(fileName))
            fileName = UUID.randomUUID().toString() + System.currentTimeMillis() + ".jpg";

        file = new File(FilePathUtils.getDir(FilePathUtils.PATH_TYPE_CAMERA_CACHE),
                fileName);

        try {
            if (!file.exists()) {
                file.createNewFile();
            }

            BufferedOutputStream bos = new BufferedOutputStream(
                    new FileOutputStream(file));
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, bos);

            bos.flush();
            bos.close();

        } catch (Exception e) {
            e.printStackTrace();
            file = null;
        }
        return file;
    }

    public static File saveTakePicture(byte[] data) {

        File file = null;

        String fileName = UUID.randomUUID().toString() + System.currentTimeMillis() + ".jpg";

        file = new File(FilePathUtils.getDir(FilePathUtils.PATH_TYPE_CAMERA_CACHE),
                fileName);

        FileOutputStream fos = null;

        try {
            if (!file.exists()) {
                file.createNewFile();
            }

            fos = new FileOutputStream(file.getPath());
            fos.write(data, 0, data.length);

        } catch (Exception e) {
            e.printStackTrace();
            file = null;
        }

        return file;
    }

}
