package tigerworkshop.webapphardwarebridge;

import com.google.gson.Gson;
import org.java_websocket.WebSocket;
import org.java_websocket.exceptions.WebsocketNotConnectedException;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tigerworkshop.webapphardwarebridge.responses.PrintRequest;

import java.lang.ref.WeakReference;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class ProxyWebSocketServer extends WebSocketServer {

    private Logger logger = LoggerFactory.getLogger(getClass());
    private HashMap<String, ArrayList<WeakReference<WebSocket>>> socketChannelSubscriptions = new HashMap<>();

    public ProxyWebSocketServer(String address, int port) {
        super(new InetSocketAddress(address, port));
    }

    @Override
    public void run() {
        try {
            super.run();
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
            System.exit(1);
        }
    }

    @Override
    public void onOpen(WebSocket connection, ClientHandshake handshake) {
        try {
            String descriptor = handshake.getResourceDescriptor();

            URI uri = new URI(descriptor);
            String channel = uri.getPath();

            addSocketToChannel(channel, connection);

            logger.info(connection.getRemoteSocketAddress().toString() + " connected to " + channel);
        } catch (URISyntaxException e) {
            logger.error(connection.getRemoteSocketAddress().toString() + " error", e);
            connection.close();
        }
    }

    @Override
    public void onClose(WebSocket connection, int code, String reason, boolean remote) {
        logger.info(connection.getRemoteSocketAddress() + " disconnected");
    }

    @Override
    public void onMessage(WebSocket connection, String message) {
        logger.info("onMessage: " + connection.getRemoteSocketAddress() + ": " + message);

        String channel = "/client";

        Gson gson = new Gson();
        PrintRequest printRequest = gson.fromJson(message, PrintRequest.class);

        channel = channel + "/" + printRequest.getId();

        ArrayList<WeakReference<WebSocket>> webSockets = getSocketListForChannel(channel);

        logger.info("Forwarding to " + webSockets.size() + " proxy client connections");
        for (Iterator<WeakReference<WebSocket>> it = webSockets.iterator(); it.hasNext(); ) {
            WeakReference<WebSocket> webSocketWeakReference = it.next();
            try {
                WebSocket webSocket = webSocketWeakReference.get();
                webSocket.send(message);
                logger.info("Forwarded to " + webSocket.getRemoteSocketAddress());
            } catch (WebsocketNotConnectedException e) {
                logger.warn("WebsocketNotConnectedException: Removing connection from list");
                it.remove();
            }
        }
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        logger.error(ex.getMessage(), ex);
    }

    @Override
    public void onStart() {
        logger.info("BridgeWebSocketServer started");
    }

    private ArrayList<WeakReference<WebSocket>> getSocketListForChannel(String channel) {
        ArrayList<WeakReference<WebSocket>> socketList = socketChannelSubscriptions.get(channel);
        if (socketList == null) {
            return new ArrayList<>();
        }
        return socketList;
    }

    private void addSocketToChannel(String channel, WebSocket socket) {
        ArrayList<WeakReference<WebSocket>> connectionList = getSocketListForChannel(channel);
        connectionList.add(new WeakReference<>(socket));
        socketChannelSubscriptions.put(channel, connectionList);
    }

    private void removeSocketFromChannel(String channel, WebSocket socket) {
        ArrayList<WeakReference<WebSocket>> connectionList = getSocketListForChannel(channel);
        connectionList.removeIf(wr -> socket.equals(wr.get()));
        socketChannelSubscriptions.put(channel, connectionList);
    }
}
