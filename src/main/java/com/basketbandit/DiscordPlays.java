package com.basketbandit;

import com.basketbandit.connection.DiscordGuild;
import com.basketbandit.connection.SocketServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Scanner;

public class DiscordPlays {
    private static final Logger log = LoggerFactory.getLogger(DiscordPlays.class);

    private static Map<String, Object> config;
    private SocketServer socketServer;
    private DiscordGuild discordGuild;

    public static void main(String[] args) {
        new DiscordPlays();
    }

    public DiscordPlays() {
        try(InputStream inputStream = new FileInputStream("./config.yaml")) {
            config = new Yaml().load(inputStream);

            if((boolean)config.get("socket_server_enabled")) {
                this.socketServer = new SocketServer((int)config.get("socket_server_port"));
                this.socketServer.setName("Socket Server Thread");
                this.socketServer.start();
            }

            if((boolean)config.get("discord_enabled")) {
                this.discordGuild = new DiscordGuild((String)config.get("bot_token"), (String)config.get("control_channel_id"));
            }

            Scanner sc = new Scanner(System.in);
            while(sc.hasNextLine()) {
                if(sc.nextLine().equals("exit")) {
                    shutdown();
                }
            }

        } catch(IOException e) {
            log.error("There was an error loading the configuration file, message: {}", e.getMessage(), e);
        }
    }

    public static Map<String, Object> getConfig() {
        return config;
    }

    public void shutdown() {
        if(socketServer != null) {
            socketServer.shutdown();
        }
        if(discordGuild != null) {
            discordGuild.shutdown();
        }
        System.exit(0);
    }
}
