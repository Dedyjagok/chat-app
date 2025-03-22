package main.java.websocket;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.regex.*;

public class WebSocketHandler extends Thread {
    private Socket socket;
    private InputStream in;
    private OutputStream out;
    private String username;
    
    public WebSocketHandler(Socket socket) {
        this.socket = socket;
    }
    
    @Override
    public void run() {
        try {
            in = socket.getInputStream();
            out = socket.getOutputStream();
            
            // Handle WebSocket handshake
            handleHandshake();
            
            // Process WebSocket frames
            while (socket.isConnected()) {
                byte[] frame = readFrame();
                if (frame == null) {
                    break;
                }
                
                String message = new String(frame, StandardCharsets.UTF_8);
                System.out.println("Received: " + message);
                
                // Check if it's a username message
                // In the run() method, modify the username handling:
                if (message.startsWith("USERNAME:")) {
                    username = message.substring(9).trim();
                    WebSocket.broadcast("SERVER: " + username + " has joined via WebSocket!", this);
                    
                    // Send current user list to the newly connected user
                    StringBuilder userList = new StringBuilder("USERLIST:");
                    synchronized (WebSocket.getConnections()) {
                        for (WebSocketHandler handler : WebSocket.getConnections()) {
                            if (handler != this && handler.username != null) {
                                userList.append(handler.username).append(",");
                            }
                        }
                    }
                    // Send the user list to only this client
                    send(userList.toString());
                } else {
                    // Regular chat message
                    WebSocket.broadcast(username + ": " + message, this);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                WebSocket.removeConnection(this);
                if (username != null) {
                    WebSocket.broadcast("SERVER: " + username + " has left the chat!", this);
                }
                socket.close();
            } catch (IOException e) {
                // Ignore
            }
        }
    }
    
    // Send a message to this client
    public void send(String message) {
        try {
            byte[] frameBytes = prepareFrame(message);
            out.write(frameBytes);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    // Handle WebSocket handshake
    private void handleHandshake() throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        
        String line;
        String key = null;
        while ((line = reader.readLine()) != null) {
            if (line.length() == 0) break;
            
            Pattern keyPattern = Pattern.compile("Sec-WebSocket-Key: (.*)");
            Matcher matcher = keyPattern.matcher(line);
            if (matcher.matches()) {
                key = matcher.group(1);
            }
        }
        
        if (key != null) {
            String acceptKey = WebSocket.getWebSocketAcceptKey(key);
            
            // Send handshake response
            String response = 
                "HTTP/1.1 101 Switching Protocols\r\n" +
                "Upgrade: websocket\r\n" +
                "Connection: Upgrade\r\n" +
                "Sec-WebSocket-Accept: " + acceptKey + "\r\n\r\n";
                
            out.write(response.getBytes(StandardCharsets.UTF_8));
            out.flush();
        }
    }
    
    // Read a WebSocket frame from the client
    private byte[] readFrame() throws IOException {
        byte[] header = new byte[2];
        int read = in.read(header, 0, 2);
        if (read == -1) return null;
        
        byte firstByte = header[0];
        byte secondByte = header[1];
        
        boolean fin = (firstByte & 0x80) != 0;
        int opcode = firstByte & 0x0F;
        boolean mask = (secondByte & 0x80) != 0;
        int payloadLength = secondByte & 0x7F;
        
        // Check for close frame
        if (opcode == 8) {
            return null;
        }
        
        // Handle payload length
        if (payloadLength == 126) {
            byte[] extendedLength = new byte[2];
            in.read(extendedLength, 0, 2);
            payloadLength = ((extendedLength[0] & 0xFF) << 8) | (extendedLength[1] & 0xFF);
        } else if (payloadLength == 127) {
            byte[] extendedLength = new byte[8];
            in.read(extendedLength, 0, 8);
            // We don't handle payloads larger than Integer.MAX_VALUE
            payloadLength = (int)((extendedLength[4] & 0xFF) << 24 | 
                                 (extendedLength[5] & 0xFF) << 16 |
                                 (extendedLength[6] & 0xFF) << 8 |
                                 (extendedLength[7] & 0xFF));
        }
        
        // Read mask
        byte[] mask_bytes = new byte[4];
        if (mask) {
            in.read(mask_bytes, 0, 4);
        }
        
        // Read payload
        byte[] payload = new byte[payloadLength];
        in.read(payload, 0, payloadLength);
        
        // Apply mask if necessary
        if (mask) {
            for (int i = 0; i < payload.length; i++) {
                payload[i] = (byte) (payload[i] ^ mask_bytes[i % 4]);
            }
        }
        
        return payload;
    }
    
    // Prepare a WebSocket frame to send to the client
    private byte[] prepareFrame(String message) {
        byte[] payload = message.getBytes(StandardCharsets.UTF_8);
        int payloadLength = payload.length;
        
        ByteArrayOutputStream frame = new ByteArrayOutputStream();
        
        // First byte: FIN bit set, opcode text
        frame.write(0x81);
        
        // Second byte: payload length (with mask bit unset)
        if (payloadLength <= 125) {
            frame.write(payloadLength);
        } else if (payloadLength <= 65535) {
            frame.write(126);
            frame.write((payloadLength >> 8) & 0xFF);
            frame.write(payloadLength & 0xFF);
        } else {
            frame.write(127);
            frame.write(0); // we're assuming payload is not > 2^31
            frame.write(0);
            frame.write(0);
            frame.write(0);
            frame.write((payloadLength >> 24) & 0xFF);
            frame.write((payloadLength >> 16) & 0xFF);
            frame.write((payloadLength >> 8) & 0xFF);
            frame.write(payloadLength & 0xFF);
        }
        
        // Add payload
        try {
            frame.write(payload);
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        return frame.toByteArray();
    }
}