package org.gr_code.minerware.manager.type.nms;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.gr_code.minerware.arena.Arena;
import org.gr_code.minerware.manager.ManageHandler;

import java.util.function.Consumer;

@SuppressWarnings("all")
public interface NMS {

    default void setNoAI(Entity entity) {
        //do nothing
    }

    default boolean isLegacy(){
        return false;
    }

    int getTypeId(Block block);

    default void setBlock(ItemStack baseItem, Block block){
        block.setType(baseItem.getType());
    }

    default void setBlock(int id, byte data, Block block){
        //do nothing
    }

    Consumer<Location> getGeneratorDoors();

    void setUpDirectionButton(Block block);

    default boolean oldVersion(){
        return false;
    }

    default boolean equalsItemStack(ItemStack itemStack, ItemStack itemStack_1){
        return itemStack.isSimilar(itemStack_1);
    }

    default void sendTitle(Player player, String string){
        if(ManageHandler.getNMS().isLegacy()) {
            //noinspection deprecation
            player.sendTitle(string, "");
            return;
        }
        player.sendTitle(string, null , 5, 65, 15);
    }

    default ItemStack[] getInventoryContents(Player player) {
        ItemStack[] itemStacks = new ItemStack[41];
        PlayerInventory inventory = player.getInventory();
        for (int i = 0; i < 36; i++) {
            ItemStack a = inventory.getItem(i);
            if(a == null)
                continue;
            itemStacks[i] = a.clone();
        }
        for(int i = 0; i < 4; i++){
            ItemStack a = inventory.getArmorContents()[i];
            if(a == null)
                continue;
            itemStacks[36+i] = a.clone();
        }
        ItemStack a = inventory.getItemInOffHand();
        itemStacks[40] = a == null ? a : a.clone();
        inventory.setItemInOffHand(null);
        inventory.clear();
        inventory.setArmorContents(new ItemStack[4]);
        return itemStacks;
    }

    default void restoreInventory(Player player, ItemStack[] contents) {
        PlayerInventory playerInventory = player.getInventory();
        for (int i = 0; i < 36; i++) {
            ItemStack a = contents[i];
            playerInventory.setItem(i, a);
        }
        playerInventory.setArmorContents(new ItemStack[]{contents[36], contents[37], contents[38], contents[39]});
        playerInventory.setItemInOffHand(contents[40]);
    }

    default ItemStack setGlowing(ItemStack itemStack){
        ItemMeta itemMeta = itemStack.getItemMeta();
        assert itemMeta != null;
        itemMeta.addEnchant(Enchantment.KNOCKBACK, 1, true);
        itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    default ItemStack setUnbreakable(ItemStack itemStack, boolean bool){
        ItemMeta itemMeta = itemStack.getItemMeta();
        assert itemMeta != null;
        itemMeta.setUnbreakable(bool);
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    default String translate(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    void playOutParticle(Location location, float offSet, Particle particle, int amount);

    void playOutParticle(Location location, float offSet, Particle particle, float speed, int amount);

    void playOutParticle(Location location, float offSetX, float offSetY, float offSetZ, Particle particle, int amount);

    void playOutParticle(Location location, float offSetX, float offSetY, float offSetZ, Particle particle, float speed, int amount);

    void playOutParticle(Location location, Player player, float offSet, Particle particle, int amount);

    void playOutParticle(Location location, Player player, float offSet, Particle particle, float speed, int amount);

    void playOutParticle(Location location, Player player, float offSetX, float offSetY, float offSetZ, Particle particle, int amount);

    void playOutParticle(Location location, Player player, float offSetX, float offSetY, float offSetZ, Particle particle, float speed, int amount);

    void spawnRedstoneParticle(Location location, float red, float green, float blue, float size);

    void spawnRedstoneParticle(Location location, Player player, float red, float green, float blue, float size);
    
    void spawnIceWinEffect(Player player, Location location);

    default void setHurtEntities(FallingBlock block) {
        //do nothing
    }

    void updateIceWinEffect(Player player, Location location);

    void sendRestorePackets(Player player, Arena arena);

    void updateBlocksWinEffect(Player player);

    void sendTitle(Player player, String title, String subTitle, int fadeIn, int showTime, int fadeOut);

    void sendActionBar(Player player, String text);

    void updateFireWinEffect(Player player);

    void updateRocketWinEffect(Player player, int stage);

    void updateExplosionWinEffect(Player player, int stage);

    enum Particle{

        ANGRY_VILLAGER(1),
        BARRIER(2),
        BUBBLE(3),
        CLOUD(4),
        CRIT(5),
        EXPLODE(6),
        FLAME(7),
        HAPPY_VILLAGER(8),
        HEART(9),
        HUGE_EXPLOSION(10),
        LARGE_EXPLOSION(11),
        LARGE_SMOKE(12),
        LAVA(13),
        MAGIC_CRIT(14),
        NOTE(15),
        PORTAL(16),
        @Deprecated
        REDSTONE(17),
        SMOKE(18),
        SNOWBALL(19);

        Particle(int id){
            this.id = id;
        }

        private final int id;

        public int getId() {
            return id;
        }

    }

}
