package org.cloudburstmc.protocol.bedrock.data.debugshape;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.cloudburstmc.math.vector.Vector2f;

/**
 * @since v1001
 */
@Data
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class DebugCone extends DebugShape {

    float height;
    int segments;
    Vector2f radii;

    public DebugCone() {
    }

    @Override
    public Type getType() {
        return Type.CONE;
    }
}
