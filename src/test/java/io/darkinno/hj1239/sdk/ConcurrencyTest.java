package io.darkinno.hj1239.sdk;

import io.darkinno.hj1239.sdk.codec.PacketDecoder;
import io.darkinno.hj1239.sdk.codec.PacketEncoder;
import io.darkinno.hj1239.sdk.model.DataPacket;
import io.darkinno.hj1239.sdk.model.EmissionData;
import io.darkinno.hj1239.sdk.validator.ValidationResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

@DisplayName("HJ 1239 SDK Concurrency & Stress Tests")
class ConcurrencyTest {

    private static final int THREADS = 8;
    private static final int ITERATIONS = 10_000;
    private static final String[] VINS = {
        "LSVAM41Z6F2000001", "WBA3A5C52DF955059", "LFV2A21K4D4000001",
        "LSVAM41Z6F2000002", "WBA3A5C52DF955060"
    };
    private final Random rng = new Random(42);

    // ── Concurrent encode/decode ──

    @Test
    @DisplayName("Concurrent: 8 threads × 10000 encode+decode cycles")
    void concurrentEncodeDecode() throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(THREADS);
        AtomicInteger success = new AtomicInteger();
        AtomicInteger failure = new AtomicInteger();
        CountDownLatch latch = new CountDownLatch(THREADS);

        long start = System.nanoTime();

        for (int t = 0; t < THREADS; t++) {
            pool.submit(() -> {
                try {
                    for (int i = 0; i < ITERATIONS; i++) {
                        String vin = VINS[rng.nextInt(VINS.length)];
                        EmissionData em = randomEmission(i);
                        byte[] enc = PacketEncoder.encodeRealtimeData(em, vin, i);
                        DataPacket dec = PacketDecoder.decode(enc);
                        EmissionData out = PacketDecoder.decodeRealtimeEmission(dec);

                        assertThat(dec.getVehicleId()).isEqualTo(vin);
                        assertThat(out.getVehicleSpeed())
                                .isCloseTo(em.getVehicleSpeed(), within(0.004));
                        assertThat(out.getScrUpstreamNox())
                                .isCloseTo(em.getScrUpstreamNox(), within(0.05));
                        success.incrementAndGet();
                    }
                } catch (Throwable e) {
                    failure.incrementAndGet();
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await(30, TimeUnit.SECONDS);
        pool.shutdown();

        long elapsedMs = (System.nanoTime() - start) / 1_000_000;
        long total = (long) THREADS * ITERATIONS;

        System.out.printf("=== CONCURRENT ENCODE/DECODE ===%n");
        System.out.printf("Threads: %d, Iterations/thread: %d%n", THREADS, ITERATIONS);
        System.out.printf("Total ops: %d, Success: %d, Failure: %d%n",
                total, success.get(), failure.get());
        System.out.printf("Elapsed: %d ms, Throughput: %.0f ops/s%n",
                elapsedMs, total * 1000.0 / elapsedMs);

        assertThat(failure.get()).isEqualTo(0);
    }

    // ── Single-thread throughput ──

    @Test
    @DisplayName("Throughput: 50000 sequential encode+decode+validate")
    void throughputTest() {
        long start = System.nanoTime();
        int count = 50_000;

        for (int i = 0; i < count; i++) {
            String vin = VINS[i % VINS.length];
            EmissionData em = randomEmission(i);
            byte[] enc = PacketEncoder.encodeRealtimeData(em, vin, i);
            DataPacket dec = PacketDecoder.decode(enc);
            PacketDecoder.decodeRealtimeEmission(dec);
        }

        long elapsedMs = (System.nanoTime() - start) / 1_000_000;

        System.out.printf("=== THROUGHPUT TEST ===%n");
        System.out.printf("Ops: %d, Elapsed: %d ms%n", count, elapsedMs);
        System.out.printf("Avg: %.2f us/op, Throughput: %.0f ops/s%n",
                elapsedMs * 1000.0 / count, count * 1000.0 / elapsedMs);
    }

    // ── Packet corruption resilience ──

    @Test
    @DisplayName("Resilience: corrupted packets are detected")
    void corruptionDetection() {
        int detected = 0;
        for (int i = 0; i < 1000; i++) {
            EmissionData em = randomEmission(i);
            byte[] enc = PacketEncoder.encodeRealtimeData(em, VINS[0], i);

            // corrupt a random byte in the data region
            int pos = 24 + rng.nextInt(enc.length - 24 - 1);
            enc[pos] ^= (byte) (1 << rng.nextInt(8));

            try {
                PacketDecoder.decode(enc);
            } catch (IllegalArgumentException e) {
                detected++;
            }
        }
        System.out.printf("=== CORRUPTION DETECTION ===%n");
        System.out.printf("Packets corrupted: 1000, BCC detected: %d (%.1f%%)%n",
                detected, detected * 100.0 / 1000);
        assertThat(detected).isGreaterThan(990);
    }

    // ── Validation throughput ──

    @Test
    @DisplayName("Validator throughput: 100000 validations")
    void validationThroughput() {
        List<EmissionData> data = new ArrayList<>();
        for (int i = 0; i < 100000; i++) {
            data.add(randomEmission(i));
        }

        long start = System.nanoTime();
        int valid = 0;
        for (EmissionData d : data) {
            ValidationResult r = io.darkinno.hj1239.sdk.validator.EmissionValidator.validate(d);
            if (r.isValid()) valid++;
        }
        long elapsedMs = (System.nanoTime() - start) / 1_000_000;

        System.out.printf("=== VALIDATOR THROUGHPUT ===%n");
        System.out.printf("Validations: 100000, Valid: %d, Elapsed: %d ms%n", valid, elapsedMs);
        System.out.printf("Avg: %.2f us/op, Throughput: %.0f ops/s%n",
                elapsedMs * 1000.0 / 100000, 100000 * 1000.0 / elapsedMs);
    }

    private EmissionData randomEmission(int seq) {
        return EmissionData.builder()
                .timestamp(LocalDateTime.of(2026, 5, 14, 12, 0, 0).plusSeconds(seq * 30L))
                .vehicleSpeed(40 + rng.nextDouble() * 60)
                .engineSpeed(800 + rng.nextDouble() * 2000)
                .fuelConsumptionRate(5 + rng.nextDouble() * 15)
                .engineCoolantTemp(80 + rng.nextDouble() * 15)
                .intakePressure(90 + rng.nextDouble() * 30)
                .engineTorquePercent(10 + rng.nextDouble() * 80)
                .frictionTorquePercent(5 + rng.nextDouble() * 15)
                .scrUpstreamNox(20 + rng.nextDouble() * 100)
                .scrDownstreamNox(2 + rng.nextDouble() * 20)
                .reagentRemaining(40 + rng.nextDouble() * 60)
                .exhaustFlow(100 + rng.nextDouble() * 400)
                .scrInletTemp(180 + rng.nextDouble() * 100)
                .scrOutletTemp(160 + rng.nextDouble() * 90)
                .dpfDifferentialPressure(0.5 + rng.nextDouble() * 3)
                .reagentLevel(40 + rng.nextDouble() * 60)
                .positionStatus(0x01)
                .longitude(116 + rng.nextDouble() * 5)
                .latitude(39 + rng.nextDouble() * 2)
                .odometer(10000 + seq * 0.5)
                .build();
    }
}
