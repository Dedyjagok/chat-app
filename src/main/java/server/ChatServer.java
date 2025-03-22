package main.java.server;

import java.io.*;
import java.net.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ChatServer {
    private static final int PORT = 9001;
    private static HashSet<PrintWriter> writers = new HashSet<>();
    private static ArrayList<String> messageHistory = new ArrayList<>();
    private static final String LOG_FILE = "logs/chat-logs.txt";
    
    public static void main(String[] args) throws Exception {
        System.out.println("Chat Server is running...");
        
        // Setup server socket
        ServerSocket listener = new ServerSocket(PORT);
        try {
            while (true) {
                // Accept new client connection
                Socket socket = listener.accept();
                // Create a new handler thread for this client
                ClientHandler handler = new ClientHandler(socket);
                handler.start();
            }
        } finally {
            listener.close();
        }
    }
    
    // Method to broadcast message to all clients
    public static void broadcast(String message) {
        // Log message
        logMessage(message);
        
        // Add to history
        messageHistory.add(message);
        
        // Send to all connected clients
        for (PrintWriter writer : writers) {
            writer.println(message);
        }
    }
    
    // Method to get message history
    public static ArrayList<String> getMessageHistory() {
        return messageHistory;
    }
    
    // Method to add client writer
    public static void addClient(PrintWriter writer) {
        writers.add(writer);
    }
    
    // Method to remove client writer
    public static void removeClient(PrintWriter writer) {
        writers.remove(writer);
    }
    
    // Method to log messages to file
    private static void logMessage(String message) {
        try {
            File file = new File(LOG_FILE);
            // Create directory if it doesn't exist
            file.getParentFile().mkdirs();
            
            FileWriter fw = new FileWriter(file, true);
            PrintWriter logWriter = new PrintWriter(fw);
            
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String timestamp = dtf.format(LocalDateTime.now());
            
            logWriter.println("[" + timestamp + "] " + message);
            logWriter.close();
        } catch (IOException e) {
            System.err.println("Error writing to log file: " + e.getMessage());
        }
    }
}