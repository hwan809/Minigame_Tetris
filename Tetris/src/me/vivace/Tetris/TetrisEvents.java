package me.vivace.Tetris;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.Inventory;

public class TetrisEvents implements Listener {
	
	public static Map<World, Board> tetris_worlds = new HashMap<World, Board>();
	public static int tworld_num = 1;
	
	@EventHandler
	public void leave_room(PlayerQuitEvent e) {
		if (tetris_worlds.values().contains(e.getPlayer())) {
			World w = e.getPlayer().getWorld();
			
			for (Player ps : w.getPlayers()) {
				ps.teleport(Bukkit.getWorld("world").getSpawnLocation());
			}
			
		    Tetris.deleteWorld(w);
		    tetris_worlds.remove(w);
		}
	}
	
	@EventHandler
	public void openmenu(PlayerSwapHandItemsEvent e) {
		Player p = e.getPlayer();
		
		if (p.isSneaking()) {
			for (Board b : tetris_worlds.values()) {
				if (b.getPlayer() == p) {
					p.openInventory(get_tinv(b));
					return;
				}
			}
			
			p.openInventory(Tetris.ims.get("main_inv").getInventory());
			
			e.setCancelled(true);
		}
	}
	
	@EventHandler
	public void playermove(PlayerMoveEvent e) {
		double x = 5.5;
		double y = 70;
		double z = 20.5;
		
		if (tetris_worlds.keySet().contains(e.getPlayer().getWorld())) {
			Board b = tetris_worlds.get(e.getPlayer().getWorld());
			if (!b.flag) {e.setCancelled(true); return;}
			
			Tpiece tp = b.nowblock;
			Player p = b.getPlayer();
			if (e.getPlayer() != b.getPlayer()) {
				e.getPlayer().setGameMode(GameMode.SPECTATOR);
			}
			
			if (p.getLocation().getX() != x || p.getLocation().getY() != y ||
					p.getLocation().getZ() != z) {
				
				tp.remove_piece();
				
				if (p.getLocation().getX() > x) {
					if (b.settled(tp, "D")) {
						p.playSound(p.getLocation(), Sound.BLOCK_CHAIN_PLACE, 1f, 1f);
					} else {
						//블록 오른쪽 1픽셀 이동
						b.pointer[1] += 1;
					}
				}
				if (p.getLocation().getX() < x) {
					if (b.settled(tp, "A")) {
						p.playSound(p.getLocation(), Sound.BLOCK_CHAIN_PLACE, 1f, 1f);
					} else {
						//블록 왼쪽 1픽셀 이동
						b.pointer[1] -= 1;
					}
				}
				if (p.getLocation().getZ() > z && !b.settled(tp, "S")
						&& !p.isSprinting()) {
					//소프트 드랍
					
					b.pointer[0] -= 1;
					b.softdrop += 1;
				}
				if (p.getLocation().getZ() < z - 0.03) {
					//회전
					if (!tp.rotate()) {
						p.playSound(p.getLocation(), Sound.BLOCK_CHAIN_PLACE, 1f, 1f);
					}
					
				}
				if (p.getLocation().getY() > y) {
					//하드 드랍
				}
				
				tp.summon_piece();
				b.update_board();
				
				p.teleport(new Location(p.getWorld(), x, y, z, 180, 0));
				p.setSprinting(false);
			}
		}	
	}
	
	@EventHandler
	public void ClickEvent(InventoryClickEvent e) {
		if (e.getClickedInventory() == null) return;
		
		Player p = (Player) e.getWhoClicked();
		int slot = e.getSlot();
		ClickType clicktype = e.getClick();
		
		if (e.getClickedInventory() == 
				Tetris.ims.get("main_inv").getInventory()) {
			
			Inventory maininv = Tetris.ims.get("main_inv").getInventory();
			
			if (slot == 4) {
				//싱글 플레이
				p.closeInventory();
				genTetrisWorld(p);
			}
			
			e.setCancelled(true);
		} else if (ChatColor.stripColor(e.getView().getTitle()).equals("Tetris")) {
			
			World w = p.getWorld();
			Board b = tetris_worlds.get(w);
			
			if (e.getCurrentItem() != null) {
				if (slot == 2) {
					//게임 시작
					if (!b.flag) {
						b.startGame();
					} else {
						p.sendMessage(ChatColor.RED + "이미 게임 중입니다.");
					}
				} else if (slot == 4) {
					//환영 문구 등
				} else if (slot == 6) {
					//나가기
					for (Player ps : w.getPlayers()) {
						ps.teleport(Bukkit.getWorld("world").getSpawnLocation());
					}
					
				    Tetris.deleteWorld(w);
				    tetris_worlds.remove(w);
				    p.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
				}
				  
				e.setCancelled(true);
				p.closeInventory();
			}
		}
		
	}
	
	public void genTetrisWorld(Player p) {
		WorldCreator c = new WorldCreator("tetris_" + tworld_num);	
		c.generator(new VoidChunk());
		World w = c.createWorld();
		
		w.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
		w.setGameRule(GameRule.SHOW_DEATH_MESSAGES, false);
		
		tworld_num++;
		
		Board b = new Board(new Location(w, 0, 62, 0), 
				new Location(w, 9, 81, 0));
		b.setPlayer(p);
		
		tetris_worlds.put(w, b);
		p.teleport(new Location(w, 5.5, 70, 20.5, 180, 0));
	}
	
	public Inventory get_tinv(Board b) {
		InventoryManager tetris_room = new InventoryManager(9, ChatColor.RED + "Tetris");
		
		tetris_room.setitem(2, new ItemStackManager(Material.GOLD_NUGGET, ChatColor.GREEN + "시작").getItemStack());
		tetris_room.setitem(6, new ItemStackManager(Material.RED_DYE, ChatColor.RED + "나가기").getItemStack());
		
		ItemStackManager ism = new ItemStackManager(Material.PAPER, ChatColor.AQUA + "[테트리스 미니게임] 환영합니다!");
		ism.addLore("-------------------");
		ism.addLore(ChatColor.WHITE + "방 번호: " + ChatColor.GOLD + b.startpoint.getWorld().getName());
		ism.addLore(ChatColor.WHITE + "현재 플레이어: " + ChatColor.BLUE + b.getPlayer().getName());
		ism.addLore("-------------------");
		
		tetris_room.setitem(4, ism.getItemStack());
		
		return tetris_room.getInventory();
	}
}
