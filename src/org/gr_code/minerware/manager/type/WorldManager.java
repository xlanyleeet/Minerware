package org.gr_code.minerware.manager.type;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Player;
import org.gr_code.minerware.cuboid.WorldGenerator;

import java.util.UUID;

public class WorldManager {

    private final World world;

    public WorldManager(World world){
        this.world = world;
    }

    private static final WorldGenerator worldGenerator = new WorldGenerator();

    public static WorldManager GenerateWorld(String paramString){
        World world = Bukkit.createWorld(new WorldCreator(paramString).generator(worldGenerator));
        assert world != null;
        world.setKeepSpawnInMemory(false);
        world.setSpawnFlags(false, true);
        world.setAutoSave(false);
        world.setSpawnLocation(100, 100, 100);
        return new WorldManager(world);
    }

    public World getWorld() {
        return world;
    }

    public void teleport(UUID uuid){
        Player player = Bukkit.getPlayer(uuid);
        assert player != null;
        if(!player.isOnline())
            return;
        player.teleport(world.getSpawnLocation());
    }
}
