package com.aliyun.fc.example;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.aliyun.fc.runtime.*;
import com.aliyuncs.fc.client.FunctionComputeClient;
import com.aliyuncs.fc.config.Config;
import com.aliyuncs.fc.model.HttpAuthType;
import com.aliyuncs.fc.model.HttpMethod;
import com.aliyuncs.fc.request.HttpInvokeFunctionRequest;
import com.aliyuncs.fc.request.InvokeFunctionRequest;
import com.aliyuncs.fc.response.InvokeFunctionResponse;


public class PreWarm implements StreamRequestHandler, FunctionInitializer {
    
    private final String region = System.getenv("REGION");
    private final String accountID = System.getenv("ACCOUNT");
    private final String functionName = System.getenv("FUNCTION_NAME");
    private final String serviceName = System.getenv("SERVICE_NAME");
    private final boolean isHttp = Boolean.parseBoolean(System.getenv("IS_HTTP"));
    private final int count = Integer.parseInt(System.getenv("COUNT"));

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
        FunctionComputeLogger logger = context.getLogger();
        logger.info(String.format("Begin PreWarm service: %s, function: %s, concurrent count: %d",
                serviceName, functionName, count));

        CountDownLatch latch = new CountDownLatch(count);

        PreWarmFunctionRunnable.reset();
        if (isHttp) {
            PreWarmHttpFunction(latch);
        } else {
            PreWarmEventFunction(latch);
        }

        try{
            latch.await();
        } catch (InterruptedException e) {
            logger.info("PreWarm interrupted");
            return;
        }
        logger.info("PreWarm success");
    }
    
    /**
     * PreWarm event function
     *
     */
    private void PreWarmEventFunction(CountDownLatch latch) {
        InvokeFunctionRequest request = new InvokeFunctionRequest(serviceName, functionName);
        for (int i = 0; i < count; i ++) {
            exec.submit(new PreWarmFunctionRunnable(latch, client, request));
        }
    }
    
    /**
     * PreWarm http function, anonymous, GET
     */
    private void PreWarmHttpFunction(CountDownLatch latch) {
        HttpInvokeFunctionRequest request = new HttpInvokeFunctionRequest(serviceName, functionName,
                HttpAuthType.ANONYMOUS, HttpMethod.GET);

        for (int i = 0; i < count; i ++) {
            exec.submit(new PreWarmFunctionRunnable(latch, client, request));
        }
    }
}

class PreWarmFunctionRunnable implements Runnable {
    private static int counter = 0;
    private final int id = counter++;

    private CountDownLatch latch;
    private final FunctionComputeClient client;
    private final InvokeFunctionRequest request;

    public PreWarmFunctionRunnable(CountDownLatch latch, final FunctionComputeClient client,
                                   InvokeFunctionRequest request){
        this.latch = latch;
        this.client = client;
        this.request = request;
    }

    public static void reset() {
        counter = 0;
    }

    public void run() {
        while (! Thread.interrupted()) {
            InvokeFunctionResponse resp = client.invokeFunction(request);
            System.out.println(resp.getStatus() + ", " + resp.getLogResult());

            if (resp.isSuccess()) {
                System.out.println(String.format("Finish PreWarm, id: %d, service: %s, function: %s, type: %s",
                        id, request.getServiceName(), request.getFunctionName(), request.getInvocationType()));
                latch.countDown();
                break;
            }
        }
    }
}
