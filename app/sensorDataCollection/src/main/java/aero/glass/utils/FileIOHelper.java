package aero.glass.utils;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.nio.channels.FileChannel;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Created by Pillio on 11/13/2017.
 */

public final class FileIOHelper {

    private static final String ROOT = Environment.getExternalStorageDirectory() + File.separator;

    private FileIOHelper() {
        // utility class
    }

    /**
     * Try to open a file. First, it tries from root "/". Next from aeroglass directory and finally
     * from asset directory.
     * @param a AssetManager
     * @param fn filename
     * @return inputStream
     * @throws IOException If it can't create FileInputStream.
     */
    public static InputStream openFile(AssetManager a, String fn) throws IOException {
        File txtFile;
        if (fn.startsWith(ROOT)) {
            txtFile = new File(fn);
        } else {
            File dir = new File(Environment.getExternalStorageDirectory(), "aeroglass");
            txtFile = new File(dir, fn);
        }

        if (txtFile.exists()) {
            return new FileInputStream(txtFile);
        } else {
            return a.open(fn);
        }
    }

    /**
     * Try to load a file. First, it tries from root "/". Next from aeroglass directory and finally
     * from asset directory.
     * @param a AssetManager
     * @param fn filename
     * @return file content
     */
    public static String loadString(AssetManager a, String fn) throws IOException {
        InputStream is = openFile(a, fn);

        int size = is.available();
        byte[] buffer = new byte[size];
        is.read(buffer);
        is.close();
        return new String(buffer, "UTF-8");
    }

    /**
     * Try to load a file.
     * @param file file
     * @return file content
     */
    public static String loadString(File file) {
        try {
            InputStream is = new FileInputStream(file);

            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            return new String(buffer, "UTF-8");
        } catch (IOException e) {
            Log.d("JSONHelper", file.getName() + " not found");
        }
        return "";
    }

    /**
     * Try to load a file from a zip file. First, it tries to open zip file from root "/". Next from
     * aeroglass directory and finally from asset directory.
     * @param a AssetManager
     * @param fn filename in zip file
     * @param zipfn zip file name
     * @return file content
     */
    public static String loadStringFromZIP(AssetManager a, String fn, String zipfn) {
        String json = "";
        try {
            ZipInputStream is = new ZipInputStream(FileIOHelper.openFile(a, zipfn));
            try {
                ZipEntry entry;
                while ((entry = is.getNextEntry()) != null) {
                    if (!entry.isDirectory()) {
                        if (fn.equals(entry.getName())) {
                            byte[] buffer = new byte[8192];
                            int bytes;
                            StringBuilder sb = new StringBuilder();
                            while ((bytes = is.read(buffer)) != -1) {
                                sb.append(new String(buffer, 0, bytes, "UTF-8"));
                            }
                            json = sb.toString();
                        }
                        is.closeEntry();
                    }
                }
            } finally {
                is.close();
            }
        } catch (IOException e) {
            Log.d("JSONHelper", fn + " not found in zip file " + zipfn);
        }
        return json;
    }

    /**
     * Try to save a String to a file.
     * @param file file
     * @param string string
     * @param append append the file if true overwrite otherwise
     */
    public static void writeString(File file, String string, boolean append) {
        PrintWriter out = null;
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            out = new PrintWriter(new FileOutputStream(file, append));
            out.println(string);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }

    /**
     * Copy file.
     * @param src source
     * @param dst destination
     * @throws IOException if any I/O error occurs
     */
    public static void copyFile(File src, File dst) throws IOException {
        FileChannel inChannel = new FileInputStream(src).getChannel();
        FileChannel outChannel = new FileOutputStream(dst).getChannel();
        try {
            inChannel.transferTo(0, inChannel.size(), outChannel);
        } finally {
            if (inChannel != null) {
                inChannel.close();
            }
            if (outChannel != null) {
                outChannel.close();
            }
        }
    }

    /**
     * Copy files from source directory to destination directory.
     * @param srcDir source directory
     * @param dstDir destination directory
     */
    public static void copyFiles(File srcDir, File dstDir) {
        for (File file : srcDir.listFiles()) {
            try {
                copyFile(file, new File(dstDir, file.getName()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Delete all files from a directory.
     * @param dir directory
     */
    public static void cleanDir(File dir) {
        if (dir.isDirectory()) {
            for (File file : dir.listFiles()) {
                file.delete();
            }
        }
    }

    public static void copyFromAssetToSdcard(AssetManager assetManager, String path, String file) {
        try {
            InputStream in = assetManager.open(file);
            File dir = new File(ROOT, path);
            if (!dir.isDirectory()) {
                dir.mkdirs();
            }
            File outFile = new File(dir, file);
            OutputStream out = new FileOutputStream(outFile);
            byte[] buffer = new byte[1024];
            int read;
            while((read = in.read(buffer)) != -1){
                out.write(buffer, 0, read);
            }

            copyStream(in, out);
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    private static void copyStream(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while((read = in.read(buffer)) != -1){
            out.write(buffer, 0, read);
        }
    }

    public static String getStringFromAssets(Context c, String path) {
        final InputStream is = getInputStreamFromAssets(c, path);
        if (is == null) {
            return null;
        }

        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();
        String line;
        try {

            br = new BufferedReader(new InputStreamReader(is));
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return sb.toString();
    }

    public static InputStream getInputStreamFromAssets(Context c, String path) {
        AssetManager as = c.getAssets();

        InputStream is = null;
        try {
            is = as.open(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (is == null) {
            return null;
        }
        return is;
    }
    public static String readStringFromFile(File f) {
        final StringBuilder sb = new StringBuilder(512);
        try {
            Reader r = new InputStreamReader(new FileInputStream(f), "UTF-8");
            int c;
            while ((c = r.read()) != -1) {
                sb.append((char) c);
            }
        } catch (IOException e) {
            Log.e("file read", "failed to read file : " + f.getAbsolutePath());
            return null;
        }
        return sb.toString();
    }
}
