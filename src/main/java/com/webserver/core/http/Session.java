package com.webserver.core.http;

import java.nio.channels.SocketChannel;

public class Session {


    public void run(SocketChannel socketChannel) {
        Request request = new Request(socketChannel);
        request.start();
        Response response = new Response(socketChannel, request);
        response.sendData();
    }
}
