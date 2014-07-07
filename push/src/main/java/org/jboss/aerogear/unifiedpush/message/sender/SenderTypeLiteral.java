package org.jboss.aerogear.unifiedpush.message.sender;

import org.jboss.aerogear.unifiedpush.api.Variant;

import javax.enterprise.util.AnnotationLiteral;

/**
 */
public class SenderTypeLiteral extends AnnotationLiteral<SenderType> implements SenderType {
    private final Class<? extends Variant> value;

    public Class<? extends Variant> value() {
        return value;
    }

    public SenderTypeLiteral(Class<? extends Variant> value) {
        this.value = value;
    }
}
