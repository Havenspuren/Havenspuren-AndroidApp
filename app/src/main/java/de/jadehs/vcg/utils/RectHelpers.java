package de.jadehs.vcg.utils;

import android.graphics.Rect;
import android.graphics.RectF;

public class RectHelpers {


    public static Rect toRect(RectF src){
        Rect dst = new Rect();
        toRect(src,dst);
        return dst;
    }


    public static void toRect(RectF src, Rect dst){
        dst.set((int)src.left,(int)src.top,(int)src.right,(int)src.bottom);
    }

    public static RectF toRectF(Rect src){
        RectF dst = new RectF();
        toRectF(src,dst);
        return dst;
    }

    public static void toRectF(Rect src, RectF dst){
        dst.set((float)src.left,(float)src.top,(float)src.right,(float)src.bottom);
    }
}
