package org.cloudburstmc.protocol.bedrock.data.datastore;

import lombok.Data;

@Data
public class DataStoreUpdate implements DataStoreAction {

    private String dataStoreName;
    private String property;
    private String path;
    private Object data;
    private int updateCount;
    private int pathUpdateCount;

    @Override
    public int getType() {
        return 0;
    }
}
