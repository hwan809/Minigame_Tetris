package me.vivace.Tetris;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.craftbukkit.libs.org.apache.commons.io.FileUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;

import me.vivace.Tetris.Commands;
import me.vivace.Tetris.Events;

public class Tetris extends JavaPlugin implements Listener{
	
	private Commands commands = new Commands();
	
	public static HashMap<String, InventoryManager> ims = new HashMap<String, InventoryManager>();
	
//	public static Scoreboard board;
//	public static Objective o;
//	public static HashMap<Player, Score> player_scoreboard = new HashMap<Player, Score>();
	
	public void onEnable() {
		getServer().getConsoleSender().sendMessage(ChatColor.GREEN + "\n\n미니게임 : TETRIS ON\n\n");
		getServer().getPluginManager().registerEvents(new Events(), this);
		getServer().getPluginManager().registerEvents(new TetrisEvents(), this);
		
		InventoryManager main = new InventoryManager(9, ChatColor.AQUA + "미니게임");
		main.setitem(4, new ItemStackManager(Material.GOLD_NUGGET, "싱글 플레이").getItemStack());
		
		ims.put("main_inv", main);
		
		/*
		board = Bukkit.getServer().getScoreboardManager().getNewScoreboard();
		o = board.registerNewObjective("test", "test");
		o.setDisplayName(ChatColor.RED + "DEATH");
		o.setDisplaySlot(DisplaySlot.SIDEBAR);
		
		for (Player p : Bukkit.getOnlinePlayers()) {
			Score s = o.getScore(p.getName());
			s.setScore(0);
			
			player_scoreboard.put(p, s);
			p.setScoreboard(board);
		}
		*/
	}
	
	public void onDisable() {
		getServer().getConsoleSender().sendMessage(ChatColor.RED + "\n\n미니게임 : TETRIS OFF\n\n");
	
		//player to mainworld
		for (Player p : Bukkit.getServer().getOnlinePlayers()) {
			p.teleport(Bukkit.getWorld("world").getSpawnLocation());
		}
		
		//remove all tworlds
		for (World w : TetrisEvents.tetris_worlds.keySet()) {
			deleteWorld(w);
		}
		
		//remove scoreboard
		for (Board b : TetrisEvents.tetris_worlds.values()) {
			b.getPlayer().setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
		}
	}
	
	public static void deleteWorld(World w) {
		
		Bukkit.getServer().unloadWorld(w.getName(), true);
		File destDir = new File("." + File.separator + w.getName());
		
		try {
		    FileUtils.deleteDirectory(destDir);
		} catch (IOException e) {
		    e.printStackTrace();
		}
	
	}
}
