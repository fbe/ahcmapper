package com.ajjpj.ahcmapper.core.diff.builder;


public class AhcMapperRefDiffItem extends AbstractAhcMapperDiffItem {
    public AhcMapperRefDiffItem(Object oldTargetMarker, Object newTargetMarker, String propertyIdentifier, Object oldValue, Object newValue) {
        super(oldTargetMarker, newTargetMarker, propertyIdentifier, oldValue, newValue);
    }
}