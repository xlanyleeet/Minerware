package org.gr_code.minerware.arena;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.gr_code.minerware.MinerPlugin;
import org.gr_code.minerware.builders.ItemBuilder;
import org.gr_code.minerware.commands.subcommand.PluginCommand;
import org.gr_code.minerware.manager.type.Utils;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;

public class Voting {

    private final boolean revoting;

    private final Map<Integer, String> actionMap = new HashMap<>();

    private int hard;

    private int normal;

    private final Inventory votingInventory;

    public Voting(){
        FileConfiguration fileConfiguration = MinerPlugin.getInstance().getOptions();
        String name = Utils.translate(fileConfiguration.getString("voting.name"));
        votingInventory = Bukkit.createInventory(new VoteHolder(), fileConfiguration.getInt("voting.size"), name);
        ConfigurationSection configurationSection = fileConfiguration.getConfigurationSection("voting.items");
        revoting = fileConfiguration.getBoolean("voting.revoting");
        assert configurationSection != null;
        for(String s : configurationSection.getKeys(false)){
            ConfigurationSection section = configurationSection.getConfigurationSection(s);
            assert section != null;
            ItemStack itemStack = ItemBuilder.fromSection(section).build();
            ItemBuilder.editVotingItem(this, itemStack);
            int slot = section.getInt("slot");
            votingInventory.setItem(slot, itemStack);
            actionMap.put(slot, section.getString("action"));
        }
    }

    public void reset(){
        normal = 0;
        hard = 0;
        update();
    }

    public Inventory getVotingInventory() {
        return votingInventory;
    }

    public int getHard() {
        return hard;
    }

    public int getNormal() {
        return normal;
    }

    public void update(){
        FileConfiguration fileConfiguration = MinerPlugin.getInstance().getOptions();
        ConfigurationSection configurationSection = fileConfiguration.getConfigurationSection("voting.items");
        assert configurationSection != null;
        for(String s : configurationSection.getKeys(false)){
            ConfigurationSection section = configurationSection.getConfigurationSection(s);
            assert section != null;
            ItemStack itemStack = ItemBuilder.fromSection(section).build();
            ItemBuilder.editVotingItem(this, itemStack);
            ItemMeta itemMeta = itemStack.getItemMeta();
            int slot = section.getInt("slot");
            Objects.requireNonNull(votingInventory.getItem(slot)).setItemMeta(itemMeta);
        }
    }

    public void onClick(int i, GamePlayer p){
        if(actionMap.get(i) == null)
            return;
        String s = actionMap.get(i);
        if(s.equals("CLOSE")){
            p.getPlayer().closeInventory();
            return;
        }
        Player player = p.getPlayer();
        if(s.startsWith("VOTE_")){
            if(!player.hasPermission("minerware.vote")){
                player.sendMessage(PluginCommand.Language.CAN_NOT_VOTE.getString());
                return;
            }
            String string = s.split("_")[1];
            if(p.getVote() != null){
                if(!revoting){
                    player.sendMessage(PluginCommand.Language.ALREADY_VOTED.getString());
                    player.closeInventory();
                    return;
                }
                if(p.getVote().equals(string))
                    return;
                remove(p.getVote());
            }
            p.setVoted(string);
            player.sendMessage(PluginCommand.Language.valueOf("VOTED_"+string).getString());
            if(string.equals("NORMAL"))
                normal++;
            else if(string.equals("HARD"))
                hard++;
            update();
        }
    }

    void remove(String task){
        if(task == null)
            return;
        switch (task){
            case "NORMAL":
                normal--;
                break;
            case "HARD":
                hard--;
        }
        update();
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Voting.class.getSimpleName() + "[", "]")
                .add("actionMap=" + actionMap)
                .add("hard=" + hard)
                .add("normal=" + normal)
                .toString();
    }

    public static class VoteHolder implements InventoryHolder {

        private final Inventory inventory = Bukkit.createInventory(null, 27);

        @Override
        public @NotNull Inventory getInventory() {
            return inventory;
        }

    }

}
