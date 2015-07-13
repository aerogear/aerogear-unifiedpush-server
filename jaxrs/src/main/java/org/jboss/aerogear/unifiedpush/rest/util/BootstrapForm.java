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

import org.jboss.aerogear.crypto.util.PKCS12;
import org.jboss.resteasy.annotations.providers.multipart.PartType;

import javax.validation.constraints.AssertTrue;
import javax.ws.rs.FormParam;

/**
 * Helper class to read values from the multipart request
 * that is performed when creating a 'bootstrap' Push Application and their variants.
 */
public class BootstrapForm {

    private String pushApplicationName;

    // Android details
    private String androidVariantName;
    private String androidGoogleKey;
    private String androidProjectNumber;

    // iOS details
    private String iOSVariantName;
    private Boolean iOSProduction; // RestEasy needs 'Boolean' here
    private String iOSPassphrase;
    private byte[] iOSCertificate;


    // Windows details
    private String windowsType;
    private String windowsVariantName;
    private String windowsSid;
    private String windowsClientSecret;


    public BootstrapForm() {

    }

    public String getPushApplicationName() {
        return pushApplicationName;
    }

    @FormParam("pushApplicationName")
    public void setPushApplicationName(String pushApplicationName) {
        this.pushApplicationName = pushApplicationName;
    }

    public String getAndroidVariantName() {
        return androidVariantName;
    }

    @FormParam("androidVariantName")
    public void setAndroidVariantName(String androidVariantName) {
        this.androidVariantName = androidVariantName;
    }

    public String getAndroidGoogleKey() {
        return androidGoogleKey;
    }

    @FormParam("androidGoogleKey")
    public void setAndroidGoogleKey(String androidGoogleKey) {
        this.androidGoogleKey = androidGoogleKey;
    }

    public String getAndroidProjectNumber() {
        return androidProjectNumber;
    }

    @FormParam("androidProjectNumber")
    public void setAndroidProjectNumber(String androidProjectNumber) {
        this.androidProjectNumber = androidProjectNumber;
    }

    public String getiOSVariantName() {
        return iOSVariantName;
    }

    @FormParam("iOSVariantName")
    public void setiOSVariantName(String iOSVariantName) {
        this.iOSVariantName = iOSVariantName;
    }

    public Boolean getiOSProduction() {
        return iOSProduction;
    }

    @FormParam("iOSProduction")
    public void setiOSProduction(Boolean iOSProduction) {
        this.iOSProduction = iOSProduction;
    }

    public String getiOSPassphrase() {
        return iOSPassphrase;
    }

    @FormParam("iOSPassphrase")
    public void setiOSPassphrase(String iOSPassphrase) {
        this.iOSPassphrase = iOSPassphrase;
    }

    public byte[] getiOSCertificate() {
        return iOSCertificate;
    }

    @FormParam("iOSCertificate")
    @PartType("application/octet-stream")
    public void setiOSCertificate(byte[] iOSCertificate) {
        this.iOSCertificate = iOSCertificate;
    }

    public String getWindowsType() {
        return windowsType;
    }

    @FormParam("windowsType")
    public void setWindowsType(String windowsType) {
        this.windowsType = windowsType;
    }


    public String getWindowsVariantName() {
        return windowsVariantName;
    }

    @FormParam("windowsVariantName")
    public void setWindowsVariantName(String windowsVariantName) {
        this.windowsVariantName = windowsVariantName;
    }

    public String getWindowsSid() {
        return windowsSid;
    }

    @FormParam("windowsSid")
    public void setWindowsSid(String windowsSid) {
        this.windowsSid = windowsSid;
    }

    public String getWindowsClientSecret() {
        return windowsClientSecret;
    }

    @FormParam("windowsClientSecret")
    public void setWindowsClientSecret(String windowsClientSecret) {
        this.windowsClientSecret = windowsClientSecret;
    }


    /**
     * Basic validations for Android, when Android is present.
     *
     * @return true if valid, otherwise false
     */
    @AssertTrue(message = "invalid android data")
    public boolean isAndroidValid() {
        if (androidVariantName != null) {

            if (androidGoogleKey == null || androidGoogleKey.isEmpty() || androidProjectNumber == null || androidProjectNumber.isEmpty()) {
                return false;
            }
        }

        return true;

    }

    /**
     * Basic validations for windows
     *
     * @return true if valid, otherwise false
     */
    @AssertTrue(message = "invalid Windows data")
    public boolean isWindowsValid() {
        // windows present?
        if (windowsVariantName != null && windowsType != null) {

            // for MPNS nothing else is needed, but for WNS we need more:
            if (windowsType.equalsIgnoreCase("wns")) {
                // only of MPNS
                if (windowsSid == null || windowsSid.isEmpty() || windowsClientSecret == null || windowsClientSecret.isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Basic validations for iOS, when iOS is present.
     *
     * @return true if valid, otherwise false
     */
    @AssertTrue(message = "the provided ios certificate passphrase does not match with the uploaded certificate")
    // TODO: this can not be named isiOS...();
    public boolean isAppleVariantValid() {
        if (iOSVariantName != null) {
            try {
                PKCS12.validate(iOSCertificate, iOSPassphrase);
            } catch (Exception e) {
                return false;
            }
        }
        return true;
    }
}
