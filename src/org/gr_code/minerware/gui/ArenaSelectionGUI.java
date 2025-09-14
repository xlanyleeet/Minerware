package org.gr_code.minerware.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.gr_code.minerware.MinerPlugin;
import org.gr_code.minerware.arena.Arena;
import org.gr_code.minerware.builders.ItemBuilder;
import org.gr_code.minerware.manager.type.resources.XMaterial;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.gr_code.minerware.manager.type.Utils.translate;

public class ArenaSelectionGUI implements Listener {

    private static final String GUI_TITLE = translate(
            MinerPlugin.getInstance().getLanguage().getString("arena-gui.title"));
    private static final int GUI_SIZE = 54; // 6 rows
    private static final java.util.Set<java.util.UUID> processingPlayers = new java.util.HashSet<>();

    public ArenaSelectionGUI() {
        Bukkit.getPluginManager().registerEvents(this, MinerPlugin.getInstance());
    }

    public void openGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, GUI_SIZE, GUI_TITLE);

        // Fill GUI with arena items
        List<Arena> arenas = new ArrayList<>(MinerPlugin.getARENA_REGISTRY());

        for (int i = 0; i < arenas.size() && i < 45; i++) { // Leave bottom row for navigation
            Arena arena = arenas.get(i);
            ItemStack arenaItem = createArenaItem(arena);
            gui.setItem(i, arenaItem);
        }

        // Add navigation items
        addNavigationItems(gui);

        player.openInventory(gui);
    }

    private ItemStack createArenaItem(Arena arena) {
        Material material;
        String statusColor;
        String status;

        switch (arena.getStage()) {
            case WAITING:
                material = Material.GREEN_WOOL;
                statusColor = "&a";
                status = translate(MinerPlugin.getInstance().getLanguage().getString("arena-gui.status.waiting"));
                break;
            case STARTING:
                material = Material.YELLOW_WOOL;
                statusColor = "&e";
                status = translate(MinerPlugin.getInstance().getLanguage().getString("arena-gui.status.starting"));
                break;
            case PLAYING:
            case NEW_GAME_STARTING:
            case FINISHED:
                material = Material.RED_WOOL;
                statusColor = "&c";
                status = translate(MinerPlugin.getInstance().getLanguage().getString("arena-gui.status.in-game"));
                break;
            default:
                material = Material.GRAY_WOOL;
                statusColor = "&7";
                status = translate(MinerPlugin.getInstance().getLanguage().getString("arena-gui.status.unknown"));
                break;
        }

        List<String> lore = new ArrayList<>();
        lore.add("");
        lore.add(translate(MinerPlugin.getInstance().getLanguage().getString("arena-gui.labels.status")) + statusColor
                + status);
        lore.add(translate(MinerPlugin.getInstance().getLanguage().getString("arena-gui.labels.players")) + "&b"
                + arena.getCurrentPlayers() + "&7/&b" + arena.getProperties().getMaxPlayers());
        lore.add(translate(MinerPlugin.getInstance().getLanguage().getString("arena-gui.labels.arena-type")) + "&f"
                + arena.getProperties().getType());
        lore.add("");

        if (arena.getStage() == Arena.Stage.WAITING || arena.getStage() == Arena.Stage.STARTING) {
            lore.add(translate(MinerPlugin.getInstance().getLanguage().getString("arena-gui.labels.click-to-join")));
        } else {
            lore.add(translate(MinerPlugin.getInstance().getLanguage().getString("arena-gui.labels.not-available")));
        }

        // Використовуємо кастомну назву арени якщо вона є, інакше звичайну назву
        String arenaDisplayName = arena.getProperties().getDisplayName() != null
                ? arena.getProperties().getDisplayName()
                : arena.getName();

        return ItemBuilder.start(Objects.requireNonNull(XMaterial.matchXMaterial(material).parseItem()))
                .setDisplayName(translate("&6&l" + arenaDisplayName))
                .setLore(lore)
                .build();
    }

    private void addNavigationItems(Inventory gui) {
        // Close button
        ItemStack closeItem = ItemBuilder.start(Objects.requireNonNull(XMaterial.BARRIER.parseItem()))
                .setDisplayName(translate(MinerPlugin.getInstance().getLanguage().getString("arena-gui.buttons.close")))
                .setLore(List.of(
                        translate(MinerPlugin.getInstance().getLanguage().getString("arena-gui.buttons.close-desc"))))
                .build();
        gui.setItem(49, closeItem);

        // Refresh button
        ItemStack refreshItem = ItemBuilder.start(Objects.requireNonNull(XMaterial.EMERALD.parseItem()))
                .setDisplayName(
                        translate(MinerPlugin.getInstance().getLanguage().getString("arena-gui.buttons.refresh")))
                .setLore(List.of(
                        translate(MinerPlugin.getInstance().getLanguage().getString("arena-gui.buttons.refresh-desc"))))
                .build();
        gui.setItem(45, refreshItem);

        // Join random arena button
        ItemStack randomItem = ItemBuilder.start(Objects.requireNonNull(XMaterial.ENDER_PEARL.parseItem()))
                .setDisplayName(
                        translate(MinerPlugin.getInstance().getLanguage().getString("arena-gui.buttons.random")))
                .setLore(List.of(
                        translate(MinerPlugin.getInstance().getLanguage().getString("arena-gui.buttons.random-desc"))))
                .build();
        gui.setItem(53, randomItem);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals(GUI_TITLE)) {
            return;
        }

        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();

        // Запобігаємо подвійному кліку
        if (processingPlayers.contains(player.getUniqueId())) {
            return;
        }

        ItemStack clickedItem = event.getCurrentItem();

        if (clickedItem == null || clickedItem.getType() == Material.AIR) {
            return;
        }

        int slot = event.getSlot();

        // Додаємо гравця до списку тих, що обробляються
        processingPlayers.add(player.getUniqueId());

        // Видаляємо через 1 секунду, щоб запобігти залипанню
        Bukkit.getScheduler().runTaskLater(MinerPlugin.getInstance(), () -> {
            processingPlayers.remove(player.getUniqueId());
        }, 20L);

        // Handle navigation buttons
        if (slot == 49) { // Close button
            player.closeInventory();
            return;
        }

        if (slot == 45) { // Refresh button
            openGUI(player);
            return;
        }

        if (slot == 53) { // Join random arena
            joinRandomArena(player);
            return;
        }

        // Handle arena selection
        if (slot < 45) {
            List<Arena> arenas = new ArrayList<>(MinerPlugin.getARENA_REGISTRY());
            if (slot < arenas.size()) {
                Arena selectedArena = arenas.get(slot);
                joinArena(player, selectedArena);
            }
        }
    }

    private void joinArena(Player player, Arena arena) {
        player.closeInventory();

        if (arena.getStage() != Arena.Stage.WAITING && arena.getStage() != Arena.Stage.STARTING) {
            player.sendMessage(
                    translate(MinerPlugin.getInstance().getLanguage().getString("arena-gui.messages.not-available")));
            return;
        }

        if (arena.getCurrentPlayers() >= arena.getProperties().getMaxPlayers()) {
            player.sendMessage(
                    translate(MinerPlugin.getInstance().getLanguage().getString("arena-gui.messages.arena-full")));
            return;
        }

        // Join the arena
        arena.addPlayer(player.getUniqueId());
        String arenaDisplayName = arena.getProperties().getDisplayName() != null
                ? arena.getProperties().getDisplayName()
                : arena.getName();
        player.sendMessage(translate(MinerPlugin.getInstance().getLanguage().getString("arena-gui.messages.joined")
                .replace("<arena>", arenaDisplayName)));
    }

    private void joinRandomArena(Player player) {
        player.closeInventory();

        List<Arena> availableArenas = new ArrayList<>();
        for (Arena arena : MinerPlugin.getARENA_REGISTRY()) {
            if ((arena.getStage() == Arena.Stage.WAITING || arena.getStage() == Arena.Stage.STARTING) &&
                    arena.getCurrentPlayers() < arena.getProperties().getMaxPlayers()) {
                availableArenas.add(arena);
            }
        }

        if (availableArenas.isEmpty()) {
            player.sendMessage(
                    translate(MinerPlugin.getInstance().getLanguage().getString("arena-gui.messages.no-arenas")));
            return;
        }

        // Знаходимо арену з найбільшою кількістю гравців для добирання
        Arena bestArena = availableArenas.get(0);
        for (Arena arena : availableArenas) {
            if (arena.getCurrentPlayers() > bestArena.getCurrentPlayers()) {
                bestArena = arena;
            }
        }

        joinArena(player, bestArena);
    }
}
