package me.vivace.Tetris;

import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Material;

public class Tpiece {
	
	//board_locs[블록 타입 (0 ~ 6)][블록 회전 (0 ~ 3)][블록 개수][0 : y, 1 : x]
	//game pointer = 19, 5
	
	static int[][][][] board_locs = {
		
		//I piece 
			{
		{{0, -2}, {0, -1}, {0, 0}, {0, 1}}, 
		{{-2, 0}, {-1, 0}, {0, 0}, {1, 0}}, 
		{{-1, -2}, {-1, -1}, {-1, 0}, {-1, 1}}, 
		{{-2, -1}, {-1, -1}, {0, -1}, {1, -1}}
			},
			
		//L piece
			{
		{{0, 0}, {-1, -2}, {-1, -1}, {-1, 0}}, 
		{{0, -1}, {-1, -1}, {-2, -1}, {-2, 0}}, 
		{{-1, -2}, {-1, -1}, {-1, 0}, {-2, -2}}, 
		{{0, -1}, {-1, -1}, {-2, -1}, {0, -2}}
			},
		
		//J piece	
			{
		{{0, -2}, {-1, -2}, {-1, -1}, {-1, 0}}, 
		{{0, -1}, {-1, -1}, {-2, -1}, {0, 0}}, 
		{{-2, 0}, {-1, -2}, {-1, -1}, {-1, 0}}, 
		{{0, -1}, {-1, -1}, {-2, -1}, {-2, -2}}
			},
		
		//S piece
			{
		{{0, -1}, {0, 0}, {-1, -2}, {-1, -1}}, 
		{{0, -1}, {-1, -1}, {-1, 0}, {-2, 0}}, 
		{{-1, -1}, {-1, 0}, {-2, -2}, {-2, -1}}, 
		{{0, -2}, {-1, -2}, {-1, -1}, {-2, -1}}
			},
					
		//Z piece
			{
		{{0, -2}, {0, -1}, {-1, -1}, {-1, 0}}, 
		{{0, 0}, {-1, -1}, {-1, 0}, {-2, -1}}, 
		{{-1, -2}, {-1, -1}, {-2, -1}, {-2, 0}}, 
		{{0, -1}, {-1, -2}, {-1, -1}, {-2, -2}}
			},
		
		//T piece
			{
		{{0, -1}, {-1, -2}, {-1, -1}, {-1, 0}}, 
		{{0, -1}, {-2, -1}, {-1, -1}, {-1, 0}}, 
		{{-2, -1}, {-1, -2}, {-1, -1}, {-1, 0}}, 
		{{0, -1}, {-1, -2}, {-1, -1}, {-2, -1}}
			},
		
		//O piece
			{
		{{0, -1}, {0, 0}, {-1, -1}, {-1, 0}}, 
		{{0, -1}, {0, 0}, {-1, -1}, {-1, 0}}, 
		{{0, -1}, {0, 0}, {-1, -1}, {-1, 0}}, 
		{{0, -1}, {0, 0}, {-1, -1}, {-1, 0}}
			}};
	
	//block_colors[RGB (0 ~ 2)][세분화된 색 (0 ~ 2)]
	
	static Material[][] block_colors = {{Material.BLUE_WOOL, Material.LAPIS_BLOCK, Material.LAPIS_ORE},
										{Material.RED_WOOL, Material.NETHER_WART_BLOCK, Material.REDSTONE_ORE},
										{Material.GREEN_CONCRETE, Material.DRIED_KELP_BLOCK, Material.MOSSY_COBBLESTONE}};
	
	//board_loc[블록 개수][0 : y, 1 : x]
	private int[][] board_loc = new int[4][2];
	
	private int blocktype;
	private int rotation;
	
	private Material block_color;
	
	private Board b;
	
	public Tpiece(Board b, int level, boolean withrotation) {
		this.board_loc = get_random_piece(withrotation);
		this.block_color = get_random_color(level);
		this.b = b;
		this.rotation = 0;
	}
	
	public Tpiece(int i) {
		this.board_loc = board_locs[i][0];
		this.block_color = get_random_color();
	}
	
	public void remove_piece() {
		for (int[] xy : this.board_loc.clone()) {
			
			int xx = xy.clone()[0] += b.pointer[0];
			int yy = xy.clone()[1] += b.pointer[1];
			
			this.b.edit_block(xx, yy, Material.AIR);
		}
		
		this.b.update_board();
	}
	
	public void summon_piece() {
		
		for (int[] xy : this.board_loc.clone()) {
			
			int xx = xy.clone()[0] += b.pointer[0];
			int yy = xy.clone()[1] += b.pointer[1];
			
			this.b.edit_block(xx, yy, this.block_color);
		}
		
		this.b.update_board();
	}
	
	public boolean rotate() {
		int newrotation = 0;
		Material[][] gameboard = this.b.getGameboard();
		
		if (this.rotation != 3) {
			newrotation = this.rotation + 1;
		}
		
		int[][] newlocs = board_locs[this.blocktype][newrotation];
	
		for (int[] newblock : newlocs) {
			int x = newblock[0] + this.b.pointer[0];
			int y = newblock[1] + this.b.pointer[1];
			
			if (x > 19 || x < 0 ||
					y > 9 || y < 0) {
				return false;
			}
			
			boolean isair = gameboard[x][y] == Material.AIR;
			boolean isselfblock = false;
			
			for (int[] nowblock : this.board_loc) {
				int xx = nowblock[0] + this.b.pointer[0];
				int yy = nowblock[1] + this.b.pointer[1];
				
				if (x == xx && y == yy) {
					isselfblock = true;
				}
			}
			if (!isair && !isselfblock) {
				return false;
			}
		}
		
		this.remove_piece();
		this.board_loc = newlocs;
		this.summon_piece();
		this.b.update_board();
		this.rotation = newrotation;
		
		return true;
	}
	
	public int[][] get_random_piece(boolean withrotation) {
		Random r = new Random();
		int i = r.nextInt(7);
		this.blocktype = i;
		
		if (withrotation) {
			return board_locs[i][r.nextInt(3)];
		} else {
			return board_locs[i][0];
		}
	}
	
	public Material get_random_color() {
		Random r = new Random();
		
		return block_colors.clone()[r.nextInt(3)][r.nextInt(3)];
	}
	
	public Material get_random_color(int i) {
		Random r = new Random();
		
		return block_colors.clone()[i % 3][r.nextInt(3)];
		
		
	}
	
	public int[] getblock(int i) {
		return this.board_loc[i].clone();
	}
	
	public Material getBlockColor() {
		return this.block_color;
	}
}
