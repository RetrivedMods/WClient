package org.cloudburstmc.protocol.bedrock.data.debugshape;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.cloudburstmc.math.vector.Vector3f;

import java.awt.*;

@Data
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class DebugSphere extends DebugShape {

    Integer segments;

    public DebugSphere() {
    }

    @Deprecated
    public DebugSphere(long id, int dimension, @Nullable Vector3f position, @Nullable Float scale, @Nullable Vector3f rotation, @Nullable Float totalTimeLeft, @Nullable Color color, Integer segments) {
        this(id, dimension, position, scale, rotation, totalTimeLeft, color, segments, null);
    }

    @Deprecated
    public DebugSphere(long id, int dimension, @Nullable Vector3f position, @Nullable Float scale, @Nullable Vector3f rotation, @Nullable Float totalTimeLeft, @Nullable Color color, Integer segments, @Nullable Long attachedToEntityId) {
        setId(id);
        setDimension(dimension);
        setPosition(position);
        setScale(scale);
        setRotation(rotation);
        setTotalTimeLeft(totalTimeLeft);
        setColor(color);
        setAttachedToEntityId(attachedToEntityId);

        setSegments(segments);
    }

    @Override
    public Type getType() {
        return Type.SPHERE;
    }
}
