package org.cloudburstmc.protocol.bedrock.data.camera;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Data;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.List;

@Data
public class CameraAimAssistPresetDefinition {
    private String identifier;
    /**
     * @deprecated since v776
     */
    private String categories;
    /**
     * @deprecated since v897
     */
    private final List<String> exclusionList = new ObjectArrayList<>();
    /**
     * @since v897
     */
    private final List<String> blockExclusionList = new ObjectArrayList<>();
    /**
     * @since v897
     */
    private final List<String> blockTagExclusionList = new ObjectArrayList<>();
    /**
     * @since v897
     */
    private final List<String> entityExclusionList = new ObjectArrayList<>();
    /**
     * @since v897
     */
    private final List<String> entityTypeFamiliesExclusionList = new ObjectArrayList<>();
    private final List<String> liquidTargetingList = new ObjectArrayList<>();
    private final List<CameraAimAssistItemSettings> itemSettings = new ObjectArrayList<>();
    @Nullable
    private String defaultItemSettings;
    @Nullable
    private String handSettings;
}