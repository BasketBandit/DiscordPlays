package com.basketbandit;

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

import javax.security.auth.login.LoginException;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

public class DiscordPlays implements EventListener {
    private static final Logger log = LoggerFactory.getLogger(DiscordPlays.class);
    private static Robot robot;
    private static JDA jda;
    private static TextChannel controlChannel;
    private static Message bHeldMessage;
    private static boolean bHeld;

    public static void main(String[] args) throws LoginException, InterruptedException, AWTException {
        robot = new Robot();
        jda = JDABuilder.createDefault(args[0]).addEventListeners(new DiscordPlays()).build();
        jda.awaitReady();
        if(args.length > 1) {
            controlChannel = jda.getTextChannelById(Long.parseLong(args[1]));
            log.info("Control channel set to: " + controlChannel.getAsMention());
        }
        if(args.length > 2) {
            controlChannel.getHistoryFromBeginning(5).queue(s -> {
                bHeldMessage = s.getRetrievedHistory().get(0);
                log.info("B Held message set to: " + bHeldMessage.getId());
            });
        }
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
                final String emote = e.getReactionEmote().getAsReactionCode();
                switch(emote) {
                    case "⬅️" -> {
                        robot.keyPress(KeyEvent.VK_LEFT);
                        robot.delay(100);
                        robot.keyRelease(KeyEvent.VK_LEFT);
                        log.info(e.getUser().getAsTag() + " | LEFT");
                    }
                    case "⬆️" -> {
                        robot.keyPress(KeyEvent.VK_UP);
                        robot.delay(100);
                        robot.keyRelease(KeyEvent.VK_UP);
                        log.info(e.getUser().getAsTag() + " | UP");
                    }
                    case "➡️" -> {
                        robot.keyPress(KeyEvent.VK_RIGHT);
                        robot.delay(100);
                        robot.keyRelease(KeyEvent.VK_RIGHT);
                        log.info(e.getUser().getAsTag() + " | RIGHT");
                    }
                    case "⬇️" -> {
                        robot.keyPress(KeyEvent.VK_DOWN);
                        robot.delay(100);
                        robot.keyRelease(KeyEvent.VK_DOWN);
                        log.info(e.getUser().getAsTag() + " | DOWN");
                    }
                    case "↖️" -> {
                        robot.keyPress(KeyEvent.VK_Q);
                        robot.delay(100);
                        robot.keyRelease(KeyEvent.VK_Q);
                        log.info(e.getUser().getAsTag() + " | UP+LEFT");
                    }
                    case "↗️" -> {
                        robot.keyPress(KeyEvent.VK_W);
                        robot.delay(100);
                        robot.keyRelease(KeyEvent.VK_W);
                        log.info(e.getUser().getAsTag() + " | UP+RIGHT");
                    }
                    case "↘️" -> {
                        robot.keyPress(KeyEvent.VK_E);
                        robot.delay(100);
                        robot.keyRelease(KeyEvent.VK_E);
                        log.info(e.getUser().getAsTag() + " | DOWN+RIGHT");
                    }
                    case "↙️" -> {
                        robot.keyPress(KeyEvent.VK_R);
                        robot.delay(100);
                        robot.keyRelease(KeyEvent.VK_R);
                        log.info(e.getUser().getAsTag() + " | DOWN+LEFT");
                    }
                    case "⏪" -> {
                        robot.keyPress(KeyEvent.VK_LEFT);
                        robot.delay(1000);
                        robot.keyRelease(KeyEvent.VK_LEFT);
                        log.info(e.getUser().getAsTag() + " | LEFT (HOLD)");
                    }
                    case "⏫" -> {
                        robot.keyPress(KeyEvent.VK_UP);
                        robot.delay(1000);
                        robot.keyRelease(KeyEvent.VK_UP);
                        log.info(e.getUser().getAsTag() + " | UP (HOLD)");
                    }
                    case "⏩" -> {
                        robot.keyPress(KeyEvent.VK_RIGHT);
                        robot.delay(1000);
                        robot.keyRelease(KeyEvent.VK_RIGHT);
                        log.info(e.getUser().getAsTag() + " | RIGHT (HOLD)");
                    }
                    case "⏬" -> {
                        robot.keyPress(KeyEvent.VK_DOWN);
                        robot.delay(1000);
                        robot.keyRelease(KeyEvent.VK_DOWN);
                        log.info(e.getUser().getAsTag() + " | DOWN (HOLD)");
                    }
                    case "🅱️" -> {
                        bHeld = !bHeld;
                        if(bHeld) {
                            robot.keyPress(KeyEvent.VK_Z);
                        } else {
                            robot.keyRelease(KeyEvent.VK_Z);
                        }
                        bHeldMessage.editMessage("🅱️ held: `" + bHeld + "`").queue();
                        log.info(e.getUser().getAsTag() + " | HOLD+B");
                    }
                    case "🇱" -> {
                        robot.keyPress(KeyEvent.VK_J);
                        robot.delay(100);
                        robot.keyRelease(KeyEvent.VK_J);
                        log.info(e.getUser().getAsTag() + " | L");
                    }
                    case "🇷" -> {
                        robot.keyPress(KeyEvent.VK_K);
                        robot.delay(100);
                        robot.keyRelease(KeyEvent.VK_K);
                        log.info(e.getUser().getAsTag() + " | R");
                    }
                    case "🇦" -> {
                        robot.keyPress(KeyEvent.VK_X);
                        robot.delay(100);
                        robot.keyRelease(KeyEvent.VK_X);
                        log.info(e.getUser().getAsTag() + " | A");
                    }
                    case "🇧" -> {
                        robot.keyPress(KeyEvent.VK_Z);
                        robot.delay(100);
                        robot.keyRelease(KeyEvent.VK_Z);
                        if(bHeld) {
                            bHeld = false;
                            bHeldMessage.editMessage("🅱️ held: `" + bHeld + "`").queue();
                        }
                        log.info(e.getUser().getAsTag() + " | B");
                    }
                    case "🇽" -> {
                        robot.keyPress(KeyEvent.VK_S);
                        robot.delay(100);
                        robot.keyRelease(KeyEvent.VK_S);
                        log.info(e.getUser().getAsTag() + " | X");
                    }
                    case "🇾" -> {
                        robot.keyPress(KeyEvent.VK_A);
                        robot.delay(100);
                        robot.keyRelease(KeyEvent.VK_A);
                        log.info(e.getUser().getAsTag() + " | Y");
                    }
                    case "⏸️" -> {
                        robot.keyPress(KeyEvent.VK_ENTER);
                        robot.delay(100);
                        robot.keyRelease(KeyEvent.VK_ENTER);
                        log.info(e.getUser().getAsTag() + " | START");
                    }
                    case "⏯️" -> {
                        robot.keyPress(KeyEvent.VK_H);
                        robot.delay(100);
                        robot.keyRelease(KeyEvent.VK_H);
                        log.info(e.getUser().getAsTag() + " | SELECT");
                    }
                    case "🔻" -> {
                        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
                        robot.delay(5000);
                        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
                        log.info(e.getUser().getAsTag() + " | PRESS");
                    }
                    case "⌛" -> {
                        robot.keyPress(KeyEvent.VK_X);
                        robot.keyPress(KeyEvent.VK_Z);
                        robot.delay(100);
                        robot.keyRelease(KeyEvent.VK_X);
                        robot.keyRelease(KeyEvent.VK_Z);
                        log.info(e.getUser().getAsTag() + " | A+B");
                    }
                }
            }
        }
    }
}
