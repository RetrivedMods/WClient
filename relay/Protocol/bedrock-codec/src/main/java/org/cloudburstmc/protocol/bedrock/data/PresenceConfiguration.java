package org.cloudburstmc.protocol.bedrock.data;

import lombok.Value;
import org.checkerframework.checker.nullness.qual.Nullable;

@Value
public class PresenceConfiguration {

    @Nullable // since v1001
    String experienceName;
    @Nullable // since v1001
    String worldName;
    /**
     * @since v1001
     */
    String richPresenceId;
}
