package de.jadehs.vcg.data.db.models;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Class which holds information about various Media files saved inside the file system
 */
@Entity
public class Media implements Serializable {

    /**
     * Enum which contains all media types a file can have. This list can change at any time
     */
    public enum MediaType {
        AUDIO(1),PICTURES(2),AR(3);
        private static final Map<Integer, MediaType> reverseLookup = new HashMap<>();
        public static MediaType fromInt(int value){

            return reverseLookup.get(value);
        }

        static {
            for (MediaType type: MediaType.values()) {
                reverseLookup.put(type.getType(), type);
            }
        }
        private final int type;

        MediaType(int type) {
            this.type = type;
        }

        public int getType() {
            return type;
        }
    }


    @PrimaryKey(autoGenerate = true)
    private long id;
    /**
     * relative path, from the media root directory to a file
     */
    @ColumnInfo(name = "path_to_file")
    private String pathToFile;
    /**
     * extra info about the file. For pictures it usually contains a description, for audio files it contains a transcription and for ar files it can contain some meta data
     */
    private String extra;
    /**
     * whether it is a audio file, a picture or an ar file
     */
    private MediaType type;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getPathToFile() {
        return pathToFile;
    }

    public void setPathToFile(String pathToFile) {
        this.pathToFile = pathToFile;
    }

    public String getExtra() {
        return extra;
    }

    public void setExtra(String extra) {
        this.extra = extra;
    }

    public MediaType getType() {
        return type;
    }

    public void setType(MediaType type) {
        this.type = type;
    }
}
