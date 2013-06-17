/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors
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

package org.aerogear.connectivity.service;

import java.util.List;

import org.aerogear.connectivity.model.AndroidVariant;
import org.aerogear.connectivity.model.PushApplication;
import org.aerogear.connectivity.model.SimplePushVariant;
import org.aerogear.connectivity.model.iOSVariant;

public interface PushApplicationService {
    
    PushApplication addPushApplication(PushApplication pushApp);
    List<PushApplication> findAllPushApplications();
    List<PushApplication> findAllPushApplicationsForDeveloper(String loginName);
    
    PushApplication findByPushApplicationID(String pushApplicationID);
    PushApplication findByPushApplicationIDForDeveloper(String pushApplicationID, String loginName);
    PushApplication updatePushApplication(PushApplication pushApp);
    void removePushApplication(PushApplication pushApp);
    
    void addiOSVariant(PushApplication pushApp, iOSVariant iOSVariant);
    void addAndroidVariant(PushApplication pushApp, AndroidVariant androidVariant);
    void addSimplePushVariant(PushApplication pushApp, SimplePushVariant simplePushVariant);
    
    List<iOSVariant> alliOSVariantsForPushApplication(PushApplication pushApp);
    List<AndroidVariant> allAndroidVariantsForPushApplication(PushApplication pushApp);
    List<SimplePushVariant> allSimplePushVariantsForPushApplication(PushApplication pushApp);
}
