package org.cloudburstmc.protocol.bedrock.data.datastore;

import lombok.Data;

@Data
public class DataStoreRemoval implements DataStoreAction {

    private String dataStoreName;

    @Override
    public int getType() {
        return 2;
    }
}
