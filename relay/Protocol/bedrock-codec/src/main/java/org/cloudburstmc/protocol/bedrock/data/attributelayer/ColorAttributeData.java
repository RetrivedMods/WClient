package org.cloudburstmc.protocol.bedrock.data.attributelayer;

import lombok.Value;

@Value
public class ColorAttributeData implements AttributeData {

    Color255RGBA value;
    Operation operation;

    public interface Color255RGBA {
    }

    @Value
    public static class StringColor implements Color255RGBA {
        String value;
    }

    @Value
    public static class ArrayColor implements Color255RGBA {
        int[] value;
    }

    public enum Operation {
        OVERRIDE, ALPHA_BLEND, ADD, SUBTRACT, MULTIPLY
    }
}
