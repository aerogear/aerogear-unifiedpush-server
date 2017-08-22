package org.jboss.aerogear.unifiedpush.rest.util;

public class CommonUtils {

    /**
     * Verify if the string sorting matches with asc or desc
     * Returns FALSE when sorting query value matches desc, otherwise it returns TRUE.
     *
     * @param sorting the sorting value from the http header
     * @return false for desc or true for as
     */
    public static Boolean isAscendingOrder(String sorting) {
        return "desc".equalsIgnoreCase(sorting) ? Boolean.FALSE : Boolean.TRUE;
    }
}
