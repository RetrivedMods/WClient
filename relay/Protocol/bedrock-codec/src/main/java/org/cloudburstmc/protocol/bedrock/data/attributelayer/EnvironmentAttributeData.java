package org.cloudburstmc.protocol.bedrock.data.attributelayer;

import lombok.Value;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.cloudburstmc.protocol.bedrock.data.camera.CameraEase;

@Value
public class EnvironmentAttributeData {

    String attributeName;
    @Nullable
    AttributeData from;
    AttributeData attribute;
    @Nullable
    AttributeData to;
    int currentTransitionTicks;
    int totalTransitionTicks;
    CameraEase easing;
    /**
     * @since v1001
     */
    int localTransitionTicks;
    /**
     * @since v1001
     */
    boolean noiseTransition;
}
