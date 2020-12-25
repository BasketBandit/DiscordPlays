package com.basketbandit.connection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SocketServer extends Thread {
    private static final Logger log = LoggerFactory.getLogger(SocketServer.class);
    private final List<SocketClient> socketClients = Collections.synchronizedList(new ArrayList<>());
    private ServerSocket serverSocket;
    private final int serverPort;
    private Robot robot;
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
                log.info("Client connected at address '" + clientAddress + "'");

                String inputLine;
                while((inputLine = in.readLine()) != null) {
                    if(inputLine.startsWith("id:")) {
                        final String input = inputLine.substring(3, Math.min(inputLine.length(), 31));
                        if(socketClients.stream().noneMatch(socketClient -> socketClient.clientNickname.equals(input))) {
                            log.info("Client at address '" + clientAddress + "' identified as '" + (clientNickname = input) + "'");
                            out.println(inputLine);
                            socketClients.add(this);
                            continue;
                        }
                        log.info("Client at address '" + clientAddress + "' rejected due to duplicate nickname");
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
                    out.println(executeWSInput(clientNickname, inputLine) ? "Gotcha!" : "Unknown :(");
                }

                in.close();
                out.close();
                clientSocket.close();
            } catch(Exception e) {
                log.error("There was an problem with the socket client, message: {}", e.getMessage());
            }
        }

        /**
         * Deals directly with input from Socket/XInput (Xbox 360, PS4 [ds4windows])
         * @param input String
         */
        private boolean executeWSInput(String clientNickname, String input) {
            if(input.contains("_")) {
                // Socket XInput
                switch(input) {
                    case "DPAD_UP_TRUE": {
                        robot.keyPress(KeyEvent.VK_UP);
                        log.info(clientNickname + " | UP");
                        return true;
                    }
                    case "DPAD_UP_FALSE": {
                        robot.keyRelease(KeyEvent.VK_UP);
                        return true;
                    }
                    case "DPAD_LEFT_TRUE": {
                        robot.keyPress(KeyEvent.VK_LEFT);
                        log.info(clientNickname + " | LEFT");
                        return true;
                    }
                    case "DPAD_LEFT_FALSE": {
                        robot.keyRelease(KeyEvent.VK_LEFT);
                        return true;
                    }
                    case "DPAD_RIGHT_TRUE": {
                        robot.keyPress(KeyEvent.VK_RIGHT);
                        log.info(clientNickname + " | RIGHT");
                        return true;
                    }
                    case "DPAD_RIGHT_FALSE": {
                        robot.keyRelease(KeyEvent.VK_RIGHT);
                        return true;
                    }
                    case "DPAD_DOWN_TRUE": {
                        robot.keyPress(KeyEvent.VK_DOWN);
                        log.info(clientNickname + " | DOWN");
                        return true;
                    }
                    case "DPAD_DOWN_FALSE": {
                        robot.keyRelease(KeyEvent.VK_DOWN);
                        return true;
                    }
                    case "A_TRUE": {
                        robot.keyPress(KeyEvent.VK_Z);
                        log.info(clientNickname + " | B");
                        return true;
                    }
                    case "A_FALSE": {
                        robot.keyRelease(KeyEvent.VK_Z);
                        return true;
                    }
                    case "B_TRUE": {
                        robot.keyPress(KeyEvent.VK_X);
                        log.info(clientNickname + " | A");
                        return true;
                    }
                    case "B_FALSE": {
                        robot.keyRelease(KeyEvent.VK_X);
                        return true;
                    }
                    case "X_TRUE": {
                        robot.keyPress(KeyEvent.VK_A);
                        log.info(clientNickname + " | Y");
                        return true;
                    }
                    case "X_FALSE": {
                        robot.keyRelease(KeyEvent.VK_A);
                        return true;
                    }
                    case "Y_TRUE": {
                        robot.keyPress(KeyEvent.VK_S);
                        log.info(clientNickname + " | X");
                        return true;
                    }
                    case "Y_FALSE": {
                        robot.keyRelease(KeyEvent.VK_S);
                        return true;
                    }
                    case "LEFT_SHOULDER_TRUE": {
                        robot.keyPress(KeyEvent.VK_J);
                        log.info(clientNickname + " | L");
                        break;
                    }
                    case "LEFT_SHOULDER_FALSE": {
                        robot.keyRelease(KeyEvent.VK_J);
                        return true;
                    }
                    case "RIGHT_SHOULDER_TRUE": {
                        robot.keyPress(KeyEvent.VK_K);
                        log.info(clientNickname + " | R");
                        return true;
                    }
                    case "RIGHT_SHOULDER_FALSE": {
                        robot.keyRelease(KeyEvent.VK_K);
                        return true;
                    }
                    case "START_TRUE": {
                        robot.keyPress(KeyEvent.VK_ENTER);
                        log.info(clientNickname + " | START");
                        return true;
                    }
                    case "START_FALSE": {
                        robot.keyRelease(KeyEvent.VK_ENTER);
                        return true;
                    }
                    case "BACK_TRUE": {
                        robot.keyPress(KeyEvent.VK_H);
                        log.info(clientNickname + " | SELECT");
                        return true;
                    }
                    case "BACK_FALSE": {
                        robot.keyRelease(KeyEvent.VK_H);
                        return true;
                    }
                }
            } else {
                // Socket Default
                switch(input) {
                    case "L": {
                        robot.keyPress(KeyEvent.VK_LEFT);
                        robot.delay(100);
                        robot.keyRelease(KeyEvent.VK_LEFT);
                        log.info(clientNickname + " | LEFT");
                        return true;
                    }
                    case "U": {
                        robot.keyPress(KeyEvent.VK_UP);
                        robot.delay(100);
                        robot.keyRelease(KeyEvent.VK_UP);
                        log.info(clientNickname + " | UP");
                        return true;
                    }
                    case "R": {
                        robot.keyPress(KeyEvent.VK_RIGHT);
                        robot.delay(100);
                        robot.keyRelease(KeyEvent.VK_RIGHT);
                        log.info(clientNickname + " | RIGHT");
                        return true;
                    }
                    case "D": {
                        robot.keyPress(KeyEvent.VK_DOWN);
                        robot.delay(100);
                        robot.keyRelease(KeyEvent.VK_DOWN);
                        log.info(clientNickname + " | DOWN");
                        return true;
                    }
                    case "UL": {
                        robot.keyPress(KeyEvent.VK_Q);
                        robot.delay(100);
                        robot.keyRelease(KeyEvent.VK_Q);
                        log.info(clientNickname + " | UP + LEFT");
                        return true;
                    }
                    case "UR": {
                        robot.keyPress(KeyEvent.VK_W);
                        robot.delay(100);
                        robot.keyRelease(KeyEvent.VK_W);
                        log.info(clientNickname + " | UP + RIGHT");
                        return true;
                    }
                    case "DR": {
                        robot.keyPress(KeyEvent.VK_E);
                        robot.delay(100);
                        robot.keyRelease(KeyEvent.VK_E);
                        log.info(clientNickname + " | DOWN + RIGHT");
                        return true;
                    }
                    case "DL": {
                        robot.keyPress(KeyEvent.VK_R);
                        robot.delay(100);
                        robot.keyRelease(KeyEvent.VK_R);
                        log.info(clientNickname + " | DOWN + LEFT");
                        return true;
                    }
                    case "L1": {
                        robot.keyPress(KeyEvent.VK_LEFT);
                        robot.delay(1000);
                        robot.keyRelease(KeyEvent.VK_LEFT);
                        log.info(clientNickname + " | LEFT (1s)");
                        return true;
                    }
                    case "U1": {
                        robot.keyPress(KeyEvent.VK_UP);
                        robot.delay(1000);
                        robot.keyRelease(KeyEvent.VK_UP);
                        log.info(clientNickname + " | UP (1s)");
                        return true;
                    }
                    case "R1": {
                        robot.keyPress(KeyEvent.VK_RIGHT);
                        robot.delay(1000);
                        robot.keyRelease(KeyEvent.VK_RIGHT);
                        log.info(clientNickname + " | RIGHT (1s)");
                        return true;
                    }
                    case "D1": {
                        robot.keyPress(KeyEvent.VK_DOWN);
                        robot.delay(1000);
                        robot.keyRelease(KeyEvent.VK_DOWN);
                        log.info(clientNickname + " | DOWN (1s)");
                        return true;
                    }
                    case "BH": {
                        bHeld = !bHeld;
                        if(bHeld) {
                            robot.keyPress(KeyEvent.VK_Z);
                        } else {
                            robot.keyRelease(KeyEvent.VK_Z);
                        }
                        log.info(clientNickname + " | B (HOLD = " + (bHeld+"").toUpperCase() + ")");
                        return true;
                    }
                    case "LB": {
                        robot.keyPress(KeyEvent.VK_J);
                        robot.delay(100);
                        robot.keyRelease(KeyEvent.VK_J);
                        log.info(clientNickname + " | L");
                        return true;
                    }
                    case "RB": {
                        robot.keyPress(KeyEvent.VK_K);
                        robot.delay(100);
                        robot.keyRelease(KeyEvent.VK_K);
                        log.info(clientNickname + " | R");
                        return true;
                    }
                    case "A": {
                        robot.keyPress(KeyEvent.VK_X);
                        robot.delay(100);
                        robot.keyRelease(KeyEvent.VK_X);
                        log.info(clientNickname + " | A");
                        return true;
                    }
                    case "B": {
                        robot.keyPress(KeyEvent.VK_Z);
                        robot.delay(100);
                        robot.keyRelease(KeyEvent.VK_Z);
                        if(bHeld) {
                            bHeld = false;
                        }
                        log.info(clientNickname + " | B");
                        return true;
                    }
                    case "X": {
                        robot.keyPress(KeyEvent.VK_S);
                        robot.delay(100);
                        robot.keyRelease(KeyEvent.VK_S);
                        log.info(clientNickname + " | X");
                        return true;
                    }
                    case "Y": {
                        robot.keyPress(KeyEvent.VK_A);
                        robot.delay(100);
                        robot.keyRelease(KeyEvent.VK_A);
                        log.info(clientNickname + " | Y");
                        return true;
                    }
                    case "P": {
                        robot.keyPress(KeyEvent.VK_ENTER);
                        robot.delay(100);
                        robot.keyRelease(KeyEvent.VK_ENTER);
                        log.info(clientNickname + " | START");
                        return true;
                    }
                    case "S": {
                        robot.keyPress(KeyEvent.VK_H);
                        robot.delay(100);
                        robot.keyRelease(KeyEvent.VK_H);
                        log.info(clientNickname + " | SELECT");
                        return true;
                    }
                }
            }
            return false;
        }
    }
}
