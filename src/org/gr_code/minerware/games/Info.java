package org.gr_code.minerware.games;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.gr_code.minerware.MinerPlugin;
import org.gr_code.minerware.arena.Arena;
import org.gr_code.minerware.arena.GamePlayer;
import org.gr_code.minerware.builders.ItemBuilder;
import org.gr_code.minerware.manager.type.resources.XMaterial;

import java.util.List;

import static java.util.Objects.requireNonNull;
import static org.gr_code.minerware.cuboid.Cuboid.getRandomLocation;
import static org.gr_code.minerware.manager.type.Utils.*;

public class Info extends MicroGame {

    FileConfiguration fileConfiguration = MinerPlugin.getInstance().getMessages();

    @Override
    public void startGame() {
        String mode = getArena().isHardMode() ? fileConfiguration.getString("messages.arena.info-miner-ware.hard-mode")
                : fileConfiguration.getString("messages.arena.info-miner-ware.normal-mode");
        assert mode != null;
        List<String> message = fileConfiguration.getStringList("messages.arena.info-miner-ware.info-message");
        String title = translate(fileConfiguration.getString("messages.arena.info-miner-ware.first-title"));
        String subtitle = translate(fileConfiguration.getString("messages.arena.info-miner-ware.first-subtitle"));
        getArena().getPlayers().forEach(gamePlayer -> {
            Player player = gamePlayer.getPlayer();
            sendTitle(player, title, subtitle, 10, 70, 20);
			message.forEach(msg -> sendMessage(player, translate(msg.replace("<mode>", mode))));
			clearInventory(player);
        });
        super.startGame();
    }

    @Override
    public void secondStartGame() {
        String title = translate(fileConfiguration.getString("messages.arena.info-miner-ware.second-title"));
        String subtitle = translate(fileConfiguration.getString("messages.arena.info-miner-ware.second-subtitle"));
    	getArena().getPlayers().forEach(gamePlayer -> {
    		Player player = gamePlayer.getPlayer();
			sendTitle(player, title, subtitle, 10, 70, 20);
    	});
    }

    @Override
    public void check() {
        if (getTime() % 5 != 0) return;
    	getArena().getPlayers().forEach(x->{
    		Player player = x.getPlayer();
    		int y = player.getLocation().getBlockY();
        	int param_y = getArena().getProperties().getFirstLocation().getBlockY() - 1 ;
        	if (y <= param_y) player.teleport(getRandomLocation(getArena()));
    	});
    }

    @Override
    public void end() {
        getArena().setStage(Arena.Stage.NEW_GAME_STARTING);
    }

    @Override
    public Game getGame() {
        return Game.INFO;
    }

    @Override
    public String getTask(GamePlayer gamePlayer) {
        return "";
    }

    @Override
    public String getName() {
        return "";
    }

    public Info(Arena arena) {
        super(180, arena, "info");
    }

    @Override
	public ItemStack getGameItemStack() {
    	return ItemBuilder.start(requireNonNull(XMaterial.BOOK.parseItem())).setDisplayName(ChatColor.GOLD + "Info MinerWare").build();
	}

}