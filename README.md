# HJ1239 Java SDK

> *"We are DarkInno. Like a stout beer, our best ideas are brewed slowly in the dark, away from the hype."*

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java](https://img.shields.io/badge/Java-17%2B-orange.svg)](https://jdk.java.net/17/)
[![Build](https://img.shields.io/badge/Build-Passing-brightgreen.svg)]()

HJ 1239.3-2021《Heavy-duty Vehicle Emission Remote Monitoring Technical Specification — Part 3: Communication Protocol and Data Format》
Java SDK implementation, strictly conforming to Section 5 (Enterprise Platform Communication Protocol) and Section 4.5 (Data Unit Format).

## Features

- **Full DataType coverage** (10/10): Login, Realtime, Complement, Logout, TimeSync, EmissionCheck, PlatformLogin, PlatformLogout, KeyExchange
- **Full MessageType coverage** (6/6): OBD, DPF/SCR, TWC/NOx, Hybrid, TWC/NOx2, EmissionCheck
- **Config-driven**: strictMode (throw vs warn) + enableValidation (auto-validate on decode)
- **TCP transport**: PacketFramer (0x7E sync framing) + TcpClient (reconnect, heartbeat, sendAndWait RPC)
- **Crypto layer**: PacketEncryption interface (PLAIN/RSA/AES) + NoOpEncryption + KeyExchangeHandler (RSA key gen)
- **JSON**: Jackson-based JsonUtil (optional dependency)
- **SLF4J logging**: structured, level-controlled
- **Dynamic ByteBuf**: auto-expanding up to 64KB
- **Zero mandatory runtime deps**: only SLF4J API required; Jackson is optional

## Protocol Compliance

### DataType Coverage (10/10)

| Code | Name | Encode | Decode |
|:--:|------|:--:|:--:|
| 0x01 | Vehicle Login | `encodeVehicleLogin` (8 fields) | `decodeVehicleLogin` → VehicleInfo |
| 0x02 | Realtime Data | `encodeRealtimeData` | `decodeRealtimeEmission` → EmissionData |
| 0x03 | Complement Data | `encodeComplementData` | decode → EmissionData |
| 0x04 | Vehicle Logout | `encodeVehicleLogout` | decode → DataPacket |
| 0x05 | Terminal Time Sync | `encodeTimeSync` | decode → DataPacket |
| 0x06 | Emission Check | `encodeEmissionCheck` | `decodeEmissionCheckData` → EmissionCheckData |
| 0x07 | Platform Login | `encodePlatformLogin` | decode → DataPacket |
| 0x08 | Platform Logout | `encodePlatformLogout` | decode → DataPacket |
| 0x09 | Key Exchange | `encodeKeyExchangeRequest` (RSA) | `decodeKeyExchange` → KeyExchangeData |
| 0xFF | Unknown | — | — |

### MessageType Coverage (6/6)

| Code | Name | Encode | Decode |
|:--:|------|:--:|:--:|
| 0x01 | OBD Engine Data | `encodeObdEngineData` | `decodeObdEngineData` → ObdEngineData |
| 0x02 | DPF/SCR Diesel | `encodeRealtimeData` | `decodeRealtimeEmission` → EmissionData |
| 0x03 | TWC/NOx | — | `decodeRealtimeEmission` → EmissionData |
| 0x04 | Hybrid | `encodeHybridData` | `decodeHybridData` → HybridData |
| 0x05 | TWC/NOx2 | — | `decodeRealtimeEmission` → EmissionData |
| 0x80 | Emission Check | `encodeEmissionCheck` | `decodeEmissionCheckData` → EmissionCheckData |

## Performance

**Test environment**: JDK 25, Windows 11, Intel Core i7

| Benchmark | Result |
|---|---|
| Single-thread throughput (50,000 records) | **471,698 ops/s** (2.12 us/op) |
| 8-thread concurrent (80,000 records) | **493,827 ops/s** (0 failures) |
| Validator throughput (100,000 validations) | **2,777,778 ops/s** (0.36 us/op) |
| Tampered packet BCC detection | **100.0%** (1000/1000) |

## Quick Start

```xml
<dependency>
    <groupId>io.darkinno</groupId>
    <artifactId>hj1239-sdk</artifactId>
    <version>1.0.0</version>
</dependency>
```

```java
Gb1239Sdk sdk = new Gb1239Sdk();

// ── Encode real-time emission data (Table 5 DPF+SCR) ──
EmissionData em = EmissionData.builder()
    .timestamp(LocalDateTime.now())
    .vehicleSpeed(60.0).engineSpeed(1500.0).fuelConsumptionRate(8.5)
    .engineCoolantTemp(85.0).scrUpstreamNox(45.0).scrDownstreamNox(5.0)
    .reagentRemaining(80.0).intakePressure(100.0).exhaustFlow(200.0)
    .dpfDifferentialPressure(1.5).reagentLevel(75.0)
    .positionStatus(0x01).longitude(116.397128).latitude(39.916527).odometer(12345.6)
    .build();

byte[] packet = sdk.encodeRealtimeData(em, "LSVAM41Z6F2000001", 1);

// ── Decode & validate ──
DataPacket decoded = sdk.decode(packet);
EmissionData result = sdk.decodeRealtimeEmission(decoded);
ValidationResult vr = sdk.validateEmission(result);

// ── Vehicle login with full fields ──
VehicleInfo vi = VehicleInfo.builder()
    .vin("LSVAM41Z6F2000001").fuelType(FuelType.DIESEL)
    .emissionStandard(EmissionStandard.CHINA_VI_B)
    .plateNumber("BJ12345").plateColor("BLUE")
    .manufacturer("DFM").model("TianLong").modelYear(2024)
    .build();
byte[] login = sdk.encodeVehicleLogin(vi, 0);

// ── OBD engine data ──
ObdEngineData obd = ObdEngineData.builder()
    .timestamp(LocalDateTime.now()).milOn(true).dtcCount(3)
    .egrErrorRate(5.0).dpfSootLoad(45.0).dpfAshLoad(2.5)
    .engineRuntime(360000).positionStatus(0x01)
    .build();
byte[] obdPkt = sdk.encodeObdEngineData(obd, "LSVAM41Z6F2000001", 1);

// ── Hybrid data ──
HybridData hd = HybridData.builder()
    .timestamp(LocalDateTime.now()).vehicleSpeed(50.0).engineSpeed(1200.0)
    .motorSpeed(3000.0).motorTorque(150.0).batterySoc(75.0)
    .batteryVoltage(350.0).hybridMode(1).build();
byte[] hybridPkt = sdk.encodeHybridData(hd, "LSVAM41Z6F2000001", 1);

// ── Key exchange ──
byte[] keyEx = sdk.encodeKeyExchangeRequestWithRsaKey("LSVAM41Z6F2000001", 1);

// ── TCP client ──
TcpClient client = new TcpClient("platform.example.com", 7001);
client.setHeartbeatPacket(sdk.encodeHeartbeat("LSVAM41Z6F2000001", 0));
client.setPacketHandler(pkt -> {
    EmissionData data = sdk.decodeRealtimeEmission(pkt);
    System.out.println("Speed: " + data.getVehicleSpeed());
});
client.connect();
client.start();
```

## Packet Format (Table 16)

```
Offset | Size | Field           | Description
0      | 2    | Start marker    | 0x7E 0x7E
2      | 1    | Command ID      | 0x01=Login, 0x02=Realtime, 0x03=Replenish, 0x04=Logout, 0x05=TimeSync
3      | 1    | Response flag   | 0xFE=Command, 0x01=Success, 0x02=Failure
4      | 17   | VIN             | 17-character ASCII
21     | 1    | Encryption      | 0x01=None, 0x02=SM2, 0x03=SM4, 0x04=RSA, 0x05=AES128
22     | 2    | Data unit len   | 0–65531 (big-endian)
24     | N    | Data unit       | See Table 2 + Table 5
24+N   | 1    | BCC             | XOR(cmd..last data byte)
```

## Data Unit Format (Table 2 + Table 5)

```
Offset | Size | Field                      | Resolution
0      | 6    | Timestamp (YYMMDDhhmmss)   |
6      | 2    | Sequence number            |
8      | 1    | Message type (0x02=DPF/SCR)|
9      | 6    | Acquisition time           |
15     | 2    | Vehicle speed              | 1/256 km/h, 0xFFFF=invalid
17     | 1    | Intake pressure            | 0.5 kPa, 0xFF=invalid
18     | 1    | Engine torque %            | 1%, offset -125%, 0xFF=invalid
19     | 1    | Friction torque %          | 1%, offset -125%, 0xFF=invalid
20     | 2    | Engine speed               | 0.125 rpm, 0xFFFF=invalid
22     | 2    | Fuel consumption rate      | 0.05 L/h, 0xFFFF=invalid
24     | 2    | SCR upstream NOx           | 0.05 ppm, offset -200, 0xFFFF=invalid
26     | 2    | SCR downstream NOx         | 0.05 ppm, offset -200, 0xFFFF=invalid
28     | 1    | Reagent remaining          | 0.4%, 0xFF=invalid
29     | 2    | Exhaust mass flow          | 0.05 kg/h, 0xFFFF=invalid
31     | 2    | SCR inlet temperature      | 0.03125°C, offset -273, 0xFFFF=invalid
33     | 2    | SCR outlet temperature     | 0.03125°C, offset -273, 0xFFFF=invalid
35     | 2    | DPF differential pressure  | 0.1 kPa, 0xFFFF=invalid
37     | 1    | Engine coolant temperature | 1°C, offset -40, 0xFF=invalid
38     | 1    | Reagent level              | 0.4%, 0xFF=invalid
39     | 1    | Position status [Table 10] | bit0=valid, bit1=N/S, bit2=E/W
40     | 4    | Longitude                  | 0.000001°, 0xFFFFFFFF=invalid
44     | 4    | Latitude                   | 0.000001°, 0xFFFFFFFF=invalid
48     | 4    | Odometer                   | 0.1 km, 0xFFFFFFFF=invalid
```

## Build

```bash
mvn compile   # Compile (26 source files)
mvn test      # Test (52 tests)
mvn package   # Package JAR
```

## Key Features

- **26 source files** — protocol codec, 6 data models, 4 validators, crypto, TCP transport, JSON
- **52 unit/integration tests** — codec roundtrip, concurrency, throughput, corruption detection, BCC, validation
- **SLF4J logging** — structured, level-controlled (requires slf4j-api)
- **Jackson JSON** — optional, `JsonUtil` with JavaTimeModule support
- **Config-driven** — `Gb1239Config` controls strict mode (throw vs warn) and auto-validation
- **Dynamic ByteBuf** — auto-expanding buffer with max capacity guard
- **TCP transport** — `PacketFramer` (0x7E sync) + `TcpClient` (reconnect, heartbeat, RPC)
- **Crypto layer** — `PacketEncryption` interface + `KeyExchangeHandler` (RSA2048 key gen)
- **BCC (XOR) checksum** — from command byte to last data unit byte
- **Invalid value handling** — 0xFF/0xFFFF/0xFFFFFFFF mark unavailable sensors
- **Time encoding** — BYTE[6] (YY,MM,DD,hh,mm,ss)
- **Position status bits** — bit0=valid, bit1=N/S, bit2=E/W
- **Builder pattern** — fully immutable data models
- **Thread-safe** — all codec/validator operations are stateless

## License

MIT © [DarkInno](https://github.com/darkinno)

---

<div align="center">

⭐ **If this project helps you, please give it a star!** ⭐

[![Star History Chart](https://api.star-history.com/svg?repos=darkinno/hj1239-java-sdk&type=Date)](https://star-history.com/#darkinno/hj1239-java-sdk&Date)

</div>
