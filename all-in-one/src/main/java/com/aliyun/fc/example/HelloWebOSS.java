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


public class HelloWebOSS implements FunctionInitializer, HttpRequestHandler {
    private FcAppLoader fcAppLoader = new FcAppLoader();

    private String ossEndPoint = "${OSSEndPoint}";
    private String bucket = "${OSSBucket}";
    private String key = "greenhouse.war";

    // Not use custom domain
    //private String userContextPath = "/2016-08-15/proxy/${YourServiceName}/${YourFunctionName}";
    
    // Use custom domain
    private String userContextPath = "/greenhouse";
    
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
        boolean initSuccess = fcAppLoader.initApp(userContextPath, HelloWebOSS.class.getClassLoader());
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
