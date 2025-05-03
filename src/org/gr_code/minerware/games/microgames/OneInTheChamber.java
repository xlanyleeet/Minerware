package org.gr_code.minerware.games.microgames;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.projectiles.ProjectileSource;
import org.gr_code.minerware.arena.Arena;
import org.gr_code.minerware.arena.GamePlayer;
import org.gr_code.minerware.arena.GamePlayer.State;
import org.gr_code.minerware.builders.ItemBuilder;
import org.gr_code.minerware.cuboid.Cuboid;
import org.gr_code.minerware.games.Game;
import org.gr_code.minerware.games.MicroGame;
import org.gr_code.minerware.manager.ManageHandler;
import org.gr_code.minerware.manager.type.Utils;
import org.gr_code.minerware.manager.type.resources.XMaterial;
import org.gr_code.minerware.manager.type.resources.XSound;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;
import static org.gr_code.minerware.cuboid.Cuboid.getRandomLocation;
import static org.gr_code.minerware.manager.type.Utils.*;

public class OneInTheChamber extends MicroGame {

	public OneInTheChamber(Arena arena) {
		super(480, arena, "one-in-the-chamber");
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
		ItemStack sword = ItemBuilder.start(requireNonNull(XMaterial.WOODEN_SWORD.parseItem())).setUnbreakable(true).build();
		int count = getArena().isHardMode() ? 2 : 1;
		ItemStack bow = ItemBuilder.start(requireNonNull(XMaterial.BOW.parseItem())).setUnbreakable(true).build();
		ItemStack arrow = ItemBuilder.start(requireNonNull(XMaterial.ARROW.parseItem())).setAmount(count).setUnbreakable(true).build();
    	getArena().getPlayers().forEach(gamePlayer -> {
    		Player player = gamePlayer.getPlayer();
			if (cuboid.notInside(player.getLocation())) player.teleport(getRandomLocation(getArena()));
			sendTitle(player, title, subtitle, 0,70,20);
            player.getInventory().setItem(0, sword);
            player.getInventory().setItem(1, bow);
            player.getInventory().setItem(2, arrow);
            gamePlayer.setState(State.PLAYING_GAME);
			player.playSound(player.getLocation(), requireNonNull(XSound.ENTITY_ARROW_HIT_PLAYER.parseSound()), 5, 1);
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
        return Game.ONE_IN_THE_CHAMBER;
    }

	@Override
	public String getTask(GamePlayer gamePlayer) {
		return translate(getString("titles.task"));
	}

    @Override
	public ItemStack getGameItemStack() {
    	return ItemBuilder.start(requireNonNull(XMaterial.ARROW.parseItem())).setDisplayName("&5&lONE IN THE CHAMBER").build();
	}

	private void damageByPlayer(EntityDamageByEntityEvent e, Player player) {
		Player damager = (Player) e.getDamager();
		UUID uuidD = damager.getUniqueId();
		GamePlayer damagerGamePlayer = getArena().getPlayer(uuidD);
		if (requireNonNull(damagerGamePlayer).getState() != State.PLAYING_GAME) return;
		setVelocity(player, damager);
		if (e.getDamage() < player.getHealth()) {
			player.damage(e.getDamage());
			return;
		}
		damager.getInventory().addItem(XMaterial.ARROW.parseItem());
		player.setHealth(20);
		damager.setHealth(20);
		if (damagerGamePlayer.getAchievement() == null) damagerGamePlayer.setAchievement("1");
		else damagerGamePlayer.setAchievement((Integer.parseInt(damagerGamePlayer.getAchievement()) + 1) + "");
		onLose(player, true);
	}

	private void damageByProjectile(EntityDamageByEntityEvent e, Player player) {
		ProjectileSource damagerEn = ((Projectile) e.getDamager()).getShooter();
		if (!(damagerEn instanceof Player)) return;
		Player damager = (Player) damagerEn;
		UUID uuid2 = damager.getUniqueId();
		if (!Utils.isInGame(uuid2)) return;
		GamePlayer damagerGamePlayer = getArena().getPlayer(uuid2);
		if (requireNonNull(damagerGamePlayer).getState() != State.PLAYING_GAME) return;
		player.setHealth(20); damager.setHealth(20);
		onLose(player, true);
		damager.getInventory().addItem(XMaterial.ARROW.parseItem());
		if (damagerGamePlayer.getAchievement() == null) damagerGamePlayer.setAchievement("1");
		else damagerGamePlayer.setAchievement((Integer.parseInt(damagerGamePlayer.getAchievement()) + 1) + "");
		e.getDamager().remove();
	}

	@Override
	public void event(Event event) {
		if (!(event instanceof EntityDamageByEntityEvent)) return;
		EntityDamageByEntityEvent e = (EntityDamageByEntityEvent) event;
		if (!(e.getEntity() instanceof Player)) return;
		Player player = (Player) e.getEntity();
		UUID uuid = player.getUniqueId();
		GamePlayer entityGamePlayer = requireNonNull(getArena().getPlayer(uuid));
		e.setCancelled(true);
		if (e.getCause() == EntityDamageByEntityEvent.DamageCause.CUSTOM) return;
		if (requireNonNull(entityGamePlayer).getState() != State.PLAYING_GAME) return;
		if (e.getDamager() instanceof Player) damageByPlayer(e, player);
		else if (e.getDamager() instanceof Projectile) damageByProjectile(e, player);
	}

}
