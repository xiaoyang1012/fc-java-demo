package com.aliyun.fc.example;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.aliyun.fc.runtime.Context;
import com.aliyun.fc.runtime.FcAppLoader;
import com.aliyun.fc.runtime.FunctionComputeLogger;
import com.aliyun.fc.runtime.FunctionInitializer;
import com.aliyun.fc.runtime.HttpRequestHandler;


public class HelloSpringBoot implements FunctionInitializer, HttpRequestHandler {
    private FcAppLoader fcAppLoader = new FcAppLoader();

    private String ossEndPoint = "${YourOSSEndPoint}";
    private String bucket = "{YourOSSBucket}";
    private String key = "demo-springboot-hello-1.0.0.war";
    
    private String userContextPath = "/2016-08-15/proxy/fun-java/demo-springboot";
    
    @Override
    public void initialize(Context context) throws IOException {
        FunctionComputeLogger fcLogger = context.getLogger();
        
        fcAppLoader.setFCContext(context);
        
        // Load code from OSS
        fcLogger.info("Begin load code: " + key);
        boolean codeSuccess = fcAppLoader.loadCodeFromOSS(ossEndPoint, bucket, key);
        if (! codeSuccess) {
            throw new IOException("Download code failed");
        }
        fcLogger.info("End load code");
        
        // Init webapp from code
        long timeBegin = System.currentTimeMillis();
        fcLogger.info("Begin load webapp");
        boolean initSuccess = fcAppLoader.initApp(userContextPath, HelloSpringBoot.class.getClassLoader());
        if(! initSuccess) {
            throw new IOException("Init web app failed");
        }
        fcLogger.info("End load webapp, elapsed: " + (System.currentTimeMillis() - timeBegin) + "ms");
    }
    
    @Override
    public void handleRequest(HttpServletRequest request, HttpServletResponse response, Context context)
            throws IOException, ServletException {
        try {
            fcAppLoader.forward(request, response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
