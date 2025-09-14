package org.gr_code.minerware.manager.type;

import com.google.common.collect.Lists;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.gr_code.minerware.MinerPlugin;
import org.gr_code.minerware.manager.type.database.type.MySQL;

import java.util.ArrayList;
import java.util.List;

public class TabManager {

    public TabManager() {
        subCommands.addAll(subCommandsNoneOp);
    }

    public List<String> onTabComplete(CommandSender commandSender, Command command, String[] strings) {
        if (command.getName().equalsIgnoreCase("minerware")) {
            if (strings.length == 1)
                return filterList(commandSender.hasPermission("minerware.admin") ? subCommands : subCommandsNoneOp,
                        strings);
            if (strings.length == 2) {
                if (strings[0].equalsIgnoreCase("join") || (strings[0].equalsIgnoreCase("delete")
                        || strings[0].equalsIgnoreCase("forceStop")
                        || strings[0].equalsIgnoreCase("edit")
                        || strings[0].equalsIgnoreCase("forceStartArena")) &&
                        commandSender.hasPermission("minerware.admin"))
                    return filterList(getSubArenas(), strings);
                if (strings[0].equalsIgnoreCase("spawnLeaderboard") &&
                        commandSender.hasPermission("minerware.admin"))
                    return filterList(getLeaderboards(), strings);
            }
        }
        return new ArrayList<>();
    }

    protected List<String> filterList(ArrayList<String> stringArrayList, String[] args) {
        String paramString = args[args.length - 1];
        List<String> finalList = new ArrayList<>();
        for (String s : stringArrayList) {
            if (s.startsWith(paramString))
                finalList.add(s);
        }
        return finalList;
    }

    protected ArrayList<String> subCommands = Lists.newArrayList("create", "edit", "setLobby",
            "spawnStatistic", "removeStatistic", "spawnLeaderboard", "removeLeaderboard", "forceStop", "forceStart",
            "version", "openGUI", "reload", "setDisplayName");

    protected ArrayList<String> subCommandsNoneOp = Lists.newArrayList("join", "randomJoin", "leave", "help", "list");

    protected ArrayList<String> getSubArenas() {
        ArrayList<String> arenas = new ArrayList<>();
        MinerPlugin.getARENA_REGISTRY().iterator()
                .forEachRemaining(arena -> arenas.add(arena.getProperties().getName()));
        return arenas;
    }

    protected ArrayList<String> getLeaderboards() {
        ArrayList<String> paths = new ArrayList<>();
        for (MySQL.Path path : MySQL.Path.values()) {
            paths.add(path.name());
        }
        return paths;
    }

}
