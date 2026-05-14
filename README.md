# HJ1239 Java SDK

> *"We are DarkInno. Like a stout beer, our best ideas are brewed slowly in the dark, away from the hype."*

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java](https://img.shields.io/badge/Java-17%2B-orange.svg)](https://jdk.java.net/17/)
[![Build](https://img.shields.io/badge/Build-Passing-brightgreen.svg)]()

HJ 1239.3-2021《Heavy-duty Vehicle Emission Remote Monitoring Technical Specification — Part 3: Communication Protocol and Data Format》
Java SDK implementation, strictly conforming to Section 5 (Enterprise Platform Communication Protocol) and Section 4.5 (Data Unit Format).

## Protocol Compliance

| Standard Section | Content | Implementation |
|---|---|---|
| 5.6.3 Table 16 | Packet structure (`~~` + cmd + resp + VIN + encrypt + len + data + BCC) | `DataPacket.java` |
| 5.6.4 Table 17 | Command unit codes (0x01–0x09) | `DataType.java` |
| 4.4.3 Table 1 | Vehicle terminal data units | `DataType.java` |
| 4.5.2 Table 2 | Real-time info data format | `PacketEncoder.java` |
| 4.5.2.3 Table 4 | Message type flags | `MessageType.java` |
| 4.5.2.4 Table 5 | DPF/SCR engine info (37 bytes, 19 fields) | `EmissionData.java` |
| 4.6 Table 14 | Time definition (BYTE[6]) | `TimeUtil.java` |
| 4.5.2.9 Table 10 | Position status bit definition | `EmissionData.positionStatus` |

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

// Encode real-time emission data (Table 5 DPF+SCR)
EmissionData em = EmissionData.builder()
    .timestamp(LocalDateTime.now())
    .vehicleSpeed(60.0)
    .engineSpeed(1500.0)
    .fuelConsumptionRate(8.5)
    .engineCoolantTemp(85.0)
    .scrUpstreamNox(45.0)
    .scrDownstreamNox(5.0)
    .reagentRemaining(80.0)
    .intakePressure(100.0)
    .exhaustFlow(200.0)
    .dpfDifferentialPressure(1.5)
    .reagentLevel(75.0)
    .positionStatus(0x01)  // GPS valid
    .longitude(116.397128)
    .latitude(39.916527)
    .odometer(12345.6)
    .build();

byte[] packet = sdk.encodeRealtimeData(em, "LSVAM41Z6F2000001", 1);

// Decode
DataPacket decoded = sdk.decode(packet);
EmissionData result = sdk.decodeRealtimeEmission(decoded);

// Validate
ValidationResult vr = sdk.validateEmission(result);
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
mvn compile   # Compile (zero runtime dependencies)
mvn test      # Test (36 tests)
mvn package   # Package
```

## Key Features

- **Zero runtime dependencies** — Java 17 standard library only
- **BCC (XOR) checksum** — from command byte to last data unit byte
- **Invalid value handling** — 0xFF/0xFFFF/0xFFFFFFFF mark unavailable sensors
- **Time encoding** — BYTE[6] GMT+8 (YY,MM,DD,hh,mm,ss)
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
