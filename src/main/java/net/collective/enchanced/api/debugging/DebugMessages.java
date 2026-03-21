package net.collective.enchanced.api.debugging;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

public interface DebugMessages {
    List<String> clientMessages = new ArrayList<>();
    List<String> serverMessages = new ArrayList<>();

    static void addClient(String text, Object... params) {
        clientMessages.add(params.length == 0 ? text : MessageFormat.format(text, params));
    }

    static void addServer(String text, Object... params) {
        serverMessages.add(params.length == 0 ? text : MessageFormat.format(text, params));
    }

    static void addCommon(String text, Object... params) {
        addClient(text, params);
        addServer(text, params);
    }
}
