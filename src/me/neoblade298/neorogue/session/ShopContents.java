package me.neoblade298.neorogue.session;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Random;

import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.equipment.Artifact;
import me.neoblade298.neorogue.equipment.Consumable;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Equipment.EquipmentClass;
import me.neoblade298.neorogue.equipment.accessories.ArmorStand;
import me.neoblade298.neorogue.equipment.accessories.Lockbox;
import me.neoblade298.neorogue.equipment.artifacts.EmeraldCluster;
import me.neoblade298.neorogue.equipment.artifacts.Enderchest;
import me.neoblade298.neorogue.equipment.artifacts.RubyCluster;
import me.neoblade298.neorogue.equipment.artifacts.SapphireCluster;
import me.neoblade298.neorogue.player.PlayerSessionData;

public class ShopContents {
	private static final Equipment[] GEMS = new Equipment[] { RubyCluster.get(), SapphireCluster.get(), EmeraldCluster.get() };
	private HashMap<Integer, ShopItem> shopItems = new HashMap<Integer, ShopItem>();

	public ShopContents(Session s, PlayerSessionData data, double discountMult) {
		int value = s.getBaseDropValue();
		EquipmentClass ec = data.getPlayerClass();
		generateEquips(s, ec, value, discountMult); // 0-9
		generateConsumables(ec, value, discountMult); // 10-12
		generateGems(discountMult); // 13-15
		generateArtifacts(data, value, discountMult); // 16-18
		generateShopArtifacts(ec, value, discountMult); // 19-21
	}

	private ShopContents(HashMap<Integer, ShopItem> shopItems) {
		this.shopItems = shopItems;
	}
	
	private ShopContents() {

	}

	public ShopItem get(int idx) {
		return shopItems.get(idx);
	}

	public static void debugGenerateEquips() {
		ShopContents sc = new ShopContents();
		Session s = null;
		Random rand = new Random();
		for (int i = 0; i < 1000; i++) {
			sc.generateEquips(s, EquipmentClass.WARRIOR, rand.nextInt(4), 1.0);
			sc.generateEquips(s, EquipmentClass.THIEF, rand.nextInt(4), 1.0);
			sc.generateEquips(s, EquipmentClass.ARCHER, rand.nextInt(4), 1.0);
			sc.generateEquips(s, EquipmentClass.MAGE, rand.nextInt(4), 1.0);

			if (i % 10 == 0) {
				System.out.println("Generated " + (i + 1) * 7 + " sets of shop equips/consumables");
			}
		}
	}

	private void generateEquips(Session s, EquipmentClass ec, int value, double discountMult) {
		// Create shop contents
		ArrayList<Equipment> equips = new ArrayList<Equipment>();
		equips.addAll(Equipment.getDrop(value, ShopInstance.NUM_ITEMS / 2, ec, EquipmentClass.SHOP, EquipmentClass.CLASSLESS));
		equips.addAll(Equipment.getDrop(value + 2, ShopInstance.NUM_ITEMS / 2, equips, ec, EquipmentClass.SHOP, EquipmentClass.CLASSLESS));
		if (s != null) s.rollUpgrades(equips, 0); // Ignore session for debugging
		
		// Generate 2 random unique sale slots
		HashSet<Integer> saleSlots = new HashSet<Integer>(2);
		while (saleSlots.size() < 2) {
			saleSlots.add(NeoRogue.gen.nextInt(equips.size()));
		}

		// First row of items
		for (int i = 0; i < 5; i++) {
			boolean sale = saleSlots.contains(i);
			int price = NeoRogue.gen.nextInt((int) (100 * discountMult), (int) (150 * discountMult));
			if (sale)
				price = (int) (price * NeoRogue.gen.nextDouble(0.4, 0.8));
			shopItems.put(i, new ShopItem(equips.get(i), price, sale));
		}

		// Second row, more expensive
		for (int i = 5; i < 10; i++) {
			boolean sale = saleSlots.contains(i);
			int price = NeoRogue.gen.nextInt((int) (150 * discountMult), (int) (200 * discountMult));
			if (sale)
				price = (int) (price * NeoRogue.gen.nextDouble(0.4, 0.8));
			shopItems.put(i, new ShopItem(equips.get(i), price, sale));
		}
	}

	private void generateConsumables(EquipmentClass ec, int value, double discountMult) {
		int idx = 10;
		for (Consumable cons : Equipment.getConsumable(value, 3, ec, EquipmentClass.SHOP, EquipmentClass.CLASSLESS)) {
			int price = NeoRogue.gen.nextInt((int) (30 * discountMult), (int) (60 * discountMult));
			shopItems.put(idx++, new ShopItem(NeoRogue.gen.nextDouble() >= 0.7 ? cons.getUpgraded() : cons, price, false));
		}
	}

	private void generateGems(double discountMult) {
		int idx = 13;
		for (Equipment gem : GEMS) {
			int price = NeoRogue.gen.nextInt((int) (100 * discountMult), (int) (200 * discountMult));
			shopItems.put(idx++, new ShopItem(gem, price, false));
		}
	}

	private void generateArtifacts(PlayerSessionData data, int value, double discountMult) {
		int idx = 16;
		for (Artifact art : Equipment.getArtifact(data.getArtifactDroptable(), value, 3, data.getPlayerClass(), EquipmentClass.SHOP, EquipmentClass.CLASSLESS)) {
			int price = NeoRogue.gen.nextInt((int) (150 * discountMult), (int) (250 * discountMult));
			shopItems.put(idx++, new ShopItem(art, price, false));
		}
	}

	private void generateShopArtifacts(EquipmentClass ec, int value, double discountMult) {
		int idx = 19;
		for (Artifact art : new Artifact[] { (Artifact) ArmorStand.get(), (Artifact) Lockbox.get(), (Artifact) Enderchest.get() }) {
			int price = NeoRogue.gen.nextInt((int) (100 * discountMult), (int) (200 * discountMult));
			shopItems.put(idx++, new ShopItem(art, price, false));
		}
	}
	
	
	public String serialize() {
		String serialized = "";
		boolean first = true;
		for (Entry<Integer, ShopItem> ent : shopItems.entrySet()) {
			if (!first) {
				serialized += ",";
			}
			first = false;
			serialized += ent.getKey() + ":" + ent.getValue().serialize();
		}
		return serialized;
	}
	
	public static ShopContents deserializeShopItems(String str) {
		HashMap<Integer, ShopItem> contents = new HashMap<Integer, ShopItem>();
		String[] split = str.split(",");
		for (int i = 0; i < split.length; i++) {
			String[] subsplit = split[i].split(":", 2);
			int idx = Integer.parseInt(subsplit[0]);
			ShopItem item = ShopItem.deserialize(subsplit[1]);
			contents.put(idx, item);
		}
		return new ShopContents(contents);
	}
}
