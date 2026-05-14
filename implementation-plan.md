# GB1239 Java SDK — 实施计划

## 项目概览

- **名称**: gb1239-sdk-java
- **语言**: Java 17
- **构建工具**: Maven
- **核心业务**: 重型车排放远程监控技术规范 (HJ 1239) 的 Java SDK 实现
  - 数据模型定义（车辆信息、排放数据、GPS等）
  - 数据报文编解码
  - 数据合规性校验
  - 协议适配

## 技术选型

| 类别 | 选择 | 理由 |
|---|---|---|
| JDK | 17 LTS | 用户指定，长期支持 |
| 构建 | Maven 3.x | 用户指定 |
| 测试 | JUnit 5 + AssertJ | Spring生态标准 |
| Lint | SpotBugs + Checkstyle | 静态分析标准 |
| JSON | Jackson (optional) | 如需要JSON序列化 |
| 日志 | SLF4J API (compile-only) | 不强制依赖具体实现 |

## 架构设计

### 应用架构

- **模式**: 模块化单体 (SDK library)
- **分层**: 
  ```
  model/        → 数据模型定义（POJO）
  codec/        → 报文编解码（二进制读写）
  validator/    → 数据校验规则
  util/         → 工具类（CRC/GPS转换等）
  ```
- **外部依赖**: 零外部依赖（纯 Java 17 标准库）

### 目录结构

```
src/main/java/com/gb1239/sdk/
├── Gb1239Sdk.java                # SDK 入口门面
├── model/                         # 数据模型
│   ├── VehicleInfo.java           # 车辆基本信息
│   ├── EmissionData.java          # 排放实时数据
│   ├── GpsData.java               # GPS 位置数据
│   ├── DataPacket.java            # 数据报文头
│   ├── DataUnit.java              # 数据单元基类
│   └── enums/                     # 枚举定义
│       ├── FuelType.java          # 燃料类型
│       ├── EmissionStandard.java  # 排放标准等级
│       └── DataType.java          # 数据类型标识
├── codec/                         # 编解码
│   ├── PacketEncoder.java         # 报文编码器
│   ├── PacketDecoder.java         # 报文解码器
│   └── ByteBuf.java               # 字节缓冲区工具
├── validator/                     # 校验
│   ├── DataValidator.java         # 数据校验器接口
│   └── EmissionValidator.java     # 排放数据校验
└── util/                          # 工具
    ├── CrcUtil.java               # CRC 校验
    ├── GpsConverter.java          # GPS 坐标转换
    └── HexUtil.java               # 十六进制工具

src/test/java/com/gb1239/sdk/
├── codec/PacketCodecTest.java
├── model/DataModelTest.java
├── validator/EmissionValidatorTest.java
└── util/CrcUtilTest.java
```

## 外部依赖

| 类型 | 选型 | 用途 |
|---|---|---|
| 外部库 | 无 | 纯Java标准库实现 |
| JSON | Jackson (optional scope) | 仅测试或可选序列化 |
| 日志 | SLF4J API | compileOnly，不绑定实现 |

## 后续阶段

- Phase 4: 核心代码开发
- Phase 5: 审查优化
- Phase 6: 文档输出（JavaDoc + README）
