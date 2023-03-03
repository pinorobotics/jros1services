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
package pinorobotics.jros1services.ros.transport.io;

import static pinorobotics.jros1services.ros.transport.ServiceConnectionHeader.*;

import id.jros1client.ros.transport.io.ConnectionHeaderWriter;
import java.io.DataOutputStream;
import java.io.IOException;
import pinorobotics.jros1services.ros.transport.ServiceConnectionHeader;

/**
 * @author lambdaprime intid@protonmail.com
 */
public class ServiceConnectionHeaderWriter extends ConnectionHeaderWriter<ServiceConnectionHeader> {

    public ServiceConnectionHeaderWriter(DataOutputStream out) {
        super(out);
    }

    @Override
    protected int calcTotalLen(ServiceConnectionHeader header) {
        int totalLen = super.calcTotalLen(header);
        var len = 0;

        len = len(SERVICE, header.service);
        if (len > 0) totalLen += len + 4;

        len = len(PERSISTENT, header.persistent);
        if (len > 0) totalLen += len + 4;

        return totalLen;
    }

    @Override
    protected void writeAllFields(ServiceConnectionHeader header) throws IOException {
        super.writeAllFields(header);
        writeField(SERVICE, header.service);
        writeField(PERSISTENT, header.persistent);
    }
}
