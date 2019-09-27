/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.aerogear.unifiedpush.message.util;

import org.jboss.aerogear.unifiedpush.api.VariantType;

public class QueueUtils {

    private static final String apnsTokenBatchQueue = "APNsTokenBatchQueue";

    private static final String gcmTokenBatchQueue = "GCMTokenBatchQueue";

    private static final String wnsTokenBatchQueue = "WNSTokenBatchQueue";

    private static final String webTokenBatchQueue = "WebTokenBatchQueue";

    private static final String apnsPushBatchQueue = "APNsPushMessageQueue";

    private static final String gcmPushBatchQueue = "GCMPushMessageQueue";

    private static final String wnsPushBatchQueue = "WNSPushMessageQueue";

    private static final String webPushBatchQueue = "WebPushMessageQueue";

    public static String selectTokenQueue(VariantType variantType) {
        switch (variantType) {
            case ANDROID:
                return gcmTokenBatchQueue;
            case IOS:
                return apnsTokenBatchQueue;
            case WINDOWS_WNS:
                return wnsTokenBatchQueue;
            case WEB_PUSH:
                return webTokenBatchQueue;
            default:
                throw new IllegalStateException("Unknown variant type queue");
        }
    }

    public static String selectPushQueue(VariantType variantType) {
        switch (variantType) {
            case ANDROID:
                return gcmPushBatchQueue;
            case IOS:
                return apnsPushBatchQueue;
            case WINDOWS_WNS:
                return wnsPushBatchQueue;
            case WEB_PUSH:
                return webPushBatchQueue;
            default:
                throw new IllegalStateException("Unknown variant type queue");
        }
    }
}