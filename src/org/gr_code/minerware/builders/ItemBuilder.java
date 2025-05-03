package org.gr_code.minerware.builders;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.gr_code.minerware.arena.Voting;
import org.gr_code.minerware.manager.ManageHandler;
import org.gr_code.minerware.manager.type.Utils;
import org.gr_code.minerware.manager.type.resources.XMaterial;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ItemBuilder {
    private final ItemStack itemStack;

    public ItemBuilder(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    public static ItemBuilder start(Material material, int amount) {
        ItemStack itemStack = new ItemStack(material, amount);
        return new ItemBuilder(itemStack);
    }

    public static ItemBuilder start(ItemStack itemStack) {
        return new ItemBuilder(itemStack.clone());
    }

    public ItemBuilder setGlowing(boolean a) {
        if (!a){
            itemStack.getEnchantments().forEach((enchantment, integer) -> itemStack.removeEnchantment(enchantment));
            return this;
        }
        return ItemBuilder.start(ManageHandler.getNMS().setGlowing(itemStack));
    }

    public ItemBuilder setDisplayName(String displayName) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        String string = Utils.translate(displayName);
        assert itemMeta != null;
        itemMeta.setDisplayName(string);
        itemStack.setItemMeta(itemMeta);
        return this;
    }

    public ItemBuilder setLore(List<String> lore) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        List<String> translated = new ArrayList<>();
        lore.forEach(string -> translated.add(Utils.translate(string)));
        assert itemMeta != null;
        itemMeta.setLore(translated);
        itemStack.setItemMeta(itemMeta);
        return this;
    }

    public ItemBuilder addEnchantment(Enchantment enchantment, int level, boolean ignoreLevelRestriction) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        assert itemMeta != null;
        itemMeta.addEnchant(enchantment, level, ignoreLevelRestriction);
        itemStack.setItemMeta(itemMeta);
        return this;
    }

    public ItemBuilder setUnbreakable(boolean unbreakable) {
        return new ItemBuilder(ManageHandler.getNMS().setUnbreakable(itemStack, unbreakable));
    }

    public ItemBuilder setAmount(int amount){
        itemStack.setAmount(amount);
        return this;
    }

    public static ItemBuilder fromSection(ConfigurationSection configurationSection){
        String material = configurationSection.getString("material");
        ItemBuilder itemBuilder = ItemBuilder.start(Objects.requireNonNull(XMaterial.valueOf(material).parseItem()));
        itemBuilder.setLore(configurationSection.getStringList("lore"));
        itemBuilder.setDisplayName(configurationSection.getString("name"));
        itemBuilder.setGlowing(configurationSection.getBoolean("glowing"));
        return itemBuilder;
    }

    public static void editVotingItem(Voting voting, ItemStack itemStack){
        ItemMeta itemMeta = itemStack.getItemMeta();
        List<String> strings = new ArrayList<>();
        assert itemMeta != null;
        for(String s : Objects.requireNonNull(itemMeta.getLore())){
            strings.add(s.replace("<hard>", voting.getHard()+"")
            .replace("<normal>", voting.getNormal()+""));
        }
        itemMeta.setLore(strings);
        itemStack.setItemMeta(itemMeta);
    }


    public ItemStack build() {
        return this.itemStack;
    }
}
