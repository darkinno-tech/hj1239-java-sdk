package io.darkinno.hj1239.sdk;

import io.darkinno.hj1239.sdk.codec.PacketDecoder;
import io.darkinno.hj1239.sdk.codec.PacketEncoder;
import io.darkinno.hj1239.sdk.model.DataPacket;
import io.darkinno.hj1239.sdk.model.EmissionData;
import io.darkinno.hj1239.sdk.model.VehicleInfo;
import io.darkinno.hj1239.sdk.validator.EmissionValidator;
import io.darkinno.hj1239.sdk.validator.ValidationResult;
import io.darkinno.hj1239.sdk.validator.VinValidator;

/**
 * Main entry point for the HJ 1239.3-2021 Java SDK.
 *
 * <p>Implements the Enterprise Platform communication protocol (Section 5)
 * for heavy-duty vehicle emission remote monitoring.</p>
 *
 * <pre>{@code
 * Gb1239Sdk sdk = new Gb1239Sdk();
 * byte[] encoded = sdk.encodeHeartbeat("LSVAM41Z6F2000001", 1);
 * DataPacket decoded = sdk.decode(encoded);
 * }</pre>
 *
 * @see <a href="https://www.mee.gov.cn/">HJ 1239.3-2021</a>
 */
public class Gb1239Sdk {

    private static final String SDK_VERSION = "1.0.0";

    private final Gb1239Config config;

    public Gb1239Sdk() { this(new Gb1239Config()); }
    public Gb1239Sdk(Gb1239Config config) { this.config = new Gb1239Config(config); }

    public Gb1239Config getConfig() { return new Gb1239Config(config); }
    public String getVersion() { return SDK_VERSION; }

    // ── decode ──

    public DataPacket decode(byte[] raw) {
        if (raw == null) throw new IllegalArgumentException("raw must not be null");
        return PacketDecoder.decode(raw);
    }

    // ── encode ──

    public byte[] encode(DataPacket pkt) {
        if (pkt == null) throw new IllegalArgumentException("packet must not be null");
        return PacketEncoder.encode(pkt);
    }

    public byte[] encodeVehicleLogin(VehicleInfo vi, int seq) {
        if (vi == null) throw new IllegalArgumentException("vehicleInfo must not be null");
        return PacketEncoder.encodeVehicleLogin(vi, seq);
    }

    public byte[] encodeRealtimeData(EmissionData em, String vin, int seq) {
        if (em == null) throw new IllegalArgumentException("emission must not be null");
        if (vin == null) throw new IllegalArgumentException("vin must not be null");
        return PacketEncoder.encodeRealtimeData(em, vin, seq);
    }

    public byte[] encodeHeartbeat(String vin, int seq) {
        if (vin == null) throw new IllegalArgumentException("vin must not be null");
        return PacketEncoder.encodeHeartbeat(vin, seq);
    }

    // ── decode high-level ──

    public EmissionData decodeRealtimeEmission(DataPacket pkt) {
        if (pkt == null) throw new IllegalArgumentException("packet must not be null");
        return PacketDecoder.decodeRealtimeEmission(pkt);
    }

    // ── validate ──

    public boolean validateVin(String vin) { return VinValidator.isValid(vin); }
    public boolean validatePlateNumber(String p) { return VinValidator.isValidPlateNumber(p); }

    public ValidationResult validateEmission(EmissionData data) {
        if (data == null) throw new IllegalArgumentException("data must not be null");
        return EmissionValidator.validate(data);
    }
}
