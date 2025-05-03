package org.gr_code.minerware.games.microgames;

import org.bukkit.GameMode;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.gr_code.minerware.arena.Arena;
import org.gr_code.minerware.arena.GamePlayer;
import org.gr_code.minerware.arena.GamePlayer.State;
import org.gr_code.minerware.builders.ItemBuilder;
import org.gr_code.minerware.cuboid.Cuboid;
import org.gr_code.minerware.games.Game;
import org.gr_code.minerware.games.MicroGame;
import org.gr_code.minerware.manager.ManageHandler;
import org.gr_code.minerware.manager.type.resources.XMaterial;
import org.gr_code.minerware.manager.type.resources.XSound;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;
import static org.gr_code.minerware.cuboid.Cuboid.getRandomLocation;
import static org.gr_code.minerware.manager.type.Utils.*;

public class PvP extends MicroGame {

	public PvP(Arena arena) {
		super(580, arena, "pvp-game");
	}

	@Override
	public String getAchievementForMsg() {
		String achievementMsg = getString("messages.achievement");
		List<GamePlayer> achievement = getArena().getPlayers().stream().filter(x -> x.getAchievement() != null).collect(Collectors.toList());
		if (achievement.isEmpty()) return "";
		int maximum = 0;
		GamePlayer gamePlayer = null;
		for (GamePlayer key : achievement) {
			int doubleKey = Integer.parseInt(key.getAchievement());
			if (doubleKey <= maximum) continue;				
			maximum = doubleKey;				
			gamePlayer = key;
		}
		String name = gamePlayer.getPlayer().getName();
		return requireNonNull(achievementMsg).replace("<name>", name).replace("<count>", Integer.toString(maximum));
	}

    @Override
    public void secondStartGame() {
		String title = translate(getString("titles.start-game"));
		String subtitle = translate(getString("titles.task"));
		Cuboid cuboid = getArena().getProperties().getCuboid();
		XMaterial xMaterial = getArena().isHardMode() ? XMaterial.DIAMOND_SWORD : XMaterial.GOLDEN_SWORD;
		XMaterial xHelmet = getArena().isHardMode() ? XMaterial.CHAINMAIL_HELMET : XMaterial.DIAMOND_HELMET;
		XMaterial xChestplate = getArena().isHardMode() ? XMaterial.LEATHER_CHESTPLATE : XMaterial.IRON_CHESTPLATE;
		XMaterial xLeggings = getArena().isHardMode() ? XMaterial.LEATHER_LEGGINGS : XMaterial.IRON_LEGGINGS;
		XMaterial xBoots = getArena().isHardMode() ? XMaterial.CHAINMAIL_BOOTS : XMaterial.DIAMOND_BOOTS;
		ItemStack helmet = ItemBuilder.start(requireNonNull(xHelmet.parseItem())).setUnbreakable(true).build();
		ItemStack chestplate = ItemBuilder.start(requireNonNull(xChestplate.parseItem())).setUnbreakable(true).build();
		ItemStack leggings = ItemBuilder.start(requireNonNull(xLeggings.parseItem())).setUnbreakable(true).build();
		ItemStack boots = ItemBuilder.start(requireNonNull(xBoots.parseItem())).setUnbreakable(true).build();
		ItemStack sword = ItemBuilder.start(requireNonNull(xMaterial.parseItem())).setUnbreakable(true).build();
		getArena().getPlayers().forEach(gamePlayer -> {
    		Player player = gamePlayer.getPlayer();
			if (cuboid.notInside(player.getLocation())) player.teleport(getRandomLocation(getArena()));
			sendTitle(player, title, subtitle,0,70,20);
            gamePlayer.setState(State.PLAYING_GAME);
            player.getInventory().setHeldItemSlot(0);
			player.playSound(player.getLocation(), requireNonNull(XSound.ENTITY_ARROW_HIT_PLAYER.parseSound()), 5, 1);
			player.setGameMode(GameMode.SURVIVAL);
			player.getInventory().setHelmet(helmet);
			player.getInventory().setChestplate(chestplate);
			player.getInventory().setLeggings(leggings);
			player.getInventory().setBoots(boots);
			player.getInventory().setItem(0, sword);
			player.getInventory().setHeldItemSlot(0);
			if (!ManageHandler.getNMS().oldVersion()) {
				AttributeInstance attribute = player.getAttribute(Attribute.GENERIC_ATTACK_SPEED);
				assert attribute != null;
				attribute.setBaseValue(30);
				player.saveData();
			}
    	});
    }
    
    @Override
    public void check() {
		if (getTime() % 5 != 0) return;
		List<GamePlayer> playerList = getArena().getPlayers();
    	if (playerList.stream().allMatch(gamePlayer -> gamePlayer.getState() != State.PLAYING_GAME)) setTime(1);
    	if (playerList.stream().filter(gamePlayer -> gamePlayer.getState() == State.PLAYING_GAME).count() == 1) setTime(1);
		int param_y = getArena().getProperties().getFirstLocation().getBlockY() - 1 ;
		playerList.stream().filter(gamePlayer -> gamePlayer.getState() == State.PLAYING_GAME).forEach(gamePlayer -> {
    		Player player = gamePlayer.getPlayer();
    		int y = player.getLocation().getBlockY();
    		if (y <= param_y) onLose(player, true);
    	});
    }

    @Override
	public void end() {
		getArena().getPlayers().stream().filter(gamePlayer -> gamePlayer.getState() == State.PLAYING_GAME)
				.forEach(gamePlayer -> onWin(gamePlayer.getPlayer(), false));
		super.end();
	}

    @Override
    public Game getGame() {
        return Game.PVP;
    }

	@Override
	public String getTask(GamePlayer gamePlayer) {
		return translate(getString("titles.task"));
	}

    @Override
	public ItemStack getGameItemStack() {
    	return ItemBuilder.start(requireNonNull(XMaterial.GOLDEN_SWORD.parseItem())).setDisplayName("&b&lPVP").build();
	}

	@Override
	public void event(Event event) {
		if (!(event instanceof EntityDamageByEntityEvent)) return;
		EntityDamageByEntityEvent e = (EntityDamageByEntityEvent) event;
		if (!(e.getEntity() instanceof Player)) return;
		if (!(e.getDamager() instanceof Player)) return;
		Player player = (Player) e.getEntity();
		Player p2 = (Player) e.getDamager();
		GamePlayer damagerGamePlayer = getArena().getPlayer(p2.getUniqueId());
		GamePlayer entityGamePlayer = requireNonNull(getArena().getPlayer(player.getUniqueId()));
		e.setCancelled(true);
		if (e.getCause() == EntityDamageByEntityEvent.DamageCause.CUSTOM) return;
		if (requireNonNull(entityGamePlayer).getState() != State.PLAYING_GAME || requireNonNull(damagerGamePlayer).getState() != State.PLAYING_GAME) return;
		setVelocity(player, p2);
		if (e.getDamage() < player.getHealth()) {
			player.damage(e.getDamage());
			return;
		}
		player.setHealth(20);
		p2.setHealth(20);
		if (damagerGamePlayer.getAchievement() == null) damagerGamePlayer.setAchievement("1");
		else damagerGamePlayer.setAchievement((Integer.parseInt(damagerGamePlayer.getAchievement()) + 1) + "");
		onLose(player, true);
	}

}