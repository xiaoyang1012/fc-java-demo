package com.aliyun.fc.example;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;

import com.aliyun.fc.runtime.Context;
import com.aliyun.fc.runtime.HttpRequestHandler;

public class Hello implements HttpRequestHandler {

    public void handleRequest(HttpServletRequest request, HttpServletResponse response, Context context)
            throws IOException, ServletException {
        String requestPath = (String) request.getAttribute("FC_REQUEST_PATH");
        String requestURI = (String) request.getAttribute("FC_REQUEST_URI");
        String requestClientIP = (String) request.getAttribute("FC_REQUEST_CLIENT_IP"); 
        
        response.setStatus(200);
        response.setHeader("header1", "value1");
        response.setHeader("header2", "value2");
        
        // Read request body
        InputStream in = request.getInputStream();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        IOUtils.copy(in, baos);
        String requestBody = baos.toString();
        
        String body = String.format("Path: %s\n Uri: %s\n IP: %s\n", requestPath, requestURI, requestClientIP);
        OutputStream out = response.getOutputStream();
        out.write((body).getBytes());
        out.write(("\n\n" + requestBody).getBytes());
        out.flush();
        out.close();
    }
}
