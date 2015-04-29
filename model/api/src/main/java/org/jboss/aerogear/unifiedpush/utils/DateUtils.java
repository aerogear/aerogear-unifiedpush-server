/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors.
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
package org.jboss.aerogear.unifiedpush.utils;

import java.util.Calendar;
import java.util.Date;

public final class DateUtils {

    private DateUtils() {
        // no-op
    }

    /**
     * Returns date in history: Today - the given number of days
     *
     * @param days number of days
     *
     * @return date object that lives n days in the past
     */
    public static Date calculatePastDate(int days) {
        final Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -days);

        return cal.getTime();
    }
}
