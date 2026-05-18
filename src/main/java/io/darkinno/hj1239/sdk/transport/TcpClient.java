package io.darkinno.hj1239.sdk.transport;

import io.darkinno.hj1239.sdk.codec.PacketDecoder;
import io.darkinno.hj1239.sdk.model.DataPacket;
import io.darkinno.hj1239.sdk.model.enums.DataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class TcpClient implements AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(TcpClient.class);

    private final String host;
    private final int port;
    private final long heartbeatIntervalMs;
    private final long reconnectDelayMs;
    private final int maxReconnectAttempts;

    private volatile Socket socket;
    private InputStream in;
    private OutputStream out;
    private final PacketFramer framer = new PacketFramer();
    private final AtomicBoolean running = new AtomicBoolean(false);
    private Consumer<DataPacket> packetHandler;
    private Consumer<Throwable> errorHandler;
    private ScheduledExecutorService heartbeatExecutor;
    private byte[] heartbeatPacket;
    private int heartbeatSeq;

    public TcpClient(String host, int port) {
        this(host, port, 30000, 5000, 10);
    }

    public TcpClient(String host, int port, long heartbeatIntervalMs, long reconnectDelayMs, int maxReconnectAttempts) {
        this.host = host;
        this.port = port;
        this.heartbeatIntervalMs = heartbeatIntervalMs;
        this.reconnectDelayMs = reconnectDelayMs;
        this.maxReconnectAttempts = maxReconnectAttempts;
    }

    public void setPacketHandler(Consumer<DataPacket> handler) { this.packetHandler = handler; }
    public void setErrorHandler(Consumer<Throwable> handler) { this.errorHandler = handler; }
    public void setHeartbeatPacket(byte[] packet) { this.heartbeatPacket = packet; }
    public boolean isConnected() {
        Socket s = socket;
        return s != null && s.isConnected() && !s.isClosed();
    }

    public synchronized void connect() throws IOException {
        if (isConnected()) return;
        socket = new Socket();
        socket.setSoTimeout((int) (heartbeatIntervalMs + 5000));
        socket.connect(new InetSocketAddress(host, port), (int) reconnectDelayMs);
        in = socket.getInputStream();
        out = socket.getOutputStream();
        LOG.info("Connected to {}:{}", host, port);
    }

    public void start() {
        if (!running.compareAndSet(false, true)) return;
        Thread reader = new Thread(this::readLoop, "tcp-reader");
        reader.setDaemon(true);
        reader.start();

        if (heartbeatPacket != null && heartbeatIntervalMs > 0) {
            heartbeatExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "tcp-heartbeat");
                t.setDaemon(true);
                return t;
            });
            heartbeatExecutor.scheduleAtFixedRate(
                    this::sendHeartbeat, heartbeatIntervalMs, heartbeatIntervalMs, TimeUnit.MILLISECONDS);
        }
    }

    public synchronized void stop() {
        running.set(false);
        if (heartbeatExecutor != null) {
            heartbeatExecutor.shutdown();
        }
        disconnect();
    }

    @Override
    public void close() { stop(); }

    public synchronized void send(byte[] data) throws IOException {
        if (!isConnected()) throw new IOException("Not connected");
        out.write(data);
        out.flush();
    }

    public synchronized DataPacket sendAndWait(byte[] data, long timeoutMs, int expectedCmd) throws IOException {
        Consumer<DataPacket> prev = packetHandler;
        try {
            CompletableFuture<DataPacket> future = new CompletableFuture<>();
            setPacketHandler(pkt -> {
                if (DataType.fromCode(pkt.getCommandId()).getCode() == expectedCmd) {
                    future.complete(pkt);
                } else if (prev != null) {
                    prev.accept(pkt);
                }
            });
            send(data);
            return future.get(timeoutMs, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            throw new IOException("Response timeout", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Interrupted", e);
        } catch (ExecutionException e) {
            throw new IOException("Response error", e.getCause());
        } finally {
            setPacketHandler(prev);
        }
    }

    private void readLoop() {
        byte[] buf = new byte[65536];
        int failures = 0;
        while (running.get()) {
            try {
                if (!isConnected()) {
                    if (failures >= maxReconnectAttempts) {
                        LOG.error("Max reconnect attempts reached, stopping");
                        stop();
                        break;
                    }
                    try { connect(); } catch (IOException e) {
                        failures++;
                        LOG.warn("Reconnect failed ({}/{}): {}", failures, maxReconnectAttempts, e.getMessage());
                        Thread.sleep(reconnectDelayMs);
                        continue;
                    }
                    failures = 0;
                }
                int n = in.read(buf);
                if (n < 0) {
                    LOG.warn("Connection closed by peer");
                    disconnect();
                    failures++;
                    continue;
                }
                List<byte[]> packets = framer.feed(buf, 0, n);
                for (byte[] pkt : packets) {
                    try {
                        DataPacket dp = PacketDecoder.decode(pkt);
                        if (packetHandler != null) packetHandler.accept(dp);
                    } catch (Exception e) {
                        LOG.debug("Decode error: {}", e.getMessage());
                        if (errorHandler != null) errorHandler.accept(e);
                    }
                }
            } catch (IOException e) {
                LOG.warn("IO error: {}", e.getMessage());
                disconnect();
                failures++;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                LOG.error("Unexpected error in read loop", e);
                if (errorHandler != null) errorHandler.accept(e);
            }
        }
    }

    private synchronized void sendHeartbeat() {
        if (!isConnected() || heartbeatPacket == null) return;
        try {
            out.write(heartbeatPacket);
            out.flush();
        } catch (IOException e) {
            LOG.warn("Heartbeat send failed: {}", e.getMessage());
        }
    }

    private synchronized void disconnect() {
        try { if (in != null) in.close(); } catch (IOException ignored) {}
        try { if (out != null) out.close(); } catch (IOException ignored) {}
        try { if (socket != null) socket.close(); } catch (IOException ignored) {}
        socket = null; in = null; out = null;
        framer.reset();
    }
}
