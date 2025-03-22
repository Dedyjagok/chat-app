package main.java.websocket;

import java.io.*;
import java.net.*;
import java.util.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.*;
import java.util.Base64;

public class WebSocket {
    private static final int WS_PORT = 9002;
    private static Set<WebSocketHandler> connections = Collections.synchronizedSet(new HashSet<WebSocketHandler>());
    
    public static void main(String[] args) throws Exception {
        System.out.println("WebSocket Server is running on port " + WS_PORT);
        ServerSocket server = new ServerSocket(WS_PORT);
        
        try {
            while (true) {
                Socket socket = server.accept();
                WebSocketHandler handler = new WebSocketHandler(socket);
                connections.add(handler);
                handler.start();
            }
        } finally {
            server.close();
        }
    }
    
    // Broadcast message to all WebSocket clients
    public static void broadcast(String message, WebSocketHandler sender) {
        synchronized (connections) {
            for (WebSocketHandler client : connections) {
                if (client != sender) {
                    client.send(message);
                }
            }
        }
    }
    
    // Remove a client from the connection pool
    public static void removeConnection(WebSocketHandler handler) {
        connections.remove(handler);
    }
    
    // Generate WebSocket accept key from client key (for handshake)
    public static String getWebSocketAcceptKey(String clientKey) throws NoSuchAlgorithmException {
        String GUID = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
        String concatenated = clientKey + GUID;
        MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
        byte[] hash = sha1.digest(concatenated.getBytes());
        return Base64.getEncoder().encodeToString(hash);
    }
}