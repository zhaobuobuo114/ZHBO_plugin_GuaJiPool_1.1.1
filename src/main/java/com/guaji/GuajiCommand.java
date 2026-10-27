package com.guaji;

import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * /guaji 管理命令
 * pos1 / pos2：以玩家站立位置设置区域对角点
 * reload：热重载配置
 * status：查看当前配置状态
 */
public class GuajiCommand implements TabExecutor {

    private final GuajiPlugin plugin;
    private final GuajiManager manager;

    private static final List<String> SUB_COMMANDS =
            Arrays.asList("pos1", "pos2", "reload", "status");

    public GuajiCommand(GuajiPlugin plugin, GuajiManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String sub = args[0].toLowerCase();

        // ---------- pos1 / pos2：设置区域角点 ----------
        if (sub.equals("pos1") || sub.equals("pos2")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("§c该命令只能由玩家在游戏内执行（以站立位置设置角点）。");
                return true;
            }
            Player p = (Player) sender;
            setPos(p, sub);
            return true;
        }

        // ---------- reload：热重载配置 ----------
        if (sub.equals("reload")) {
            plugin.reloadConfig();
            manager.reload();
            sender.sendMessage("§a✔ 挂机池配置已热重载！");
            sender.sendMessage("§7正在计时的玩家：§e" + manager.getTrackedCount()
                    + " §7人（时长未变时进度保留）");
            return true;
        }

        // ---------- status：查看状态 ----------
        if (sub.equals("status")) {
            sendStatus(sender);
            return true;
        }

        sendHelp(sender);
        return true;
    }

    /** 设置角点并立即写入配置生效 */
    private void setPos(Player p, String sub) {
        // 先重读磁盘配置，防止覆盖编辑器中尚未触发自动热重载的修改
        plugin.reloadConfig();
        String key = "region." + sub;
        plugin.getConfig().set("region.world", p.getWorld().getName());
        plugin.getConfig().set(key, Arrays.asList(
                p.getLocation().getBlockX(),
                p.getLocation().getBlockY(),
                p.getLocation().getBlockZ()));
        plugin.saveConfig();
        manager.reload();

        // 操作反馈：音效 + 提示
        p.playSound(p.getLocation(), Sound.BLOCK_NOTE_PLING, 1.0f, 1.6f);
        p.sendMessage("§a✔ 挂机区域角点 " + sub.toUpperCase() + " 已设置为：§e"
                + p.getLocation().getBlockX() + ", "
                + p.getLocation().getBlockY() + ", "
                + p.getLocation().getBlockZ()
                + " §a（世界：" + p.getWorld().getName() + "）");

        RegionData region = manager.getRegion();
        if (region == null) {
            p.sendMessage("§7提示：请继续设置另一个角点，两个角点都设置后区域才会生效。");
        } else {
            p.sendMessage("§a区域已生效！范围：§e("
                    + region.getMinX() + ", " + region.getMinY() + ", " + region.getMinZ()
                    + ") §a→ §e("
                    + region.getMaxX() + ", " + region.getMaxY() + ", " + region.getMaxZ() + ")");
        }
    }

    /** 发送状态信息 */
    private void sendStatus(CommandSender sender) {
        sender.sendMessage("§6§l===== 挂机池状态 =====");
        RegionData region = manager.getRegion();
        if (region == null) {
            sender.sendMessage("§c区域：未配置（用 /guaji pos1、/guaji pos2 设置）");
        } else {
            sender.sendMessage("§e世界：" + region.getWorld().getName());
            sender.sendMessage("§e范围：(" + region.getMinX() + ", " + region.getMinY() + ", " + region.getMinZ()
                    + ") → (" + region.getMaxX() + ", " + region.getMaxY() + ", " + region.getMaxZ() + ")");
        }
        sender.sendMessage("§e挂机时长：" + manager.getDuration() + " 秒（" + manager.formatDuration() + "）");
        sender.sendMessage("§e奖励命令：§a" + manager.getRewards().size() + " §e条");
        sender.sendMessage("§e自动热重载：" + (manager.isAutoReload() ? "§a开启" : "§c关闭"));
        sender.sendMessage("§e倒计时音效：" + manager.getTickSeconds() + " 秒内滴答"
                + " §7| §e奖励音效：" + (manager.hasRewardSound() ? "§a已配置" : "§c关闭"));
    }

    /** 发送帮助 */
    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§6§l===== 挂机池命令 =====");
        sender.sendMessage("§e/guaji pos1 §7- 设置区域角点1（以站立位置）");
        sender.sendMessage("§e/guaji pos2 §7- 设置区域角点2（以站立位置）");
        sender.sendMessage("§e/guaji reload §7- 热重载配置（保留挂机进度）");
        sender.sendMessage("§e/guaji status §7- 查看当前配置状态");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        if (args.length == 1) {
            List<String> result = new ArrayList<String>();
            for (String s : SUB_COMMANDS) {
                if (s.startsWith(args[0].toLowerCase())) {
                    result.add(s);
                }
            }
            return result;
        }
        return new ArrayList<String>();
    }
}
