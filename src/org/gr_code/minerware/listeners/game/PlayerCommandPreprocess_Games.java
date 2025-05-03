package org.gr_code.minerware.listeners.game;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.gr_code.minerware.MinerPlugin;
import org.gr_code.minerware.commands.subcommand.PluginCommand;
import org.gr_code.minerware.manager.type.Utils;

import java.util.List;
import java.util.UUID;

public class PlayerCommandPreprocess_Games implements Listener {
    @EventHandler
    public void onPreprocess(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        if (!Utils.isInGame(uuid)) return;
        String string = event.getMessage();
        List<String> strings = MinerPlugin.getInstance().getOptions().getStringList("blocked-commands");
        if (strings.isEmpty()) return;
        if (!strings.contains(string.split(" ")[0].replace("/", ""))) return;
        if (player.hasPermission("minerware.admin")) return;
        player.sendMessage(PluginCommand.Language.IN_GAME.getString());
        event.setCancelled(true);
    }
}
