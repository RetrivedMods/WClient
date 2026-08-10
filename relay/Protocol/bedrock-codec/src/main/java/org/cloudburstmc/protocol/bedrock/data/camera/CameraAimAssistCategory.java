package org.cloudburstmc.protocol.bedrock.data.camera;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Data;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;
import java.util.List;

@Data
public class CameraAimAssistCategory {
    private String name;
    private List<CameraAimAssistPriority> entityPriorities = new ObjectArrayList<>();
    private List<CameraAimAssistPriority> blockPriorities = new ArrayList<>();
    private List<CameraAimAssistPriority> blockTagPriorities = new ArrayList<>();
    /**
     * @since v897
     */
    private List<CameraAimAssistPriority> entityTypeFamiliesPriorities = new ArrayList<>();
    @Nullable
    private Integer entityDefaultPriorities;
    @Nullable
    private Integer blockDefaultPriorities;
}