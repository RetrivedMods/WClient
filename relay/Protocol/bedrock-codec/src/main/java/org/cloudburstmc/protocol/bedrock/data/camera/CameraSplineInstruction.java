package org.cloudburstmc.protocol.bedrock.data.camera;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.cloudburstmc.math.vector.Vector3f;

import java.util.List;

@Data
@AllArgsConstructor
public class CameraSplineInstruction {

    private float totalTime;
    private CameraSplineType type;
    private List<Vector3f> curve;
    private List<SplineProgressOption> progressKeyFrames;
    private List<SplineRotationOption> rotationOption;
    private String splineIdentifier;
    private boolean loadFromJson;

    public CameraSplineInstruction(float totalTime, CameraSplineType type, List<Vector3f> curve, List<SplineProgressOption> progressKeyFrames, List<SplineRotationOption> rotationOption) {
        this.totalTime = totalTime;
        this.type = type;
        this.curve = curve;
        this.progressKeyFrames = progressKeyFrames;
        this.rotationOption = rotationOption;
        this.splineIdentifier = "";
        this.loadFromJson = false;
    }

    @Data
    @AllArgsConstructor
    public static class SplineProgressOption {

        private float value;
        private float time;
        private CameraEase ease;
    }

    @Data
    @AllArgsConstructor
    public static class SplineRotationOption {

        private Vector3f keyFrameValues;
        private float keyFrameTimes;
        private CameraEase ease;
    }
}
