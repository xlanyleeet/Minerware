package org.gr_code.minerware.listeners.sign;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.gr_code.minerware.MinerPlugin;
import org.gr_code.minerware.arena.Arena;
import org.gr_code.minerware.commands.subcommand.PluginCommand;
import org.gr_code.minerware.manager.type.ServerManager;
import org.gr_code.minerware.manager.type.SignManager;
import org.gr_code.minerware.manager.type.Utils;

import java.util.Objects;

public class SignChange_Sign implements Listener {

    private static final FileConfiguration fileConfiguration = MinerPlugin.getInstance().getOptions();;

    @EventHandler
    public void onChange(SignChangeEvent event) {
        if (!(Objects.requireNonNull(event.getLine(0)).equalsIgnoreCase("[MinerWare]")))
            return;
        Player player = event.getPlayer();
        if(!event.getPlayer().hasPermission("minerware.admin")){
            player.sendMessage(PluginCommand.Language.NO_PERMISSIONS.getString());
            event.getBlock().breakNaturally();
            return;
        }
        if(Objects.requireNonNull(event.getLine(1)).equalsIgnoreCase("randomJoin")){
            event.setLine(0,  Utils.translate(SignManager.title));
            event.setLine(1, Utils.translate(fileConfiguration.getString("sign.random-join.line-1")));
            return;
        }
        if(ServerManager.getArena(event.getLine(1)) == null){
            player.sendMessage(PluginCommand.Language.NOT_EXIST.getString());
            event.getBlock().breakNaturally();
            return;
        }
        event.setLine(0, Utils.translate(SignManager.title));
        Arena arena = ServerManager.getArena(event.getLine(1));
        event.setLine(1, Utils.translate("&c" + event.getLine(1)));
        assert arena != null;
        event.setLine(2, arena.getStage().getSignString());
        event.setLine(3, Utils.translate("&8" + arena.getCurrentPlayers() + " / " + arena.getProperties().getMaxPlayers()));
        SignManager.addSign(event.getBlock());
    }
}


