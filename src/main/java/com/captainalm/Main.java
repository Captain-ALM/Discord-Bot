package com.captainalm;

import com.captainalm.discord.Bot;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.concurrent.CompletionException;

public class Main {
    public static HashMap<String, String> settings = new HashMap<String, String>();
    public static String token;
    public static String manager = "";
    public static Bot bot;

    public static void main(String[] args) {
        writeLine("Starting the Captain-ALM-Bot...");
        if (args != null) {
            decryptArgs(args);
            if (settings.containsKey("token")) {
                try {
                    token = new String(Base64.getDecoder().decode(settings.get("token")), StandardCharsets.UTF_8);
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                    writeLine("Captain-ALM-Bot Has Encountered an Invalid Token!");
                    System.exit(1);
                }
                if (settings.containsKey("manager")) {
                    manager = settings.get("manager");
                }
                if (!manager.contains("#")) {
                    manager = "";
                }
                try {
                    bot = new Bot(token, manager);
                } catch (CompletionException e) {
                    e.printStackTrace();
                    writeLine("Captain-ALM-Bot Has Encountered a Login Failure!");
                    System.exit(1);
                }
                writeLine("Captain-ALM-Bot Has Logged In!");
                bot.registerListeners();
                bot.waitForExit();
                bot.unregisterListeners();
                bot.disconnect();
                writeLine("Captain-ALM-Bot Has Logged Out!");
            } else {
                writeLine("Captain-ALM-Bot Needs a Token!");
                System.exit(1);
            }
        } else {
            // printUsage();
            System.exit(1);
        }
        System.exit(0);
    }

    public static void decryptArgs(String[] args) {
        for (String carg : args) {
            boolean hasEquals = carg.contains("=");
            boolean isSwitch = carg.startsWith("-");
            String cSwitch = "";
            String cValue = "";
            if (isSwitch && !hasEquals) {
                cSwitch = carg.substring(1).toLowerCase();
            } else if (isSwitch) {
                cSwitch = carg.substring(1, carg.indexOf("=")).toLowerCase();
                cValue = carg.substring(carg.indexOf("=") + 1);
            }
            if (!settings.containsKey(cSwitch)) {
                settings.put(cSwitch, cValue);
            }
        }
    }

    public static void write(String out) {
        System.out.print(out);
    }

    public static void writeLine(String out) {
        System.out.println(out);
    }
}
