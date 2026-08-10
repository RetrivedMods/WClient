package org.cloudburstmc.protocol.bedrock.data;

import lombok.Value;

import java.util.List;

@Value
public class SerializableVoxelShape {

    SerializableCells cells;
    List<Float> xCoordinates;
    List<Float> yCoordinates;
    List<Float> zCoordinates;

    @Value
    public static class SerializableCells {

        short xSize;
        short ySize;
        short zSize;
        List<Short> storage;
    }
}
