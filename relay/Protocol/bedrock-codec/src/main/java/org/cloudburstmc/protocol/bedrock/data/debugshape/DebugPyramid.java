package org.cloudburstmc.protocol.bedrock.data.debugshape;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * @since v1001
 */
@Data
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class DebugPyramid extends DebugShape {

    float height;
    float width;
    @Nullable
    Float depth;

    public DebugPyramid() {
    }

    @Override
    public Type getType() {
        return Type.PYRAMID;
    }
}
