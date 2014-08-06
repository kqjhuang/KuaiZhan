package zhanmanager.zhan.sohu.com.animation_test;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Environment;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FileUtil {

    public static final String PARENT_PATH = "Sohu";
    public static final String CHILD_PATH = "Kuaizhan";
    public static final String IMAGE_PATH = "快站照片";
    // 隐藏文件夹，避免被MediaStore扫描
    public static final String CACHE_PATH = "cache";
    public static final String SOHU_KUAIZHAN_PATH = "/" + PARENT_PATH + "/" + CHILD_PATH;

    private static String mSdcardRootDir;

    public static String getSdcardRootDir() {
        if (mSdcardRootDir == null) {
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                File file = new File(Environment.getExternalStorageDirectory().getPath() + SOHU_KUAIZHAN_PATH);
                if (!file.exists()) {
                    if (file.mkdirs()) {
                        mSdcardRootDir = file.getAbsolutePath();
                    }
                } else {
                    mSdcardRootDir = file.getAbsolutePath();
                }
            }
        }
        return mSdcardRootDir;
    }

    private static String mSdcardImageDir;

    public static String getSdcardImageDir() {
        if (mSdcardImageDir == null) {
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                File file = new File(Environment.getExternalStorageDirectory().getPath() +
                        SOHU_KUAIZHAN_PATH + "/" + IMAGE_PATH);
                if (!file.exists()) {
                    if (file.mkdirs()) {
                        mSdcardImageDir = file.getAbsolutePath();
                    }
                } else {
                    mSdcardImageDir = file.getAbsolutePath();
                }
            }
        }
        return mSdcardImageDir;
    }

    private static String mSdcardCacheDir;

    public static String getSdcardCacheDir() {
        if (mSdcardCacheDir == null) {
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                File file = new File(Environment.getExternalStorageDirectory().getPath() +
                        SOHU_KUAIZHAN_PATH + "/" + CACHE_PATH);
                if (!file.exists()) {
                    if (file.mkdirs()) {
                        mSdcardCacheDir = file.getAbsolutePath();
                    }
                } else {
                    mSdcardCacheDir = file.getAbsolutePath();
                }
            }
        }
        return mSdcardCacheDir;
    }

    @SuppressLint("SimpleDateFormat")
    public static Uri generateImageUri() {
        String imageName = new SimpleDateFormat("快站-yyyy-MM-dd-HH-mm-ss").format(new Date()) + ".jpg";
        String imagePath = getSdcardImageDir() + "/" + imageName;
        File imageFile = new File(imagePath);
        return Uri.fromFile(imageFile);
    }

    public static Uri generateCacheUri(String name) {
        String imageName = name + ".edit";
        String imagePath = getSdcardCacheDir() + "/" + imageName;
        File imageFile = new File(imagePath);
        return Uri.fromFile(imageFile);
    }

    public static void writeFile(File file, byte[] bytes) throws IOException {
        DataOutputStream out = null;
        try {
            out = new DataOutputStream(new FileOutputStream(file));
            out.write(bytes);
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }

    public static String writeCacheImage(String name, byte[] bytes) throws IOException {
        if (!name.endsWith(".jpg") && !name.endsWith(".JPG")
                && !name.endsWith(".jpeg") && !name.endsWith(".JPEG")) {
            name += ".jpg";
        }
        String cachePath = getSdcardCacheDir() + "/" + name;
        File cacheFile = new File(cachePath);
        writeFile(cacheFile, bytes);
        return cachePath;
    }

    public static String md5(String string) {
        byte[] hash;
        try {
            hash = MessageDigest.getInstance("MD5").digest(string.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Huh, MD5 should be supported?", e);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Huh, UTF-8 should be supported?", e);
        }

        StringBuilder hex = new StringBuilder(hash.length * 2);
        for (byte b : hash) {
            if ((b & 0xFF) < 0x10) hex.append("0");
            hex.append(Integer.toHexString(b & 0xFF));
        }
        return hex.toString();
    }

}
