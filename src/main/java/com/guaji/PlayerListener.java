package com.guaji;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * 玩家事件监听
 * 玩家退出服务器时清理其倒计时数据（下次上线重新计满）
 */
public class PlayerListener implements Listener {

    private final GuajiManager manager;

    public PlayerListener(GuajiManager manager) {
        this.manager = manager;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        manager.clear(e.getPlayer().getUniqueId());
    }
}
