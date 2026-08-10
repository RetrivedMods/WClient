package org.cloudburstmc.protocol.bedrock.data.debugshape;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.cloudburstmc.math.vector.Vector3f;

import java.awt.*;

@Getter
@Data
@AllArgsConstructor
public class DebugShape {

    private long id;
    /**
     * @since v859
     */
    private int dimension;

    @Nullable
    private Vector3f position;
    @Nullable
    private Float scale;
    @Nullable
    private Vector3f rotation;
    @Nullable
    private Float totalTimeLeft;
    @Nullable
    private Color color;
    @Nullable
    private Long attachedToEntityId;
    @Nullable
    private Float maximumRenderDistance;

    public DebugShape() {
    }

    public DebugShape(long id) {
        this(id, 0);
    }

    public DebugShape(long id, int dimension) {
        this.id = id;
        this.dimension = dimension;
    }

    public Type getType() {
        return null;
    }

    public enum Type {
        LINE,
        BOX,
        SPHERE,
        CIRCLE,
        TEXT,
        ARROW,
        /**
         * @since v1001
         */
        CYLINDER,
        /**
         * @since v1001
         */
        PYRAMID,
        /**
         * @since v1001
         */
        ELLIPSOID,
        /**
         * @since v1001
         */
        CONE
    }
}
