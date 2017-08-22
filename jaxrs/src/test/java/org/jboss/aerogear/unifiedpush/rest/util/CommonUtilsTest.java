package org.jboss.aerogear.unifiedpush.rest.util;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.jboss.aerogear.unifiedpush.rest.util.CommonUtils.isAscendingOrder;

public class CommonUtilsTest {

    @Test
    public void verifyAscendingOrderFromNullValue() {
        assertThat(isAscendingOrder(null)).isTrue();
    }

    @Test
    public void verfiyAscendingOrderFromDescValue() {
        assertThat(isAscendingOrder("deSc")).isFalse();
        assertThat(isAscendingOrder("desc")).isFalse();
        assertThat(isAscendingOrder("DESC")).isFalse();
    }

    @Test
    public void verifyAscendingOrderFromAscValue() {
        assertThat(isAscendingOrder("foo")).isTrue();
        assertThat(isAscendingOrder("AsC")).isTrue();
    }

}