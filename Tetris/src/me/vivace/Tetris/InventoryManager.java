package me.vivace.Tetris;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class InventoryManager {
	
	private Inventory i;
	private HashMap<Integer, InventoryManager> link_map = new HashMap<Integer, InventoryManager>();
	
	public InventoryManager(int slotsize, String s) {
		this.i = Bukkit.createInventory(null, slotsize, s);
	}
	
	public void link(int slot, ItemStack i, InventoryManager im) {
		this.i.setItem(slot, i);
		this.link_map.put(slot, im);
	}
	
	public void setitem(int slot, ItemStack i) {
		this.i.setItem(slot, i);
	}
	
	public void open_im(Player p, InventoryManager im) {
		p.openInventory(im.i);
	}
	
	public Inventory getInventory() {
		return this.i;
	}
}
