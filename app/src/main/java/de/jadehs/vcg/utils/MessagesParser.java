package de.jadehs.vcg.utils;

import com.stfalcon.chatkit.commons.models.IMessage;
import com.stfalcon.chatkit.commons.models.IUser;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import de.jadehs.vcg.data.model.ImageMessage;
import de.jadehs.vcg.data.model.TextMessage;

/**
 * parses the given string as multiple messages
 * <p>
 * This instance can't be reused
 */
public class MessagesParser {

    /**
     * User which is used for messages of the user
     */
    public static final IUser SELF_USER = new IUser() {
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
    /**
     * User which is used for messages of the tour guide
     */
    public static final IUser OTHER_USER = new IUser() {
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

    /**
     * Retruns all messages of the given string
     *
     * @return all messages
     */
    public List<IMessage> parseMessages() {
        List<IMessage> messages = new LinkedList<>();
        while (linesLeft()) {
            messages.add(getNextMessage());
        }

        return messages;
    }


    /**
     * Returns the next message, should only be used when parseMessages
     *
     * @return
     */
    public IMessage getNextMessage() {
        ensureLinesLeft();
        if (!isMessageStart()) {
            throw new IllegalStateException("not start of message");
        }

        StringBuilder text = new StringBuilder();
        IUser user = null;
        boolean isImage = false;
        boolean isAudio = false;

        do {
            if (isMessageStart()) {
                String line = getLine();
                String userPrefix = line.substring(0, SELF_PREFIX.length());
                line = line.substring(SELF_PREFIX.length());
                if (userPrefix.equals(SELF_PREFIX)) {
                    user = SELF_USER;
                } else if (userPrefix.equals(OTHER_PREFIX)) {
                    user = OTHER_USER;
                } else {
                    throw new IllegalStateException("Illegal User identifier. Must be one of " + Arrays.toString(LINE_START_PREFIXES));
                }

                if (!line.startsWith(AFTER_PREFIX_SEPERATOR)) {
                    if (line.startsWith(IMAGE_PREFIX)) {
                        isImage = true;
                    } else if (line.startsWith(AUDIO_PREFIX)) {
                        isAudio = true;
                    } else {
                        throw new IllegalStateException("Illegal Content identifier");
                    }
                    line = line.substring(IMAGE_PREFIX.length());
                }
                if (!line.startsWith(AFTER_PREFIX_SEPERATOR)) {
                    throw new IllegalStateException("Unexpected Token in line " + line);
                }

                text.append(line.substring(AFTER_PREFIX_SEPERATOR.length()));

            } else {
                text.append(getLine());
            }

            text.append(System.lineSeparator());

            if (isImage || isAudio) {
                break;
            }
        } while (linesLeft() && !isMessageStart());

        IMessage message;

        String id = "m" + line;
        String buildText = text.substring(0, text.length() - 1);
        if (isImage) {
            message = new ImageMessage(id, buildText, user);
        } else if (isAudio) {
            throw new UnsupportedOperationException("not yet implemented");
        } else {
            message = new TextMessage(id, buildText, user);
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
        ensureLinesLeft();
        return originalLines[line++];
    }

    private String peekLine() {
        ensureLinesLeft();
        return originalLines[line];
    }

    private boolean linesLeft() {
        return line < originalLines.length;
    }

    private void ensureLinesLeft() {
        if (!linesLeft()) {
            throw new IllegalStateException("No more lines left!");
        }
    }

}
