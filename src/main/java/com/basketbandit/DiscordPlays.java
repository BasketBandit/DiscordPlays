package com.basketbandit;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.react.GenericGuildMessageReactionEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class DiscordPlays implements EventListener {
    private static final Logger log = LoggerFactory.getLogger(DiscordPlays.class);
    private static Robot robot;

    // Discord Functionality
    private static boolean discordEnabled;
    private static JDA jda;
    private static String botToken;
    private static String controlChannelId;
    private static TextChannel controlChannel;
    private static boolean bHeld;

    // Socket Server Functionality
    private static boolean socketServerEnabled;
    private static int serverSocketPort;
    private static ServerSocket serverSocket;

    public static void main(String[] args) {
        new DiscordPlays();
    }

    public DiscordPlays() {
        try(InputStream inputStream = new FileInputStream("./config.yaml")) {
            Map<String, String> config = new Yaml().load(inputStream);
            discordEnabled = Boolean.parseBoolean(config.get("discord_enabled"));
            botToken = config.get("bot_token");
            controlChannelId = config.get("control_channel_id");
            socketServerEnabled = Boolean.parseBoolean(config.get("socket_server_enabled"));
            serverSocketPort = Integer.parseInt(config.get("socket_server_port"));
        } catch(IOException e) {
            log.error("There was an error loading the configuration file, message: {}", e.getMessage(), e);
        }

        try {
            robot = new Robot();
        } catch(AWTException e) {
            log.error("There was a problem setting up the robot, message: {}", e.getMessage());
        }

        if(discordEnabled) {
            initDiscord();
        }

        if(socketServerEnabled) {
            initSocketServer();
        }
    }

    private void initDiscord() {
        try {
            jda = JDABuilder.createDefault(botToken).addEventListeners(this).build();
            jda.awaitReady();
            controlChannel = jda.getTextChannelById(Long.parseLong(controlChannelId));
            log.info("Discord control channel set to: " + controlChannel.getAsMention());
        } catch(Exception e) {
            log.error("There was a problem with JDA, message: {}", e.getMessage());
        }
    }

    private void initSocketServer() {
        try {
            log.info("Starting socket server on port " + serverSocketPort);
            serverSocket = new ServerSocket(serverSocketPort);
            while(true) {
                new DiscordPlaysSocketClientHandler(serverSocket.accept()).start();
            }
        } catch(IOException e) {
            log.error("There was a problem with the socket server, message: {}", e.getMessage());
        }
    }

    private static class DiscordPlaysSocketClientHandler extends Thread {
        private final Socket clientSocket;
        private final String hostAddress;

        public DiscordPlaysSocketClientHandler(Socket socket) {
            this.clientSocket = socket;
            this.hostAddress = clientSocket.getInetAddress().getHostAddress();
        }

        public void run() {
            try {
                log.info(clientSocket.getInetAddress().getHostAddress() + "-chan connected! :D");
                PrintWriter out = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream(), StandardCharsets.UTF_8), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), StandardCharsets.UTF_8));
                out.println("Hey " + clientSocket.getInetAddress().getHostAddress() + "-chan! u/////u");

                String inputLine;
                while((inputLine = in.readLine()) != null) {
                    if(".".equals(inputLine)) {
                        out.println("Bye bye " + clientSocket.getInetAddress().getHostAddress() + "-chan!");
                        break;
                    }
                    out.println("Gotcha!");
                    executeWSInput(hostAddress, inputLine);
                }

                in.close();
                out.close();
                clientSocket.close();
                log.info(clientSocket.getInetAddress().getHostAddress() + "-chan disconnect! :(");
            } catch(Exception e) {
                log.error("There was an problem with the socket client, message: {}", e.getMessage());
            }
        }
    }

    @Override
    public void onEvent(@NotNull GenericEvent event) {
        if(event instanceof GenericGuildMessageReactionEvent) {
            if(((GenericGuildMessageReactionEvent) event).getUser().isBot()) {
                return; // no bots allowed :)
            }

            GenericGuildMessageReactionEvent e = (GenericGuildMessageReactionEvent) event;
            if(e.getChannel() == controlChannel) {
                executeDiscordInput(e);
                return;
            }
        }

        if(event instanceof GuildMessageReceivedEvent) {
            if(((GuildMessageReceivedEvent) event).getAuthor().isBot()) {
                return; // no bots allowed :)
            }

            GuildMessageReceivedEvent e = (GuildMessageReceivedEvent) event;
            if(e.getMessage().getContentRaw().equals("?setupController")) {
                controlChannel.sendMessage("Movement").queue(s -> {
                    s.addReaction("⬅️").queue();
                    s.addReaction("⬆️").queue();
                    s.addReaction("➡️").queue();
                    s.addReaction("⬇️").queue();
                    s.addReaction("↖️").queue();
                    s.addReaction("↗️").queue();
                    s.addReaction("↙️").queue();
                    s.addReaction("↘️").queue();
                });

                controlChannel.sendMessage("Advanced Movement").queue(s -> {
                    s.addReaction("⏪").queue();
                    s.addReaction("⏫").queue();
                    s.addReaction("⏩").queue();
                    s.addReaction("⏬").queue();
                    s.addReaction("🅱️").queue();
                });

                controlChannel.sendMessage("Action ").queue(s -> {
                    s.addReaction("🇦").queue();
                    s.addReaction("🇧").queue();
                    s.addReaction("🇽").queue();
                    s.addReaction("🇾").queue();
                    s.addReaction("🇱").queue();
                    s.addReaction("🇷").queue();
                });

                controlChannel.sendMessage("Special").queue(s -> {
                    s.addReaction("⏸️").queue();
                    s.addReaction("⏯️").queue();
                    s.addReaction("🔻").queue();
                    s.addReaction("⌛").queue();
                });
            }
        }
    }

    /**
     * Deals directly with input from Socket/XInputs (Xbox 360, PS4 [ds4windows])
     * @param input String
     */
    private static void executeWSInput(String hostAddress, String input) {
        if(input.contains("_")) {
            // Socket XInput
            switch(input) {
                case "DPAD_UP_TRUE": {
                    robot.keyPress(KeyEvent.VK_UP);
                    log.info(hostAddress + " | UP");
                    break;
                }
                case "DPAD_UP_FALSE": {
                    robot.keyRelease(KeyEvent.VK_UP);
                    break;
                }
                case "DPAD_LEFT_TRUE": {
                    robot.keyPress(KeyEvent.VK_LEFT);
                    log.info(hostAddress + " | LEFT");
                    break;
                }
                case "DPAD_LEFT_FALSE": {
                    robot.keyRelease(KeyEvent.VK_LEFT);
                    break;
                }
                case "DPAD_RIGHT_TRUE": {
                    robot.keyPress(KeyEvent.VK_RIGHT);
                    log.info(hostAddress + " | RIGHT");
                    break;
                }
                case "DPAD_RIGHT_FALSE": {
                    robot.keyRelease(KeyEvent.VK_RIGHT);
                    break;
                }
                case "DPAD_DOWN_TRUE": {
                    robot.keyPress(KeyEvent.VK_DOWN);
                    log.info(hostAddress + " | DOWN");
                    break;
                }
                case "DPAD_DOWN_FALSE": {
                    robot.keyRelease(KeyEvent.VK_DOWN);
                    break;
                }
                case "A_TRUE": {
                    robot.keyPress(KeyEvent.VK_Z);
                    log.info(hostAddress + " | B");
                    break;
                }
                case "A_FALSE": {
                    robot.keyRelease(KeyEvent.VK_Z);
                    break;
                }
                case "B_TRUE": {
                    robot.keyPress(KeyEvent.VK_X);
                    log.info(hostAddress + " | A");
                    break;
                }
                case "B_FALSE": {
                    robot.keyRelease(KeyEvent.VK_X);
                    break;
                }
                case "X_TRUE": {
                    robot.keyPress(KeyEvent.VK_A);
                    log.info(hostAddress + " | Y");
                    break;
                }
                case "X_FALSE": {
                    robot.keyRelease(KeyEvent.VK_A);
                    break;
                }
                case "Y_TRUE": {
                    robot.keyPress(KeyEvent.VK_S);
                    log.info(hostAddress + " | X");
                    break;
                }
                case "Y_FALSE": {
                    robot.keyRelease(KeyEvent.VK_S);
                    break;
                }
                case "LEFT_SHOULDER_TRUE": {
                    robot.keyPress(KeyEvent.VK_J);
                    log.info(hostAddress + " | L");
                    break;
                }
                case "LEFT_SHOULDER_FALSE": {
                    robot.keyRelease(KeyEvent.VK_J);
                    break;
                }
                case "RIGHT_SHOULDER_TRUE": {
                    robot.keyPress(KeyEvent.VK_K);
                    log.info(hostAddress + " | R");
                    break;
                }
                case "RIGHT_SHOULDER_FALSE": {
                    robot.keyRelease(KeyEvent.VK_K);
                    break;
                }
                case "START_TRUE": {
                    robot.keyPress(KeyEvent.VK_ENTER);
                    log.info(hostAddress + " | START");
                    break;
                }
                case "START_FALSE": {
                    robot.keyRelease(KeyEvent.VK_ENTER);
                    break;
                }
                case "BACK_TRUE": {
                    robot.keyPress(KeyEvent.VK_H);
                    log.info(hostAddress + " | SELECT");
                    break;
                }
                case "BACK_FALSE": {
                    robot.keyRelease(KeyEvent.VK_H);
                    break;
                }
            }
        } else {
            // Socket Default
            switch(input) {
                case "L" : {
                    robot.keyPress(KeyEvent.VK_LEFT);
                    robot.delay(100);
                    robot.keyRelease(KeyEvent.VK_LEFT);
                    log.info(hostAddress + " | LEFT");
                    break;
                }
                case "U" : {
                    robot.keyPress(KeyEvent.VK_UP);
                    robot.delay(100);
                    robot.keyRelease(KeyEvent.VK_UP);
                    log.info(hostAddress + " | UP");
                    break;
                }
                case "R" : {
                    robot.keyPress(KeyEvent.VK_RIGHT);
                    robot.delay(100);
                    robot.keyRelease(KeyEvent.VK_RIGHT);
                    log.info(hostAddress + " | RIGHT");
                    break;
                }
                case "D" : {
                    robot.keyPress(KeyEvent.VK_DOWN);
                    robot.delay(100);
                    robot.keyRelease(KeyEvent.VK_DOWN);
                    log.info(hostAddress + " | DOWN");
                    break;
                }
                case "UL" : {
                    robot.keyPress(KeyEvent.VK_Q);
                    robot.delay(100);
                    robot.keyRelease(KeyEvent.VK_Q);
                    log.info(hostAddress + " | UP + LEFT");
                    break;
                }
                case "UR" : {
                    robot.keyPress(KeyEvent.VK_W);
                    robot.delay(100);
                    robot.keyRelease(KeyEvent.VK_W);
                    log.info(hostAddress + " | UP + RIGHT");
                    break;
                }
                case "DR" : {
                    robot.keyPress(KeyEvent.VK_E);
                    robot.delay(100);
                    robot.keyRelease(KeyEvent.VK_E);
                    log.info(hostAddress + " | DOWN + RIGHT");
                    break;
                }
                case "DL" : {
                    robot.keyPress(KeyEvent.VK_R);
                    robot.delay(100);
                    robot.keyRelease(KeyEvent.VK_R);
                    log.info(hostAddress + " | DOWN + LEFT");
                    break;
                }
                case "L1" : {
                    robot.keyPress(KeyEvent.VK_LEFT);
                    robot.delay(1000);
                    robot.keyRelease(KeyEvent.VK_LEFT);
                    log.info(hostAddress + " | LEFT (1s)");
                    break;
                }
                case "U1" : {
                    robot.keyPress(KeyEvent.VK_UP);
                    robot.delay(1000);
                    robot.keyRelease(KeyEvent.VK_UP);
                    log.info(hostAddress + " | UP (1s)");
                    break;
                }
                case "R1" : {
                    robot.keyPress(KeyEvent.VK_RIGHT);
                    robot.delay(1000);
                    robot.keyRelease(KeyEvent.VK_RIGHT);
                    log.info(hostAddress + " | RIGHT (1s)");
                    break;
                }
                case "D1" : {
                    robot.keyPress(KeyEvent.VK_DOWN);
                    robot.delay(1000);
                    robot.keyRelease(KeyEvent.VK_DOWN);
                    log.info(hostAddress + " | DOWN (1s)");
                    break;
                }
                case "BH" : {
                    bHeld = !bHeld;
                    if(bHeld) {
                        robot.keyPress(KeyEvent.VK_Z);
                    } else {
                        robot.keyRelease(KeyEvent.VK_Z);
                    }
                    log.info(hostAddress + " | B (hold = " + bHeld + ")");
                    break;
                }
                case "LB" : {
                    robot.keyPress(KeyEvent.VK_J);
                    robot.delay(100);
                    robot.keyRelease(KeyEvent.VK_J);
                    log.info(hostAddress + " | L");
                    break;
                }
                case "RB" : {
                    robot.keyPress(KeyEvent.VK_K);
                    robot.delay(100);
                    robot.keyRelease(KeyEvent.VK_K);
                    log.info(hostAddress + " | R");
                    break;
                }
                case "A" : {
                    robot.keyPress(KeyEvent.VK_X);
                    robot.delay(100);
                    robot.keyRelease(KeyEvent.VK_X);
                    log.info(hostAddress + " | A");
                    break;
                }
                case "B" : {
                    robot.keyPress(KeyEvent.VK_Z);
                    robot.delay(100);
                    robot.keyRelease(KeyEvent.VK_Z);
                    if(bHeld) {
                        bHeld = false;
                    }
                    log.info(hostAddress + " | B");
                    break;}
                case "X" : {
                    robot.keyPress(KeyEvent.VK_S);
                    robot.delay(100);
                    robot.keyRelease(KeyEvent.VK_S);
                    log.info(hostAddress + " | X");
                    break;
                }
                case "Y" : {
                    robot.keyPress(KeyEvent.VK_A);
                    robot.delay(100);
                    robot.keyRelease(KeyEvent.VK_A);
                    log.info(hostAddress + " | Y");
                    break;
                }
                case "P" : {
                    robot.keyPress(KeyEvent.VK_ENTER);
                    robot.delay(100);
                    robot.keyRelease(KeyEvent.VK_ENTER);
                    log.info(hostAddress + " | START");
                    break;
                }
                case "S" : {
                    robot.keyPress(KeyEvent.VK_H);
                    robot.delay(100);
                    robot.keyRelease(KeyEvent.VK_H);
                    log.info(hostAddress + " | SELECT");
                    break;
                }
            }
        }
    }

    /**
     * Deals directly with input from Discord.
     * @param e {@link GenericGuildMessageReactionEvent}
     */
    private static void executeDiscordInput(GenericGuildMessageReactionEvent e) {
        switch(e.getReactionEmote().getAsReactionCode()) {
            case "⬅️" : {
                robot.keyPress(KeyEvent.VK_LEFT);
                robot.delay(100);
                robot.keyRelease(KeyEvent.VK_LEFT);
                log.info(e.getUser().getAsTag() + " | LEFT");
                break;
            }
            case "⬆️" : {
                robot.keyPress(KeyEvent.VK_UP);
                robot.delay(100);
                robot.keyRelease(KeyEvent.VK_UP);
                log.info(e.getUser().getAsTag() + " | UP");
                break;
            }
            case "➡️" : {
                robot.keyPress(KeyEvent.VK_RIGHT);
                robot.delay(100);
                robot.keyRelease(KeyEvent.VK_RIGHT);
                log.info(e.getUser().getAsTag() + " | RIGHT");
                break;
            }
            case "⬇️" : {
                robot.keyPress(KeyEvent.VK_DOWN);
                robot.delay(100);
                robot.keyRelease(KeyEvent.VK_DOWN);
                log.info(e.getUser().getAsTag() + " | DOWN");
                break;
            }
            case "↖️" : {
                robot.keyPress(KeyEvent.VK_Q);
                robot.delay(100);
                robot.keyRelease(KeyEvent.VK_Q);
                log.info(e.getUser().getAsTag() + " | UP + LEFT");
                break;
            }
            case "↗️" : {
                robot.keyPress(KeyEvent.VK_W);
                robot.delay(100);
                robot.keyRelease(KeyEvent.VK_W);
                log.info(e.getUser().getAsTag() + " | UP + RIGHT");
                break;
            }
            case "↘️" : {
                robot.keyPress(KeyEvent.VK_E);
                robot.delay(100);
                robot.keyRelease(KeyEvent.VK_E);
                log.info(e.getUser().getAsTag() + " | DOWN + RIGHT");
                break;
            }
            case "↙️" : {
                robot.keyPress(KeyEvent.VK_R);
                robot.delay(100);
                robot.keyRelease(KeyEvent.VK_R);
                log.info(e.getUser().getAsTag() + " | DOWN + LEFT");
                break;
            }
            case "⏪" : {
                robot.keyPress(KeyEvent.VK_LEFT);
                robot.delay(1000);
                robot.keyRelease(KeyEvent.VK_LEFT);
                log.info(e.getUser().getAsTag() + " | LEFT (1s)");
                break;
            }
            case "⏫" : {
                robot.keyPress(KeyEvent.VK_UP);
                robot.delay(1000);
                robot.keyRelease(KeyEvent.VK_UP);
                log.info(e.getUser().getAsTag() + " | UP (1s)");
                break;
            }
            case "⏩" : {
                robot.keyPress(KeyEvent.VK_RIGHT);
                robot.delay(1000);
                robot.keyRelease(KeyEvent.VK_RIGHT);
                log.info(e.getUser().getAsTag() + " | RIGHT (1s)");
                break;
            }
            case "⏬" : {
                robot.keyPress(KeyEvent.VK_DOWN);
                robot.delay(1000);
                robot.keyRelease(KeyEvent.VK_DOWN);
                log.info(e.getUser().getAsTag() + " | DOWN (1s)");
                break;
            }
            case "🅱️" : {
                bHeld = !bHeld;
                if(bHeld) {
                    robot.keyPress(KeyEvent.VK_Z);
                } else {
                    robot.keyRelease(KeyEvent.VK_Z);
                }
                log.info(e.getUser().getAsTag() + " | B (hold = " + bHeld + ")");
                break;
            }
            case "🇱" : {
                robot.keyPress(KeyEvent.VK_J);
                robot.delay(100);
                robot.keyRelease(KeyEvent.VK_J);
                log.info(e.getUser().getAsTag() + " | L");
                break;
            }
            case "🇷" : {
                robot.keyPress(KeyEvent.VK_K);
                robot.delay(100);
                robot.keyRelease(KeyEvent.VK_K);
                log.info(e.getUser().getAsTag() + " | R");
                break;
            }
            case "🇦" : {
                robot.keyPress(KeyEvent.VK_X);
                robot.delay(100);
                robot.keyRelease(KeyEvent.VK_X);
                log.info(e.getUser().getAsTag() + " | A");
                break;
            }
            case "🇧" : {
                robot.keyPress(KeyEvent.VK_Z);
                robot.delay(100);
                robot.keyRelease(KeyEvent.VK_Z);
                if(bHeld) {
                    bHeld = false;
                }
                log.info(e.getUser().getAsTag() + " | B");
                break;}
            case "🇽" : {
                robot.keyPress(KeyEvent.VK_S);
                robot.delay(100);
                robot.keyRelease(KeyEvent.VK_S);
                log.info(e.getUser().getAsTag() + " | X");
                break;
            }
            case "🇾" : {
                robot.keyPress(KeyEvent.VK_A);
                robot.delay(100);
                robot.keyRelease(KeyEvent.VK_A);
                log.info(e.getUser().getAsTag() + " | Y");
                break;
            }
            case "⏸️" : {
                robot.keyPress(KeyEvent.VK_ENTER);
                robot.delay(100);
                robot.keyRelease(KeyEvent.VK_ENTER);
                log.info(e.getUser().getAsTag() + " | START");
                break;
            }
            case "⏯️" : {
                robot.keyPress(KeyEvent.VK_H);
                robot.delay(100);
                robot.keyRelease(KeyEvent.VK_H);
                log.info(e.getUser().getAsTag() + " | SELECT");
                break;
            }
        }
    }
}
