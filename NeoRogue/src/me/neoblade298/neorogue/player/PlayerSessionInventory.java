package me.neoblade298.neorogue.player;

import java.util.ArrayList;

import me.neoblade298.neorogue.equipment.*;

public class PlayerSessionInventory {
	private ArrayList<Weapon> weapons = new ArrayList<Weapon>(3);
	private ArrayList<Armor> armors = new ArrayList<Armor>(3);
	private Offhand offhand;
	private ArrayList<Accessory> accessories = new ArrayList<Accessory>(6);
	private ArrayList<Artifact> artifacts = new ArrayList<Artifact>();
}
