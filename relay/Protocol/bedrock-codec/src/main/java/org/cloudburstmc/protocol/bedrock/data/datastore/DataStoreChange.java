package org.cloudburstmc.protocol.bedrock.data.datastore;

import lombok.Data;

@Data
public class DataStoreChange implements DataStoreAction {

    private String dataStoreName;
    private String property;
    private Object newValue;
    private int updateCount;

    @Override
    public int getType() {
        return 1;
    }
}
