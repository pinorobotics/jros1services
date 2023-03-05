/*
 * Copyright 2022 jrosservices project
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
import id.jros1client.impl.ObjectsFactory;
import id.jrosclient.utils.RosNameUtils;
import id.jrosmessages.Message;
import pinorobotics.jrosservices.msgs.ServiceDefinition;

/**
 * Factory methods to create {@link JRos1ServiceClient}
 *
 * @author lambdaprime intid@protonmail.com
 */
public class JRos1ServiceClientFactory {
    private static final RosNameUtils nameUtils = new RosNameUtils();
    private static final ObjectsFactory objectsFactory = new ObjectsFactory();

    /**
     * Create client for ROS1 Services
     *
     * @param client ROS1 client
     * @param serviceDefinition message type definitions for an service
     * @param serviceName name of the ROS1 service which will be executing the requests
     * @param <R> request message type
     * @param <A> response message type
     */
    public <R extends Message, A extends Message> JRos1ServiceClient<R, A> createClient(
            JRos1Client client, ServiceDefinition<R, A> serviceDefinition, String serviceName) {
        return new JRos1ServiceClient<>(
                client,
                nameUtils,
                serviceDefinition,
                serviceName,
                objectsFactory.createTextUtils(client.getClientConfiguration()));
    }
}
