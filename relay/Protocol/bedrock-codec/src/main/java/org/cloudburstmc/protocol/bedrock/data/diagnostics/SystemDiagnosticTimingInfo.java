package org.cloudburstmc.protocol.bedrock.data.diagnostics;

import lombok.Value;

@Value
public class SystemDiagnosticTimingInfo {
    String displayName;
    long systemIndex;
    long timeInNs;
    byte percentOfTotal;
}
