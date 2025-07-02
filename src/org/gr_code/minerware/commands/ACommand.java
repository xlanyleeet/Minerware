package org.gr_code.minerware.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.gr_code.minerware.manager.type.TabManager;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public abstract class ACommand extends BukkitCommand {
    public ACommand(String name) {
        super(name);
    }

    @Override
    public boolean execute(@NotNull CommandSender commandSender, @NotNull String s, String[] strings) {
        onCommand(commandSender, strings);
        return true;
    }
    public abstract void onCommand(CommandSender commandSender, String[] args);

    @Override
    public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, String[] args) throws IllegalArgumentException {
        return new TabManager().onTabComplete(sender, this, args);
    }
}



