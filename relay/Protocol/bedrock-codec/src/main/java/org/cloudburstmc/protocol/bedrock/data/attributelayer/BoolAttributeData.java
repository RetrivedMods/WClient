package org.cloudburstmc.protocol.bedrock.data.attributelayer;

import lombok.Value;

@Value
public class BoolAttributeData implements AttributeData {

    boolean value;
    Operation operation;

    public enum Operation {
        OVERRIDE, ALPHA_BLEND, AND, NAND, OR, NOR, XOR, XNOR
    }
}
