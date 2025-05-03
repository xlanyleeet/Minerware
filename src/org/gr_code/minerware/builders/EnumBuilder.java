package org.gr_code.minerware.builders;

import org.bukkit.inventory.ItemStack;
import org.gr_code.minerware.manager.type.Utils;
import org.gr_code.minerware.manager.type.resources.XMaterial;

import java.util.Arrays;
import java.util.Objects;

public enum EnumBuilder {


    BEACON(10, ItemBuilder.start(Objects.requireNonNull(XMaterial.BEACON.parseItem())).setDisplayName("&aLocations")
            .setLore(Arrays.asList("", Utils.translate("&7You can modify here"),
                    Utils.translate("&7all game location and"),
                    Utils.translate("&7teleport to them by click."))).build()),

    FLOOR(12, ItemBuilder.start(Objects.requireNonNull(XMaterial.CRAFTING_TABLE.parseItem())).setDisplayName("&aFloor")
            .setLore(InventoryBuilder.FLOOR_LIST).build()),

    SQUARES(14, ItemBuilder.start(Objects.requireNonNull(XMaterial.BRICKS.parseItem())).setDisplayName("&aSquares")
            .setLore(InventoryBuilder.FLOOR_LIST).build()),

    PLAYERS(16, ItemBuilder.start(Objects.requireNonNull(XMaterial.PLAYER_HEAD.parseItem())).setDisplayName("&aPlayers")
            .setLore(Arrays.asList("", Utils.translate("&7You can select there"),
                    Utils.translate("&7the minimum players to"),
                    Utils.translate("&7start the game."))).build()),

    SETTINGS(28, ItemBuilder.start(Objects.requireNonNull(XMaterial.COMPARATOR.parseItem())).setDisplayName("&cSettings")
            .setLore(Arrays.asList("", Utils.translate("&7You can set up all"),
                    Utils.translate("&7arena options and flags."),
                    Utils.translate("&7you could disable and"),
                    Utils.translate("&7enable games also"),
                    Utils.translate("&7choose the arena type."))).build()),

    ENABLE(30, ItemBuilder.start(Objects.requireNonNull(XMaterial.ARMOR_STAND.parseItem())).setDisplayName("&cFinish creating")
            .setLore(Arrays.asList("", Utils.translate("&7Click there to finish"),
                    Utils.translate("&7current editing session."),
                    Utils.translate("&7You may edit arena"),
                    Utils.translate("&7after saving it."))).build()),

    DELETE(32, ItemBuilder.start(Objects.requireNonNull(XMaterial.BARRIER.parseItem())).setDisplayName("&cRemove the arena")
            .setLore(Arrays.asList("", Utils.translate("&7Click there to delete"),
                    Utils.translate("&7current editing session"),
                    Utils.translate("&7and delete world."))).build());

    EnumBuilder(int a, ItemStack itemStack) {
        this.a = a;
        this.itemStack = itemStack;
    }

    private final int a;

    private final ItemStack itemStack;

    public ItemStack getItemStack() {
        return itemStack;
    }

    public int getSlot() {
        return a;
    }

}

