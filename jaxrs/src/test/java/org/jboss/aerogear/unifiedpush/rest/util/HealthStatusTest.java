/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.aerogear.unifiedpush.rest.util;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.jboss.aerogear.unifiedpush.service.impl.health.HealthDetails;
import org.jboss.aerogear.unifiedpush.service.impl.health.HealthStatus;
import org.jboss.aerogear.unifiedpush.service.impl.health.Status;
import org.junit.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class HealthStatusTest {

    @Test
    public void shouldReturnJson() throws IOException {
        //given
        HealthStatus status = getHealthStatus();

        //when
        final ObjectMapper mapper = new ObjectMapper();
        final JsonNode value = mapper.valueToTree(status);

        //then
        final JsonNode format = mapper.reader().readTree(getClass().getResourceAsStream("/health-format.json"));

        //because the json file will use int for the 'runtime' field and the model long
        assertThat(value.toString()).isEqualTo(format.toString());
    }

    @Test
    public void shouldCountMultipleErrors() throws IOException {
        //given
        HealthStatus status = getHealthStatus();
        HealthDetails secondError = new HealthDetails();
        secondError.setTestStatus(Status.WARN);
        status.add(secondError);

        assertThat(status.getSummary()).isEqualTo("There are 2 errors found");
    }

    private HealthStatus getHealthStatus() {
        HealthStatus status = new HealthStatus();
        final HealthDetails details = new HealthDetails();
        details.setDescription("db status");
        details.setResult("couldn't connect");
        details.setRuntime(111);
        details.setTestStatus(Status.CRIT);
        status.add(details);
        return status;
    }
}
