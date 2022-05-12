package de.jadehs.vcg.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.File;

public class ImageUtils {


    public static Bitmap getImageOfBounds(File picture, int width, int height){
        if(picture == null) return null;



        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;


        BitmapFactory.decodeFile(picture.getAbsolutePath(),options);
        options.inSampleSize = calculateInSampleSize(options,width,height);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(picture.getAbsolutePath(),options);
    }


    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;

                if(inSampleSize<0){
                    return 1;
                }
            }
        }

        return inSampleSize;
    }
}
