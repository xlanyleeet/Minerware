package org.gr_code.minerware.listeners.setup;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.gr_code.minerware.MinerPlugin;
import org.gr_code.minerware.arena.Properties;
import org.gr_code.minerware.builders.InventoryBuilder;
import org.gr_code.minerware.commands.subcommand.PluginCommand;
import org.gr_code.minerware.games.Game;
import org.gr_code.minerware.manager.type.Utils;
import org.gr_code.minerware.manager.type.resources.XMaterial;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

public class InventoryClick_Setup implements Listener {
    @EventHandler
    public void onClick(InventoryClickEvent inventoryClickEvent) {
        HumanEntity humanEntity = inventoryClickEvent.getWhoClicked();
        UUID uuid = humanEntity.getUniqueId();
        if (!Utils.isInSession(uuid))
            return;
        if (inventoryClickEvent.getCurrentItem() != null &&
                inventoryClickEvent.getCurrentItem().hasItemMeta() &&
                Objects.requireNonNull(inventoryClickEvent.getCurrentItem().getItemMeta()).hasDisplayName()
                && inventoryClickEvent.getCurrentItem().getItemMeta().getDisplayName()
                        .equals(Objects.requireNonNull(Utils.CLOSED_MENU.getItemMeta()).getDisplayName())) {
            inventoryClickEvent.setCancelled(true);
            return;
        }
        if (inventoryClickEvent.getClickedInventory() == null)
            return;
        if (Objects.requireNonNull(inventoryClickEvent.getClickedInventory()).getHolder() != null)
            return;
        Properties properties = PluginCommand.getArenaHashMap().get(uuid);
        switch (inventoryClickEvent.getAction()) {
            case MOVE_TO_OTHER_INVENTORY:
            case HOTBAR_MOVE_AND_READD:
            case HOTBAR_SWAP:
                inventoryClickEvent.setCancelled(true);
                return;
        }
        ItemStack itemStack = inventoryClickEvent.getCurrentItem();
        if (itemStack == null)
            return;
        if (properties.closed)
            properties.closed = false;
        inventoryClickEvent.setCancelled(true);
        switch (properties.getTask()) {
            case "OPENED DEFAULT":
                switch (inventoryClickEvent.getSlot()) {
                    case 14:
                        if (properties.getCuboid() == null) {
                            humanEntity.sendMessage(PluginCommand.Language.CUBOID_NOT_SELECTED.getString());
                            properties.setTask("OPENED DEFAULT");
                            humanEntity.closeInventory();
                            return;
                        }
                        String string = inventoryClickEvent.getClick().toString();
                        if (string.startsWith("RIGHT")) {
                            properties.restoreSquares();
                            return;
                        }
                        if (string.startsWith("LEFT")) {
                            properties.saveSquares();
                            return;
                        }
                        return;
                    case 12:
                        if (properties.getCuboid() == null) {
                            humanEntity.sendMessage(PluginCommand.Language.CUBOID_NOT_SELECTED.getString());
                            properties.setTask("OPENED DEFAULT");
                            humanEntity.closeInventory();
                            return;
                        }
                        string = inventoryClickEvent.getClick().toString();
                        if (string.startsWith("RIGHT")) {
                            properties.restoreCuboid();
                            return;
                        }
                        if (string.startsWith("LEFT")) {
                            properties.saveCuboid();
                            return;
                        }
                        return;
                    case 10:
                        properties.setTask("OPENED LOCATIONS");
                        humanEntity.openInventory(properties.getLocationsGUI());
                        return;
                    case 28:
                        properties.setTask("OPENED SETTINGS");
                        humanEntity.openInventory(properties.getSettingsGUI());
                        return;
                    case 16:
                        if (properties.getType() == null) {
                            humanEntity.sendMessage(PluginCommand.Language.TYPE_SELECT.getString());
                            properties.setTask("OPENED DEFAULT");
                            humanEntity.closeInventory();
                            return;
                        }
                        properties.setTask("OPENED PLAYERS");
                        humanEntity.openInventory(InventoryBuilder.generatePlayerInventory(properties.getMaxPlayers()));
                        return;
                    case 30:
                        if (!properties.canFinish()) {
                            humanEntity.sendMessage(PluginCommand.Language.NOT_READY.getString());
                            properties.setTask("OPENED DEFAULT");
                            humanEntity.closeInventory();
                            return;
                        }
                        Properties.finish(properties);
                        humanEntity.closeInventory();
                        PluginCommand.getArenaHashMap().remove(humanEntity.getUniqueId());
                        humanEntity.getInventory().setItem(8, null);
                        Utils.teleportToLobby((Player) humanEntity);
                        humanEntity.sendMessage(PluginCommand.Language.SAVED.getString().replace("<arena_name>",
                                properties.getDisplayName()));
                        return;
                    case 32:
                        properties.setTask("OPENED REMOVE");
                        humanEntity.openInventory(Properties.REMOVE);
                }
                return;
            case "OPENED SETTINGS":
                switch (inventoryClickEvent.getSlot()) {
                    case 26:
                        properties.setTask("OPENED DEFAULT");
                        humanEntity.openInventory(properties.getEditGUI());
                        return;
                    case 15:
                        if (properties.getCuboid() != null) {
                            humanEntity.sendMessage(PluginCommand.Language.CUBOID_FAIL.getString());
                            properties.setTask("OPENED DEFAULT");
                            humanEntity.closeInventory();
                            return;
                        }
                        properties.setTask("OPENED TYPE");
                        humanEntity.openInventory(Properties.TYPE);
                        return;
                    case 11:
                        properties.setTask("OPENED GAMES");
                        humanEntity.openInventory(properties.getPaginatedGames().get(1));
                        return;

                }
                return;
            case "OPENED TYPE":
                if (inventoryClickEvent.getCurrentItem() == null || inventoryClickEvent.getCurrentItem()
                        .getType() == XMaterial.GRAY_STAINED_GLASS_PANE.parseMaterial())
                    return;
                if (inventoryClickEvent.getSlot() == 26) {
                    properties.setTask("OPENED SETTINGS");
                    humanEntity.openInventory(properties.getSettingsGUI());
                    return;
                }
                String type = ChatColor.stripColor(
                        Objects.requireNonNull(Objects.requireNonNull(itemStack).getItemMeta()).getDisplayName());
                if (properties.getType() == null || !properties.getType().equals(type)) {
                    properties.setMinPlayers(0);
                }
                properties.setType(type);
                properties.setTask("OPENED SETTINGS");
                humanEntity.openInventory(properties.getSettingsGUI());
                return;
            case "OPENED LOCATIONS":
                switch (inventoryClickEvent.getSlot()) {
                    case 10:
                        String string = inventoryClickEvent.getClick().toString();
                        if (string.startsWith("RIGHT")) {
                            Location location = properties.getFirstLocation();
                            if (location == null) {
                                humanEntity.sendMessage(PluginCommand.Language.LOCATION_NULL.getString());
                                properties.setTask("OPENED DEFAULT");
                                humanEntity.closeInventory();
                                return;
                            }
                            humanEntity.teleport(location);
                            humanEntity.sendMessage(PluginCommand.Language.TELEPORTED.getString());
                            properties.setTask("OPENED DEFAULT");
                            return;
                        }
                        if (string.startsWith("LEFT")) {
                            if (properties.getCuboid() != null) {
                                humanEntity.sendMessage(PluginCommand.Language.CUBOID_FAIL.getString());
                                properties.setTask("OPENED DEFAULT");
                                humanEntity.closeInventory();
                                return;
                            }
                            properties.setBaseYaw(humanEntity.getLocation().clone().getYaw());
                            properties.setFirstLocation(humanEntity.getLocation().getBlock().getLocation().clone());
                            humanEntity.sendMessage(PluginCommand.Language.LOCATION_SET.getString()
                                    .replace("<location>", locationToMessage(humanEntity.getLocation())));
                            return;
                        }
                        return;
                    case 26:
                        properties.setTask("OPENED DEFAULT");
                        humanEntity.openInventory(properties.getEditGUI());
                        return;
                    case 12:
                        string = inventoryClickEvent.getClick().toString();
                        if (string.startsWith("RIGHT")) {
                            Location location = properties.getLobbyLocationWinner();
                            if (location == null) {
                                humanEntity.sendMessage(PluginCommand.Language.LOCATION_NULL.getString());
                                properties.setTask("OPENED DEFAULT");
                                humanEntity.closeInventory();
                                return;
                            }
                            humanEntity.teleport(location);
                            humanEntity.sendMessage(PluginCommand.Language.TELEPORTED.getString());
                            properties.setTask("OPENED DEFAULT");
                            return;
                        }
                        if (string.startsWith("LEFT")) {
                            if (properties.getCuboid() == null) {
                                humanEntity.sendMessage(PluginCommand.Language.CUBOID_NOT_SELECTED.getString());
                                properties.setTask("OPENED DEFAULT");
                                humanEntity.closeInventory();
                                return;
                            }
                            if (properties.getCuboid().notInside(humanEntity.getLocation().clone().add(0, -1, 0))) {
                                humanEntity.closeInventory();
                                properties.setTask("OPENED DEFAULT");
                                properties.setLobbyLocationWinner(humanEntity.getLocation().clone(), true);
                                humanEntity.sendMessage(PluginCommand.Language.LOCATION_SET.getString()
                                        .replace("<location>", locationToMessage(humanEntity.getLocation())));
                                return;
                            }
                            humanEntity.sendMessage(PluginCommand.Language.INSIDE_CUBOID.getString());
                            properties.setTask("OPENED DEFAULT");
                            humanEntity.closeInventory();
                            return;
                        }
                        return;
                    case 14:
                        string = inventoryClickEvent.getClick().toString();
                        if (string.startsWith("RIGHT")) {
                            Location location = properties.getLobbyLocationLoser();
                            if (location == null) {
                                humanEntity.sendMessage(PluginCommand.Language.LOCATION_NULL.getString());
                                properties.setTask("OPENED DEFAULT");
                                humanEntity.closeInventory();
                                return;
                            }
                            humanEntity.teleport(location);
                            humanEntity.sendMessage(PluginCommand.Language.TELEPORTED.getString());
                            properties.setTask("OPENED DEFAULT");
                            return;
                        }
                        if (string.startsWith("LEFT")) {
                            if (properties.getCuboid() == null) {
                                humanEntity.sendMessage(PluginCommand.Language.CUBOID_NOT_SELECTED.getString());
                                properties.setTask("OPENED DEFAULT");
                                humanEntity.closeInventory();
                                return;
                            }
                            if (properties.getCuboid().notInside(humanEntity.getLocation().clone().add(0, -1, 0))) {
                                humanEntity.closeInventory();
                                properties.setTask("OPENED DEFAULT");
                                properties.setLobbyLocationLoser(humanEntity.getLocation().clone(), true);
                                humanEntity.sendMessage(PluginCommand.Language.LOCATION_SET.getString()
                                        .replace("<location>", locationToMessage(humanEntity.getLocation())));
                                return;
                            }
                            humanEntity.sendMessage(PluginCommand.Language.INSIDE_CUBOID.getString());
                            properties.setTask("OPENED DEFAULT");
                            humanEntity.closeInventory();
                            return;
                        }
                        return;
                    case 16:
                        string = inventoryClickEvent.getClick().toString();
                        boolean bool = properties.getFirstLocation() != null && properties.getType() != null;
                        if (string.startsWith("LEFT")) {
                            if (bool) {
                                if (properties.getCuboid() != null) {
                                    humanEntity.sendMessage(PluginCommand.Language.CUBOID_FAIL.getString());
                                    properties.setTask("OPENED DEFAULT");
                                    humanEntity.closeInventory();
                                    return;
                                }
                                properties.setup(true);
                                properties.saveSquares();
                                properties.saveCuboid();
                                return;
                            }
                            humanEntity.sendMessage(PluginCommand.Language.LOCATION_AND_TYPE.getString());
                            properties.setTask("OPENED DEFAULT");
                            humanEntity.closeInventory();
                            return;
                        }
                        if (string.startsWith("RIGHT")) {
                            if (properties.getCuboid() == null) {
                                humanEntity.sendMessage(PluginCommand.Language.CUBOID_NOT_SELECTED.getString());
                                properties.setTask("OPENED DEFAULT");
                                humanEntity.closeInventory();
                                return;
                            }
                            properties.destroyCuboid();
                            return;
                        }
                        return;
                }
                return;
            case "OPENED PLAYERS":
                if (inventoryClickEvent.getSlot() == 26) {
                    properties.setTask("OPENED DEFAULT");
                    humanEntity.openInventory(properties.getEditGUI());
                    return;
                }
                if (!itemStack.getType().equals(XMaterial.PLAYER_HEAD.parseMaterial()))
                    return;
                properties.setMinPlayers(itemStack.getAmount());
                properties.setTask("OPENED DEFAULT");
                humanEntity.openInventory(properties.getEditGUI());
                return;
            case "OPENED REMOVE":
                if (inventoryClickEvent.getSlot() == 15) {
                    properties.setTask("OPENED DEFAULT");
                    humanEntity.openInventory(properties.getEditGUI());
                    return;
                }
                if (inventoryClickEvent.getSlot() == 11) {
                    MinerPlugin minerPlugin = MinerPlugin.getInstance();
                    FileConfiguration fileConfiguration = minerPlugin.getArenas();
                    File file = minerPlugin.getArenasFile();
                    humanEntity.closeInventory();
                    humanEntity.sendMessage(PluginCommand.Language.DELETED.getString());
                    fileConfiguration.set("arenas." + properties.getName(), null);
                    try {
                        fileConfiguration.save(file);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Utils.deleteWorld(properties.getName());
                    PluginCommand.getArenaHashMap().remove(humanEntity.getUniqueId());
                    humanEntity.getInventory().setItem(8, null);
                    return;
                }
            case "OPENED GAMES":
                int index = properties.getPaginatedGames().indexOf(inventoryClickEvent.getClickedInventory());
                if (inventoryClickEvent.getSlot() == 49) {
                    properties.setTask("OPENED BOSS GAMES");
                    humanEntity.openInventory(properties.getPaginatedGames().get(0));
                    return;
                }
                if (inventoryClickEvent.getSlot() == 46) {
                    itemStack = inventoryClickEvent.getCurrentItem();
                    if (Objects.requireNonNull(itemStack.getItemMeta()).getDisplayName().contains("Previous")) {
                        humanEntity.openInventory(properties.getPaginatedGames().get(index - 1));
                        return;
                    }
                    properties.setTask("OPENED SETTINGS");
                    humanEntity.openInventory(properties.getSettingsGUI());
                    return;
                }
                if (inventoryClickEvent.getSlot() == 52) {
                    itemStack = inventoryClickEvent.getCurrentItem();
                    if (itemStack == null)
                        return;
                    if (Objects.requireNonNull(itemStack.getItemMeta()).getDisplayName().contains("Next")) {
                        humanEntity.openInventory(properties.getPaginatedGames().get(index + 1));
                        return;
                    }
                }
                Game game = Game.c(itemStack);
                if (game == null)
                    return;
                if (properties.getDisabledGames().contains(game.name())) {
                    properties.removeDisabledGame(game.name());
                    return;
                }
                if (Game.a(properties.getPaginatedGames()) == 1 && game.a()) {
                    humanEntity.sendMessage(PluginCommand.Language.BOSS_FAIL.getString());
                    properties.setTask("OPENED DEFAULT");
                    humanEntity.closeInventory();
                    return;
                }
                if (Game.a_(properties.getPaginatedGames()) == 15 && !game.a()) {
                    humanEntity.sendMessage(PluginCommand.Language.GAMES_FAIL.getString());
                    properties.setTask("OPENED DEFAULT");
                    humanEntity.closeInventory();
                    return;
                }
                properties.addDisabledGame(game.name());
                return;
            case "OPENED BOSS GAMES":
                if (inventoryClickEvent.getSlot() == 49) {
                    properties.setTask("OPENED GAMES");
                    humanEntity.openInventory(properties.getPaginatedGames().get(1));
                    return;
                }
                if (inventoryClickEvent.getSlot() == 46) {
                    properties.setTask("OPENED SETTINGS");
                    humanEntity.openInventory(properties.getSettingsGUI());
                    return;
                }
                game = Game.c(itemStack);
                if (game == null)
                    return;
                if (properties.getDisabledGames().contains(game.name())) {
                    properties.removeDisabledGame(game.name());
                    return;
                }
                if (Game.a(properties.getPaginatedGames()) == 1 && game.a()) {
                    humanEntity.sendMessage(PluginCommand.Language.BOSS_FAIL.getString());
                    properties.setTask("OPENED DEFAULT");
                    humanEntity.closeInventory();
                    return;
                }
                properties.addDisabledGame(game.name());
        }
    }

    private static String locationToMessage(Location location) {
        return "x: " + format(location.getX()) + ", "
                + "y: " + format(location.getY()) + ", "
                + "z: " + format(location.getZ());
    }

    private static String format(double number) {
        return String.format("%.2f", number).replace(",", ".");
    }

}
