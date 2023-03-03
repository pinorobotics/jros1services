/*
 * Copyright 2023 jrosservices project
 * 
 * Website: https://github.com/pinorobotics/jros1services
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package pinorobotics.jros1services;

import id.jros1client.JRos1Client;
import id.jros1client.ros.responses.Response.StatusCode;
import id.jros1client.ros.responses.StringResponse;
import id.jrosclient.utils.RosNameUtils;
import id.jrosclient.utils.TextUtils;
import id.jrosmessages.Message;
import id.xfunction.Preconditions;
import id.xfunction.lang.XThread;
import id.xfunction.logging.TracingToken;
import id.xfunction.logging.XLogger;
import id.xfunction.util.LazyService;
import java.io.IOException;
import java.net.URI;
import java.util.concurrent.CompletableFuture;
import pinorobotics.jros1services.ros.transport.ServiceTcpRosClient;
import pinorobotics.jrosservices.JRosServiceClient;
import pinorobotics.jrosservices.exceptions.JRosServiceClientException;
import pinorobotics.jrosservices.msgs.ServiceDefinition;

/**
 * @author lambdaprime intid@protonmail.com
 */
public class JRos1ServiceClient<R extends Message, A extends Message> extends LazyService
        implements JRosServiceClient<R, A> {
    private JRos1Client client;
    private ServiceDefinition<R, A> serviceDefinition;
    private String serviceName;
    private TracingToken tracingToken;
    private TextUtils textUtils;
    private ServiceTcpRosClient<R, A> serviceNodeClient;
    private XLogger logger;

    /**
     * Creates a new instance of the client. Users should not call it directly but use {@link
     * JRos1ActionClientFactory} instead.
     *
     * @param client ROS1 client
     * @param serviceDefinition message type definitions for a service
     * @param serviceName name of the service which will execute the requests
     */
    JRos1ServiceClient(
            JRos1Client client,
            RosNameUtils nameUtils,
            ServiceDefinition<R, A> serviceDefinition,
            String serviceName,
            TextUtils textUtils) {
        this.client = client;
        this.serviceDefinition = serviceDefinition;
        this.textUtils = textUtils;
        this.serviceName = nameUtils.toAbsoluteName(serviceName);
        tracingToken = new TracingToken(serviceName, "" + hashCode());
        logger = XLogger.getLogger(getClass(), tracingToken);
    }

    @Override
    public CompletableFuture<A> sendRequestAsync(R requestMessage) {
        Preconditions.notNull(requestMessage, "requestMessage is null");
        logger.entering("sendRequest " + serviceName);
        startLazy();
        return serviceNodeClient.sendRequest(requestMessage);
    }

    @Override
    protected void onStart() {
        logger.entering("Starting service client for " + serviceName);
        var config = client.getClientConfiguration();
        var serviceLookupResult = StringResponse.EMPTY;
        while (serviceLookupResult.statusCode != StatusCode.SUCCESS) {
            logger.fine("Waiting for service {0} to be available on master node", serviceName);
            serviceLookupResult =
                    client.getMasterApi().lookupService(config.getCallerId(), serviceName);
            if (serviceLookupResult.statusCode == StatusCode.FAILURE)
                throw new JRosServiceClientException(
                        "Could not lookup service %s. ROS Master API returned failure: %s",
                        serviceName, serviceLookupResult);
            XThread.sleep(1000);
        }
        if (serviceLookupResult.value.isEmpty())
            throw new JRosServiceClientException(
                    "Could not lookup service %s. ROS Master API returned empty response: %s",
                    serviceName, serviceLookupResult);
        logger.fine("Service {0} available on: {1}", serviceName, serviceLookupResult);
        var serviceEndpoint = URI.create(serviceLookupResult.value);
        serviceNodeClient =
                new ServiceTcpRosClient<>(
                        tracingToken,
                        config.getCallerId(),
                        serviceName,
                        serviceEndpoint,
                        serviceDefinition,
                        textUtils);
        try {
            serviceNodeClient.connect(serviceName);
        } catch (IOException e) {
            throw new JRosServiceClientException(e);
        }
    }

    @Override
    protected void onClose() {
        logger.entering("Closing service client for " + serviceName);
        serviceNodeClient.close();
    }
}
