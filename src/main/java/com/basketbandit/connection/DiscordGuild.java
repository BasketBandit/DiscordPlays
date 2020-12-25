package com.basketbandit.connection;

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

import java.awt.*;
import java.awt.event.KeyEvent;

public class DiscordGuild implements EventListener {
    private static final Logger log = LoggerFactory.getLogger(DiscordGuild.class);
    private static JDA jda;
    private static TextChannel controlChannel;
    private Robot robot;
    private boolean bHeld;

    public DiscordGuild(String botToken, String controlChannelId) {
        try {
            this.robot = new Robot();
        } catch(AWTException e) {
            log.error("There was a problem setting up the robot, message: {}", e.getMessage());
        }

        try {
            jda = JDABuilder.createDefault(botToken).addEventListeners(this).build();
            jda.awaitReady();
            controlChannel = jda.getTextChannelById(Long.parseLong(controlChannelId));
            log.info("Discord control channel set to: " + controlChannel.getAsMention());
        } catch(Exception e) {
            log.error("There was a problem with JDA, message: {}", e.getMessage());
        }
    }

    public void shutdown() {
        jda.shutdown();
        robot = null;
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
     * Deals directly with input from Discord.
     * @param e {@link GenericGuildMessageReactionEvent}
     */
    private void executeDiscordInput(GenericGuildMessageReactionEvent e) {
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
