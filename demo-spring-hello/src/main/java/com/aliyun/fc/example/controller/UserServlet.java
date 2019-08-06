package com.aliyun.fc.example.controller;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;


@WebServlet(asyncSupported=true)
public class UserServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        System.out.println("UserServlet invoked, contextPath: " + req.getContextPath());
        PrintWriter writer = resp.getWriter();
        writer.write("Get response from UserServlet");
        writer.flush();
        writer.close();
    }
    
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String username = req.getParameter("username");
        String pwd = req.getParameter("pwd");
        System.out.println(username + "--" + pwd);
        
        OutputStream os = resp.getOutputStream();
        HttpSession session = req.getSession();
        Object obj = session.getAttribute("user");
        if(obj == null) {
            session.setAttribute("user", "username: " + username + ", pwd: " + pwd);
            os.write(("New session: " + username + ", pwd: " + pwd).getBytes());
        }else {
            System.out.println(obj.getClass());
            String user = (String)session.getAttribute("user");
            os.write(("Existed session: " + user).getBytes());
        }
        resp.setContentType("text/html");
    }
}
