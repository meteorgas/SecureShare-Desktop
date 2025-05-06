package network;

import utils.Config;

import javax.swing.*;
import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Handles peer discovery for the SecureShare application using UDP broadcast.
 */
public class PeerDiscovery {
    
    /**
     * Class to represent a discovered receiver device.
     */
    public static class ReceiverDevice {
        private final String name;
        private final String ipAddress;
        private final int port;

        public ReceiverDevice(String name, String ipAddress, int port) {
            this.name = name;
            this.ipAddress = ipAddress;
            this.port = port;
        }

        public String getName() {
            return name;
        }

        public String getIpAddress() {
            return ipAddress;
        }

        public int getPort() {
            return port;
        }

        @Override
        public String toString() {
            return name + " (" + ipAddress + ")";
        }
    }
    
    private DatagramSocket socket;
    private SwingWorker<Void, String> discoveryThread;
    private final List<Consumer<String>> logListeners = new CopyOnWriteArrayList<>();
    private final List<ReceiverDevice> discoveredDevices = new ArrayList<>();
    
    /**
     * Adds a log listener to receive discovery-related log messages.
     * 
     * @param listener A consumer that will receive log messages
     */
    public void addLogListener(Consumer<String> listener) {
        logListeners.add(listener);
    }
    
    /**
     * Removes a previously added log listener.
     * 
     * @param listener The listener to remove
     */
    public void removeLogListener(Consumer<String> listener) {
        logListeners.remove(listener);
    }
    
    /**
     * Logs a message to all registered log listeners.
     * 
     * @param message The message to log
     */
    private void log(String message) {
        for (Consumer<String> listener : logListeners) {
            listener.accept(message);
        }
    }
    
    /**
     * Starts the discovery service for a receiver.
     * This opens a UDP socket and listens for discovery requests.
     * 
     * @param port The TCP port that the receiver is listening on for file transfers
     * @return true if the service was started successfully, false otherwise
     */
    public boolean startReceiver(int port) {
        try {
            // Close any existing discovery socket
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }

            // Create a new discovery socket
            socket = new DatagramSocket(Config.DISCOVERY_PORT);
            discoveryThread = new ReceiverDiscoveryThread(port);
            discoveryThread.execute();
            log("Discovery service started on port " + Config.DISCOVERY_PORT);
            return true;
        } catch (SocketException e) {
            log("Error starting discovery service: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Stops the discovery service.
     */
    public void stop() {
        if (discoveryThread != null && !discoveryThread.isDone()) {
            discoveryThread.cancel(true);
        }

        if (socket != null && !socket.isClosed()) {
            socket.close();
        }

        log("Discovery service stopped");
    }
    
    /**
     * Searches for available receiver devices on the network.
     * 
     * @param callback A callback that will be called when the search is complete with the list of discovered devices
     */
    public void searchDevices(Consumer<List<ReceiverDevice>> callback) {
        // Clear previous discoveries
        discoveredDevices.clear();
        
        log("Searching for receiver devices...");

        // Create and execute the discovery worker
        SenderDiscoveryThread worker = new SenderDiscoveryThread(callback);
        worker.execute();
    }
    
    /**
     * SwingWorker class to handle UDP discovery for the receiver.
     */
    private class ReceiverDiscoveryThread extends SwingWorker<Void, String> {
        private final int listeningPort;
        
        public ReceiverDiscoveryThread(int listeningPort) {
            this.listeningPort = listeningPort;
        }
        
        @Override
        protected Void doInBackground() throws Exception {
            try {
                byte[] buffer = new byte[1024];

                while (!isCancelled()) {
                    try {
                        // Prepare to receive a packet
                        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

                        // Wait for a discovery packet
                        socket.receive(packet);

                        // Convert the packet data to a string
                        String message = new String(packet.getData(), 0, packet.getLength());

                        // Check if it's a discovery message
                        if (message.equals(Config.DISCOVERY_REQUEST)) {
                            // Get the sender's address and port
                            InetAddress senderAddress = packet.getAddress();
                            int senderPort = packet.getPort();

                            publish("Discovery request from: " + senderAddress.getHostAddress());

                            // Get the local hostname
                            String deviceName = InetAddress.getLocalHost().getHostName();

                            // Create the response message
                            String response = Config.DISCOVERY_RESPONSE_PREFIX + deviceName + "|" + listeningPort;
                            byte[] responseData = response.getBytes();

                            // Send the response back to the sender
                            DatagramPacket responsePacket = new DatagramPacket(
                                responseData, responseData.length, senderAddress, senderPort);
                            socket.send(responsePacket);

                            publish("Sent availability response to: " + senderAddress.getHostAddress());
                        }
                    } catch (IOException e) {
                        if (!isCancelled()) {
                            publish("Discovery error: " + e.getMessage());
                        }
                    }
                }
            } finally {
                if (socket != null && !socket.isClosed()) {
                    socket.close();
                }
            }

            return null;
        }

        @Override
        protected void process(java.util.List<String> chunks) {
            for (String message : chunks) {
                log(message);
            }
        }
    }
    
    /**
     * SwingWorker class to handle device discovery for the sender.
     */
    private class SenderDiscoveryThread extends SwingWorker<Void, String> {
        private final Consumer<List<ReceiverDevice>> callback;
        
        public SenderDiscoveryThread(Consumer<List<ReceiverDevice>> callback) {
            this.callback = callback;
        }
        
        @Override
        protected Void doInBackground() {
            try (DatagramSocket socket = new DatagramSocket()) {
                // Enable broadcast
                socket.setBroadcast(true);

                // Set timeout
                socket.setSoTimeout(Config.DISCOVERY_TIMEOUT);

                // Create the discovery message
                byte[] sendData = Config.DISCOVERY_REQUEST.getBytes();

                // Send to broadcast address
                InetAddress broadcastAddress = InetAddress.getByName("255.255.255.255");
                DatagramPacket sendPacket = new DatagramPacket(
                    sendData, sendData.length, broadcastAddress, Config.DISCOVERY_PORT);

                publish("Sending discovery broadcast...");
                socket.send(sendPacket);

                // Listen for responses until timeout
                byte[] receiveData = new byte[1024];

                long endTime = System.currentTimeMillis() + Config.DISCOVERY_TIMEOUT;

                while (System.currentTimeMillis() < endTime) {
                    try {
                        // Prepare to receive a response
                        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

                        // Wait for a response
                        socket.receive(receivePacket);

                        // Process the response
                        String response = new String(receivePacket.getData(), 0, receivePacket.getLength());

                        // Check if it's a valid response
                        if (response.startsWith(Config.DISCOVERY_RESPONSE_PREFIX)) {
                            String[] parts = response.split("\\|");
                            if (parts.length >= 3) {
                                String deviceName = parts[1];
                                String ipAddress = receivePacket.getAddress().getHostAddress();
                                int port;

                                try {
                                    port = Integer.parseInt(parts[2]);
                                } catch (NumberFormatException e) {
                                    port = Config.DEFAULT_PORT;
                                }

                                // Create a new device and add it to the list
                                ReceiverDevice device = new ReceiverDevice(deviceName, ipAddress, port);
                                discoveredDevices.add(device);

                                publish("Found receiver: " + device);
                            }
                        }
                    } catch (SocketTimeoutException e) {
                        // Timeout is expected, just continue
                        break;
                    }
                }

                publish("Discovery completed. Found " + discoveredDevices.size() + " receiver(s).");

            } catch (IOException e) {
                publish("Error during device discovery: " + e.getMessage());
            }

            return null;
        }

        @Override
        protected void process(java.util.List<String> chunks) {
            for (String message : chunks) {
                log(message);
            }
        }

        @Override
        protected void done() {
            // Call the callback with the discovered devices
            callback.accept(new ArrayList<>(discoveredDevices));
        }
    }
}