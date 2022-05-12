package de.jadehs.vcg.data.db.converter;

import android.provider.MediaStore;

import androidx.room.TypeConverter;

import de.jadehs.vcg.data.db.models.Media;

public class Converters {

    @TypeConverter
    public static Media.MediaType fromIntToMediaType(int value){
        return Media.MediaType.fromInt(value);
    }

    @TypeConverter
    public static int MediaTypeToInt(Media.MediaType type){
        return type.getType();
    }



}
