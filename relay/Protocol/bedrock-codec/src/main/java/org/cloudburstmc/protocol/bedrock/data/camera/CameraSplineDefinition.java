package org.cloudburstmc.protocol.bedrock.data.camera;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CameraSplineDefinition {

    private String name;
    private CameraSplineInstruction instruction;
}
