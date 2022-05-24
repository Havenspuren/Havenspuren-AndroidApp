package de.jadehs.vcg.data.model;

import androidx.annotation.Nullable;

import com.stfalcon.chatkit.commons.models.IUser;
import com.stfalcon.chatkit.commons.models.MessageContentType;

import java.util.Date;

public class ImageMessage implements MessageContentType.Image {

    private final String id;
    private final String image;
    private final IUser user;

    public ImageMessage(String id, String image, IUser user) {
        this.id = id;
        this.image = image;
        this.user = user;
    }
    @Nullable
    @Override
    public String getImageUrl() {
        return image;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getText() {
        return "test image text";
    }

    @Override
    public IUser getUser() {
        return user;
    }

    @Override
    public Date getCreatedAt() {
        return new Date();
    }
}
