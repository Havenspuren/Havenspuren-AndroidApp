package de.jadehs.vcg.data.model;

import com.stfalcon.chatkit.commons.models.IMessage;
import com.stfalcon.chatkit.commons.models.IUser;

import java.util.Date;

public class TextMessage implements IMessage {

    private final String id;
    private final String text;
    private final IUser user;

    public TextMessage(String id, String text, IUser user) {
        this.id = id;
        this.text = text;
        this.user = user;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getText() {
        return text;
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
