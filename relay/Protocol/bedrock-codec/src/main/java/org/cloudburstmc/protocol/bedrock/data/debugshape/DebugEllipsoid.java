package org.cloudburstmc.protocol.bedrock.data.debugshape;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.cloudburstmc.math.vector.Vector3f;

/**
 * @since v1001
 */
@Data
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class DebugEllipsoid extends DebugShape {

    int segments;
    Vector3f radii;

    public DebugEllipsoid() {
    }

    @Override
    public Type getType() {
        return Type.ELLIPSOID;
    }
}
