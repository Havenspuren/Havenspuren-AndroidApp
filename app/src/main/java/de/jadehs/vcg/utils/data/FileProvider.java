package de.jadehs.vcg.utils.data;

import android.content.Context;
import android.net.Uri;

import java.io.File;

import de.jadehs.vcg.MyApplicationKt;
import de.jadehs.vcg.data.db.models.Media;

/**
 * does implement some helper functions to get the fill file path of files inside storage
 */
public class FileProvider {

    private final Context context;
    private final File mediaRoot;
    private final File mapsRoot;

    public FileProvider(Context context) {
        this.context = context;
        this.mediaRoot = new File(this.context.getFilesDir(), MyApplicationKt.MEDIA_FILES_PATH);
        this.mapsRoot = new File(this.context.getFilesDir(), MyApplicationKt.MAPS_FILES_PATH);
    }

    public File getMediaFile(String fileName) {
        return new File(this.mediaRoot, fileName);
    }

    public File getMediaFile(Media media) {
        return getMediaFile(media.getPathToFile());
    }

    public Uri getMediaUri(String fileName) {
        return Uri.fromFile(getMediaFile(fileName));
    }

    public Uri getMediaUri(Media media) {
        return this.getMediaUri(media.getPathToFile());
    }

    public File getMapsFile(String path) {
        if(!path.endsWith(".map")){
            path += ".map";
        }
        return new File(this.mapsRoot, path);
    }

    public Uri getMapsUri(String path) {
        return Uri.fromFile(getMapsFile(path));
    }

    public File getHopperFolder(String path) {
        if(!path.endsWith("-gh")){
            path += "-gh";
        }
        return new File(this.mapsRoot, path);
    }

    public Uri getHopperFolderUri(String path) {
        return Uri.fromFile(getHopperFolder(path));
    }

    public File getMediaRoot() {
        return this.mediaRoot;
    }

    public File getMapsRoot() {
        return this.mapsRoot;
    }
}
