package main.java.client;

import java.io.*;
import java.net.*;

public class client {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 9001;
    private BufferedReader in;
    private PrintWriter out;
    private Socket socket;
    private String username;
    private GUI gui;
    
    public client() {
        gui = new GUI(this);
    }
    
    // Connect to the server
    public boolean connect(String username) {
        try {
            this.username = username;
            socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            
            // Start a thread to listen for incoming messages
            new Thread(new MessageHandler()).start();
            return true;
        } catch (IOException e) {
            System.err.println("Error connecting to server: " + e.getMessage());
            return false;
        }
    }
    
    // Send a message to the server
    public void sendMessage(String message) {
        if (out != null) {
            out.println(message);
        }
    }
    
    // Disconnect from the server
    public void disconnect() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            System.err.println("Error closing connection: " + e.getMessage());
        }
    }
    
    // Inner class for handling incoming messages
    private class MessageHandler implements Runnable {
        @Override
        public void run() {
            try {
                String serverMessage;
                
                // First message is expected to be a prompt for username
                serverMessage = in.readLine();
                if (serverMessage.equals("SUBMIT_USERNAME")) {
                    out.println(username);
                }
                
                // Continue reading messages
                while ((serverMessage = in.readLine()) != null) {
                    final String message = serverMessage;
                    
                    // Update the GUI on the Event Dispatch Thread
                    javax.swing.SwingUtilities.invokeLater(() -> {
                        gui.displayMessage(message);
                    });
                }
            } catch (IOException e) {
                if (!socket.isClosed()) {
                    System.err.println("Error reading from server: " + e.getMessage());
                    // Update GUI to show disconnection
                    javax.swing.SwingUtilities.invokeLater(() -> {
                        gui.displayMessage("Disconnected from server");
                        gui.setConnectionStatus(false);
                    });
                }
            }
        }
    }
    
    public static void main(String[] args) {
        new client();
    }
}