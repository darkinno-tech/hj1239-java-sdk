package io.darkinno.hj1239.sdk.codec;

import java.nio.charset.StandardCharsets;

public class ByteBuf {

    private static final int DEFAULT_MAX_CAPACITY = 65536;

    private byte[] buffer;
    private int readIndex;
    private int writeIndex;
    private final int maxCapacity;

    public ByteBuf(int capacity) {
        this(capacity, DEFAULT_MAX_CAPACITY);
    }

    public ByteBuf(int capacity, int maxCapacity) {
        this.buffer = new byte[capacity];
        this.readIndex = 0;
        this.writeIndex = 0;
        this.maxCapacity = maxCapacity;
    }

    public ByteBuf(byte[] data) {
        if (data == null) {
            throw new IllegalArgumentException("data must not be null");
        }
        this.buffer = data.clone();
        this.readIndex = 0;
        this.writeIndex = data.length;
        this.maxCapacity = Math.max(data.length, DEFAULT_MAX_CAPACITY);
    }

    public byte readByte() {
        checkReadable(1);
        return buffer[readIndex++];
    }

    public short readShort() {
        checkReadable(2);
        short value = (short) ((buffer[readIndex] & 0xFF) << 8 | (buffer[readIndex + 1] & 0xFF));
        readIndex += 2;
        return value;
    }

    public int readInt() {
        checkReadable(4);
        int value = (buffer[readIndex] & 0xFF) << 24
                | (buffer[readIndex + 1] & 0xFF) << 16
                | (buffer[readIndex + 2] & 0xFF) << 8
                | (buffer[readIndex + 3] & 0xFF);
        readIndex += 4;
        return value;
    }

    public long readLong() {
        checkReadable(8);
        long value = (long) (buffer[readIndex] & 0xFF) << 56
                | (long) (buffer[readIndex + 1] & 0xFF) << 48
                | (long) (buffer[readIndex + 2] & 0xFF) << 40
                | (long) (buffer[readIndex + 3] & 0xFF) << 32
                | (long) (buffer[readIndex + 4] & 0xFF) << 24
                | (buffer[readIndex + 5] & 0xFF) << 16
                | (buffer[readIndex + 6] & 0xFF) << 8
                | (buffer[readIndex + 7] & 0xFF);
        readIndex += 8;
        return value;
    }

    public byte[] readBytes(int length) {
        checkReadable(length);
        byte[] result = new byte[length];
        System.arraycopy(buffer, readIndex, result, 0, length);
        readIndex += length;
        return result;
    }

    public String readString(int length) {
        byte[] bytes = readBytes(length);
        int end = bytes.length;
        for (int i = 0; i < bytes.length; i++) {
            if (bytes[i] == 0) {
                end = i;
                break;
            }
        }
        return new String(bytes, 0, end, StandardCharsets.ISO_8859_1);
    }

    public void writeByte(byte value) {
        ensureCapacity(1);
        buffer[writeIndex++] = value;
    }

    public void writeShort(short value) {
        ensureCapacity(2);
        buffer[writeIndex++] = (byte) (value >> 8);
        buffer[writeIndex++] = (byte) value;
    }

    public void writeInt(int value) {
        ensureCapacity(4);
        buffer[writeIndex++] = (byte) (value >> 24);
        buffer[writeIndex++] = (byte) (value >> 16);
        buffer[writeIndex++] = (byte) (value >> 8);
        buffer[writeIndex++] = (byte) value;
    }

    public void writeLong(long value) {
        ensureCapacity(8);
        buffer[writeIndex++] = (byte) (value >> 56);
        buffer[writeIndex++] = (byte) (value >> 48);
        buffer[writeIndex++] = (byte) (value >> 40);
        buffer[writeIndex++] = (byte) (value >> 32);
        buffer[writeIndex++] = (byte) (value >> 24);
        buffer[writeIndex++] = (byte) (value >> 16);
        buffer[writeIndex++] = (byte) (value >> 8);
        buffer[writeIndex++] = (byte) value;
    }

    public void writeBytes(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return;
        }
        ensureCapacity(bytes.length);
        System.arraycopy(bytes, 0, buffer, writeIndex, bytes.length);
        writeIndex += bytes.length;
    }

    public void writeString(String value, int length) {
        byte[] bytes = value != null ? value.getBytes(StandardCharsets.ISO_8859_1) : new byte[0];
        ensureCapacity(length);
        int count = Math.min(bytes.length, length);
        System.arraycopy(bytes, 0, buffer, writeIndex, count);
        for (int i = count; i < length; i++) {
            buffer[writeIndex + i] = 0;
        }
        writeIndex += length;
    }

    public int readableBytes() {
        return writeIndex - readIndex;
    }

    public byte[] toByteArray() {
        byte[] result = new byte[writeIndex];
        System.arraycopy(buffer, 0, result, 0, writeIndex);
        return result;
    }

    public int capacity() {
        return buffer.length;
    }

    public void resetRead() {
        readIndex = 0;
    }

    public void resetWrite() {
        writeIndex = 0;
        readIndex = 0;
    }

    private void checkReadable(int size) {
        if (readIndex + size > writeIndex) {
            throw new IndexOutOfBoundsException(
                    "Not enough readable bytes: required " + size
                            + ", available " + (writeIndex - readIndex));
        }
    }

    private void ensureCapacity(int additional) {
        int required = writeIndex + additional;
        if (required > buffer.length) {
            if (required > maxCapacity) {
                throw new IndexOutOfBoundsException(
                        "Buffer overflow: required " + required
                                + " exceeds max capacity " + maxCapacity);
            }
            int newCapacity = Math.min(Math.max(buffer.length * 2, required), maxCapacity);
            byte[] newBuffer = new byte[newCapacity];
            System.arraycopy(buffer, 0, newBuffer, 0, writeIndex);
            buffer = newBuffer;
        }
    }
}
