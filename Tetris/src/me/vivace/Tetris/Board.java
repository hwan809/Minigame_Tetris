package me.vivace.Tetris;

import java.util.ArrayList;
import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public class Board {
	
	static int BOARD_X = 10;
	static int BOARD_Y = 20;
	static int BOARD_Z = 1;
	static int[] falling_speed = {48, 43, 38, 33, 28, 23, 18, 
			13, 8, 6, 5, 4, 3, 2, 1};
	
	static int[] transition = {10, 20, 30, 40, 50, 60, 70, 80, 90, 100, 
			100, 100, 100, 100, 100, 110, 120, 130, 140};
	
	Location startpoint, endpoint;
	
	boolean flag = false;	
	
	Tpiece nowblock, nextblock;
	private Material[][] gameboard = new Material[20][10];

	private Player gameplayer;
	private World w;
	int timer;
	
	private Scoreboard board;
	private Objective o;
	
	private Score s_level;
	private Score s_lines;
	private Score s_score;
	private Score s_level_lines;
	
	int softdrop = 0;
	
	/*
	 * 스코어링 
	 * 1. 소프트드랍 시 +1점 
	 * 2. 점수 획득: 
	 * 		Single - 40 * (레벨 + 1) 
	 * 		Double - 100 * (레벨 + 1)
	 * 		Triple - 300 * (레벨 + 1) '
	 * 		Tetris (4 lines) - 1200 * (레벨 + 1)
	 * 
	 * 3. 점수 계산에 들어가는 상수(레벨값)은
	 * 줄을 없애고 난 뒤의 레벨값을 따른다.
	 * 
	 * 트랜지션
	 * 1.
	 */
	
	int[] pointer = {19, 5};
	
	public Board(Location l, Location j) {
		if (!is_board_size(l, j)) {
			//Exception 처리
			Bukkit.broadcastMessage("size 아웃!");
			return;
		}
		
		double lx = Math.min(l.getX(), j.getX());
		double ly = Math.min(l.getY(), j.getY());
		double jx = Math.max(l.getX(), j.getX());
		double jy = Math.max(l.getY(), j.getY());
		
		this.startpoint = new Location(j.getWorld(), lx, ly, j.getZ());
		this.endpoint = new Location(l.getWorld(), jx, jy, l.getZ());
		this.w = l.getWorld();
		
		//init_gameboard
		this.init_board();
		
		update_board();
	}
	
	public void init_board() {
		for (int a = -1; a < 21; a++) {
			for (int b = -1; b < 11; b++) {
				
				Location temploc = new Location(this.w, this.startpoint.getX() + b,
						this.startpoint.getY() + a, this.startpoint.getZ());
				
				temploc.getBlock().setType(Material.BLACK_STAINED_GLASS);
				temploc.setX(temploc.getZ() - 1);
				temploc.getBlock().setType(Material.GLASS);
			}
		}
		
		for (int i = 0; i < this.gameboard.length; i++) {
            Arrays.fill(this.gameboard[i] , Material.AIR);
        }
		
		//발판
		Location pedal = new Location(this.w, 5, 69, 20);
		pedal.getBlock().setType(Material.BLACK_CONCRETE);
	}
	
	public boolean is_board_size(Location l, Location j) {
		
		boolean xsize = Math.abs(l.getBlockX() - j.getBlockX()) + 1 == BOARD_X;
		boolean ysize = Math.abs(l.getBlockY() - j.getBlockY()) + 1 == BOARD_Y;
		boolean zsize = Math.abs(l.getBlockZ() - j.getBlockZ()) + 1 == BOARD_Z;
		
		return xsize && ysize && zsize;
	}
	
	public void update_board() {
		
		for (int i = 0; i < 20; i++) {
			for (int j = 0; j < 10; j++) {
				
				Location temploc = new Location(this.w, this.startpoint.getX() + j,
						this.startpoint.getY() + i, this.startpoint.getZ());
				
				temploc.getBlock().setType(gameboard[i][j]);
			}
		}
	}
	
	public void edit_block(int x, int y, Material m) {
		this.gameboard[x][y] = m;
	}
	
	public void setPlayer(Player p) {
		this.gameplayer = p;
	}
	
	public Player getPlayer() {
		return gameplayer;
	}
	
	public void print_loc() {
		Bukkit.broadcastMessage(this.startpoint.toString());
		Bukkit.broadcastMessage(this.endpoint.toString());
	}
	
	public void startGame() {
		this.flag = true;
		for (Material[] m : this.gameboard) {
			Arrays.fill(m, Material.AIR);
		}
		
		this.board = Bukkit.getServer().getScoreboardManager().getNewScoreboard();
		this.o = this.board.registerNewObjective("test", "test");
		this.o.setDisplayName(ChatColor.BLUE + "!!!Tetris World!!!");
		this.o.setDisplaySlot(DisplaySlot.SIDEBAR);
		
		s_level = this.o.getScore("LEVEL:");
		s_score = this.o.getScore("SCORE:");
		s_lines = this.o.getScore("LINES:");
		s_level_lines = this.o.getScore("LEVEL_LINES: ");
		
		s_level.setScore(0);
		s_score.setScore(0);
		s_lines.setScore(0);
		s_level_lines.setScore(0);
		
		Tpiece nowpiece = new Tpiece(this, this.s_level.getScore(), false);
		Tpiece nextpiece = new Tpiece(this, this.s_level.getScore(), false);
		
		this.nowblock = nowpiece;
		this.nextblock = nextpiece;
		
		nowpiece.summon_piece();
		pointer_y();
	
		getPlayer().setScoreboard(this.board);
	}
	
	public String rainbow(String s) {
		String[] strArray = s.split("");
		String ret = "";
		
		for (int i = 0; i < strArray.length; i++) {
			ret += ChatColor.values()[i % ChatColor.values().length] + strArray[i];
		}
		
		return ret;
	}
	
	public void gameOver() {
		this.flag = false;
		
		getPlayer().sendTitle(ChatColor.RED + "GAME OVER", "점수: " + this.s_score.getScore());
		getPlayer().setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
		
//		for (Player ps : this.w.getPlayers()) {
//			ps.teleport(Bukkit.getWorld("world").getSpawnLocation());
//		}
//		
//	    Tetris.deleteWorld(w);
//	    TetrisEvents.tetris_worlds.remove(w);
	}
	
	public void pointer_y() {
		Board b = this;
		long a = falling_speed[this.s_level.getScore()];
		
		new BukkitRunnable() {
			
			@Override
			public void run() {	
				b.timer = this.getTaskId();
				Tpiece p = b.nowblock;
				if (settled(p, "S")) {
					b.nowblock = nextblock;
					b.nextblock = new Tpiece(b, b.s_level.getScore(), false);
						
					b.pointer[0] = 19;
					b.pointer[1] = 5;
					
					ArrayList<Material[]> board_list = new ArrayList<Material[]>(Arrays.asList(b.gameboard));
					int deleted_row = 0;
					
					for (int i = 0; i < board_list.size(); i++) {
						if (row_clear(b.gameboard[i])) {

							board_list.remove(i - deleted_row);
							deleted_row += 1;
							
							Material[] temp = new Material[10];
							Arrays.fill(temp, Material.AIR);
							
							board_list.add(temp);
						}
					}
					
					for (int j = 0; j < board_list.size(); j++) {
						b.gameboard[j] = board_list.get(j);
					}
					
					for (int k = 0; k < 4; k++) {
						int[] coor = b.nowblock.getblock(k);
						
						coor[0] += b.pointer[0];
						coor[1] += b.pointer[1];
						
						if (b.gameboard[coor[0]][coor[1]] != Material.AIR) {
							gameOver();
							
							cancel();
							return;
						}
					}
					
					b.s_level_lines.setScore(b.s_level_lines.getScore() + deleted_row);
					b.s_lines.setScore(b.s_lines.getScore() + deleted_row);
					
					int drop_points = b.softdrop;
					b.softdrop = 0;
					int line_points = 0;
					
					boolean level_up = false;
					
					if (b.transition[b.s_level.getScore()] <= b.s_level_lines.getScore()) {
						level_up = true;
						b.s_level_lines.setScore(b.s_level_lines.getScore() - 
								b.transition[b.s_level.getScore()]);
						b.s_level.setScore(b.s_level.getScore() + 1);
					}
					
					String row_prefix = "";
					
					if (deleted_row == 1) {
						line_points = 40 * (b.s_level.getScore() + 1);
						row_prefix = "!SINGLE!";
					} else if (deleted_row == 2) {
						line_points = 100 * (b.s_level.getScore() + 1);
						row_prefix = "!DOUBLE!";
					} else if (deleted_row == 3) {
						line_points = 300 * (b.s_level.getScore() + 1);
						row_prefix = "!TRIPLE!";
					} else if (deleted_row == 4) {
						line_points = 1200 * (b.s_level.getScore() + 1);
						row_prefix = "!TETRIS!";
					}
					
					int addpoints = drop_points + line_points;
					
					if (addpoints > 0) {
						if (level_up) {
							getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, 
								new TextComponent(ChatColor.GOLD + "LEVEL UP!"));
						} else if (deleted_row > 0) {
							getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, 
									new TextComponent(ChatColor.GOLD + row_prefix + " : " + line_points));
						}
					}
					
					b.s_score.setScore(b.s_score.getScore() + drop_points + line_points);
					
					b.nowblock.summon_piece();
					b.update_board();
						
					pointer_y();
						
					cancel();
					return;
				}
				
				p.remove_piece();
				b.pointer[0]--;
				p.summon_piece();
				b.update_board();
			}
			
		}.runTaskTimer(Tetris.getPlugin(Tetris.class), a, a);
	}
	
	public boolean settled(Tpiece p, String s) {
		
		for (int i = 0; i < 4; i++) {
			int underblockx = p.getblock(i)[0] + this.pointer[0];
			int underblocky = p.getblock(i)[1] + this.pointer[1];
			
			if (s == "S") {
				underblockx -= 1;
			} else if (s == "A") {
				underblocky -= 1;
			} else if (s == "D") {
				underblocky += 1;
			}
			
			if (underblockx == -1 || underblocky <= -1 || underblocky >= 10) return true;
			
			boolean isair = this.gameboard[underblockx][underblocky] == Material.AIR;
			boolean isselfblock = false;
			
			for (int j = 0; j < 4; j++) {
				int tempblockx = p.getblock(j)[0] + this.pointer[0];
				int tempblocky = p.getblock(j)[1] + this.pointer[1];
				
				if (underblockx == tempblockx && underblocky == tempblocky) {
					isselfblock = true;
				}
			}
			if (!isair && !isselfblock) {
				return true;
			}
		}
		
		return false;
	}
	
	public boolean row_clear(Material[] row) {
		for (Material m : row) {
			if (m == Material.AIR) {
				
				return false;
			}
		}
		
		return true;
	}
	
	public Material[][] getGameboard() {
		return this.gameboard;
	}
}