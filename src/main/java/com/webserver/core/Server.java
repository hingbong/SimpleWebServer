package com.webserver.core;

import com.webserver.core.http.Session;
import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

/**
 * @author Hingbong
 */
public class Server {

    public static final File WEB_ROOT = new File("web_root");
    private static final Server SERVER = new Server();
    private ServerSocketChannel serverSocket;
    private Selector selector;

    private Server() {
        try {
            serverSocket = ServerSocketChannel.open();
            // bind the port to the channel
            serverSocket.bind(new InetSocketAddress(8888));
            selector = Selector.open();
            // set the server socket channel to non-blocking
            serverSocket.configureBlocking(false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        //noinspection InfiniteLoopStatement
        while (true) {
            SERVER.start();
        }
    }

    private void start() {
        try {
            serverSocket.register(
                selector,
                // register the channel to the selector to listen the socket
                SelectionKey.OP_ACCEPT);
            while (selector.select() > 0) {
                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    // get the selection key and remove it from selector
                    iterator.remove();
                    // if the key is not valid,pass it
                    if (key.isValid()) {
                        // see if the key is acceptable and go on
                        if (key.isAcceptable()) {
                            // use server socket channel to accept the socket channel
                            SocketChannel socketChannel = serverSocket.accept();
                            // set the socket channel to non-blocking
                            socketChannel.configureBlocking(false);
                            // register the channel readable to selector
                            socketChannel.register(selector, SelectionKey.OP_READ);
                            // see if the key is readable and go on
                        } else if (key.isReadable()) {
                            SocketChannel channel = (SocketChannel) key.channel();
                            channel.configureBlocking(false);
                            new Session(channel).run();
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
