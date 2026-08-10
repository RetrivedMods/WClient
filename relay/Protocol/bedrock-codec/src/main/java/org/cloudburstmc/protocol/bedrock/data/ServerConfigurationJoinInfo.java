package org.cloudburstmc.protocol.bedrock.data;

import lombok.Value;
import org.checkerframework.checker.nullness.qual.Nullable;

@Value
public class ServerConfigurationJoinInfo {

    @Nullable
    GatheringsConfigurationJoinInfo gatheringsConfigurationJoinInfo;
    @Nullable
    ClientStoreEntrypointConfiguration clientStoreEntrypointConfiguration;
    @Nullable
    PresenceConfiguration presenceConfiguration;
}
