package com.nisovin.magicspells.util;

import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;

import com.nisovin.magicspells.MagicSpells;

public class V1_11EntityTypeHandler {
	
	private static boolean initialized = false;
	private static boolean v_1_11API = false;
	private static boolean one_12 = false;
	
	public static void initialize() {
		if (initialized) return;
		
		EntityType type = null;
		try {
			type = EntityType.valueOf("STRAY");
		} catch (Throwable ignored) {
			// No op
		}
		if (type != null) v_1_11API = true;
		
		one_12 = Bukkit.getServer().getVersion().contains("1.12");
		
		initialized = true;
	}
	
	public static boolean is1_12() {
		return one_12;
	}
	
	public static boolean newEntityTypesPresent() {
		initialize();
		return v_1_11API;
	}
	
	public static boolean isInitialized() {
		return initialized;
	}
}
