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

public class RocketWinEffect extends WinEffect {

    private final Arena arena;

    private int time;

    public RocketWinEffect(Arena arena, int time) {
        super(arena, time);
        this.time = super.getTime();
        this.arena = arena;
    }

    @Override
    public void update() {
        if (time == super.getTime())
            start();
        doTask();
    }

    @Override
    public void start() {
        Properties properties = arena.getProperties();
        Cuboid cuboid = properties.getCuboid();
        var modernAPI = ManageHandler.getModernAPI();
        properties.destroySquares();
        cuboid.getLocations().stream()
                .filter(location -> location.getBlockY() == properties.getFirstLocation().getBlockY())
                .forEachOrdered(location -> modernAPI
                        .setBlock(Objects.requireNonNull(XMaterial.GRASS_BLOCK.parseItem()), location.getBlock()));
    }

    @Override
    public void stop() {
        Properties properties = arena.getProperties();
        properties.restoreSquares();
        properties.restoreCuboid();
        for (GamePlayer gamePlayer : arena.getPlayers()) {
            org.bukkit.entity.Player player = Bukkit.getPlayer(gamePlayer.getUUID());
            ManageHandler.getModernAPI().sendRestorePackets(player, arena);
            assert player != null;
            player.setFlying(false);
            player.setAllowFlight(true);
        }
    }

    @Override
    public ItemStack getItemStack() {
        return ItemBuilder.start(Objects.requireNonNull(XMaterial.FEATHER.parseItem())).setDisplayName("&aRocket")
                .build();
    }

    public void doTask() {
        var modernAPI = ManageHandler.getModernAPI();
        if (time == 0) {
            stop();
            return;
        }
        for (GamePlayer player : arena.getPlayers())
            modernAPI.updateRocketWinEffect(Bukkit.getPlayer(player.getUUID()), super.getTime() - time);
        time--;
    }

    @Override
    public Type getType() {
        return Type.ROCKET;
    }

}
