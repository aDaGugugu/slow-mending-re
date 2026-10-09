# Slow Mending Re

控制经验修补成功概率、限制装备修补次数，并支持经验修补拓展卡。原作者：**super_boy_520**；本仓库基于 [aDaGugugu/slow-mending-re](https://github.com/aDaGugugu/slow-mending-re) 维护。

![mendingitem](img/mendingitem.png)

## 2.2.3 / Minecraft 1.20–26.3

- 同一个 JAR 面向 **Paper 1.20–26.3**，基于范围内最早的 `paper-api:1.20-R0.1-SNAPSHOT` 编译，不使用 NMS 或 CraftBukkit 内部类。
- 插件使用 **Java 17 字节码**，可在 Java 17 / 21 / 25 上加载；服务端本身的 Java 要求仍须满足。此版本使用 Paper 的 Adventure 物品文本 API，不声明纯 Spigot、CraftBukkit 或 Folia 兼容性。
- 游戏内直接显示修补模式，设置命令支持“无限”“无限不减速”“禁用”，无需记忆特殊数字。
- 保留 `slow_mending_re` 插件名称、原配置目录、原数字命令及内部次数含义。
- 次数及新拓展卡使用 PDC 存储。兼容旧 Lore 次数、旧前缀及完整的旧版拓展卡。
- 修复空手交互、双手重复触发、首次自定义 Lore 物品重复扣次、发卡覆盖物品、异常配置及整数溢出。
- 移除启停时阻塞主线程的外部诗词请求；缓存拓展卡配置，交互时不再读磁盘。

## 安装 / 升级

1. 停服，将 `plugins` 内旧版插件 JAR 替换为 `slow-mending-re-2.2.3.jar`，保留原来的 `plugins/slow_mending_re/` 配置目录。
2. 按服务端版本选择 Java：1.20–1.20.4 使用 Java 17；1.20.5–1.21.11 使用 Java 21；26.x 使用 Java 25。
3. 配置缺少新增选项时会使用默认值，已有配置文件不会被覆盖。

旧装备在下次有效修补尝试或通过命令/拓展卡修改时，自动将数字模式改为文字，并迁移至 PDC；已有 PDC 数值保持不变。仅查询 `info` 会直接显示文字状态，不修改物品。尚未迁移的旧物品依赖 Lore 前缀识别；若要修改前缀，请把旧前缀加入 `Old_Mend_Frequency_Lore_Name`。迁移后的物品会记住显示前缀，再修改配置不会重置次数。

新版本补充次数会恢复由本版本添加破损前缀前的名称，保留丰富文本；若玩家之后在铁砧改名，则保留玩家的新名称。旧版本已经改名的物品未保存原名称，无法可靠自动还原。

下载：[GitHub Actions 构建产物](https://github.com/Junnstoy/slow-mending-re/actions/workflows/build.yml)。从成功构建的 Artifacts 下载插件 JAR。

## 版本兼容说明

| 服务端范围 | 测试运行时 | 同一插件 JAR |
| --- | --- | --- |
| Paper 1.20–1.20.4 | Java 17 | 支持 |
| Paper 1.20.5–1.21.11 | Java 21 | 支持 |
| Paper 26.1.x–26.3 | Java 25 | 支持 |

`api-version: '1.20'` 表示插件使用的最低 API，不是把新服务端降级为 1.20。Java 17 字节码也不代表新服务端能用 Java 17 启动。Paper 兼容分支可按其自身的 API/Java 要求使用，完整实测范围以 [TESTING.md](TESTING.md) 为准。

2.2.0 曾使用 26.3 API 和 Java 25，不能用于旧服；需要跨版本支持请更换为 2.2.3。PDC 键及配置路径没有变化，可以直接保留 2.2.0 / 2.2.1 / 2.2.2 的插件数据。插件支持多个版本并不意味着 Minecraft 世界或物品格式支持降级。

## 配置与计数规则

沿用原有配置路径：

```yaml
Setting:
  AHI_Mend: true
  Slow_Mend:
    Enable: true
    Mitigation_Factor: 5
  Max_Mend_Limit:
    Enable: true
    Max_Number: 1000
    Count_Successful_Only: false
    Mend_Frequency_Lore_Name: '§9剩余修补次数：'
    Old_Mend_Frequency_Lore_Name: []
```

`Mitigation_Factor` 必须 >= 1。修补放行概率为 `1 / Mitigation_Factor`；失败时取消修补事件，经验如何进入经验条由服务端处理，**不是强制额外扣除经验**。

`Count_Successful_Only: false` 保留旧版按修补尝试扣次的规则：即使随机未放行，也扣 1 次。设为 `true` 后，仅本插件放行的修补扣 1 次。已被更早事件监听器取消、修补量为 0、没有损伤的物品不会扣次；同优先级后执行的插件仍可能改变最终事件结果。

| 游戏内显示 | `set` 可输入 | 配置 / 旧数字命令 |
| --- | --- | --- |
| 正整数 | 如 `1000` | 相同整数 |
| 已耗尽（无法修补） | `0` | `0` |
| 无限次数（遵循减速设置） | `无限` 或 `unlimited` | `-1` |
| 无限次数（不受减速限制） | `无限不减速` 或 `unlimited-fast` | `-2` |
| 禁止经验修补 | `禁用` 或 `disabled` | `-3` |

物品 Lore、查询提示和新设置卡使用以上文字；`add` 模式的卡片显示“增加 5 次”或“减少 1 次”。旧数字仍可输入，配置文件和 PDC 仍保存原来的整数。英文模式名称不区分大小写。

关闭 `Max_Mend_Limit.Enable` 会关闭全部按物品次数的规则，包括以上特殊值。关闭 `AHI_Mend` 会禁止修补；显式持有 `slowmending.bypass` 权限的玩家可绕过所有限制。

损坏的计数数据会阻止该物品修补，不会自动发放一份新次数。管理员可以用 `set` 修正。`add` 的负数扣减最低到 0，不会隐式进入无限次数状态；特殊状态必须用 `set` 修改，超过整数上限会被拒绝。

## 命令与权限

`/slowmending` 的别名是 `/slmend`。以下管理命令需要 `slowmending.command`（默认 OP）。

| 命令 | 功能 |
| --- | --- |
| `/slmend info <player> [main\|off]` | 查看物品次数；无记录时显示配置初始值 |
| `/slmend set <player> <次数或模式> [main\|off]` | 设置次数或模式，可初始化尚未修补的耐久物品 |
| `/slmend add <player> <num> [main\|off]` | 增加或扣减次数 |
| `/slmend givecard <set\|add> <player> <quantity> <frequency>` | 先选择模式，再向目标背包发放拓展卡；控制台可用 |
| `/slmend reload` | 验证并重载配置；失败时保持旧的有效设置 |
| `/slmend help`、`/slmend version` | 帮助及真实构建版本 |

`slowmending.bypass` 默认 **false**（包括 OP），仅在明确赋予后生效。

例如：`/slmend set Steve 无限`、`/slmend set Steve 无限不减速 off`、`/slmend set Steve 禁用`。发放模式卡可用 `/slmend givecard set Steve 1 无限不减速`；发放增加 100 次的卡可用 `/slmend givecard add Steve 1 100`。Tab 补全依次提示模式、在线玩家、数量和对应模式的数值；`add` 只接受增减整数，例如 `/slmend add Steve -1` 表示扣除一次。

旧发卡写法 `/slmend givecard Steve 1 无限不减速 set` 仍可执行，方便保留已有脚本；帮助和 Tab 补全以模式在前的新写法为准。

发卡数量为 1–2304，并受实际背包空间限制；按物品最大堆叠量拆分。空间不足时整次拒绝，不覆盖主手，也不丢弃到地面。

## 拓展卡

主手持卡，副手放置带有经验修补附魔的耐久装备，右键使用。成功更改后才消耗一张；目标无效、次数没有变化、超出上限或溢出时不扣卡。

`ExpansionCard/cardconfig.yml` 保留 `Enable`、`AllowBeyond`、`AllowSetSP`，增加：

```yaml
ExpansionCard:
  AcceptLegacyCards: true
```

默认兼容旧卡，使用完旧卡后可关闭此选项，只接受带 PDC 的新卡。旧卡依赖当前 `cardinfo.yml` 的原模式文本和次数前缀解析；更改这些文案前应先用完旧卡。新卡不依赖 Lore 文案。`AllowSetSP` 只允许 `set` 模式主动设置特殊值；`add` 不会改变特殊状态。被其他插件明确禁止物品使用的交互不会消耗卡。

升级不会批量改写背包中已有卡片的 Lore；旧卡仍能使用，新发放的卡片采用文字说明。

## 构建与验证

```sh
mvn --batch-mode clean verify
```

产物：`target/slow-mending-re-2.2.3.jar`。测试说明见 [TESTING.md](TESTING.md)，更新记录见 [CHANGELOG.md](CHANGELOG.md)。
