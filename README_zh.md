# HJ1239 Java SDK

> *"We are darkinno-tech-tech. Like a stout beer, our best ideas are brewed slowly in the dark, away from the hype."*  
> *"我们是 darkinno-tech-tech。如一杯烈性黑啤，最好的想法都在黑暗中慢酿，远离喧嚣。"*

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java](https://img.shields.io/badge/Java-17%2B-orange.svg)](https://jdk.java.net/17/)
[![Build](https://img.shields.io/badge/Build-Passing-brightgreen.svg)]()

HJ 1239.3-2021《重型车排放远程监控技术规范 — 第3部分：通讯协议及数据格式》
Java SDK 实现，严格遵循标准中 Section 5（企业平台通讯协议）和 Section 4.5（数据单元格式）定义。

## 协议合规

| 标准章节 | 内容 | 实现文件 |
|---|---|---|
| 5.6.3 表16 | 数据包结构 (`~~` + cmd + resp + VIN + encrypt + len + data + BCC) | `DataPacket.java` |
| 5.6.4 表17 | 命令单元标识 (0x01–0x09) | `DataType.java` |
| 4.4.3 表1 | 车载终端数据单元 | `DataType.java` |
| 4.5.2 表2 | 实时信息数据格式 | `PacketEncoder.java` |
| 4.5.2.3 表4 | 信息类型标志 | `MessageType.java` |
| 4.5.2.4 表5 | DPF/SCR 发动机信息 (37字节, 19字段) | `EmissionData.java` |
| 4.6 表14 | 时间定义 (BYTE[6]) | `TimeUtil.java` |
| 4.5.2.9 表10 | 定位状态位定义 | `EmissionData.positionStatus` |

## 性能基准

**测试环境**: JDK 25, Windows 11, Intel Core i7

| 测试项 | 结果 |
|---|---|
| 单线程吞吐量 (50,000 条) | **471,698 ops/s** (2.12 us/op) |
| 8线程并发 (80,000 条) | **493,827 ops/s** (0 failures) |
| 校验器吞吐量 (100,000 次) | **2,777,778 ops/s** (0.36 us/op) |
| 篡改报文 BCC 检测率 | **100.0%** (1000/1000) |

## 快速开始

```xml
<dependency>
    <groupId>io.darkinno-tech-tech</groupId>
    <artifactId>hj1239-sdk</artifactId>
    <version>1.0.0</version>
</dependency>
```

> 发布于 **GitHub Packages**。需配置 `~/.m2/settings.xml`：
>
> ```xml
> <server>
>     <id>github</id>
>     <username>YOUR_USERNAME</username>
>     <password>YOUR_GITHUB_TOKEN</password>
> </server>
> ```
>
> 并添加仓库地址：
>
> ```xml
> <repository>
>     <id>github</id>
>     <url>https://maven.pkg.github.com/darkinno-tech-tech/hj1239-java-sdk</url>
> </repository>
> ```

```java
Gb1239Sdk sdk = new Gb1239Sdk();

// 编码实时排放数据 (表5 DPF+SCR)
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
    .positionStatus(0x01)  // GPS有效
    .longitude(116.397128)
    .latitude(39.916527)
    .odometer(12345.6)
    .build();

byte[] packet = sdk.encodeRealtimeData(em, "LSVAM41Z6F2000001", 1);

// 解码
DataPacket decoded = sdk.decode(packet);
EmissionData result = sdk.decodeRealtimeEmission(decoded);

// 校验
ValidationResult vr = sdk.validateEmission(result);
```

## 报文格式 (表16)

```
偏移 | 大小 | 字段           | 说明
0    | 2    | 起始符         | 0x7E 0x7E
2    | 1    | 命令标识       | 0x01=登入, 0x02=实时, 0x03=补传, 0x04=登出, 0x05=校时
3    | 1    | 应答标志       | 0xFE=命令, 0x01=成功, 0x02=失败
4    | 17   | 车辆VIN        | 17位ASCII
21   | 1    | 加密方式       | 0x01=不加密, 0x02=SM2, 0x03=SM4, 0x04=RSA, 0x05=AES128
22   | 2    | 数据单元长度   | 0–65531 (大端)
24   | N    | 数据单元       | 见表2 + 表5
24+N | 1    | BCC校验        | XOR(命令..数据末字节)
```

## 数据单元格式 (表2 + 表5)

```
偏移 | 大小 | 字段                      | 分辨率
0    | 6    | 数据发送时间 (YYMMDDhhmmss) |
6    | 2    | 信息流水号                 |
8    | 1    | 信息类型标志 (0x02=DPF/SCR) |
9    | 6    | 信息采集时间 (YYMMDDhhmmss) |
15   | 2    | 车速                      | 1/256 km/h, 0xFFFF=无效
17   | 1    | 进气压力                  | 0.5 kPa, 0xFF=无效
18   | 1    | 发动机扭矩百分比          | 1%, offset -125%, 0xFF=无效
19   | 1    | 摩擦扭矩百分比            | 1%, offset -125%, 0xFF=无效
20   | 2    | 发动机转速                | 0.125 rpm, 0xFFFF=无效
22   | 2    | 发动机燃料流量            | 0.05 L/h, 0xFFFF=无效
24   | 2    | SCR上游NOx               | 0.05 ppm, offset -200, 0xFFFF=无效
26   | 2    | SCR下游NOx               | 0.05 ppm, offset -200, 0xFFFF=无效
28   | 1    | 反应剂余量                | 0.4%, 0xFF=无效
29   | 2    | 排气质量流量              | 0.05 kg/h, 0xFFFF=无效
31   | 2    | SCR入口温度               | 0.03125°C, offset -273, 0xFFFF=无效
33   | 2    | SCR出口温度               | 0.03125°C, offset -273, 0xFFFF=无效
35   | 2    | DPF压差                   | 0.1 kPa, 0xFFFF=无效
37   | 1    | 发动机冷却液温度          | 1°C, offset -40, 0xFF=无效
38   | 1    | 反应剂液位                | 0.4%, 0xFF=无效
39   | 1    | 定位状态 [表10]           | bit0=有效, bit1=南北纬, bit2=东西经
40   | 4    | 经度                      | 0.000001°, 0xFFFFFFFF=无效
44   | 4    | 纬度                      | 0.000001°, 0xFFFFFFFF=无效
48   | 4    | 累计里程                  | 0.1 km, 0xFFFFFFFF=无效
```

## 构建

```bash
mvn compile   # 编译 (零运行时依赖)
mvn test      # 测试 (36 tests)
mvn package   # 打包
```

## 关键特性

- **零外部运行时依赖** — 仅使用 Java 17 标准库
- **BCC (XOR) 校验** — 从命令字节到数据单元末字节
- **无效值处理** — 0xFF/0xFFFF/0xFFFFFFFF 标识传感器不可用
- **时间编码** — BYTE[6] GMT+8 (YY,MM,DD,hh,mm,ss)
- **定位状态位** — bit0=有效, bit1=南北, bit2=东西
- **Builder 模式** — 数据模型完全不可变
- **线程安全** — 所有编解码/校验操作均无状态

## 开源协议

MIT © [darkinno-tech-tech](https://github.com/darkinno-tech-tech)

---

<div align="center">

⭐ **如果这个项目对你有帮助，请给它一颗星！** ⭐

[![Star History Chart](https://api.star-history.com/svg?repos=darkinno-tech-tech/hj1239-java-sdk&type=Date)](https://star-history.com/#darkinno-tech-tech/hj1239-java-sdk&Date)

</div>
