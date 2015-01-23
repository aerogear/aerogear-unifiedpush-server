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
package org.jboss.aerogear.unifiedpush.message.util;


import org.junit.Test;
import static org.junit.Assert.*;
import java.util.Properties;

public class ConfigurationUtilsTest {

    @Test
    public void testExistingTryGetProperty(){
        Properties properties = System.getProperties();
        properties.setProperty("MyNiceProp","MyNiceValue");
        assertEquals(ConfigurationUtils.tryGetProperty("MyNiceProp"),"MyNiceValue");
    }

    @Test
    public void testNonExistingTryGetProperty(){
        assertNull(ConfigurationUtils.tryGetProperty("MyNiceOtherProp"));
    }

}
