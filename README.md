# 【ZHBO】plugin_GuaJiPool_1.1.1 · 挂机池插件

> Minecraft **1.12.2**（Spigot / Paper）挂机池插件 —— 玩家站在指定区域内挂机，屏幕实时显示大字倒计时，倒计时结束自动发放可配置奖励，并带有音效、粒子等反馈。

![version](https://img.shields.io/badge/version-1.1.1-2ea44f)
![platform](https://img.shields.io/badge/platform-Spigot%20%7C%20Paper%201.12.2-orange)
![java](https://img.shields.io/badge/Java-8-blue)
[![download](https://img.shields.io/badge/%E4%B8%8B%E8%BD%BD-ZHBO__plugin__GuaJiPool__1.1.1.jar-2ea44f?logo=github)](https://github.com/zhaobuobuo114/ZHBO_plugin_GuaJiPool_1.1.1/releases/latest)

> 👤 **作者**：zhaobuobuo ｜ 📧 **联系方式**：1071576270@QQ.COM
> 🐙 **GitHub**：<https://github.com/zhaobuobuo114>

---

## 功能特性

- **区域挂机计时**：用两个对角点圈出长方体挂机区域，玩家进入后开始独立倒计时
- **大字倒计时**：Title 每秒刷新，显示剩余时间（`9分59秒` / `30秒`）
- **可配置奖励**：倒计时结束后由控制台执行任意命令（给物品、加金币、执行其他插件命令等）
- **倒计时音效**：最后 X 秒每秒一声滴答，音调随剩余时间升高
- **奖励反馈**：聊天提示 + 大字提示 + 音效 + 粒子效果，可分别关闭
- **自动热重载**：修改 `config.yml` 并保存后自动生效，无需重启或手动 reload
- **进度智能保留**：热重载时若挂机时长未变，玩家倒计时进度不丢失
- **配置容错**：音效 / 粒子名填错只在后台警告并跳过，不影响插件运行

---

## 安装步骤

1. 下载 Release 中的 `ZHBO_plugin_GuaJiPool_1.1.1.jar`
2. 关闭服务器，把 jar 放入服务端的 `plugins/` 目录
3. 启动服务器，插件会自动生成 `plugins/ZHBO_plugin_GuaJiPool_1.1.1/config.yml`
4. 给管理员添加权限 `guaji.admin`（OP 默认拥有）
5. 进入游戏，按下方「快速开始」设置挂机区域

> 建议通过重启服务器加载插件；使用 `/reload` 热加载插件可能导致部分监听器异常。

---

## 快速开始（5 步）

1. 在游戏中建好挂机池建筑（如一个封闭房间）
2. 站在区域的一个角落，执行：

   ```
   /guaji pos1
   ```

3. 走到对角位置，执行：

   ```
   /guaji pos2
   ```

   两个角点都设置后，聊天栏会提示区域已生效及具体范围。

4. 打开 `plugins/ZHBO_plugin_GuaJiPool_1.1.1/config.yml`，按需修改挂机时长与奖励命令（见下方配置说明）
5. 执行 `/guaji status` 检查区域、时长、奖励命令数量是否正确

> **坐标说明**：角点取玩家**脚下方块**的坐标（X/Y/Z）。玩家是否在区域内，按脚下方块坐标判断，因此区域的 Y 范围需要包含玩家站立时脚部的高度。
>
> **世界说明**：两个角点必须在同一世界；每次执行 `pos1` / `pos2` 时，区域世界都会更新为当前玩家所在世界。

---

## 命令说明

所有子命令均需要权限 `guaji.admin`（默认仅 OP）。

| 命令 | 作用 |
| --- | --- |
| `/guaji` | 显示命令帮助 |
| `/guaji pos1` | 以站立位置设置区域角点 1，并立即写入配置生效 |
| `/guaji pos2` | 以站立位置设置区域角点 2，并立即写入配置生效 |
| `/guaji reload` | 热重载配置；时长未变时保留玩家挂机进度 |
| `/guaji status` | 查看当前区域范围、挂机时长、奖励命令数量、自动热重载与音效状态 |

命令支持 Tab 补全。区域角点设置成功时会有提示音与聊天反馈。

---

## 配置文件详解

路径：`plugins/ZHBO_plugin_GuaJiPool_1.1.1/config.yml`

```yaml
# 挂机区域（推荐在游戏内用 /guaji pos1、/guaji pos2 设置）
region:
  world: world
  pos1: [0, 60, 0]
  pos2: [10, 80, 10]

# 每次奖励所需的挂机时长（秒）
duration: 600

# 奖励命令列表（由控制台执行，%player% 会被替换为玩家名）
rewards:
  - "give %player% diamond 1"
  - "say %player% 获得了挂机奖励！"

# 玩家离开区域时的处理：true=重置倒计时 / false=暂停保留
reset-on-leave: true

# 自动热重载：检测到 config.yml 被修改保存后自动重载
auto-reload: true

# 挂机中的大字倒计时显示
display:
  enabled: true
  title: "§6§l距离下次奖励还剩"
  subtitle: "§e%time%"

# 奖励发放时的反馈
reward:
  message: "§a✔ 你获得了挂机奖励！"
  title: "§a§l✔ 挂机奖励已发放"
  subtitle: ""
  particle: "FIREWORKS_SPARK;40"

# 音效配置
sounds:
  countdown-tick-seconds: 5
  countdown-tick: "BLOCK_NOTE_PLING;1.0;1.2"
  reward: "ENTITY_PLAYER_LEVELUP;1.0;1.0"
```

### 区域与时长

| 配置项 | 默认值 | 说明 |
| --- | --- | --- |
| `region.world` | `world` | 区域所在世界名 |
| `region.pos1` | `[0, 60, 0]` | 角点 1 的 X/Y/Z 坐标 |
| `region.pos2` | `[10, 80, 10]` | 角点 2 的 X/Y/Z 坐标，两个角点会自动归一化，顺序无关 |
| `duration` | `600` | 每次奖励所需挂机秒数，最小 1 秒 |

- 玩家进入区域后，该玩家倒计时从 `duration` 开始逐秒递减
- 倒计时结束发放奖励，然后自动重置为 `duration` 重新计时
- 修改 `duration` 会立即清空所有玩家进度

### 奖励命令

- `rewards` 中的每条命令都由**控制台**执行，因此可以调用其他插件命令
- `%player%` 会替换为当前获得奖励的玩家名
- 示例：

  ```yaml
  rewards:
    - "give %player% diamond 3"
    - "eco give %player% 500"
    - "broadcast %player% 挂机完成，获得奖励！"
  ```

- 空字符串会被自动跳过

### 离开区域与热重载

| 配置项 | 默认值 | 说明 |
| --- | --- | --- |
| `reset-on-leave` | `true` | `true`：离开区域立即清零，回来重新计时；`false`：进度暂停保留，回来继续计时 |
| `auto-reload` | `true` | 监听 `config.yml` 修改时间，保存后自动重载；关闭后需执行 `/guaji reload` |

热重载规则：`duration` 未变时保留所有正在计时的玩家进度；`duration` 变更时重置全部进度。

### 倒计时显示

| 配置项 | 默认值 | 说明 |
| --- | --- | --- |
| `display.enabled` | `true` | 是否显示大字倒计时 |
| `display.title` | `§6§l距离下次奖励还剩` | 大标题，支持 `&` 或 `§` 颜色代码 |
| `display.subtitle` | `§e%time%` | 副标题，`%time%` 替换为剩余时间；留空则只显示大标题 |

时间格式：60 秒以上显示为 `X分Y秒`，不足 60 秒显示为 `X秒`。

### 奖励反馈

| 配置项 | 默认值 | 说明 |
| --- | --- | --- |
| `reward.message` | `§a✔ 你获得了挂机奖励！` | 聊天提示，留空禁用 |
| `reward.title` | `§a§l✔ 挂机奖励已发放` | 奖励大字标题，留空禁用 |
| `reward.subtitle` | 空 | 奖励大字副标题 |
| `reward.particle` | `FIREWORKS_SPARK;40` | 粒子效果，格式 `粒子名;数量`，留空禁用 |

### 音效

| 配置项 | 默认值 | 说明 |
| --- | --- | --- |
| `sounds.countdown-tick-seconds` | `5` | 最后 X 秒播放滴答音，`0` 为禁用 |
| `sounds.countdown-tick` | `BLOCK_NOTE_PLING;1.0;1.2` | 滴答音，格式 `音效名;音量;音调`，音调会随剩余秒数自动递增 |
| `sounds.reward` | `ENTITY_PLAYER_LEVELUP;1.0;1.0` | 奖励发放音效，格式同上 |

- 音效名请使用 1.12.2 标准名称，如 `ENTITY_PLAYER_LEVELUP`、`BLOCK_NOTE_PLING`、`ENTITY_EXPERIENCE_ORB_PICKUP`、`BLOCK_ANVIL_LAND`、`ENTITY_FIREWORK_LAUNCH`
- 插件内置旧版音效名兼容，以下旧名可直接使用：

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

- 音效或粒子名填错时，后台会输出警告并跳过该项，不会中断挂机流程

---

## 常见问题

**Q：设置完两个角点后玩家挂机没有反应？**
- 确认两个角点设置在同一世界，且执行 `/guaji status` 能看到正确范围
- 确认玩家脚下方块坐标在区域范围内（Y 轴范围要覆盖玩家站立高度）
- 确认区域所在世界已正常加载

**Q：倒计时大字不显示？**
- 检查 `display.enabled` 是否为 `true`
- 若同时安装了其他会发送 Title 的插件，可能相互覆盖

**Q：倒计时结束没有发奖励？**
- 检查 `rewards` 中的命令能否在控制台直接执行成功
- 检查 `%player%` 是否写对
- 执行 `/guaji status` 确认奖励命令数量不为 0

**Q：修改配置后没有生效？**
- 确认 `auto-reload: true`，且文件已保存；仍无效时执行 `/guaji reload`
- 音效 / 粒子配置错误只会在后台警告，不会在游戏内报错

**Q：重启服务器后挂机进度没了？**
- 倒计时进度仅保存在内存中，插件卸载、服务器重启或玩家退出都会清空，这是设计行为

**Q：玩家离开区域进度怎么算？**
- `reset-on-leave: true`：清零重新计时
- `reset-on-leave: false`：暂停保留，回区域后继续

---

## 构建

需要 JDK 8 + Maven：

```bash
mvn clean package
```

构建产物：`target/ZHBO_plugin_GuaJiPool_1.1.1.jar`

---

## 作者与联系方式

- 作者：zhaobuobuo
- 邮箱：1071576270@QQ.COM
- GitHub：<https://github.com/zhaobuobuo114>
