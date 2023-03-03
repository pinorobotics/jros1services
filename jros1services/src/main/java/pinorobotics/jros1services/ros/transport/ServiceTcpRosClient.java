/*
 * Copyright 2020 jrosservices project
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
package pinorobotics.jros1services.ros.transport;

import id.jros1client.ros.transport.ConnectionHeader;
import id.jros1client.ros.transport.MessagePacket;
import id.jros1client.ros.transport.TcpRosClientConnector;
import id.jros1client.ros.transport.io.ConnectionHeaderWriter;
import id.jros1client.ros.transport.io.MessagePacketReader;
import id.jros1client.ros.transport.io.MessagePacketWriter;
import id.jros1messages.MessageSerializationUtils;
import id.jrosclient.utils.TextUtils;
import id.jrosmessages.Message;
import id.jrosmessages.MessageMetadataAccessor;
import id.jrosmessages.RosInterfaceType;
import id.xfunction.Preconditions;
import id.xfunction.logging.TracingToken;
import id.xfunction.logging.XLogger;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import pinorobotics.jros1services.ros.transport.io.ServiceConnectionHeaderReader;
import pinorobotics.jros1services.ros.transport.io.ServiceConnectionHeaderWriter;
import pinorobotics.jros1services.ros.transport.io.ServiceResponseReader;
import pinorobotics.jrosservices.exceptions.JRosServiceClientException;
import pinorobotics.jrosservices.msgs.ServiceDefinition;

/**
 * TCPROS client which communicates with publishing ROS1 node using {@link RosInterfaceType#SERVICE}
 * interface.
 *
 * @author lambdaprime intid@protonmail.com
 */
public class ServiceTcpRosClient<R extends Message, A extends Message>
        implements TcpRosClientConnector.Processor<ServiceConnectionHeader>, AutoCloseable {

    private static final MessageSerializationUtils SERIALIZATION_UTILS =
            new MessageSerializationUtils();

    private class PendingResult {
        private R request;
        private CompletableFuture<A> result;

        public PendingResult(R request) {
            this.request = request;
            result = new CompletableFuture<>();
        }
    }

    private MessageMetadataAccessor metadataAccessor = new MessageMetadataAccessor();
    private ServiceDefinition<R, A> serviceDefinition;
    private MessagePacketWriter<ServiceConnectionHeader> writer;
    private BlockingQueue<PendingResult> pendingResults = new LinkedBlockingQueue<>();
    private ServiceResponseReader reader;
    private XLogger logger;
    private TcpRosClientConnector<A, ServiceConnectionHeader> connector;
    private TextUtils utils;
    private boolean isStopped;

    public ServiceTcpRosClient(
            TracingToken tracingToken,
            String callerId,
            String topic,
            URI serviceEndpoint,
            ServiceDefinition<R, A> serviceDefinition,
            TextUtils utils) {
        Preconditions.equals("rosrpc", serviceEndpoint.getScheme(), "Protocol is not supported");
        this.serviceDefinition = serviceDefinition;
        this.utils = utils;
        tracingToken = new TracingToken(tracingToken, "" + hashCode());
        logger = XLogger.getLogger(getClass(), tracingToken);
        connector =
                new TcpRosClientConnector<>(
                        tracingToken,
                        callerId,
                        topic,
                        serviceEndpoint.getHost(),
                        serviceEndpoint.getPort(),
                        serviceDefinition.getServiceResponseMessage(),
                        utils,
                        this) {
                    @Override
                    protected ServiceConnectionHeader handshake(ServiceConnectionHeader header)
                            throws IOException {
                        var ch = super.handshake(header);
                        Preconditions.isTrue(
                                ch.requestType.isPresent(),
                                "Service handshake response missing the request type");
                        Preconditions.isTrue(
                                ch.responseType.isPresent(),
                                "Service handshake response missing the response type");
                        Preconditions.equals(
                                metadataAccessor.getName(
                                        serviceDefinition.getServiceRequestMessage()),
                                ch.requestType.get(),
                                "Service request type mismatch");
                        Preconditions.equals(
                                metadataAccessor.getName(
                                        serviceDefinition.getServiceResponseMessage()),
                                ch.responseType.get(),
                                "Service response type mismatch");
                        return ch;
                    }
                };
    }

    @Override
    public ConnectionHeaderWriter<ServiceConnectionHeader> newConnectionHeaderWriter(
            DataOutputStream dos) {
        writer = new MessagePacketWriter<>(dos);
        return new ServiceConnectionHeaderWriter(dos);
    }

    @Override
    public MessagePacketReader<ServiceConnectionHeader> newMessagePacketReader(
            DataInputStream dis) {
        reader = new ServiceResponseReader(dis, new ServiceConnectionHeaderReader(dis));
        return new MessagePacketReader<>(dis, new ServiceConnectionHeaderReader(dis));
    }

    public CompletableFuture<A> sendRequest(R request) {
        var pendingResult = new PendingResult(request);
        pendingResults.add(pendingResult);
        return pendingResult.result;
    }

    @Override
    public void processNextMessage() throws Exception {
        var pendingResult = pendingResults.poll(40, TimeUnit.SECONDS);
        if (pendingResult == null) return;
        var body = SERIALIZATION_UTILS.write(pendingResult.request);
        logger.fine("Sending new request body: {0}", utils.toString(body));
        writer.write(new MessagePacket(ConnectionHeader.EMPTY, body));
        var response = reader.readResponse();
        body = response.body();
        if (response.okByte() != 1)
            throw new JRosServiceClientException(" failed", new String(body));
        if (body.length > 0) {
            var responseMessage =
                    SERIALIZATION_UTILS.read(body, serviceDefinition.getServiceResponseMessage());
            logger.fine("Response received {0}", responseMessage);
            pendingResult.result.complete(responseMessage);
        } else {
            logger.warning("Received empty message data");
        }
    }

    @Override
    public void close() {
        isStopped = true;
    }

    @Override
    public boolean isStopped() {
        return isStopped;
    }

    @Override
    public void onError(Exception e) {
        logger.severe("Connection with ROS service is closed due to error", e);
        pendingResults.forEach(pr -> pr.result.completeExceptionally(e));
    }

    public void connect(String serviceName) throws IOException {
        connector.connect(
                new ServiceConnectionHeader().withService(serviceName).withPersistent("1"));
    }
}
