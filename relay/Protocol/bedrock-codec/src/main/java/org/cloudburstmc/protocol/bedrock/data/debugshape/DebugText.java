package org.cloudburstmc.protocol.bedrock.data.debugshape;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.cloudburstmc.math.vector.Vector3f;

import java.awt.*;

@Data
@EqualsAndHashCode(callSuper = true)
public class DebugText extends DebugShape {

    String text;
    /**
     * @since v975
     */
    boolean useRotation;
    /**
     * @since v975
     */
    @Nullable
    Color backgroundColor;
    /**
     * @since v975
     */
    boolean depthTest;
    /**
     * @since v975
     */
    boolean showBackface;
    /**
     * @since v975
     */
    boolean showTextBackface;

    public DebugText() {
    }

    @Deprecated
    public DebugText(long id, int dimension, @Nullable Vector3f position, @Nullable Float scale, @Nullable Vector3f rotation, @Nullable Float totalTimeLeft, @Nullable Color color, String text) {
        this(id, dimension, position, scale, rotation, totalTimeLeft, color, text, null);
    }

    @Deprecated
    public DebugText(long id, int dimension, @Nullable Vector3f position, @Nullable Float scale, @Nullable Vector3f rotation, @Nullable Float totalTimeLeft, @Nullable Color color, String text, @Nullable Long attachedToEntityId) {
        setId(id);
        setDimension(dimension);
        setPosition(position);
        setScale(scale);
        setRotation(rotation);
        setTotalTimeLeft(totalTimeLeft);
        setColor(color);
        setAttachedToEntityId(attachedToEntityId);

        setText(text);
    }

    @Override
    public Type getType() {
        return Type.TEXT;
    }
}
