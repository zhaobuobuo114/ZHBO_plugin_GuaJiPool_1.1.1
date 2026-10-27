# 【ZHBO】plugin_GuaJiPool_1.1.1 · 挂机池插件

> Minecraft **1.12.2**（Spigot / Paper）服务端挂机池插件 —— 区域挂机计时、大字倒计时、控制台奖励命令、音效与粒子反馈，全部配置化。

![version](https://img.shields.io/badge/version-1.1.1-2ea44f)
![platform](https://img.shields.io/badge/platform-Spigot%20%7C%20Paper%201.12.2-orange)
![java](https://img.shields.io/badge/Java-8-blue)
![config](https://img.shields.io/badge/%E5%85%A8%E9%85%8D%E7%BD%AE%E5%8C%96-YAML-informational)
[![download](https://img.shields.io/badge/%E4%B8%8B%E8%BD%BD-ZHBO__plugin__GuaJiPool__1.1.1.jar-2ea44f?logo=github)](https://github.com/zhaobuobuo114/ZHBO_plugin_GuaJiPool_1.1.1/releases/latest)

> 👤 **作者**：zhaobuobuo ｜ 📧 **联系方式**：1071576270@QQ.COM
> 🐙 **GitHub**：<https://github.com/zhaobuobuo114>

---

## 目录

- [简介](#简介)
- [功能特性](#功能特性)
- [环境要求](#环境要求)
- [安装](#安装)
- [指令与权限](#指令与权限)
- [显示与反馈](#显示与反馈)
- [配置文件详解](#配置文件详解)
- [数据存储](#数据存储)
- [编译构建](#编译构建)
- [常见问题](#常见问题)
- [更新日志](#更新日志)
- [插件定制](#插件定制)

---

## 简介

`【ZHBO】plugin_GuaJiPool_1.1.1` 是一套开箱即用的区域挂机系统。管理员用 `/guaji pos1`、`/guaji pos2` 圈出挂机区域，玩家站在区域内即可开始计时：屏幕实时显示大字倒计时，倒计时结束后由控制台执行奖励命令，并播放音效与粒子反馈，随后自动进入下一轮。

挂机时长、奖励命令、提示文本、音效与粒子均可通过 `config.yml` 修改，**不需要改代码**。奖励通过控制台指令发放，因此可以接入任意物品 / 经济 / 点券 / 点卡插件。

## 功能特性

| 功能 | 说明 |
| --- | --- |
| 区域挂机计时 | 两个对角点圈出长方体区域，每位玩家独立计时，互不影响 |
| 大字倒计时 | Title 每秒刷新剩余时间，60 秒以上显示为 `X分Y秒` |
| 奖励命令 | 倒计时结束由控制台执行 `rewards` 中的命令，`%player%` 替换为玩家名 |
| 循环计奖 | 发奖后自动重置为满时长继续计时，无需玩家重新进出 |
| 离开处理 | 可配置离开区域立即清零，或暂停保留进度、返回后继续 |
| 倒计时音效 | 最后 X 秒每秒一声滴答，音调随剩余时间升高 |
| 奖励反馈 | 聊天提示 + 大字提示 + 音效 + 粒子，可分别关闭 |
| 自动热重载 | 修改 `config.yml` 保存后自动生效，无需重启或手动 reload |
| 进度保留 | 热重载时若挂机时长未变，玩家进度不丢失；时长变更才重置 |
| 配置容错 | 音效 / 粒子名填错只后台警告并跳过，不影响挂机流程 |
| 权限控制 | 所有管理命令统一由 `guaji.admin` 控制，默认仅 OP |

## 环境要求

- 服务端：**Spigot / Paper 1.12.2**（其他版本不保证兼容，音效与粒子名为 1.12.2 规范）
- Java：**8** 及以上
- 前置插件：**无**
- 可选：任意权限管理插件（用于分配 `guaji.admin` 权限）

## 安装

1. 到 [Releases](https://github.com/zhaobuobuo114/ZHBO_plugin_GuaJiPool_1.1.1/releases/latest) 下载插件本体 `ZHBO_plugin_GuaJiPool_1.1.1.jar`，放入服务端 `plugins/` 目录（也可按 [编译构建](#编译构建) 自行打包）。
2. 启动服务器，插件会自动生成 `plugins/ZHBO_plugin_GuaJiPool_1.1.1/config.yml`。
3. 按需修改 `config.yml`（挂机时长、奖励命令、音效等）。
4. 进入游戏，用 `/guaji pos1`、`/guaji pos2` 设置挂机区域，或用 `/guaji reload` 重载配置。
5. 执行 `/guaji status` 检查区域、时长与奖励命令数量。

> **升级提示**：新版本可能新增配置项。升级后请先备份并删除旧 `config.yml`，让插件重新生成，再把自定义内容迁移回去。
> **目录说明**：插件内部名为 `ZHBO_plugin_GuaJiPool_1.1.1`，配置目录固定为 `plugins/ZHBO_plugin_GuaJiPool_1.1.1/`。

## 指令与权限

主命令 `/guaji`，无别名。

| 指令 | 说明 | 权限 |
| --- | --- | --- |
| `/guaji` | 查看帮助 | `guaji.admin` |
| `/guaji pos1` | 以玩家站立位置设置区域角点 1，立即写入配置生效 | `guaji.admin` |
| `/guaji pos2` | 以玩家站立位置设置区域角点 2，立即写入配置生效 | `guaji.admin` |
| `/guaji reload` | 热重载配置，时长未变时保留挂机进度 | `guaji.admin` |
| `/guaji status` | 查看区域范围、挂机时长、奖励命令数量与音效状态 | `guaji.admin` |

| 权限节点 | 说明 | 默认 |
| --- | --- | --- |
| `guaji.admin` | 使用全部 `/guaji` 管理命令 | OP |

命令支持 Tab 补全（`pos1` / `pos2` / `reload` / `status`）。`pos1`、`pos2` 只能由游戏内玩家执行，控制台执行会提示错误。

> **坐标说明**：角点取玩家**脚下方块**的 X/Y/Z 坐标，判断玩家是否在区域内同样按脚下方块计算，因此区域 Y 范围需要包含玩家站立时脚部的高度。两个角点必须在同一世界，每次设置都会把区域世界更新为当前玩家所在世界。

## 显示与反馈

**倒计时大字（Title）**

- 玩家在区域内时每秒刷新一次，`display.title` 为大标题，`display.subtitle` 为副标题
- `%time%` 替换为剩余时间：60 秒以上显示 `X分Y秒`，不足 60 秒显示 `X秒`
- 刷新参数为 `fadeIn 0 / stay 25 / fadeOut 0`，保证每秒覆盖时不闪烁
- 可通过 `display.enabled: false` 整体关闭，倒计时音效不受影响

**奖励反馈**

- 聊天提示：`reward.message`，留空禁用
- 大字提示：`reward.title` + `reward.subtitle`，留空禁用
- 音效：`sounds.reward`，支持音量与音调
- 粒子：`reward.particle`，在玩家身体位置周围散开

**倒计时音效**

- 仅最后 `sounds.countdown-tick-seconds` 秒播放，设为 `0` 禁用
- 基础音调取自 `sounds.countdown-tick`，剩余秒数越小音调越高

## 配置文件详解

配置文件位于 `plugins/ZHBO_plugin_GuaJiPool_1.1.1/config.yml`，文本支持 `&` 或 `§` 颜色代码。

### region · 挂机区域

```yaml
region:
  world: world          # 区域所在世界名
  pos1: [0, 60, 0]      # 角点 1 的 X/Y/Z
  pos2: [10, 80, 10]    # 角点 2 的 X/Y/Z，顺序无关，自动归一化
```

- 推荐在游戏内用 `/guaji pos1`、`/guaji pos2` 站在对应位置设置
- 两个角点都配置且世界已加载时区域才生效，否则后台只警告一次
- 区域为长方体，X/Y/Z 三个方向都会判断

### 挂机与奖励

```yaml
duration: 600           # 每次奖励所需挂机时长（秒），最小 1
reset-on-leave: true    # true=离开清零 / false=暂停保留，回来继续
auto-reload: true       # 检测 config.yml 修改后自动热重载

rewards:
  - "give %player% diamond 1"
  - "say %player% 获得了挂机奖励！"
```

- `rewards` 中每条命令都由**控制台**执行，可调用其他插件命令
- `%player%` 会替换为获得奖励的玩家名，空字符串自动跳过
- 修改 `duration` 会立即清空所有玩家进度；其他配置热重载时进度保留

### display · 大字倒计时

```yaml
display:
  enabled: true
  title: "§6§l距离下次奖励还剩"
  subtitle: "§e%time%"
```

- `subtitle` 留空 `""` 则只显示大标题
- `%time%` 是唯一占位符

### reward · 奖励反馈

```yaml
reward:
  message: "§a✔ 你获得了挂机奖励！"
  title: "§a§l✔ 挂机奖励已发放"
  subtitle: ""
  particle: "FIREWORKS_SPARK;40"   # 格式：粒子名;数量，留空禁用
```

- `message` / `title` 留空即禁用对应提示
- 粒子数量会被修正为至少 1，粒子名错误时后台警告并跳过

### sounds · 音效

```yaml
sounds:
  countdown-tick-seconds: 5                     # 最后 X 秒滴答，0=禁用
  countdown-tick: "BLOCK_NOTE_PLING;1.0;1.2"    # 格式：音效名;音量;音调
  reward: "ENTITY_PLAYER_LEVELUP;1.0;1.0"
```

音效名使用 1.12.2 标准名称。插件内置旧版名称兼容，以下旧名可直接填写：

| 旧名 | 自动映射为 |
| --- | --- |
| `LEVEL_UP` | `ENTITY_PLAYER_LEVELUP` |
| `ORB_PICKUP` / `XP_ORB_PICKUP` | `ENTITY_EXPERIENCE_ORB_PICKUP` |
| `NOTE_PLING` | `BLOCK_NOTE_PLING` |
| `ANVIL_LAND` | `BLOCK_ANVIL_LAND` |
| `FIREWORK_LAUNCH` | `ENTITY_FIREWORK_LAUNCH` |
| `FIREWORK_BLAST` | `ENTITY_FIREWORK_BLAST` |
| `CLICK` | `UI_BUTTON_CLICK` |
| `LEVEL_SOUND` | `ENTITY_PLAYER_LEVELUP` |

> 音效名或粒子名配置错误时，后台输出警告并跳过该项，不会中断挂机流程。

## 数据存储

- **无数据文件**：除 `config.yml` 外，插件不生成任何数据文件
- 倒计时进度仅保存在内存中，按玩家 UUID 记录
- 玩家退出、插件卸载、服务器重启都会清空进度，下次进入重新计时
- 如需要持久化挂机进度，需要自行二次开发

## 编译构建

```bash
git clone <本仓库地址>
cd ZHBO_plugin_GuaJiPool_1.1.1
mvn clean package
```

产物：`target/ZHBO_plugin_GuaJiPool_1.1.1.jar`

构建说明：

- 使用 **Maven**，JDK 8，目标字节码 1.8
- `finalName` 直接取 `project.name`，因此产物名与插件名一致，不会重复拼接版本号
- 依赖 `spigot-api 1.12.2-R0.1-SNAPSHOT`（provided，不打进 jar），仓库指向 Aikar 聚合仓库
- 如需更换 API 版本，修改 `pom.xml` 中的 `spigot-api` 版本即可（音效与粒子名需同步适配）

## 常见问题

**Q：设置区域后玩家挂机没有反应？**
A：先执行 `/guaji status` 确认区域范围正确、世界已加载；再确认玩家脚下方块坐标在区域内，且区域 Y 范围覆盖玩家站立高度。两个角点必须在同一世界。

**Q：倒计时大字不显示？**
A：检查 `display.enabled` 是否为 `true`。若同时安装了其他会发送 Title 的插件，可能相互覆盖，可先停用其他同类插件排查。

**Q：倒计时结束没有发奖励？**
A：检查 `rewards` 中的命令能否在控制台直接执行成功，确认 `%player%` 拼写正确，并用 `/guaji status` 确认奖励命令数量不为 0。

**Q：修改配置后没有生效？**
A：确认 `auto-reload: true` 且文件已保存；仍无效时执行 `/guaji reload`。音效 / 粒子配置错误只会在后台警告，不会在游戏内报错。

**Q：重启服务器后挂机进度没了？**
A：倒计时仅保存在内存中，玩家退出、插件卸载或服务器重启都会清空，这是设计行为。

**Q：玩家离开区域进度怎么算？**
A：`reset-on-leave: true` 立即清零，回来重新计时；`reset-on-leave: false` 暂停保留，回区域后继续。

**Q：支持 1.13+ 吗？**
A：本版本面向 1.12.2 编写，音效与粒子名为 1.12.2 规范。如需其他版本适配，见下方定制服务。

## 更新日志

### v1.1.1

- 插件与仓库更名为 `【ZHBO】plugin_GuaJiPool_1.1.1`（插件内部名 `ZHBO_plugin_GuaJiPool_1.1.1`）
- 插件作者信息统一为 `zhaobuobuo`，GitHub 作者 `zhaobuobuo114`
- 新增联系方式 `1071576270@QQ.COM`（插件元数据、README、Release）
- 区域挂机计时与每人独立倒计时
- Title 大字倒计时，剩余时间每秒刷新
- 控制台奖励命令，支持 `%player%` 占位符与循环发放
- 最后 X 秒滴答音效，音调随剩余时间升高
- 奖励聊天提示 / 大字提示 / 音效 / 粒子效果
- `config.yml` 自动热重载，时长不变时保留挂机进度
- 音效名兼容 1.8 旧名，配置错误仅后台警告

## 插件定制

> **承接所有版本 Minecraft 服务端插件定制**
> 挂机 / 签到 / 抽奖 / 副本 / 经济 / GUI / 数据统计等均可定制，支持 1.7.10 ~ 最新版本。
>
> **联系方式**
> 作者：`zhaobuobuo`
> 邮箱：`1071576270@QQ.COM`
> GitHub：<https://github.com/zhaobuobuo114>

如果这个插件对你有帮助，欢迎 Star ⭐ 支持一下。
