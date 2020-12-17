package me.vivace.Tetris;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;

public class Events implements Listener {
	
	//개발용
	@EventHandler
	public void break_block(BlockBreakEvent e) {
		if (!e.getPlayer().getItemInHand().getType().equals(Material.BLAZE_ROD)) return;

		Location b_loc = e.getBlock().getLocation();
		Bukkit.broadcastMessage(ChatColor.GOLD + "당신은 좌표 " + ChatColor.WHITE + b_loc.getBlockX() +" " + 
				b_loc.getBlockY() + " " + b_loc.getBlockZ() + ChatColor.GOLD +" 에서 " + 
				ChatColor.AQUA + e.getBlock().getType().toString() + ChatColor.GOLD + " 를 부쉈습니다!");
		
		//Board b = new Board(new Location(Bukkit.getWorld("world"), -141, 24, -14), 
		//		new Location(Bukkit.getWorld("world"), -150, 5, -14));
		
		e.setCancelled(true);
	}
	
//	@EventHandler
//	public void jumpmap(PlayerSwapHandItemsEvent e) {
//		e.getPlayer().setHealth(0);
//		e.setCancelled(true);
//		int score = Tetris.player_scoreboard.get(e.getPlayer()).getScore();
//		
//		Tetris.player_scoreboard.get(e.getPlayer()).setScore(score + 1);
//	}
	
}
