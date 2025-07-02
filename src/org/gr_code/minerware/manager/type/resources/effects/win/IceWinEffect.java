package org.gr_code.minerware.manager.type.resources.effects.win;

import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.gr_code.minerware.arena.Arena;
import org.gr_code.minerware.arena.GamePlayer;
import org.gr_code.minerware.arena.Properties;
import org.gr_code.minerware.builders.ItemBuilder;
import org.gr_code.minerware.cuboid.Cuboid;
import org.gr_code.minerware.manager.ManageHandler;
// No NMS import needed
import org.gr_code.minerware.manager.type.resources.XMaterial;
import org.gr_code.minerware.manager.type.resources.effects.WinEffect;

import java.util.Objects;

public class IceWinEffect extends WinEffect {

    private int time;

    private final Arena arena;

    public IceWinEffect(Arena arena, int time) {
        super(arena, time);
        this.time = super.getTime();
        this.arena = super.getArena();
    }

    @Override
    public void update() {
        if (time == super.getTime())
            start();
        doTask();
    }

    @Override
    public void start() {
        var modernAPI = ManageHandler.getModernAPI();
        Properties properties = arena.getProperties();
        Cuboid cuboid = properties.getCuboid();
        properties.destroySquares();
        for (GamePlayer player : arena.getPlayers()) {
            cuboid.getLocations().stream()
                    .filter(location -> location.getBlockY() == properties.getFirstLocation().getBlockY())
                    .forEach(location -> modernAPI.spawnIceWinEffect(Bukkit.getPlayer(player.getUUID()), location));
        }
    }

    public void doTask() {
        if (time == 0) {
            stop();
            return;
        }
        var modernAPI = ManageHandler.getModernAPI();
        Properties properties = arena.getProperties();
        Cuboid cuboid = properties.getCuboid();
        for (GamePlayer player : arena.getPlayers()) {
            cuboid.getLocations().stream()
                    .filter(location -> location.getBlockY() == properties.getFirstLocation().getBlockY())
                    .forEach(location -> modernAPI.updateIceWinEffect(Bukkit.getPlayer(player.getUUID()), location));
        }
        time--;
    }

    @Override
    public void stop() {
        Properties properties = arena.getProperties();
        properties.restoreSquares();
        for (GamePlayer player : arena.getPlayers()) {
            ManageHandler.getModernAPI().sendRestorePackets(Bukkit.getPlayer(player.getUUID()), arena);
        }
    }

    @Override
    public ItemStack getItemStack() {
        return ItemBuilder.start(Objects.requireNonNull(XMaterial.ICE.parseItem())).setDisplayName("&bIce").build();
    }

    @Override
    public Type getType() {
        return Type.ICE;
    }

}
