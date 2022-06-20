package de.jadehs.vcg.utils.data;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import androidx.arch.core.util.Function;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class FileUtils {
    private static final String TAG = "FileUtils";


    public static void copyAllFilesFromAssetsIfNotExists(Context context, String source, File folder) {
        copyFileFromAssets(context, new IfNotExistsFunction(folder), source, folder);
    }


    /**
     * does copy all assets files from
     *
     * @param context the context of the assets
     * @param decider a function which is called on every file which is copied to decide if the file gets really copied
     * @param source  the string which chooses which files gets copied.
     * @param folder  the destination folder, all files get copied to
     */
    public static void copyFileFromAssets(Context context, Function<String, Boolean> decider, String source, File folder) {
        AssetManager assets = context.getAssets();
        if (!folder.exists())
            if (!folder.mkdirs()) throw new IllegalStateException("Folder couldn't get created");


        try {
            String[] files = assets.list(source);

            for (String file : files) {
                if (decider.apply(file)) {
                    try (InputStream assetStream = assets.open(source + "/" + file)) {
                        copy(assetStream, new FileOutputStream(new File(folder, file)));
                    } catch (IOException e) {
                        Log.w(TAG, "copyFileFromAssets: File was returned from assets.list but does not exist. Trying to open it as an directory: " + e.getMessage());
                        copyFileFromAssets(context, decider, source + "/" + file, new File(folder, file));
                    }
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "copyFileFromAssets", e);
        }
    }


    public static void copy(InputStream from, FileOutputStream to) {

        try (BufferedInputStream fromStream = new BufferedInputStream(from); BufferedOutputStream toStream = new BufferedOutputStream(to)) {


            byte[] buffer = new byte[1024];
            int lenghtRead;
            while ((lenghtRead = fromStream.read(buffer)) > 0) {
                toStream.write(buffer, 0, lenghtRead);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }



    public static class IfNotExistsFunction implements Function<String, Boolean> {

        private File outputDir;

        public IfNotExistsFunction(File outputDir) {
            this.outputDir = outputDir;
        }

        @Override
        public Boolean apply(String input) {
            File file = new File(outputDir, input);
            return !file.exists() || file.isDirectory();
        }
    }
}
