package com.webserver.core.servlet;

/**
 * @author Hingbong
 */
public interface HttpServlet {

    String DIR = "function/";

    /**
     * do something
     */
    void service();
}
