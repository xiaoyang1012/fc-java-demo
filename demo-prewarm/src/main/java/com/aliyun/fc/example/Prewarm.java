package com.aliyun.fc.example;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.aliyun.fc.runtime.Context;
import com.aliyun.fc.runtime.Credentials;
import com.aliyun.fc.runtime.FunctionInitializer;
import com.aliyun.fc.runtime.StreamRequestHandler;
import com.aliyuncs.fc.client.FunctionComputeClient;
import com.aliyuncs.fc.config.Config;
import com.aliyuncs.fc.model.HttpAuthType;
import com.aliyuncs.fc.model.HttpMethod;
import com.aliyuncs.fc.request.HttpInvokeFunctionRequest;
import com.aliyuncs.fc.request.InvokeFunctionRequest;
import com.aliyuncs.fc.response.InvokeFunctionResponse;

public class Prewarm implements StreamRequestHandler, FunctionInitializer {
    
    private final String region = System.getenv("REGION");
    private final String accountID = System.getenv("ACCOUNT");
    private final String functionName = System.getenv("FUCNTION_NAME");
    private final String serviceName = System.getenv("SERVICE_NAME");
    private final boolean isHttp = Boolean.parseBoolean(System.getenv("IS_HTTP"));
    private final int count = Integer.parseInt(System.getenv("COUNT"));
    private AtomicInteger curCount = new AtomicInteger(0);
    
    private FunctionComputeClient client;
    private ExecutorService exec = Executors.newFixedThreadPool(count);
    
    public void initialize(Context context) throws IOException {
        Credentials creds = context.getExecutionCredentials();
        Config config = new Config(region, accountID,
                creds.getAccessKeyId(), creds.getAccessKeySecret(), creds.getSecurityToken(), true);
                
        client = new FunctionComputeClient(config);
        context.getLogger().info("Initialize client: " + client);
    }
    
    public void handleRequest(InputStream input, OutputStream output, Context context) throws IOException {
        curCount.set(0);
        if (isHttp) {
            prewarmHttpFunction(count);
        } else {
            prewarmEventFunction(count);
        }
        
        while(true) {
            if(curCount.get() == count) {
                break;
            }
            
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        context.getLogger().info("Prewarm succ");
    }
    
    /**
     * Prewam event function
     * @param count
     */
    private void prewarmEventFunction(int count) {
        for (int i = 0; i < count; i ++) {
            exec.submit(new PrewarmEventFunctionRunnable());
        }
    }
    
    /**
     * Prewarm http function
     * @param count
     */
    private void prewarmHttpFunction(int count) {
        for (int i = 0; i < count; i ++) {
            exec.submit(new PrewarmHttpFunctionRunnable());
        }
    }
    
    class PrewarmEventFunctionRunnable implements Runnable {
        public void run() {
            while (! Thread.interrupted()) {
                InvokeFunctionRequest request = new InvokeFunctionRequest(serviceName, functionName);
                InvokeFunctionResponse resp = client.invokeFunction(request);
                System.out.println(resp.getStatus() + ", " + resp.getLogResult());
                
                if (resp.isSuccess()) {
                    int cnt = curCount.incrementAndGet();
                    System.out.println("Finish event function prewarm: " + cnt);
                    break;
                }
            }
        }
    }
    
    class PrewarmHttpFunctionRunnable implements Runnable {
        public void run() {
            while (! Thread.interrupted()) {
                HttpInvokeFunctionRequest request = new HttpInvokeFunctionRequest(
                        serviceName, functionName, HttpAuthType.ANONYMOUS, HttpMethod.GET);
                InvokeFunctionResponse resp = client.invokeFunction(request);
                System.out.println(resp.getStatus() + ", " + resp.getLogResult());
                
                if (resp.isSuccess()) {
                    int cnt = curCount.incrementAndGet();
                    System.out.println("Finish http function prewarm: " + cnt);
                    break;
                }
            }
        }
    }
}
