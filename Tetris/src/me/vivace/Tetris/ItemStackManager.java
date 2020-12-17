package me.vivace.Tetris;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ItemStackManager {
	private ItemStack i;
	
	public ItemStackManager(Material m, String s) {
		ItemStack temp = new ItemStack(m);
		ItemMeta imeta = temp.getItemMeta();
		imeta.setDisplayName(s);
		temp.setItemMeta(imeta);
		
		this.i = temp;
	}
	
	public void setLore(String s) {
		ItemStack item = this.getItemStack();
		ItemMeta imeta = item.getItemMeta();
		List<String> lores = new ArrayList<String>();
		lores.add(s);
		
		imeta.setLore(lores);
		item.setItemMeta(imeta);
	}
	
	public void addLore(String s) {
		ItemStack item = this.getItemStack();
		ItemMeta imeta = item.getItemMeta();
		List<String> lores;
		if (imeta.hasLore()) {
			lores = imeta.getLore();
		} else {
			lores = new ArrayList<String>();
		}
		
		lores.add(s);
		
		imeta.setLore(lores);
		item.setItemMeta(imeta);
	}
	
	public ItemStack getItemStack() {
		return this.i;
	}
}
