package com.guaji;

import org.bukkit.plugin.java.JavaPlugin;

/**
 * GuajiPool 主类
 * 挂机池插件：玩家在指定区域内挂机，屏幕显示大字倒计时，倒计时结束发放可配置奖励
 */
public class GuajiPlugin extends JavaPlugin {

    private GuajiManager manager;

    @Override
    public void onEnable() {
        // 生成默认配置文件
        saveDefaultConfig();

        // 初始化核心管理器（解析配置、区域、奖励）
        manager = new GuajiManager(this);
        manager.reload();

        // 注册命令（带 Tab 补全）
        getCommand("guaji").setExecutor(new GuajiCommand(this, manager));

        // 注册玩家退出监听（清理倒计时数据）
        getServer().getPluginManager().registerEvents(new PlayerListener(manager), this);

        // 启动每秒一次的挂机计时任务（延迟 1 秒后开始）
        getServer().getScheduler().runTaskTimer(this, manager, 20L, 20L);

        getLogger().info("挂机池插件已启用！使用 /guaji pos1、/guaji pos2 设置挂机区域。");
    }

    @Override
    public void onDisable() {
        // 取消所有定时任务并清空倒计时数据
        getServer().getScheduler().cancelTasks(this);
        if (manager != null) {
            manager.clearAll();
        }
        getLogger().info("挂机池插件已卸载。");
    }
}
