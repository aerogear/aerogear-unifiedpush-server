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

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Responsible for Logging on UPS and prevent log forgery
 * This class could be easily replaced by Log4j or SLF4J or any other logging framework
 * The motivation behind this implementation is to fix a security issue while
 * <a href="https://issues.jboss.org/browse/AGPUSH-1086">AGPUSH-1086</a> is not solved.
 */
public class AeroGearLogger {

    private Logger logger;

    private AeroGearLogger(Logger logger) {
        this.logger = logger;
    }

    public static AeroGearLogger getInstance(Class clazz) {
        if (clazz == null) {
            throw new IllegalArgumentException("Class required");
        }
        final String name = clazz.getSimpleName();
        return getInstance(name);
    }

    private static AeroGearLogger getInstance(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Name required");
        }
        Logger log = Logger.getLogger(name);
        return new AeroGearLogger(log);
    }

    public void info(String message){
        logger.info(format(message));
    }

    public void warning(String message){
        logger.log(Level.WARNING, format(message));
    }

    public void severe(String message){
        logger.log(Level.SEVERE, format(message));
    }

    public void severe(String message, Throwable t){
        logger.log(Level.SEVERE, format(message), t);
    }

    public void fine(String message){
        logger.log(Level.FINE, format(message));
    }

    public void finest(String message){
        logger.log(Level.FINEST, format(message));
    }

    /**
     * Taken with some modifications from Log4j
     * @see <a href="https://github.com/apache/logging-log4j2/blob/master/log4j-core/src/main/java/org/apache/logging/log4j/core/pattern/EncodingPatternConverter.java">logging-log4j2</a>
     * @param logMessage
     * @return Encoded string
     */
    private String format(final String logMessage) {
        final StringBuilder message = new StringBuilder(logMessage);
        final StringBuilder str = new StringBuilder();
        for (int i = 0; i < message.length(); i++) {
            final char c = message.charAt(i);
            switch (c) {
                case '\r':
                    str.append("\\r");
                    break;
                case '\n':
                    str.append("\\n");
                    break;
                case '&':
                    str.append("&amp;");
                    break;
                case '<':
                    str.append("&lt;");
                    break;
                case '>':
                    str.append("&gt;");
                    break;
                case '"':
                    str.append("&quot;");
                    break;
                case '\'':
                    str.append("&apos;");
                    break;
                case '/':
                    str.append("&#x2F;");
                    break;
                default:
                    str.append(c);
                    break;
            }
        }
        return str.toString();
    }
}