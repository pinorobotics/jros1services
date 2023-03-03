/*
 * Copyright 2021 jrosservices project
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
package pinorobotics.jros1services.tests.integration.add_two_ints_msgs;

import id.jrosmessages.Message;
import id.jrosmessages.MessageMetadata;
import id.jrosmessages.RosInterfaceType;
import id.xfunction.XJson;
import java.util.Objects;

/**
 * @author lambdaprime intid@protonmail.com
 */
@MessageMetadata(
        name = AddTwoIntsRequestMessage.NAME,
        interfaceType = RosInterfaceType.SERVICE,
        md5sum = "6a2e34150c00229791cc89ff309fff21")
public class AddTwoIntsRequestMessage implements Message {

    static final String NAME = "add_two_ints_srvs/TwoIntsRequest";

    public long a;

    public long b;

    public AddTwoIntsRequestMessage() {}

    public AddTwoIntsRequestMessage(long a, long b) {
        this.a = a;
        this.b = b;
    }

    @Override
    public int hashCode() {
        return Objects.hash(a, b);
    }

    @Override
    public boolean equals(Object obj) {
        var other = (AddTwoIntsRequestMessage) obj;
        return Objects.equals(a, other.b) && Objects.equals(a, other.b);
    }

    @Override
    public String toString() {
        return XJson.asString(
                "a", a,
                "b", b);
    }
}
