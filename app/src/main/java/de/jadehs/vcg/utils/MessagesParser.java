package de.jadehs.vcg.utils;

import com.stfalcon.chatkit.commons.models.IMessage;
import com.stfalcon.chatkit.commons.models.IUser;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import de.jadehs.vcg.data.model.ImageMessage;
import de.jadehs.vcg.data.model.TextMessage;

public class MessagesParser {

    private static final IUser SELF_USER = new IUser() {
        @Override
        public String getId() {
            return "me";
        }

        @Override
        public String getName() {
            return "Current User";
        }

        @Override
        public String getAvatar() {
            return null;
        }
    };
    private static final IUser OTHER_USER = new IUser() {
        @Override
        public String getId() {
            return "ai";
        }

        @Override
        public String getName() {
            return "Current User";
        }

        @Override
        public String getAvatar() {
            return null;
        }
    };

    private static final String SELF_PREFIX = "+";
    private static final String OTHER_PREFIX = "-";
    private static final String IMAGE_PREFIX = "#";
    private static final String AUDIO_PREFIX = "!";
    private static final String AFTER_PREFIX_SEPERATOR = " ";

    private static final String[] LINE_START_PREFIXES = {SELF_PREFIX, OTHER_PREFIX};


    private final String[] originalLines;

    private int line = 0;

    public MessagesParser(String originalMessage) {
        this.originalLines = originalMessage.split(System.lineSeparator());
    }

    private List<IMessage> parseMessages() {
        List<IMessage> messages = new LinkedList<>();
        while (linesLeft()) {
            messages.add(getNextMessage());
        }

        return messages;
    }


    public IMessage getNextMessage() {
        if (!isMessageStart()) {
            throw new IllegalStateException("not start of message");
        }

        StringBuilder text = new StringBuilder();
        String mediaPath = "";
        IUser user = null;
        boolean isImage = false;
        boolean isAudio = false;

        do {
            if (isMessageStart()) {
                String line = getLine();
                String userPrefix = line.substring(0, 1);
                String woUserPrefix = line.substring(1);
                if (userPrefix.equals(SELF_PREFIX)) {
                    user = SELF_USER;
                } else if (userPrefix.equals(OTHER_PREFIX)) {
                    user = OTHER_USER;
                } else {
                    throw new IllegalStateException("Illegal User identifier. Must be one of " + Arrays.toString(LINE_START_PREFIXES));
                }

                if (woUserPrefix.startsWith(AFTER_PREFIX_SEPERATOR)) {
                    text.append(woUserPrefix.substring(AFTER_PREFIX_SEPERATOR.length()));
                } else {
                    if (woUserPrefix.startsWith(IMAGE_PREFIX)) {
                        isImage = true;
                    } else if (woUserPrefix.startsWith(AUDIO_PREFIX)) {
                        isAudio = true;
                    } else {
                        throw new IllegalStateException("adf");
                    }


                }
            } else {
                text.append(getLine());
            }
        } while (linesLeft() && isMessageStart());

        IMessage message;

        String id = "m" + line;
        if (isImage) {
            message = new ImageMessage(id, mediaPath, user);
        } else if (isAudio) {
            message = new TextMessage(id, mediaPath, user);
        } else {
            message = new TextMessage(id, text.toString(), user);
        }

        return message;
    }


    private boolean isMessageStart() {

        for (String prefix : LINE_START_PREFIXES) {
            if (peekLine().startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    private String getLine() {
        return originalLines[line++];
    }

    private String peekLine() {
        return originalLines[line];
    }

    private boolean linesLeft() {
        return line < originalLines.length;
    }

}
