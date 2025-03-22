# Real-time Chat Application

A dual-protocol chat application that supports both standard Socket connections and WebSockets, enabling real-time communication.

## Features
- Real-time messaging using both Socket and WebSocket protocols
- User presence detection (join/leave notifications)
- Message history for new users joining the chat
- User list showing currently active participants
- Dual client support: Web browser interface and Java desktop client
- Message logging system with timestamps

## Technologies Used

### **Backend**
- Java Socket API for traditional socket connections
- Custom WebSocket implementation for real-time chat
- Thread-based client handling for concurrent connections

### **Frontend**
- HTML5, CSS3, and JavaScript for the web interface
- Java Swing for the desktop client GUI
- WebSocket API for browser-server communication

## Project Structure

```plaintext
chat-app/
│── frontend/        # Web client files
│   ├── index.html   # Web UI markup
│   ├── styles.css   # Web UI styling
│── chat/            # Web client logic
│── main/            # Server & socket server implementation
│── java/
│   ├── server/
│   │   ├── ChatServer.java
│   │   ├── ClientHandler.java
│   ├── websocket/
│   │   ├── WebSocket.java
│   │   ├── WebSocketHandler.java
│── client/          # Java desktop client
│── gui/             # GUI Java
│── resources/
│   ├── application.properties  # Configuration
│── logs/            # Message log directory
│── LICENSE          # GNU GPL v3 License
```

## Installation and Setup

### **Prerequisites**
- Java Development Kit (JDK) 8 or newer
- Web browser with WebSocket support (for web client)

### **Compiling the Application**

1. Clone the repository or download the source code.
2. Navigate to the project root directory.
3. Compile the Java files:

```bash
javac -d . src/main/java/server/*.java src/main/java/websocket/*.java src/main/java/client/*.java
```

### **Running the Application**
**Starting Server:**
1. Socket Server :
```bash
java main.java.server.ChatServer
```
you should see : "Chat Server is running..."

2. WebSocket Server:
```bash
java main.java.websocket.WebSocket
```
you should see: "WebSocket Server is running on port 9002"

### **Accesing the Web Client**
 You have two options to access the web client:
 1. **Using a local web server**
 - set up a simple HTTP server in frontend directory:
 ```bash
 cd front end
 #if you have Python installed:
 python -m http.server 8080
 ```

 - Open your browser and navigate to : http://localhost:8080

 2. **Directly opening the HTML file:**
 - open 'index.html' directly in your browser
 - Note: Some browsers might restrict WebSocket connections from files opened directly

 ## **Running the java Desktop Client**
 This will open a graphical user interface where you can enter your username and connect to the chat.
 

 ## Usage Instructions

### For Web Client:
1. Enter a username in the text field
2. Select connection type (WebSocket is recommended and selected by default)
3. Click "Connect"
4. Once connected, you can send messages using the message input field

### For Java Client:
1. Enter a username in the text field
2. Click "Connect"
3. Send messages using the message input field

---

## Configuration

The application can be configured via the `application.properties` file:

- [`server.socket.port`](#) : Port for the Socket server (default: `9001`)
- [`server.websocket.port`](#) : Port for the WebSocket server (default: `9002`)
- [`log.file.path`](#) : Path for chat logs (default: `logs/chat-logs.txt`)
- [`log.enabled`](#) : Enable/disable logging (default: `true`)
- [`user.timeout.seconds`](#) : User inactivity timeout (default: `300 seconds`)

## License

This project is licensed under the GNU General Public License v3.0 - see the 📜 [`LICENSE`](./LICENSE) file for details.

---

## Author

**Dedy Hutahaean Putra**  
GitHub: [https://github.com/Dedyjagok](https://github.com/Dedyjagok)



