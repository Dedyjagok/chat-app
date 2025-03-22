document.addEventListener('DOMContentLoaded', () => {
    // Elements
    const statusElement = document.getElementById('status');
    const usernameInput = document.getElementById('username-input');
    const usernameContainer = document.getElementById('username-container');
    const messageContainer = document.getElementById('message-container');
    const connectBtn = document.getElementById('connect-btn');
    const messageInput = document.getElementById('message-input');
    const sendBtn = document.getElementById('send-btn');
    const chatMessages = document.getElementById('chat-messages');
    const userList = document.getElementById('users');
    
    // Connection settings
    const connectionTypeInputs = document.querySelectorAll('input[name="connection"]');
    let connectionType = 'websocket';
    let socket = null;
    let username = '';
    
    // Server details
    const WEBSOCKET_URL = 'ws://localhost:9002';
    const SOCKET_HOST = 'localhost';
    const SOCKET_PORT = 9001;
    
    // Set the connection type based on radio selection
    connectionTypeInputs.forEach(input => {
        input.addEventListener('change', (e) => {
            connectionType = e.target.value;
        });
    });
    
    // Connect button event listener
    connectBtn.addEventListener('click', () => {
        username = usernameInput.value.trim();
        
        if (!username) {
            addMessage('Please enter a valid username', 'system');
            return;
        }
        
        connect();
    });
    
    // Send button event listener
    sendBtn.addEventListener('click', sendMessage);
    
    // Message input event listener for Enter key
    messageInput.addEventListener('keypress', (e) => {
        if (e.key === 'Enter') {
            sendMessage();
        }
    });
    
    // Connect to the server based on selected connection type
    function connect() {
        if (connectionType === 'websocket') {
            connectWebSocket();
        } else {
            connectSocket();
        }
    }
    
    // Connect using WebSocket
    function connectWebSocket() {
        try {
            socket = new WebSocket(WEBSOCKET_URL);
            
            socket.onopen = () => {
                statusElement.textContent = 'Connected';
                statusElement.className = 'connected';
                
                // Send username
                socket.send('USERNAME:' + username);
                
                // Show message input and hide username input
                usernameContainer.style.display = 'none';
                messageContainer.style.display = 'flex';
                
                addMessage('Connected via WebSocket!', 'system');
            };
            
            socket.onmessage = (event) => {
                const message = event.data;
                processMessage(message);
            };
            
            socket.onclose = () => {
                statusElement.textContent = 'Disconnected';
                statusElement.className = 'disconnected';
                usernameContainer.style.display = 'flex';
                messageContainer.style.display = 'none';
                addMessage('Disconnected from server', 'system');
            };
            
            socket.onerror = (error) => {
                console.error('WebSocket error:', error);
                addMessage('Error: Could not connect to WebSocket server', 'system');
            };
        } catch (error) {
            console.error('WebSocket connection error:', error);
            addMessage('Error: ' + error.message, 'system');
        }
    }
    
    // Connect using Socket (TCP)
    function connectSocket() {
        // Since browsers can't directly connect to TCP sockets,
        // we'll simulate this with a WebSocket that talks to our Socket server
        // In a real implementation, you'd need a proxy server or use HTTP for this
        
        addMessage('Socket connection is not supported in the browser directly.', 'system');
        addMessage('Please select WebSocket or use the Java client.', 'system');
        
        // In a real-world application, you would make an HTTP request to a proxy server
        // that would then communicate with the Java Socket server
    }
    
    // Send message
    function sendMessage() {
        const messageText = messageInput.value.trim();
        
        if (!messageText || !socket) {
            return;
        }
        
        // Send message
        if (socket && socket.readyState === WebSocket.OPEN) {
            socket.send(messageText);
            
            // Add to UI
            addMessage(messageText, 'user', username);
            
            // Clear input
            messageInput.value = '';
        } else {
            addMessage('Not connected to server', 'system');
        }
    }
    
    // Process incoming message
    function processMessage(message) {
        // Check if it's a system message
        if (message.startsWith('SERVER:')) {
            const serverMsg = message.substring(7).trim();
            addMessage(serverMsg, 'system');
            
            // Update user list based on join/leave
            updateUserList(serverMsg);
        } else if (message.startsWith('SUBMIT_USERNAME')) {
            // This would only come from a Socket connection
            // We don't handle this here since we're using WebSockets in the browser
        } else {
            // Regular user message
            const colonIndex = message.indexOf(':');
            if (colonIndex > -1) {
                const sender = message.substring(0, colonIndex).trim();
                const content = message.substring(colonIndex + 1).trim();
                
                if (sender !== username) {
                    addMessage(content, 'other', sender);
                }
            } else {
                addMessage(message, 'other');
            }
        }
    }
    
    // Add message to the UI
    function addMessage(text, type, sender) {
        const messageElement = document.createElement('div');
        messageElement.classList.add('message', type);
        
        // Add sender name if provided and it's not a system message
        let messageContent = text;
        if (sender && type !== 'system') {
            const senderSpan = document.createElement('strong');
            senderSpan.textContent = sender + ': ';
            messageElement.appendChild(senderSpan);
            messageContent = text;
        }
        
        const textNode = document.createTextNode(messageContent);
        messageElement.appendChild(textNode);
        
        chatMessages.appendChild(messageElement);
        
        // Auto scroll to bottom
        chatMessages.scrollTop = chatMessages.scrollHeight;
    }
    
    // Update the user list based on join/leave messages
    function updateUserList(message) {
        const joined = message.match(/(\w+) has joined/);
        const left = message.match(/(\w+) has left/);
        
        if (joined) {
            const user = joined[1];
            if (user !== username) {
                const userItem = document.createElement('li');
                userItem.textContent = user;
                userItem.id = 'user-' + user;
                userList.appendChild(userItem);
            }
        } else if (left) {
            const user = left[1];
            const userItem = document.getElementById('user-' + user);
            if (userItem) {
                userList.removeChild(userItem);
            }
        }
    }
});