package com.basketbandit.connection;

import com.basketbandit.DiscordPlays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.*;

public class SocketServer extends Thread {
    private static final Logger log = LoggerFactory.getLogger(SocketServer.class);
    private final List<SocketClient> socketClients = Collections.synchronizedList(new ArrayList<>());
    private ServerSocket serverSocket;
    private final int serverPort;
    private Robot robot;
    private Map<String, Map<String, String>> controllerBinds = (Map<String, Map<String, String>>) DiscordPlays.getConfig().get("player_bind");
    private boolean active = true;

    public SocketServer(int port) {
        try {
            this.robot = new Robot();
        } catch(AWTException e) {
            log.error("There was a problem setting up the robot, message: {}", e.getMessage());
        }

        this.serverPort = port;
    }

    public void run() {
        try {
            log.info("Starting socket server on port " + serverPort);
            this.serverSocket = new ServerSocket(serverPort);
            while(active) {
                new SocketClient(serverSocket.accept()).start();
            }
            serverSocket.close();
        } catch(IOException e) {
            log.error("There was a problem with the socket server, message: {}", e.getMessage());
        }
    }

    public void shutdown() {
        active = false;
        robot = null;
    }

    private class SocketClient extends Thread {
        private final Socket clientSocket;
        private final String clientAddress;
        private String clientNickname;
        private boolean bHeld; // client-based bheld state. (risky?)

        public SocketClient(Socket socket) {
            this.clientSocket = socket;
            this.clientAddress = clientSocket.getInetAddress().getHostAddress();
        }

        public void run() {
            try {
                PrintWriter out = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream(), StandardCharsets.UTF_8), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), StandardCharsets.UTF_8));
                log.info("Client connected from address '" + clientAddress + "'");

                String inputLine;
                while((inputLine = in.readLine()) != null) {
                    if(inputLine.startsWith("@")) {
                        final String input = inputLine.substring(1, Math.min(inputLine.length(), 31));
                        if(socketClients.stream().noneMatch(socketClient -> socketClient.clientNickname.equals(input))) {
                            log.info("Client from address '" + clientAddress + "' identified as '" + (clientNickname = input) + "'");
                            out.println(inputLine);
                            socketClients.add(this);
                            continue;
                        }
                        log.info("Client from address '" + clientAddress + "' rejected due to duplicate nickname");
                        out.println("'" + input + "' is already in use, please choose another nickname!");
                        break;
                    }
                    if(inputLine.startsWith("H-hi")) {
                        out.println("Hey " + clientNickname + "-chan! u/////u");
                        continue;
                    }
                    if(inputLine.equals(".")) {
                        socketClients.remove(socketClients.stream().filter(client -> client.clientNickname.equals(clientNickname)).findFirst().get());
                        out.println("Bye bye " + clientNickname + "-chan!");
                        log.info(clientNickname + "-chan disconnected! :(");
                        break;
                    }

                    String[] playerInput = inputLine.split(":");
                    if(playerInput.length > 1) {
                        out.println(executeWSInput(clientNickname, Integer.parseInt(playerInput[0]), playerInput[1]) ? "Gotcha!" : "Unknown :(");
                    }
                }

                in.close();
                out.close();
                clientSocket.close();
            } catch(Exception e) {
                log.warn("There was a problem with client from address '{}' ({}), message: {}", clientAddress, clientNickname, e.getMessage());
                if(e.getMessage().equals("Connection reset")) {
                    if(socketClients.remove(this)) {
                        log.info("Client from address '{}' ({}) was successfully disconnected", clientAddress, clientNickname);
                        return;
                    }
                    log.error("Unable to disconnect client from address '{}', nickname '{}' locked", clientAddress, clientNickname);
                }
            }
        }

        /**
         * Deals directly with input from Socket/XInput (Xbox 360, PS4 [ds4windows])
         * @param input String
         */
        private boolean executeWSInput(String clientNickname, int player, String input) {
            try {
                Map<String, String> keys = controllerBinds.get("p" + player); // Map<String, Character> still produces <String, String> is this a bug?
                if(input.contains("_")) {
                    // Socket XInput
                    switch(input) {
                        case "DPAD_UP_TRUE": {
                            robot.keyPress(keys.get("up").charAt(0));
                            log.info(clientNickname + " | P" + player + " | UP");
                            return true;
                        }
                        case "DPAD_UP_FALSE": {
                            robot.keyRelease(keys.get("up").charAt(0));
                            return true;
                        }
                        case "DPAD_LEFT_TRUE": {
                            robot.keyPress(keys.get("left").charAt(0));
                            log.info(clientNickname + " | P" + player + " | LEFT");
                            return true;
                        }
                        case "DPAD_LEFT_FALSE": {
                            robot.keyRelease(keys.get("left").charAt(0));
                            return true;
                        }
                        case "DPAD_RIGHT_TRUE": {
                            robot.keyPress(keys.get("right").charAt(0));
                            log.info(clientNickname + " | P" + player + " | RIGHT");
                            return true;
                        }
                        case "DPAD_RIGHT_FALSE": {
                            robot.keyRelease(keys.get("right").charAt(0));
                            return true;
                        }
                        case "DPAD_DOWN_TRUE": {
                            robot.keyPress(keys.get("down").charAt(0));
                            log.info(clientNickname + " | P" + player + " | DOWN");
                            return true;
                        }
                        case "DPAD_DOWN_FALSE": {
                            robot.keyRelease(keys.get("down").charAt(0));
                            return true;
                        }
                        case "A_TRUE": {
                            robot.keyPress(keys.get("a").charAt(0));
                            log.info(clientNickname + " | P" + player + " | B");
                            return true;
                        }
                        case "A_FALSE": {
                            robot.keyRelease(keys.get("a").charAt(0));
                            return true;
                        }
                        case "B_TRUE": {
                            robot.keyPress(keys.get("b").charAt(0));
                            log.info(clientNickname + " | P" + player + " | A");
                            return true;
                        }
                        case "B_FALSE": {
                            robot.keyRelease(keys.get("b").charAt(0));
                            return true;
                        }
                        case "X_TRUE": {
                            robot.keyPress(keys.get("x").charAt(0));
                            log.info(clientNickname + " | P" + player + " | Y");
                            return true;
                        }
                        case "X_FALSE": {
                            robot.keyRelease(keys.get("x").charAt(0));
                            return true;
                        }
                        case "Y_TRUE": {
                            robot.keyPress(keys.get("y").charAt(0));
                            log.info(clientNickname + " | P" + player + " | X");
                            return true;
                        }
                        case "Y_FALSE": {
                            robot.keyRelease(keys.get("y").charAt(0));
                            return true;
                        }
                        case "LEFT_SHOULDER_TRUE": {
                            robot.keyPress(keys.get("l").charAt(0));
                            log.info(clientNickname + " | P" + player + " | L");
                            break;
                        }
                        case "LEFT_SHOULDER_FALSE": {
                            robot.keyRelease(keys.get("l").charAt(0));
                            return true;
                        }
                        case "RIGHT_SHOULDER_TRUE": {
                            robot.keyPress(keys.get("r").charAt(0));
                            log.info(clientNickname + " | P" + player + " | R");
                            return true;
                        }
                        case "RIGHT_SHOULDER_FALSE": {
                            robot.keyRelease(keys.get("r").charAt(0));
                            return true;
                        }
                        case "START_TRUE": {
                            robot.keyPress(keys.get("start").charAt(0));
                            log.info(clientNickname + " | P" + player + " | START");
                            return true;
                        }
                        case "START_FALSE": {
                            robot.keyRelease(keys.get("start").charAt(0));
                            return true;
                        }
                        case "BACK_TRUE": {
                            robot.keyPress(keys.get("select").charAt(0));
                            log.info(clientNickname + " | P" + player + " | SELECT");
                            return true;
                        }
                        case "BACK_FALSE": {
                            robot.keyRelease(keys.get("select").charAt(0));
                            return true;
                        }
                    }
                } else {
                    // Socket Default
                    switch(input) {
                        case "L": {
                            robot.keyPress(keys.get("left").charAt(0));
                            robot.delay(100);
                            robot.keyRelease(keys.get("left").charAt(0));
                            log.info(clientNickname + " | P" + player + " | LEFT");
                            return true;
                        }
                        case "U": {
                            robot.keyPress(keys.get("up").charAt(0));
                            robot.delay(100);
                            robot.keyRelease(keys.get("up").charAt(0));
                            log.info(clientNickname + " | P" + player + " | UP");
                            return true;
                        }
                        case "R": {
                            robot.keyPress(keys.get("right").charAt(0));
                            robot.delay(100);
                            robot.keyRelease(keys.get("right").charAt(0));
                            log.info(clientNickname + " | P" + player + " | RIGHT");
                            return true;
                        }
                        case "D": {
                            robot.keyPress(keys.get("down").charAt(0));
                            robot.delay(100);
                            robot.keyRelease(keys.get("down").charAt(0));
                            log.info(clientNickname + " | P" + player + " | DOWN");
                            return true;
                        }
                        case "UL": {
                            robot.keyPress(keys.get("up").charAt(0));
                            robot.keyPress(keys.get("left").charAt(0));
                            robot.delay(100);
                            robot.keyRelease(keys.get("up").charAt(0));
                            robot.keyRelease(keys.get("left").charAt(0));
                            log.info(clientNickname + " | P" + player + " | UP + LEFT");
                            return true;
                        }
                        case "UR": {
                            robot.keyPress(keys.get("up").charAt(0));
                            robot.keyPress(keys.get("right").charAt(0));
                            robot.delay(100);
                            robot.keyRelease(keys.get("up").charAt(0));
                            robot.keyRelease(keys.get("right").charAt(0));
                            log.info(clientNickname + " | P" + player + " | UP + RIGHT");
                            return true;
                        }
                        case "DR": {
                            robot.keyPress(keys.get("down").charAt(0));
                            robot.keyPress(keys.get("right").charAt(0));
                            robot.delay(100);
                            robot.keyRelease(keys.get("down").charAt(0));
                            robot.keyRelease(keys.get("right").charAt(0));
                            log.info(clientNickname + " | P" + player + " | DOWN + RIGHT");
                            return true;
                        }
                        case "DL": {
                            robot.keyPress(keys.get("down").charAt(0));
                            robot.keyPress(keys.get("left").charAt(0));
                            robot.delay(100);
                            robot.keyRelease(keys.get("down").charAt(0));
                            robot.keyRelease(keys.get("left").charAt(0));
                            log.info(clientNickname + " | P" + player + " | DOWN + LEFT");
                            return true;
                        }
                        case "L1": {
                            robot.keyPress(keys.get("left").charAt(0));
                            robot.delay(1000);
                            robot.keyRelease(keys.get("left").charAt(0));
                            log.info(clientNickname + " | P" + player + " | LEFT (1s)");
                            return true;
                        }
                        case "U1": {
                            robot.keyPress(keys.get("up").charAt(0));
                            robot.delay(1000);
                            robot.keyRelease(keys.get("up").charAt(0));
                            log.info(clientNickname + " | P" + player + " | UP (1s)");
                            return true;
                        }
                        case "R1": {
                            robot.keyPress(keys.get("right").charAt(0));
                            robot.delay(1000);
                            robot.keyRelease(keys.get("right").charAt(0));
                            log.info(clientNickname + " | P" + player + " | RIGHT (1s)");
                            return true;
                        }
                        case "D1": {
                            robot.keyPress(keys.get("down").charAt(0));
                            robot.delay(1000);
                            robot.keyRelease(keys.get("down").charAt(0));
                            log.info(clientNickname + " | P" + player + " | DOWN (1s)");
                            return true;
                        }
                        case "BH": {
                            bHeld = !bHeld;
                            if(bHeld) {
                                robot.keyPress(keys.get("b").charAt(0));
                            } else {
                                robot.keyRelease(keys.get("b").charAt(0));
                            }
                            log.info(clientNickname + " | P" + player + " | B (HOLD = " + (bHeld + "").toUpperCase() + ")");
                            return true;
                        }
                        case "LB": {
                            robot.keyPress(keys.get("l").charAt(0));
                            robot.delay(100);
                            robot.keyRelease(keys.get("l").charAt(0));
                            log.info(clientNickname + " | P" + player + " | L");
                            return true;
                        }
                        case "RB": {
                            robot.keyPress(keys.get("r").charAt(0));
                            robot.delay(100);
                            robot.keyRelease(keys.get("r").charAt(0));
                            log.info(clientNickname + " | P" + player + " | R");
                            return true;
                        }
                        case "A": {
                            robot.keyPress(keys.get("a").charAt(0));
                            robot.delay(100);
                            robot.keyRelease(keys.get("a").charAt(0));
                            log.info(clientNickname + " | P" + player + " | A");
                            return true;
                        }
                        case "B": {
                            robot.keyPress(keys.get("b").charAt(0));
                            robot.delay(100);
                            robot.keyRelease(keys.get("b").charAt(0));
                            if(bHeld) {
                                bHeld = false;
                            }
                            log.info(clientNickname + " | P" + player + " | B");
                            return true;
                        }
                        case "X": {
                            robot.keyPress(keys.get("x").charAt(0));
                            robot.delay(100);
                            robot.keyRelease(keys.get("x").charAt(0));
                            log.info(clientNickname + " | P" + player + " | X");
                            return true;
                        }
                        case "Y": {
                            robot.keyPress(keys.get("y").charAt(0));
                            robot.delay(100);
                            robot.keyRelease(keys.get("y").charAt(0));
                            log.info(clientNickname + " | P" + player + " | Y");
                            return true;
                        }
                        case "P": {
                            robot.keyPress(keys.get("start").charAt(0));
                            robot.delay(100);
                            robot.keyRelease(keys.get("start").charAt(0));
                            log.info(clientNickname + " | P" + player + " | START");
                            return true;
                        }
                        case "S": {
                            robot.keyPress(keys.get("select").charAt(0));
                            robot.delay(100);
                            robot.keyRelease(keys.get("select").charAt(0));
                            log.info(clientNickname + " | P" + player + " | SELECT");
                            return true;
                        }
                    }
                }
            } catch(Exception e) {
                log.error("There was a problem executing that command, message: {}", e.getMessage(), e);
                return false;
            }
            return false;
        }
    }
}
