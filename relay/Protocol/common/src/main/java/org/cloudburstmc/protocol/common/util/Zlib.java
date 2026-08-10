package org.cloudburstmc.protocol.common.util;

import java.nio.ByteBuffer;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.CompositeByteBuf;
import io.netty.util.concurrent.FastThreadLocal;

/**
 * as we do not want to use com.nukkitx.natives,
 * we override the original class and use pure java implement
 */
public class Zlib {
    public static final Zlib DEFAULT = new Zlib(false);
    public static final Zlib RAW = new Zlib(true);

    private static final int CHUNK = 8192;

    private final boolean raw;
    private final FastThreadLocal<Inflater> inflaterLocal;
    private final FastThreadLocal<Deflater> deflaterLocal;

    private Zlib(boolean raw) {
        this.raw = raw;
        this.inflaterLocal = new FastThreadLocal<Inflater>() {
            @Override
            protected Inflater initialValue() {
                return new Inflater(Zlib.this.raw);
            }
        };
        this.deflaterLocal = new FastThreadLocal<Deflater>() {
            @Override
            protected Deflater initialValue() {
                return new Deflater(7, Zlib.this.raw);
            }
        };
    }

    public ByteBuf inflate(ByteBuf buffer, int maxSize) throws DataFormatException {
        ByteBuf source = null;
        ByteBuf decompressed = ByteBufAllocator.DEFAULT.ioBuffer();

        try {
            if (!buffer.isDirect() || (buffer instanceof CompositeByteBuf && ((CompositeByteBuf) buffer).numComponents() > 1)) {
                // We don't have a direct buffer. Create one.
                ByteBuf temporary = ByteBufAllocator.DEFAULT.ioBuffer();
                temporary.writeBytes(buffer);
                source = temporary;
            } else {
                source = buffer;
            }

            Inflater inflater = inflaterLocal.get();
            inflater.reset();
            ByteBuffer nioBuffer = source.internalNioBuffer(source.readerIndex(), source.readableBytes());
            inflater.setInput(nioBuffer.array(), nioBuffer.arrayOffset() + nioBuffer.position(), nioBuffer.remaining());

            byte[] output = new byte[CHUNK];
            while (!inflater.finished()) {
                int written = inflater.inflate(output);
                if (written < 1) {
                    break;
                }
                decompressed.writeBytes(output, 0, written);
                if (maxSize > 0 && decompressed.writerIndex() >= maxSize) {
                    throw new DataFormatException("Inflated data exceeds maximum size");
                }
            }
            decompressed.retain();
            return decompressed;
        } finally {
            decompressed.release();
            if (source != null && source != buffer) {
                source.release();
            }
        }
    }

    public void deflate(ByteBuf uncompressed, ByteBuf compressed, int level) throws DataFormatException {
        ByteBuf destination = null;
        ByteBuf source = null;
        try {
            if (!uncompressed.isDirect() || (uncompressed instanceof CompositeByteBuf && ((CompositeByteBuf) uncompressed).numComponents() > 1)) {
                // Source is not a direct buffer. Work on a temporary direct buffer and then write the contents out.
                source = ByteBufAllocator.DEFAULT.ioBuffer();
                source.writeBytes(uncompressed);
            } else {
                source = uncompressed;
            }

            if (!compressed.isDirect()) {
                // Destination is not a direct buffer. Work on a temporary direct buffer and then write the contents out.
                destination = ByteBufAllocator.DEFAULT.ioBuffer();
            } else {
                destination = compressed;
            }

            Deflater deflater = deflaterLocal.get();
            deflater.reset();
            deflater.setLevel(level);
            ByteBuffer nioBuffer = source.internalNioBuffer(source.readerIndex(), source.readableBytes());
            deflater.setInput(nioBuffer.array(), nioBuffer.arrayOffset() + nioBuffer.position(), nioBuffer.remaining());

            byte[] output = new byte[CHUNK];
            while (!deflater.finished()) {
                int written = deflater.deflate(output);
                destination.writeBytes(output, 0, written);
            }

            if (destination != compressed) {
                compressed.writeBytes(destination);
            }
        } finally {
            if (source != null && source != uncompressed) {
                source.release();
            }
            if (destination != null && destination != compressed) {
                destination.release();
            }
        }
    }
}
