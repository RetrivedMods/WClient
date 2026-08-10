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
public class DebugCylinder extends DebugShape {

    float height;
    int segments;
    Vector2f radiusX;
    Vector2f radiusZ;

    public DebugCylinder() {
    }

    @Override
    public Type getType() {
        return Type.CYLINDER;
    }
}
