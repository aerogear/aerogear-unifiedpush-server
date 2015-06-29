package org.jboss.aerogear.unifiedpush.message;

import javax.annotation.Resource;
import javax.jms.Queue;

import org.jboss.aerogear.unifiedpush.message.cache.AbstractServiceCache;

public class MockServiceCacheForCluster extends AbstractServiceCache<Integer> {

    private static final int INSTANCE_LIMIT = 5;
    private static final long INSTANTIATION_TIMEOUT = 7500;
    private static final long DISPOSAL_DELAY = 5000;

    @Resource(mappedName = "java:/queue/APNsBadgeLeaseQueue")
    private Queue queue;

    public MockServiceCacheForCluster() {
        super(INSTANCE_LIMIT, INSTANTIATION_TIMEOUT, DISPOSAL_DELAY);
    }

    @Override
    public Queue getBadgeQueue() {
        return queue;
    }
}