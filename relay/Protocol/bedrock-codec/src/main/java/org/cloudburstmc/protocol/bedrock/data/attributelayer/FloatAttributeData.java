package org.cloudburstmc.protocol.bedrock.data.attributelayer;

import lombok.Value;
import org.checkerframework.checker.nullness.qual.Nullable;

@Value
public class FloatAttributeData implements AttributeData {

    float value;
    Operation operation;
    @Nullable
    Float constraintMin;
    @Nullable
    Float constraintMax;

    public enum Operation {
        OVERRIDE, ALPHA_BLEND, ADD, SUBTRACT, MULTIPLY, MINIMUM, MAXIMUM
    }
}
