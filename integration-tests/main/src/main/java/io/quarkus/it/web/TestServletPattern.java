package io.quarkus.it.web;

import java.io.IOException;

import javax.inject.Inject;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(name = "MyServletPattern", urlPatterns = "/testservletpattern/*")
public class TestServletPattern extends HttpServlet {

    @Inject
    HttpServletResponse injectedResponse;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        injectedResponse.getWriter().write("hello world");
    }
}
