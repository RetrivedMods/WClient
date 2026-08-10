package org.cloudburstmc.protocol.bedrock.data.diagnostics;

import lombok.Value;

@Value
public class WhiskerScopeDataSummary {

    String label;
    String indentation;
    long totalHighCostNS;
    long totalMidCostNS;
    long totalLowCostNS;
}
