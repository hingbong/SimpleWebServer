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

public class Server {

    public static final File WEB_ROOT = new File("web_root");
    private static final Server SERVER = new Server();
    private ServerSocketChannel serverSocket;
    private Selector selector;

    private Server() {
        try {
            serverSocket = ServerSocketChannel.open();
            serverSocket.bind(new InetSocketAddress(8888)); // bind the port to the channel
            selector = Selector.open();
            serverSocket.configureBlocking(false); // set the server socket channel to non-blocking
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        //noinspection InfiniteLoopStatement
        while (true) {
            SERVER.start();
            Thread.sleep(100);
        }
    }

    private void start() {
        try {
            serverSocket.register(selector,
                SelectionKey.OP_ACCEPT); // register the channel to the selector to listen the socket
            while (selector.select() > 0) {
                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    iterator.remove(); // get the selection key and remove it from selector
                    if (key.isValid()) { // if the key is not valid,pass it
                        if (key.isAcceptable()) { // see if the key is acceptable and go on
                            SocketChannel socketChannel = serverSocket
                                .accept(); // use server socket channel to accept the socket channel
                            socketChannel
                                .configureBlocking(false); // set the socket channel to non-blocking
                            socketChannel.register(selector,
                                SelectionKey.OP_READ); // register the channel readable to selector
                        } else if (key.isReadable()) { // see if the key is readable and go on
                            SocketChannel channel = (SocketChannel) key.channel();
                            channel.configureBlocking(false);
                            new Session().run(channel);
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}


