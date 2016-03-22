package org.jboss.aerogear.unifiedpush.message;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.commons.lang.RandomStringUtils;
import org.jboss.aerogear.unifiedpush.api.VariantType;
import org.jboss.aerogear.unifiedpush.api.validation.DeviceTokenValidator;
import org.junit.Test;

public class DeviceTokenValidatorTest {
	private static final char[] iosTokenChars = new char[] { 'a', 'b', 'c', 'd', 'e', 'f', '1', '2', '3', '4', '5', '6',
			'7', '8', '9', '0', ' ', '-' };

	@Test
	public void testRandomToken() {
		final VariantType iosVariantType = VariantType.IOS;
		assertThat(DeviceTokenValidator.isValidDeviceTokenForVariant(RandomStringUtils.random(65, iosTokenChars),
				iosVariantType)).isTrue();
	}
}
