package org.jboss.aerogear.unifiedpush.service;

import org.jboss.aerogear.unifiedpush.test.archive.UnifiedPushArchive;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertTrue;

@RunWith(Arquillian.class)
public class SimpleTest {


    @Deployment
    public static WebArchive archive() {
        return UnifiedPushArchive.forTestClass(SimpleTest.class)
                .as(WebArchive.class);
    }

    @Test
    public void testBob() {
        assertTrue(true);
    }

}
