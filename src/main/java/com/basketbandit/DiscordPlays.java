package com.basketbandit;

import com.neovisionaries.ws.client.*;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.react.GenericGuildMessageReactionEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionRemoveEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class DiscordPlays implements EventListener {
    private static final Logger log = LoggerFactory.getLogger(DiscordPlays.class);
    private static String[] argz;
    private static Robot robot;
    private static JDA jda;
    private static TextChannel controlChannel;
    private static Message bHeldMessage;
    private static boolean bHeld;
    private static ServerSocket serverSocket;

    public static void main(String[] args) {
        argz = args;
        new DiscordPlays();
    }

    public DiscordPlays() {
        try {
            robot = new Robot();
        } catch(AWTException e) {
            log.error("There was a problem setting up the robot... {}", e.getMessage(), e);
        }

        try {
            jda = JDABuilder.createDefault(argz[0]).addEventListeners(this).build();
            jda.awaitReady();

            if(argz.length > 1) {
                controlChannel = jda.getTextChannelById(Long.parseLong(argz[1]));
                log.info("Control channel set to: " + controlChannel.getAsMention());
            }
            if(argz.length > 2) {
                controlChannel.getHistoryFromBeginning(5).queue(s -> {
                    bHeldMessage = s.getRetrievedHistory().get(0);
                    log.info("B Held message set to: " + bHeldMessage.getId());
                });
            }
        } catch(Exception e) {
            log.error("There was a problem setting up JDA... {}", e.getMessage(), e);
        }

        try {
            log.info("Starting socket server on port 3197...");
            serverSocket = new ServerSocket(3197);
            while(true) {
                new DiscordPlaysClientHandler(serverSocket.accept()).start();
            }
        } catch(IOException e) {
            log.error("There was a problem with the socket server... {}", e.getMessage(), e);
        }
    }

    private static class DiscordPlaysClientHandler extends Thread {
        private Socket clientSocket;
        private PrintWriter out;
        private BufferedReader in;

        public DiscordPlaysClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        public void run() {
            try {
                log.info("New socket client connected...");
                out = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream(), StandardCharsets.UTF_8), true);
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), StandardCharsets.UTF_8));

                String inputLine;
                while((inputLine = in.readLine()) != null) {
                    if(".".equals(inputLine)) {
                        out.println("bye");
                        break;
                    }
                    executeInput(inputLine);
                    out.println(inputLine);
                }

                in.close();
                out.close();
                clientSocket.close();
                log.info("New socket client disconnected...");
            } catch(Exception e) {
                log.error("There was an error... {}", e.getMessage(), e);
            }
        }
    }

    public void stop() throws IOException {
        serverSocket.close();
    }

    @Override
    public void onEvent(@NotNull GenericEvent event) {
        if(event instanceof ReadyEvent) {
            log.info("DiscordPlays is ready!");
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

                controlChannel.sendMessage("🅱️ held: `" + bHeld + "`").queue(s -> bHeldMessage = s);
            }
        }

        if(event instanceof GuildMessageReactionAddEvent || event instanceof GuildMessageReactionRemoveEvent) {
            if(((GenericGuildMessageReactionEvent) event).getUser().isBot()) {
                return; // no bots allowed :)
            }

            GenericGuildMessageReactionEvent e = (event instanceof GuildMessageReactionAddEvent) ? (GuildMessageReactionAddEvent) event : (GuildMessageReactionRemoveEvent) event;
            if(e.getChannel() == controlChannel) {
                executeInput(e.getReactionEmote().getAsReactionCode());
            }
        }
    }

    private static void executeInput(String input) {
        switch(input) {
            case "⬅️" -> {
                robot.keyPress(KeyEvent.VK_LEFT);
                robot.delay(100);
                robot.keyRelease(KeyEvent.VK_LEFT);
                log.info("LEFT");
            }
            case "⬆️" -> {
                robot.keyPress(KeyEvent.VK_UP);
                robot.delay(100);
                robot.keyRelease(KeyEvent.VK_UP);
                log.info("UP");
            }
            case "➡️" -> {
                robot.keyPress(KeyEvent.VK_RIGHT);
                robot.delay(100);
                robot.keyRelease(KeyEvent.VK_RIGHT);
                log.info("RIGHT");
            }
            case "⬇️" -> {
                robot.keyPress(KeyEvent.VK_DOWN);
                robot.delay(100);
                robot.keyRelease(KeyEvent.VK_DOWN);
                log.info("DOWN");
            }
            case "↖️" -> {
                robot.keyPress(KeyEvent.VK_Q);
                robot.delay(100);
                robot.keyRelease(KeyEvent.VK_Q);
                log.info("UP+LEFT");
            }
            case "↗️" -> {
                robot.keyPress(KeyEvent.VK_W);
                robot.delay(100);
                robot.keyRelease(KeyEvent.VK_W);
                log.info("UP+RIGHT");
            }
            case "↘️" -> {
                robot.keyPress(KeyEvent.VK_E);
                robot.delay(100);
                robot.keyRelease(KeyEvent.VK_E);
                log.info("DOWN+RIGHT");
            }
            case "↙️" -> {
                robot.keyPress(KeyEvent.VK_R);
                robot.delay(100);
                robot.keyRelease(KeyEvent.VK_R);
                log.info("DOWN+LEFT");
            }
            case "⏪" -> {
                robot.keyPress(KeyEvent.VK_LEFT);
                robot.delay(1000);
                robot.keyRelease(KeyEvent.VK_LEFT);
                log.info("LEFT (HOLD)");
            }
            case "⏫" -> {
                robot.keyPress(KeyEvent.VK_UP);
                robot.delay(1000);
                robot.keyRelease(KeyEvent.VK_UP);
                log.info("UP (HOLD)");
            }
            case "⏩" -> {
                robot.keyPress(KeyEvent.VK_RIGHT);
                robot.delay(1000);
                robot.keyRelease(KeyEvent.VK_RIGHT);
                log.info("RIGHT (HOLD)");
            }
            case "⏬" -> {
                robot.keyPress(KeyEvent.VK_DOWN);
                robot.delay(1000);
                robot.keyRelease(KeyEvent.VK_DOWN);
                log.info("DOWN (HOLD)");
            }
            case "🅱️" -> {
                bHeld = !bHeld;
                if(bHeld) {
                    robot.keyPress(KeyEvent.VK_Z);
                } else {
                    robot.keyRelease(KeyEvent.VK_Z);
                }
                bHeldMessage.editMessage("🅱️ held: `" + bHeld + "`").queue();
                log.info("HOLD+B");
            }
            case "🇱" -> {
                robot.keyPress(KeyEvent.VK_J);
                robot.delay(100);
                robot.keyRelease(KeyEvent.VK_J);
                log.info("L");
            }
            case "🇷" -> {
                robot.keyPress(KeyEvent.VK_K);
                robot.delay(100);
                robot.keyRelease(KeyEvent.VK_K);
                log.info("R");
            }
            case "🇦" -> {
                robot.keyPress(KeyEvent.VK_X);
                robot.delay(100);
                robot.keyRelease(KeyEvent.VK_X);
                log.info("A");
            }
            case "🇧" -> {
                robot.keyPress(KeyEvent.VK_Z);
                robot.delay(100);
                robot.keyRelease(KeyEvent.VK_Z);
                if(bHeld) {
                    bHeld = false;
                    bHeldMessage.editMessage("🅱️ held: `" + bHeld + "`").queue();
                }
                log.info("B");
            }
            case "🇽" -> {
                robot.keyPress(KeyEvent.VK_S);
                robot.delay(100);
                robot.keyRelease(KeyEvent.VK_S);
                log.info("X");
            }
            case "🇾" -> {
                robot.keyPress(KeyEvent.VK_A);
                robot.delay(100);
                robot.keyRelease(KeyEvent.VK_A);
                log.info("Y");
            }
            case "⏸️" -> {
                robot.keyPress(KeyEvent.VK_ENTER);
                robot.delay(100);
                robot.keyRelease(KeyEvent.VK_ENTER);
                log.info("START");
            }
            case "⏯️" -> {
                robot.keyPress(KeyEvent.VK_H);
                robot.delay(100);
                robot.keyRelease(KeyEvent.VK_H);
                log.info("SELECT");
            }
            case "🔻" -> {
                robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
                robot.delay(5000);
                robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
                log.info("PRESS");
            }
            case "⌛" -> {
                robot.keyPress(KeyEvent.VK_X);
                robot.keyPress(KeyEvent.VK_Z);
                robot.delay(100);
                robot.keyRelease(KeyEvent.VK_X);
                robot.keyRelease(KeyEvent.VK_Z);
                log.info("A+B");
            }
        }
    }
}
