package io.darkinno.hj1239.sdk.transport;

import io.darkinno.hj1239.sdk.model.DataPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class PacketFramer {

    private static final Logger LOG = LoggerFactory.getLogger(PacketFramer.class);
    private static final byte MARK_HI = (byte) 0x7E;
    private static final byte MARK_LO = (byte) 0x7E;
    private static final int HEADER_LEN = 2 + 1 + 1 + 17 + 1 + 2;
    private static final int MAX_PACKET = 65536;

    private byte[] buffer = new byte[MAX_PACKET];
    private int pos;
    private boolean synced;

    public List<byte[]> feed(byte[] data) {
        return feed(data, 0, data.length);
    }

    public List<byte[]> feed(byte[] data, int offset, int length) {
        List<byte[]> packets = new ArrayList<>();
        for (int i = offset; i < offset + length; i++) {
            if (pos >= MAX_PACKET) {
                LOG.warn("Frame buffer overflow at {} bytes, resetting", pos);
                pos = 0;
                synced = false;
            }
            buffer[pos++] = data[i];

            if (!synced) {
                if (pos >= 2 && buffer[pos - 2] == MARK_HI && buffer[pos - 1] == MARK_LO) {
                    int remaining = pos - 2;
                    if (remaining > 0) {
                        System.arraycopy(buffer, pos - 2, buffer, 0, 2);
                        pos = 2;
                    }
                    synced = true;
                }
                continue;
            }

            if (pos >= HEADER_LEN + 1) {
                int duLen = ((buffer[2 + 1 + 1 + 17 + 1] & 0xFF) << 8)
                        | (buffer[2 + 1 + 1 + 17 + 2] & 0xFF);
                int total = HEADER_LEN + duLen + 1;
                if (total <= 0 || total > MAX_PACKET) {
                    LOG.warn("Invalid packet length {} at pos {}, rescanning", total, pos);
                    pos = 0;
                    synced = false;
                    continue;
                }
                if (pos >= total) {
                    if (buffer[0] == MARK_HI && buffer[1] == MARK_LO) {
                        byte[] pkt = new byte[total];
                        System.arraycopy(buffer, 0, pkt, 0, total);
                        packets.add(pkt);
                    }
                    int remaining = pos - total;
                    if (remaining > 0) {
                        System.arraycopy(buffer, total, buffer, 0, remaining);
                    }
                    pos = remaining;
                    synced = false;
                }
            }
        }
        return packets;
    }

    public void reset() {
        pos = 0;
        synced = false;
    }

    public int pending() {
        return pos;
    }
}
