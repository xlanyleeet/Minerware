package org.gr_code.minerware.builders;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.gr_code.minerware.games.Game;
import org.gr_code.minerware.manager.type.Utils;
import org.gr_code.minerware.manager.type.resources.XMaterial;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class InventoryBuilder {

    private static final List<String> CUBOID_LIST =
            Arrays.asList("", Utils.translate("&7You could create cuboid here."),
                    Utils.translate("&7Here is an instruction:"),
                    Utils.translate("&eLEFT &7click - create cuboid"),
                    Utils.translate("&eRIGHT &7click - destroy it"));

    private static final List<String> STRING_LIST =
            Arrays.asList("",
                    Utils.translate("&7Location methods there:"),
                    Utils.translate("&eLEFT &7click - set location"),
                    Utils.translate("&eRIGHT &7click - teleport"));

    public static final List<String> FLOOR_LIST =
            Arrays.asList("", Utils.translate("&7You could save your buildings there."),
                    Utils.translate("&7Here is an instruction:"),
                    Utils.translate("&eLEFT &7click - save buildings"),
                    Utils.translate("&eRIGHT &7click - restore"));

    private final Inventory inventory;

    public InventoryBuilder(Inventory inventory){
        this.inventory = inventory;
    }

    public static InventoryBuilder InventoryCreate(String arenaName, int size){
        return new InventoryBuilder(Bukkit.createInventory(null, size, Utils.translate(arenaName)));
    }

    public InventoryBuilder generateArenaInventory(){
        for(EnumBuilder enumBuilder : EnumBuilder.values())
            inventory.setItem(enumBuilder.getSlot(), enumBuilder.getItemStack());
        fillInventory(XMaterial.GRAY_STAINED_GLASS_PANE.parseItem());
        return this;
    }

    public InventoryBuilder generateOptionsInventory(){
        InventoryBuilder inventoryBuilder = InventoryBuilder.InventoryCreate("Settings", 27);
        Inventory inventory = inventoryBuilder.getInventory();
        inventory.setItem(11, ItemBuilder.start(Objects.requireNonNull(XMaterial.RED_BED.parseItem())).setDisplayName("&aGames")
                .setLore(Arrays.asList("", Utils.translate("&7You can enable and"),
                        Utils.translate("&7disable games there."))).build());
        inventory.setItem(15, ItemBuilder.start(Objects.requireNonNull(XMaterial.PAINTING.parseItem()))
                .setLore(Arrays.asList("", Utils.translate("&7You can change the "),
                        Utils.translate("&7arena type and size there."))).setDisplayName("&cType").build());
        fillWithBack(XMaterial.GRAY_STAINED_GLASS_PANE.parseItem(), inventory);
        return inventoryBuilder;
    }

    public InventoryBuilder generateTypeInventory(){
        InventoryBuilder inventoryBuilder = InventoryBuilder.InventoryCreate("Type", 27);
        Inventory inventory = inventoryBuilder.getInventory();
        inventory.setItem(10, ItemBuilder.start(Objects.requireNonNull(XMaterial.COAL_BLOCK.parseItem())).setDisplayName("&6&lMICRO")
                .setLore(Arrays.asList("", Utils.translate("&7Cuboid size: &c&l12"), Utils.translate("&7Maximum players: &c&l4"))).build());
        inventory.setItem(12, ItemBuilder.start(Objects.requireNonNull(XMaterial.IRON_BLOCK.parseItem())).setDisplayName("&6&lMINI")
                .setLore(Arrays.asList("", Utils.translate("&7Cuboid size: &c&l16"), Utils.translate("&7Maximum players: &c&l10"))).build());
        inventory.setItem(14, ItemBuilder.start(Objects.requireNonNull(XMaterial.GOLD_BLOCK.parseItem())).setDisplayName("&6&lDEFAULT")
                .setLore(Arrays.asList("", Utils.translate("&7Cuboid size: &c&l24"), Utils.translate("&7Maximum players: &c&l16"))).build());
        inventory.setItem(16, ItemBuilder.start(Objects.requireNonNull(XMaterial.DIAMOND_BLOCK.parseItem())).setDisplayName("&6&lMEGA")
                .setLore(Arrays.asList("", Utils.translate("&7Cuboid size: &c&l29"), Utils.translate("&7Maximum players: &c&l20"))).build());
        fillWithBack(XMaterial.GRAY_STAINED_GLASS_PANE.parseItem(), inventory);
        return inventoryBuilder;
    }

    public InventoryBuilder generateLocationsInventory(){
        InventoryBuilder inventoryBuilder = InventoryBuilder.InventoryCreate("Locations", 27);
        Inventory inventory = inventoryBuilder.getInventory();
        inventory.setItem(10, ItemBuilder.start(Objects.requireNonNull(XMaterial.WOODEN_AXE.parseItem())).setDisplayName("&aSelection").setLore(STRING_LIST).build());
        inventory.setItem(12, ItemBuilder.start(Objects.requireNonNull(XMaterial.LIME_WOOL.parseItem())).setDisplayName("&aWinners").setLore(STRING_LIST).build());
        inventory.setItem(14, ItemBuilder.start(Objects.requireNonNull(XMaterial.RED_WOOL.parseItem())).setDisplayName("&aLosers").setLore(STRING_LIST).build());
        inventory.setItem(16, ItemBuilder.start(Objects.requireNonNull(XMaterial.TRIPWIRE_HOOK.parseItem())).setDisplayName("&cCuboid").setLore(CUBOID_LIST).build());
        fillWithBack(XMaterial.GRAY_STAINED_GLASS_PANE.parseItem(), inventory);
        return inventoryBuilder;
    }

    public static List<Inventory> generateGamesInventory(){
        List<Inventory> list = new ArrayList<>();
        int a = 0; int b = 0;
        int inventory = 1;
        int length = Game.values().length - 6;
        list.add(generatePaginatedBossGames());
        list.add(generatePaginatedGames(length, false));
        for(Game game : Game.values()){
            if(game.m())
                continue;
            if(a == 45){
                a = 0;
                inventory++;
                list.add(generatePaginatedGames(length - a, true));
            }
            ItemStack itemStack = ItemBuilder.start(game.createGame(null).getGameItemStack()).setGlowing(true)
                    .setLore(Arrays.asList("", Utils.translate("&7Enabled:&a&l true"))).build();
            if(game.isBossGame()){
                list.get(0).setItem(b, itemStack);
                b++;
                continue;
            }
            list.get(inventory).setItem(a, itemStack);
            a++;
            length--;
        }
        return list;
    }

    private static Inventory generatePaginatedGames(int amount, boolean hasPrevious){
        ItemStack next = ItemBuilder.start(Objects.requireNonNull(XMaterial.ARROW.parseItem()))
                .setDisplayName("&cNext &7page.").build();
        ItemStack previous = ItemBuilder.start(Objects.requireNonNull(XMaterial.ARROW.parseItem()))
                .setDisplayName("&cPrevious &7page.").build();
        ItemStack games = ItemBuilder.start(Objects.requireNonNull(XMaterial.REDSTONE_BLOCK.parseItem()))
                .setDisplayName("&aGames").setLore(Arrays.asList("", "&7Click to switch to boss games.")).build();
        Inventory inventory = Bukkit.createInventory(null, 54, Utils.translate("Games"));
        inventory.setItem(46,
                ItemBuilder.start(Objects.requireNonNull(XMaterial.OAK_DOOR.parseItem())).setDisplayName("&e&lBACK").build());
        if(amount > 45)
            inventory.setItem(52, next);
        if(hasPrevious)
            inventory.setItem(46, previous);
        inventory.setItem(49, games);
        return inventory;
    }

    private static Inventory generatePaginatedBossGames(){
        ItemStack games = ItemBuilder.start(Objects.requireNonNull(XMaterial.IRON_BLOCK.parseItem()))
                .setDisplayName("&aGames").setLore(Arrays.asList("", "&7Click to switch to games.")).build();
        Inventory inventory = Bukkit.createInventory(null, 54, Utils.translate("Boss games"));
        inventory.setItem(46,
                ItemBuilder.start(Objects.requireNonNull(XMaterial.OAK_DOOR.parseItem())).setDisplayName("&e&lBACK").build());
        inventory.setItem(49, games);
        return inventory;
    }



    public static Inventory generatePlayerInventory(int max){
        InventoryBuilder inventoryBuilder = InventoryBuilder.InventoryCreate("Minimum players", 27);
        Inventory inventory = inventoryBuilder.getInventory();
        for (int i = 0; i < max-1; i++) {
            inventory.setItem(i, ItemBuilder.start(Objects.requireNonNull(XMaterial.PLAYER_HEAD.parseItem())).setGlowing(true).setAmount(i+2).setDisplayName("&c&l"+(i+2)+"&7 players").build());
        }
        inventoryBuilder.fillWithBack(XMaterial.GRAY_STAINED_GLASS_PANE.parseItem(), inventory);
        return inventory;
    }

    public static Inventory generateRemoveInventory(){
        InventoryBuilder inventoryBuilder = InventoryBuilder.InventoryCreate("Remove", 27);
        Inventory inventory = inventoryBuilder.getInventory();
        inventory.setItem(11, ItemBuilder.start(Objects.requireNonNull(XMaterial.LIME_STAINED_GLASS.parseItem())).setDisplayName("&a&lYES").build());
        inventory.setItem(15, ItemBuilder.start(Objects.requireNonNull(XMaterial.RED_STAINED_GLASS.parseItem())).setDisplayName("&c&lNO").build());
        inventoryBuilder.fillInventory(XMaterial.GRAY_STAINED_GLASS_PANE.parseItem(), inventory);
        return inventory;
    }

    public Inventory getInventory() {
        return inventory;
    }

    private void fillInventory(ItemStack itemStack){
        for(int i = 0; i<inventory.getSize(); i++){
            if(inventory.getItem(i) == null)
                inventory.setItem(i, itemStack);
        }
    }

    private void fillInventory(ItemStack itemStack, Inventory inventory){
        for(int i = 0; i<inventory.getSize(); i++){
            if(inventory.getItem(i) == null)
                inventory.setItem(i, itemStack);
        }
    }

    private void fillWithBack(ItemStack itemStack, Inventory inventory){
        fillInventory(itemStack, inventory);
        inventory.setItem(inventory.getSize()-1, ItemBuilder.start(Objects.requireNonNull(XMaterial.OAK_DOOR.parseItem())).setDisplayName("&e&lBACK").build());
    }

}


