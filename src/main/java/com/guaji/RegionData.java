package com.guaji;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;

import java.util.List;

/**
 * 区域数据类
 * 表示一个长方体挂机区域（世界 + 两个对角坐标），用于判断玩家是否在挂机池内
 */
public class RegionData {

    private final World world;
    private final int minX, minY, minZ;
    private final int maxX, maxY, maxZ;

    public RegionData(World world, int x1, int y1, int z1, int x2, int y2, int z2) {
        this.world = world;
        // 自动归一化坐标，保证 min/max 顺序正确（无论 pos1/pos2 哪个在前）
        this.minX = Math.min(x1, x2);
        this.maxX = Math.max(x1, x2);
        this.minY = Math.min(y1, y2);
        this.maxY = Math.max(y1, y2);
        this.minZ = Math.min(z1, z2);
        this.maxZ = Math.max(z1, z2);
    }

    /**
     * 从配置节点加载区域
     *
     * @return 加载成功返回区域对象，配置缺失或世界不存在返回 null
     */
    public static RegionData load(ConfigurationSection sec) {
        if (sec == null) {
            return null;
        }
        String worldName = sec.getString("world");
        List<Integer> pos1 = sec.getIntegerList("pos1");
        List<Integer> pos2 = sec.getIntegerList("pos2");
        if (worldName == null || pos1.size() < 3 || pos2.size() < 3) {
            return null;
        }
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            return null;
        }
        return new RegionData(world,
                pos1.get(0), pos1.get(1), pos1.get(2),
                pos2.get(0), pos2.get(1), pos2.get(2));
    }

    /** 区域是否有效（世界已加载） */
    public boolean isValid() {
        return world != null;
    }

    /** 判断位置是否在区域内 */
    public boolean contains(Location loc) {
        if (world == null || loc == null || loc.getWorld() == null) {
            return false;
        }
        if (!loc.getWorld().equals(world)) {
            return false;
        }
        int x = loc.getBlockX();
        int y = loc.getBlockY();
        int z = loc.getBlockZ();
        return x >= minX && x <= maxX
                && y >= minY && y <= maxY
                && z >= minZ && z <= maxZ;
    }

    public World getWorld() {
        return world;
    }

    public int getMinX() {
        return minX;
    }

    public int getMinY() {
        return minY;
    }

    public int getMinZ() {
        return minZ;
    }

    public int getMaxX() {
        return maxX;
    }

    public int getMaxY() {
        return maxY;
    }

    public int getMaxZ() {
        return maxZ;
    }
}
