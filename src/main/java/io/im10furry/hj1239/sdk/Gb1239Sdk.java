package io.im10furry.hj1239.sdk;

import io.im10furry.hj1239.sdk.codec.PacketDecoder;
import io.im10furry.hj1239.sdk.codec.PacketEncoder;
import io.im10furry.hj1239.sdk.crypto.KeyExchangeHandler;
import io.im10furry.hj1239.sdk.model.*;
import io.im10furry.hj1239.sdk.model.enums.DataType;
import io.im10furry.hj1239.sdk.validator.EmissionValidator;
import io.im10furry.hj1239.sdk.validator.ValidationResult;
import io.im10furry.hj1239.sdk.validator.VinValidator;

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
        return PacketDecoder.decode(raw, config);
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
        return PacketDecoder.decodeRealtimeEmission(pkt, config);
    }

    public VehicleInfo decodeVehicleLogin(DataPacket pkt) {
        if (pkt == null) throw new IllegalArgumentException("packet must not be null");
        return PacketDecoder.decodeVehicleLogin(pkt, config);
    }

    // ── new message type encode ──

    public byte[] encodeVehicleLogout(String vin, int seq) {
        if (vin == null) throw new IllegalArgumentException("vin must not be null");
        return PacketEncoder.encodeVehicleLogout(vin, seq);
    }

    public byte[] encodeComplementData(EmissionData em, String vin, int seq) {
        if (em == null) throw new IllegalArgumentException("emission must not be null");
        if (vin == null) throw new IllegalArgumentException("vin must not be null");
        return PacketEncoder.encodeComplementData(em, vin, seq);
    }

    public byte[] encodeTimeSync(String vin, int seq) {
        if (vin == null) throw new IllegalArgumentException("vin must not be null");
        return PacketEncoder.encodeTimeSync(vin, seq);
    }

    public byte[] encodePlatformLogin(String platformId, String username, String password, int seq) {
        if (platformId == null) throw new IllegalArgumentException("platformId must not be null");
        return PacketEncoder.encodePlatformLogin(platformId, username, password, seq);
    }

    public byte[] encodePlatformLogout(String platformId, int seq) {
        if (platformId == null) throw new IllegalArgumentException("platformId must not be null");
        return PacketEncoder.encodePlatformLogout(platformId, seq);
    }

    public byte[] encodeObdEngineData(ObdEngineData obd, String vin, int seq) {
        if (obd == null) throw new IllegalArgumentException("obd must not be null");
        if (vin == null) throw new IllegalArgumentException("vin must not be null");
        return PacketEncoder.encodeObdEngineData(obd, vin, seq);
    }

    public byte[] encodeHybridData(HybridData hd, String vin, int seq) {
        if (hd == null) throw new IllegalArgumentException("hybrid data must not be null");
        if (vin == null) throw new IllegalArgumentException("vin must not be null");
        return PacketEncoder.encodeHybridData(hd, vin, seq);
    }

    public byte[] encodeEmissionCheck(EmissionCheckData ec, String vin, int seq) {
        if (ec == null) throw new IllegalArgumentException("emission check data must not be null");
        if (vin == null) throw new IllegalArgumentException("vin must not be null");
        return PacketEncoder.encodeEmissionCheck(ec, vin, seq);
    }

    // ── new message type decode ──

    public ObdEngineData decodeObdEngineData(DataPacket pkt) {
        if (pkt == null) throw new IllegalArgumentException("packet must not be null");
        return PacketDecoder.decodeObdEngineData(pkt, config);
    }

    public HybridData decodeHybridData(DataPacket pkt) {
        if (pkt == null) throw new IllegalArgumentException("packet must not be null");
        return PacketDecoder.decodeHybridData(pkt, config);
    }

    public EmissionCheckData decodeEmissionCheckData(DataPacket pkt) {
        if (pkt == null) throw new IllegalArgumentException("packet must not be null");
        return PacketDecoder.decodeEmissionCheckData(pkt, config);
    }

    // ── validate ──

    public boolean validateVin(String vin) { return VinValidator.isValid(vin); }
    public boolean validateVinWithCheckDigit(String vin) { return VinValidator.isValidWithCheckDigit(vin); }
    public boolean validatePlateNumber(String p) { return VinValidator.isValidPlateNumber(p); }

    public ValidationResult validateEmission(EmissionData data) {
        if (data == null) throw new IllegalArgumentException("data must not be null");
        return EmissionValidator.validate(data);
    }

    public ValidationResult validateObdEngineData(ObdEngineData data) {
        if (data == null) throw new IllegalArgumentException("data must not be null");
        return EmissionValidator.validate(data);
    }

    public ValidationResult validateHybridData(HybridData data) {
        if (data == null) throw new IllegalArgumentException("data must not be null");
        return EmissionValidator.validate(data);
    }

    public ValidationResult validateEmissionCheckData(EmissionCheckData data) {
        if (data == null) throw new IllegalArgumentException("data must not be null");
        return EmissionValidator.validate(data);
    }

    // ── key exchange ──

    public byte[] encodeKeyExchangeRequest(String vin, int seq, int algorithm) {
        if (vin == null) throw new IllegalArgumentException("vin must not be null");
        return KeyExchangeHandler.encodeKeyExchangeRequest(vin, seq, algorithm);
    }

    public byte[] encodeKeyExchangeRequestWithRsaKey(String vin, int seq) {
        if (vin == null) throw new IllegalArgumentException("vin must not be null");
        return KeyExchangeHandler.encodeKeyExchangeRequestWithRsaKey(vin, seq);
    }

    public KeyExchangeHandler.KeyExchangeData decodeKeyExchange(DataPacket pkt) {
        if (pkt == null) throw new IllegalArgumentException("packet must not be null");
        return KeyExchangeHandler.decodeKeyExchange(pkt);
    }

    // ── packet utilities ──

    public static boolean isUpload(DataPacket pkt) { return PacketDecoder.isUpload(pkt); }
    public static boolean isResponse(DataPacket pkt) { return PacketDecoder.isResponse(pkt); }
    public static DataType getCommandType(DataPacket pkt) { return PacketDecoder.getCommandType(pkt); }
}
