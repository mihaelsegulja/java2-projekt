package hr.algebra.uno.util;

import hr.algebra.uno.jndi.ConfigurationKey;
import hr.algebra.uno.jndi.ConfigurationReader;
import hr.algebra.uno.rmi.ChatRemoteService;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;
import java.util.Optional;

import static hr.algebra.uno.UnoApplication.playerType;

public class ChatUtils {
    private static final Logger log = LoggerFactory.getLogger(ChatUtils.class);

    private ChatUtils() {}

    public static Timeline getChatRefreshTimeline(ChatRemoteService chatRemoteService, TextArea chatMessageTextArea)
    {
        Timeline chatMessagesRefreshTimeLine = new Timeline(new KeyFrame(Duration.ZERO, e -> {
            try {
                List<String> chatMessages =  chatRemoteService.getAllMessages();
                StringBuilder textMessagesBuilder = new StringBuilder();

                for(String message : chatMessages) {
                    textMessagesBuilder.append(message).append("\n");
                }

                chatMessageTextArea.setText(textMessagesBuilder.toString());

            } catch (RemoteException ex) {
                log.error("Failed to refresh chat timeline", ex);
            }
        }), new KeyFrame(Duration.seconds(1)));

        chatMessagesRefreshTimeLine.setCycleCount(Animation.INDEFINITE);
        return chatMessagesRefreshTimeLine;
    }

    public static void sendChatMessage(ChatRemoteService chatRemoteService, TextField chatMessageTextField)
    {
        String chatMessage = chatMessageTextField.getText();
        if(chatMessage.isBlank()) return;
        chatMessageTextField.clear();
        try {
            chatRemoteService.sendChatMessage(playerType + ": " + chatMessage);
        } catch (RemoteException e) {
            log.error("Failed to send chat message", e);
        }
    }

    public static Optional<ChatRemoteService> initializeChatRemoteService() {

        Optional<ChatRemoteService> chatRemoteServiceOptional = Optional.empty();

        try {
            Registry registry = LocateRegistry.getRegistry(
                    ConfigurationReader.getStringValueForKey(ConfigurationKey.HOSTNAME),
                    ConfigurationReader.getIntegerValueForKey(ConfigurationKey.RMI_SERVER_PORT));
            chatRemoteServiceOptional = Optional.of((ChatRemoteService) registry.lookup(ChatRemoteService.REMOTE_OBJECT_NAME));
        } catch (RemoteException | NotBoundException e) {
            log.error("Failed to initialize chat remote service", e);
        }

        return chatRemoteServiceOptional;
    }
}
