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
import java.util.Optional;

/**
 * <a href= "http://wiki.ros.org/ROS/Connection%20Header">Connection Header</a>
 *
 * @author lambdaprime intid@protonmail.com
 */
public class ServiceConnectionHeader extends ConnectionHeader {

    public static final String SERVICE = "service";
    public static final String REQUEST_TYPE = "request_type";
    public static final String RESPONSE_TYPE = "response_type";
    public static final String PERSISTENT = "persistent";

    /** Name of the service the subscriber is connecting to */
    public Optional<String> service = Optional.empty();

    /** Service request type. */
    public Optional<String> requestType = Optional.empty();

    /** Service response type. */
    public Optional<String> responseType = Optional.empty();

    /**
     * Sent from a service client to a service. If '1', keep connection open for multiple requests.
     */
    public Optional<String> persistent = Optional.empty();

    public ServiceConnectionHeader withService(String service) {
        this.service = Optional.of(service);
        return this;
    }

    public ServiceConnectionHeader withRequestType(String requestType) {
        this.requestType = Optional.of(requestType);
        return this;
    }

    public ServiceConnectionHeader withResponseType(String responseType) {
        this.responseType = Optional.of(responseType);
        return this;
    }

    public ServiceConnectionHeader withPersistent(String persistent) {
        this.persistent = Optional.of(persistent);
        return this;
    }

    @Override
    public void add(String key, String value) {
        switch (key) {
            case SERVICE:
                withService(value);
                break;
            case REQUEST_TYPE:
                withRequestType(value);
                break;
            case RESPONSE_TYPE:
                withResponseType(value);
                break;
            case PERSISTENT:
                withPersistent(value);
                break;
            default:
                super.add(key, value);
        }
    }

    @Override
    protected Object[] getAdditionalFields() {
        return new Object[] {
            SERVICE,
            service.orElse("empty"),
            REQUEST_TYPE,
            requestType.orElse("empty"),
            RESPONSE_TYPE,
            responseType.orElse("empty"),
            PERSISTENT,
            persistent.orElse("empty"),
        };
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (this == obj) return true;
        if (this.getClass() != obj.getClass()) return false;
        ServiceConnectionHeader ch = (ServiceConnectionHeader) obj;
        return super.equals(ch)
                && service.equals(ch.service)
                && service.equals(ch.service)
                && requestType.equals(ch.requestType)
                && responseType.equals(ch.responseType)
                && persistent.equals(ch.persistent);
    }
}
