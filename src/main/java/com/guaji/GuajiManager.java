package com.guaji;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 挂机核心管理器
 * 每秒执行一次：检查玩家是否在区域内 → 大字倒计时 → 倒计时结束发放奖励
 */
public class GuajiManager implements Runnable {

    private final GuajiPlugin plugin;

    /** 每个玩家的剩余倒计时（秒） */
    private final Map<UUID, Integer> remaining = new HashMap<UUID, Integer>();

    // ===== 配置字段 =====
    private RegionData region;                 // 挂机区域
    private int duration;                       // 每次奖励所需挂机时长（秒）
    private List<String> rewards;              // 奖励命令列表
    private boolean resetOnLeave;              // 离开区域是否重置倒计时
    private boolean displayEnabled;            // 是否显示大字倒计时
    private String displayTitle;               // 倒计时大标题
    private String displaySubtitle;            // 倒计时副标题
    private String rewardMessage;              // 奖励聊天提示
    private String rewardTitle;                // 奖励大字标题
    private String rewardSubtitle;             // 奖励大字副标题
    private SoundData rewardSound;             // 奖励发放提示音效
    private SoundData tickSound;               // 倒计时滴答音效
    private int tickSeconds;                   // 倒计时提示音触发秒数（最后 X 秒）
    private ParticleData particle;             // 奖励粒子

    /** 自动热重载：检测到 config.yml 修改后自动重载 */
    private boolean autoReload;
    private File configFile;                   // config.yml 文件引用
    private long lastModified;                 // 上次读取时的文件修改时间戳

    /** 区域未配置时只警告一次，避免每秒刷屏 */
    private boolean warned = false;

    /** 音效数据（名称;音量;音调） */
    private static class SoundData {
        final Sound type;
        final float volume;
        final float pitch;

        SoundData(Sound type, float volume, float pitch) {
            this.type = type;
            this.volume = volume;
            this.pitch = pitch;
        }
    }

    /** 粒子数据（名称;数量） */
    private static class ParticleData {
        final Particle type;
        final int count;

        ParticleData(Particle type, int count) {
            this.type = type;
            this.count = count;
        }
    }

    public GuajiManager(GuajiPlugin plugin) {
        this.plugin = plugin;
    }

    /** 从 config.yml 重新加载全部配置（支持 /guaji reload 与自动热重载） */
    public void reload() {
        int oldDuration = duration;
        FileConfiguration c = plugin.getConfig();

        // 挂机时长（至少 1 秒）
        duration = Math.max(1, c.getInt("duration", 600));

        // 奖励命令
        rewards = c.getStringList("rewards");
        if (rewards == null) {
            rewards = new ArrayList<String>();
        }

        // 离开区域处理方式
        resetOnLeave = c.getBoolean("reset-on-leave", true);

        // 自动热重载开关
        autoReload = c.getBoolean("auto-reload", true);

        // 倒计时显示
        displayEnabled = c.getBoolean("display.enabled", true);
        displayTitle = color(c.getString("display.title", "§6§l距离下次奖励还剩"));
        displaySubtitle = color(c.getString("display.subtitle", "§e%time%"));

        // 奖励反馈
        rewardMessage = color(c.getString("reward.message", "§a✔ 你获得了挂机奖励！"));
        rewardTitle = color(c.getString("reward.title", "§a§l✔ 挂机奖励已发放"));
        rewardSubtitle = color(c.getString("reward.subtitle", ""));

        // 音效 / 粒子（配置错误时后台警告并跳过，不影响正常流程）
        tickSeconds = Math.max(0, c.getInt("sounds.countdown-tick-seconds", 5));
        tickSound = parseSound(c.getString("sounds.countdown-tick", ""));
        rewardSound = parseSound(c.getString("sounds.reward", ""));
        particle = parseParticle(c.getString("reward.particle", ""));

        // 区域（可能为 null = 未配置）
        region = RegionData.load(c.getConfigurationSection("region"));

        // 热重载保留进度：时长未变时保留玩家倒计时，仅时长修改才重置
        if (duration != oldDuration) {
            remaining.clear();
            plugin.getLogger().info("挂机时长已变更（" + oldDuration + " → " + duration
                    + " 秒），所有倒计时已重置。");
        }
        warned = false;

        // 记录配置文件时间戳，供自动热重载比对
        updateFileStamp();
    }

    /** 每秒执行的核心逻辑 */
    @Override
    public void run() {
        // 自动热重载：检测配置文件是否被外部修改（编辑器保存等）
        checkAutoReload();

        // 区域未配置：只警告一次然后跳过
        if (region == null || !region.isValid()) {
            if (!warned) {
                plugin.getLogger().warning("挂机区域未配置或世界未加载！请使用 /guaji pos1、/guaji pos2 设置区域。");
                warned = true;
            }
            return;
        }

        for (Player p : Bukkit.getOnlinePlayers()) {
            UUID id = p.getUniqueId();

            if (region.contains(p.getLocation())) {
                // 在区域内：倒计时递减（首次进入初始化为满时长）
                int t = remaining.containsKey(id) ? remaining.get(id) : duration;

                if (t <= 1) {
                    // 倒计时结束：发放奖励并重置
                    giveReward(p);
                    remaining.put(id, duration);
                } else {
                    t--;
                    remaining.put(id, t);
                    playTickSound(p, t);
                    displayCountdown(p, t);
                }
            } else {
                // 不在区域内：按配置决定重置还是保留
                if (resetOnLeave) {
                    remaining.remove(id);
                }
            }
        }
    }

    /**
     * 自动热重载：每秒检测 config.yml 的修改时间戳
     * 文件被外部修改（如编辑器保存）时自动重载配置，无需执行命令或重启
     */
    private void checkAutoReload() {
        if (!autoReload || configFile == null || !configFile.exists()) {
            return;
        }
        long m = configFile.lastModified();
        if (m > lastModified) {
            plugin.reloadConfig();
            reload();
            String msg = "检测到 config.yml 被修改，已自动热重载配置。";
            plugin.getLogger().info(msg);
            // 通知在线管理员
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.hasPermission("guaji.admin")) {
                    p.sendMessage("§6[挂机池] §a" + msg);
                }
            }
        }
    }

    /** 刷新配置文件时间戳（saveConfig 写入后同步记录，避免自动热重载重复触发） */
    private void updateFileStamp() {
        configFile = new File(plugin.getDataFolder(), "config.yml");
        lastModified = configFile.exists() ? configFile.lastModified() : 0L;
    }

    /** 显示大字倒计时（Title 常驻刷新，每秒覆盖一次不闪烁） */
    private void displayCountdown(Player p, int seconds) {
        if (!displayEnabled) {
            return;
        }
        String time = formatTime(seconds);
        String sub = displaySubtitle.replace("%time%", time);
        // fadeIn=0, stay=25, fadeOut=0：保证每秒刷新时文字连续显示
        p.sendTitle(displayTitle, sub, 0, 25, 0);
    }

    /**
     * 倒计时滴答音：最后 tickSeconds 秒每秒一声，音调逐秒升高
     * 独立于大字显示开关，即使关闭大字倒计时音效仍然生效
     */
    private void playTickSound(Player p, int seconds) {
        if (tickSeconds <= 0 || tickSound == null || seconds > tickSeconds) {
            return;
        }
        // 剩余秒数越小音调越高，最后一秒最高（如 5 秒配置时音调 1.2→1.8）
        float pitch = tickSound.pitch + (tickSeconds - seconds) * 0.15f;
        p.playSound(p.getLocation(), tickSound.type, tickSound.volume, pitch);
    }

    /** 发放奖励：执行命令 + 聊天提示 + 大字提示 + 音效 + 粒子 */
    private void giveReward(Player p) {
        // 控制台执行奖励命令
        ConsoleCommandSender console = Bukkit.getConsoleSender();
        for (String cmd : rewards) {
            if (cmd == null || cmd.isEmpty()) {
                continue;
            }
            Bukkit.dispatchCommand(console, cmd.replace("%player%", p.getName()));
        }

        // 聊天提示
        if (rewardMessage != null && !rewardMessage.isEmpty()) {
            p.sendMessage(rewardMessage);
        }

        // 大字提示
        if (rewardTitle != null && !rewardTitle.isEmpty()) {
            p.sendTitle(rewardTitle, rewardSubtitle, 0, 40, 10);
        }

        // 音效
        if (rewardSound != null) {
            p.playSound(p.getLocation(), rewardSound.type, rewardSound.volume, rewardSound.pitch);
        }

        // 粒子（围绕玩家身体散开）
        if (particle != null) {
            p.spawnParticle(particle.type, p.getLocation().add(0, 1, 0),
                    particle.count, 0.5, 0.5, 0.5, 0.05);
        }
    }

    /** 格式化剩余时间：X秒 / X分X秒 */
    private String formatTime(int seconds) {
        if (seconds >= 60) {
            int m = seconds / 60;
            int s = seconds % 60;
            return s == 0 ? m + "分" : m + "分" + s + "秒";
        }
        return seconds + "秒";
    }

    /** 颜色代码转换（支持 & 符号） */
    private String color(String s) {
        if (s == null) {
            return "";
        }
        return ChatColor.translateAlternateColorCodes('&', s);
    }

    /** 解析音效配置 "音效名;音量;音调"，错误返回 null 并后台警告 */
    private SoundData parseSound(String s) {
        if (s == null || s.trim().isEmpty()) {
            return null;
        }
        try {
            String[] parts = s.split(";");
            Sound type = Sound.valueOf(resolveSoundName(parts[0].trim()));
            float vol = parts.length > 1 ? Float.parseFloat(parts[1].trim()) : 1.0f;
            float pit = parts.length > 2 ? Float.parseFloat(parts[2].trim()) : 1.0f;
            return new SoundData(type, vol, pit);
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("音效配置错误：'" + s + "' 已跳过。"
                    + "注意 1.12.2 音效名如 ENTITY_PLAYER_LEVELUP、BLOCK_NOTE_PLING。");
            return null;
        }
    }

    /**
     * 音效名解析：自动兼容 1.8 旧版音效名 → 1.12.2 标准名
     * 避免服主照旧教程填写 LEVEL_UP 等旧名时静默失效
     */
    private static String resolveSoundName(String name) {
        String n = name.toUpperCase();
        if (SOUND_ALIASES.containsKey(n)) {
            return SOUND_ALIASES.get(n);
        }
        return n;
    }

    /** 常见旧版音效名 → 1.12.2 标准名映射 */
    private static final Map<String, String> SOUND_ALIASES = new HashMap<String, String>();

    static {
        SOUND_ALIASES.put("LEVEL_UP", "ENTITY_PLAYER_LEVELUP");
        SOUND_ALIASES.put("ORB_PICKUP", "ENTITY_EXPERIENCE_ORB_PICKUP");
        SOUND_ALIASES.put("XP_ORB_PICKUP", "ENTITY_EXPERIENCE_ORB_PICKUP");
        SOUND_ALIASES.put("NOTE_PLING", "BLOCK_NOTE_PLING");
        SOUND_ALIASES.put("ANVIL_LAND", "BLOCK_ANVIL_LAND");
        SOUND_ALIASES.put("FIREWORK_LAUNCH", "ENTITY_FIREWORK_LAUNCH");
        SOUND_ALIASES.put("FIREWORK_BLAST", "ENTITY_FIREWORK_BLAST");
        SOUND_ALIASES.put("CLICK", "UI_BUTTON_CLICK");
        SOUND_ALIASES.put("LEVEL_SOUND", "ENTITY_PLAYER_LEVELUP");
    }

    /** 解析粒子配置 "粒子名;数量"，错误返回 null 并后台警告 */
    private ParticleData parseParticle(String s) {
        if (s == null || s.trim().isEmpty()) {
            return null;
        }
        try {
            String[] parts = s.split(";");
            Particle type = Particle.valueOf(parts[0].trim().toUpperCase());
            int count = parts.length > 1 ? Integer.parseInt(parts[1].trim()) : 20;
            return new ParticleData(type, Math.max(1, count));
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("粒子配置错误：'" + s + "' 已跳过，请检查粒子名/参数是否正确。");
            return null;
        }
    }

    /** 玩家退出服务器时清理倒计时数据 */
    public void clear(UUID id) {
        remaining.remove(id);
    }

    /** 插件卸载时清空全部数据 */
    public void clearAll() {
        remaining.clear();
    }

    // ===== 供命令查询状态 =====

    public RegionData getRegion() {
        return region;
    }

    public int getDuration() {
        return duration;
    }

    public List<String> getRewards() {
        return rewards;
    }

    /** 当前有倒计时进度的玩家数（热重载时进度被保留） */
    public int getTrackedCount() {
        return remaining.size();
    }

    public boolean isAutoReload() {
        return autoReload;
    }

    public int getTickSeconds() {
        return tickSeconds;
    }

    public boolean hasRewardSound() {
        return rewardSound != null;
    }

    public String formatDuration() {
        return formatTime(duration);
    }
}
