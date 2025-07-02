package org.gr_code.minerware.games.microgames;

import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
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
import java.util.UUID;

import static java.util.Objects.requireNonNull;
import static org.gr_code.minerware.cuboid.Cuboid.getRandomLocation;
import static org.gr_code.minerware.cuboid.Cuboid.getSize;
import static org.gr_code.minerware.manager.type.Utils.sendTitle;
import static org.gr_code.minerware.manager.type.Utils.translate;

public class Sneak extends MicroGame {

	public Sneak(Arena arena) {
		super(180, arena, "sneak-game");
	}

    @Override
    public void secondStartGame() {
		String title = translate(getString("titles.start-game"));
		String subtitle = translate(getString("titles.task"));
		Cuboid cuboid = getArena().getProperties().getCuboid();
    	getArena().getPlayers().forEach(gamePlayer -> {
    		Player player = gamePlayer.getPlayer();
			if (getArena().isHardMode() || cuboid.notInside(player.getLocation())) player.teleport(getRandomLocation(getArena()));
			sendTitle(player, title, subtitle,0,70,20);
            gamePlayer.setState(State.PLAYING_GAME);
			player.playSound(player.getLocation(), requireNonNull(XSound.ENTITY_ARROW_HIT_PLAYER.parseSound()), 5, 1);
    	});
    }

    private void spawnTNTs() {
		for (int i = 0; i < getSize(getArena()) / 6; i ++) {
			Location loc = getRandomLocation(getArena()).add(Math.random(), 5, Math.random());
			TNTPrimed tnt = (TNTPrimed) requireNonNull(loc.getWorld()).spawnEntity(loc, EntityType.PRIMED_TNT);
			if (!ManageHandler.getModernAPI().oldVersion()) tnt.setGlowing(true);
			tnt.setFuseTicks(40);
		}
	}
    
    @Override
    public void check() {
		if (getTime() % 5 != 0) return;
		if (getTime() % 30 == 0 && getArena().isHardMode()) spawnTNTs();
		List<GamePlayer> playerList = getArena().getPlayers();
    	if (playerList.stream().allMatch(gamePlayer -> gamePlayer.getState() != State.PLAYING_GAME)) setTime(1);
		int param_y = getArena().getProperties().getFirstLocation().getBlockY() - 1 ;
		playerList.stream().filter(gamePlayer -> gamePlayer.getState() == State.PLAYING_GAME).forEach(gamePlayer -> {
    		Player player = gamePlayer.getPlayer();
    		int y = player.getLocation().getBlockY();
    		if (y <= param_y || !player.isSneaking()) onLose(player, true);
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
        return Game.SNEAK;
    }

	@Override
	public String getTask(GamePlayer gamePlayer) {
		return translate(getString("titles.task"));
	}

    @Override
	public ItemStack getGameItemStack() {
    	return ItemBuilder.start(requireNonNull(XMaterial.COOKED_RABBIT.parseItem())).setDisplayName("&6&lDONT STOP SNEAKING").build();
	}

	@Override
	public void event(Event event) {
		if (event instanceof EntityDamageEvent) entityDamage(event);
		else if (event instanceof EntityExplodeEvent) entityExplode(event);
	}

	private void entityDamage(Event event) {
		EntityDamageEvent e = (EntityDamageEvent) event;
		Player player = (Player) e.getEntity();
		UUID uuid = player.getUniqueId();
		if (e.getCause() != EntityDamageEvent.DamageCause.ENTITY_EXPLOSION) return;
		GamePlayer gamePlayer = requireNonNull(getArena().getPlayer(uuid));
		if (requireNonNull(gamePlayer).getState() != State.PLAYING_GAME) return;
		e.setCancelled(false);
		if (e.getDamage() < player.getHealth()) return;
		e.setCancelled(true);
		onLose(player, true);
	}

	private void entityExplode(Event event) {
		EntityExplodeEvent e = (EntityExplodeEvent) event;
		if (e.getEntity() == null) return;
		if (e.getEntity().getType() != EntityType.PRIMED_TNT) return;
		e.setCancelled(false);
	}

}


