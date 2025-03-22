package main.java.client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class GUI {
    private JFrame frame;
    private JTextArea chatArea;
    private JTextField messageField;
    private JTextField usernameField;
    private JButton connectButton;
    private JButton sendButton;
    private JLabel statusLabel;
    private JList<String> userList;
    private DefaultListModel<String> userListModel;
    
    private client client;
    private boolean connected = false;
    
    public GUI(client client) {
        this.client = client;
        initializeUI();
    }
    
    private void initializeUI() {
        // Create main frame
        frame = new JFrame("Chat Application");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLayout(new BorderLayout(10, 10));
        
        // North panel - Connection status and controls
        JPanel northPanel = new JPanel();
        northPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        
        statusLabel = new JLabel("Status: Disconnected");
        statusLabel.setForeground(Color.RED);
        northPanel.add(statusLabel);
        
        usernameField = new JTextField(15);
        northPanel.add(new JLabel("Username:"));
        northPanel.add(usernameField);
        
        connectButton = new JButton("Connect");
        northPanel.add(connectButton);
        
        frame.add(northPanel, BorderLayout.NORTH);
        
        // Center panel - Chat area
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        JScrollPane chatScrollPane = new JScrollPane(chatArea);
        
        // East panel - User list
        userListModel = new DefaultListModel<>();
        userList = new JList<>(userListModel);
        JScrollPane userScrollPane = new JScrollPane(userList);
        userScrollPane.setPreferredSize(new Dimension(150, 0));
        
        // Create split pane for chat and user list
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, 
                                             chatScrollPane, userScrollPane);
        splitPane.setResizeWeight(0.8);
        frame.add(splitPane, BorderLayout.CENTER);
        
        // South panel - Message input and send button
        JPanel southPanel = new JPanel();
        southPanel.setLayout(new BorderLayout(5, 5));
        
        messageField = new JTextField();
        messageField.setEnabled(false);
        southPanel.add(messageField, BorderLayout.CENTER);
        
        sendButton = new JButton("Send");
        sendButton.setEnabled(false);
        southPanel.add(sendButton, BorderLayout.EAST);
        
        frame.add(southPanel, BorderLayout.SOUTH);
        
        // Add event listeners
        connectButton.addActionListener(e -> handleConnect());
        sendButton.addActionListener(e -> sendMessage());
        messageField.addActionListener(e -> sendMessage());
        
        // Handle window close
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (connected) {
                    client.disconnect();
                }
                System.exit(0);
            }
        });
        
        // Show the frame
        frame.setVisible(true);
    }
    
    // Handle connect button click
    private void handleConnect() {
        if (!connected) {
            String username = usernameField.getText().trim();
            if (username.isEmpty()) {
                JOptionPane.showMessageDialog(frame, 
                    "Please enter a username", 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Try to connect
            if (client.connect(username)) {
                setConnectionStatus(true);
            } else {
                JOptionPane.showMessageDialog(frame, 
                    "Failed to connect to server", 
                    "Connection Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        } else {
            // Disconnect
            client.disconnect();
            setConnectionStatus(false);
        }
    }
    
    // Send message to server
    private void sendMessage() {
        if (connected) {
            String message = messageField.getText().trim();
            if (!message.isEmpty()) {
                client.sendMessage(message);
                messageField.setText("");
            }
        }
    }
    
    // Set connection status and update UI
    public void setConnectionStatus(boolean isConnected) {
        this.connected = isConnected;
        
        if (isConnected) {
            statusLabel.setText("Status: Connected");
            statusLabel.setForeground(new Color(0, 128, 0)); // Dark green
            connectButton.setText("Disconnect");
            messageField.setEnabled(true);
            sendButton.setEnabled(true);
            usernameField.setEnabled(false);
        } else {
            statusLabel.setText("Status: Disconnected");
            statusLabel.setForeground(Color.RED);
            connectButton.setText("Connect");
            messageField.setEnabled(false);
            sendButton.setEnabled(false);
            usernameField.setEnabled(true);
            userListModel.clear();
        }
    }
    
    // Display a message in the chat area
    public void displayMessage(String message) {
        chatArea.append(message + "\n");
        
        // Auto scroll to bottom
        chatArea.setCaretPosition(chatArea.getDocument().getLength());
        
        // Update user list based on join/leave messages
        if (message.startsWith("SERVER:")) {
            updateUserList(message.substring(7).trim());
        }
    }
    
    // Update the user list based on join/leave messages
    private void updateUserList(String message) {
        String username = null;
        boolean joined = false;
        
        if (message.contains("has joined")) {
            int index = message.indexOf("has joined");
            username = message.substring(0, index).trim();
            joined = true;
        } else if (message.contains("has left")) {
            int index = message.indexOf("has left");
            username = message.substring(0, index).trim();
            joined = false;
        }
        
        if (username != null) {
            if (joined) {
                if (!containsUser(username)) {
                    userListModel.addElement(username);
                }
            } else {
                for (int i = 0; i < userListModel.size(); i++) {
                    if (userListModel.get(i).equals(username)) {
                        userListModel.remove(i);
                        break;
                    }
                }
            }
        }
    }
    
    // Check if user is already in the list
    private boolean containsUser(String username) {
        for (int i = 0; i < userListModel.size(); i++) {
            if (userListModel.get(i).equals(username)) {
                return true;
            }
        }
        return false;
    }
}