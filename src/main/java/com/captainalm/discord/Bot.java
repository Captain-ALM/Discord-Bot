package com.captainalm.discord;

import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.message.Messageable;
import org.javacord.api.entity.user.UserStatus;
import org.javacord.api.listener.message.MessageCreateListener;
import org.javacord.api.util.event.ListenerManager;

public class Bot {
    private final DiscordApi api;
    private boolean executing;
    private final Object waitLock = new Object();
    private final boolean hasManager;
    private final String manager;
    private ListenerManager<MessageCreateListener> managerListener;

    public Bot(String token, String manager) {
        executing = true;
        if (manager == null) manager = "";
        this.manager = manager;
        api = new DiscordApiBuilder().setToken(token).setAllIntents().login().join();
        api.updateStatus(UserStatus.DO_NOT_DISTURB);
        api.updateActivity("Starting...");
        hasManager = !manager.equals("");
        if (hasManager) {
            api.getCachedUserByDiscriminatedName(manager).ifPresent(u -> {
                sendMessage(u, "Starting...");
            });
        }
    }

    public void registerListeners() {
        managerListener = api.addMessageCreateListener(message -> {
            if (message.isPrivateMessage()) {
                String cmd = message.getReadableMessageContent().toLowerCase();
                if (cmd.equals("%manager%") && hasManager) sendMessage(message.getChannel(), manager);
                else if (cmd.equals("%halt%") && manager.equals(message.getMessageAuthor().getDiscriminatedName())) {
                    writeLine("Captain-ALM-Bot Has Been Told to Terminate!");
                    sendMessage(message.getChannel(),"Terminating...");
                    executing = false;
                    synchronized (waitLock) {
                        waitLock.notifyAll();
                    }
                }
            }
        });
        api.updateStatus(UserStatus.ONLINE);
        api.unsetActivity();
    }

    public void waitForExit() {
        synchronized (waitLock) {
            while (executing) {
                try {
                    waitLock.wait();
                } catch (InterruptedException e) {
                    executing = false;
                }
            }
        }
    }

    public void unregisterListeners() {
        api.updateStatus(UserStatus.DO_NOT_DISTURB);
        api.updateActivity("Stopping...");
        managerListener.remove();
    }

    public void disconnect() {
        api.disconnect();
    }

    private void sendMessage(Messageable channel, String content) {
        channel.sendMessage(content).exceptionally(e -> {
            e.printStackTrace();
            return null;
        });
    }

    public static void writeLine(String out) {
        System.out.println(out);
    }
}
