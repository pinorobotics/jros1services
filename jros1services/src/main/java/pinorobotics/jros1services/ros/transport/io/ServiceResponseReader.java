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
package pinorobotics.jros1services.ros.transport.io;

import id.jros1client.ros.transport.io.ConnectionHeaderReader;
import id.jros1client.ros.transport.io.MessagePacketReader;
import java.io.DataInputStream;
import java.io.IOException;
import pinorobotics.jros1services.ros.transport.ServiceConnectionHeader;

/**
 * @author lambdaprime intid@protonmail.com
 */
public class ServiceResponseReader {

    /**
     * Services include an 'ok' byte in response to each service request message.
     *
     * <ul>
     *   <li>If the ok byte is true (1), it must be followed by the service response message.
     *   <li>If the ok byte is false (0), it must be followed by a serialized string representing
     *       the error message (same length + bytes format that ROS messages use for serializing
     *       strings, potentially the string can be empty which is the case if a service just
     *       returns false).
     * </ul>
     *
     * @author lambdaprime intid@protonmail.com
     */
    public record Response(byte okByte, byte[] body) {}

    private MessagePacketReader<ServiceConnectionHeader> reader;
    private DataInputStream input;

    public ServiceResponseReader(
            DataInputStream input, ConnectionHeaderReader<ServiceConnectionHeader> headerReader) {
        reader = new MessagePacketReader<>(input, headerReader);
        this.input = input;
    }

    public Response readResponse() throws IOException {
        var okByte = input.readByte();
        var body = reader.readBody();
        return new Response(okByte, body);
    }
}
