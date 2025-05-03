package org.gr_code.minerware.games.microgames;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.gr_code.minerware.arena.Arena;
import org.gr_code.minerware.arena.GamePlayer;
import org.gr_code.minerware.arena.GamePlayer.State;
import org.gr_code.minerware.builders.ItemBuilder;
import org.gr_code.minerware.cuboid.Cuboid;
import org.gr_code.minerware.games.Game;
import org.gr_code.minerware.games.MicroGame;
import org.gr_code.minerware.manager.type.resources.XMaterial;
import org.gr_code.minerware.manager.type.resources.XSound;

import static java.util.Objects.requireNonNull;
import static org.gr_code.minerware.cuboid.Cuboid.getRandomLocation;
import static org.gr_code.minerware.manager.type.Utils.sendTitle;
import static org.gr_code.minerware.manager.type.Utils.translate;

public class DontMove extends MicroGame {
	
	public DontMove(Arena arena) {
		super(280, arena, "dont-move-game");
	}

	@Override
	public void secondStartGame() {
		String title = translate(getString("titles.start-game"));
		String subtitle = translate(getString("titles.task"));
		Cuboid cuboid = getArena().getProperties().getCuboid();
    	getArena().getPlayers().forEach(gamePlayer -> {
    		Player player = gamePlayer.getPlayer();
			if (cuboid.notInside(player.getLocation())) 
				player.teleport(getRandomLocation(getArena()));
			sendTitle(player, title, subtitle, 0, 70, 20);
            gamePlayer.setState(State.PLAYING_GAME);
			player.playSound(player.getLocation(), requireNonNull(XSound.ENTITY_ARROW_HIT_PLAYER.parseSound()), 5, 1);
			String xPos = String.format("%.1f", ((float) player.getLocation().getX())).replace(",", ".");
			String yPos = String.format("%.1f", ((float) player.getLocation().getY())).replace(",", ".");
			String zPos = String.format("%.1f", ((float) player.getLocation().getZ())).replace(",", ".");
			String yaw = Integer.toString((int) player.getLocation().getYaw());
			String pitch = Integer.toString((int) player.getLocation().getPitch());
			gamePlayer.setTask(xPos + ":" + yPos + ":" + zPos + ":" + yaw + ":" + pitch);
		});
    }

    @Override
    public void end() {
		getArena().getPlayers().stream().filter(gamePlayer -> gamePlayer.getState() == State.PLAYING_GAME)
				.forEach(gamePlayer -> onWin(gamePlayer.getPlayer(), false));
		super.end();
	}

	@Override
    public void check() {
		if (getTime() % 5 != 0) return;
    	if (getArena().getPlayers().stream().allMatch(gamePlayer -> gamePlayer.getState() != State.PLAYING_GAME)) setTime(1);
		int param_y = getArena().getProperties().getFirstLocation().getBlockY() - 1 ;
		getArena().getPlayers().stream().filter(gamePlayer -> gamePlayer.getState() == State.PLAYING_GAME).forEach(x->{
    		Player player = x.getPlayer();
    		String[] old = x.getTask().split(":");
    		double oldX = Double.parseDouble(old[0]); double oldY = Double.parseDouble(old[1]);
    		double oldZ = Double.parseDouble(old[2]); int oldYaw = Integer.parseInt(old[3]);
    		int oldPitch = Integer.parseInt(old[4]);
    		double nowX = Double.parseDouble(String.format("%.1f", ((float) player.getLocation().getX())).replace(",", "."));
    		double nowY = Double.parseDouble(String.format("%.1f", ((float) player.getLocation().getY())).replace(",", "."));
    		double nowZ = Double.parseDouble(String.format("%.1f", ((float) player.getLocation().getZ())).replace(",", "."));
    		int nowYaw = (int) player.getLocation().getYaw();
    		int nowPitch = (int) player.getLocation().getPitch();
    		x.setTask(nowX + ":" + nowY + ":" + nowZ + ":" + nowYaw + ":" + nowPitch);
    		boolean isMoving = oldX != nowX || oldY != nowY || oldZ != nowZ || oldYaw != nowYaw || oldPitch != nowPitch;
    		if (isMoving || player.isSneaking()) onLose(player, true);
    		int y = player.getLocation().getBlockY();
    		if (y <= param_y) onLose(player, true);
    	});
    }

    @Override
    public Game getGame() {
        return Game.DONT_MOVE;
    }

	@Override
	public String getTask(GamePlayer gamePlayer) {
		return translate(getString("titles.task"));
	}
    
    @Override
	public ItemStack getGameItemStack() {
    	return ItemBuilder.start(requireNonNull(XMaterial.IRON_BARS.parseItem())).setDisplayName("&9&lDO NOT MOVE").build();
	}

}
