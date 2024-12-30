package me.neoblade298.neorogue.session;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.equipment.Artifact;
import me.neoblade298.neorogue.equipment.Consumable;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Equipment.EquipmentClass;
import me.neoblade298.neorogue.equipment.artifacts.EmeraldCluster;
import me.neoblade298.neorogue.equipment.artifacts.RubyCluster;
import me.neoblade298.neorogue.equipment.artifacts.SapphireCluster;
import me.neoblade298.neorogue.player.PlayerSessionData;

public class ShopContents {
	private static final Equipment[] GEMS = new Equipment[] { RubyCluster.get(), SapphireCluster.get(), EmeraldCluster.get() };
	private HashMap<Integer, ShopItem> shopItems = new HashMap<Integer, ShopItem>();

	public ShopContents(Session s, PlayerSessionData data) {
		generateEquips(s, data); // 0-9
		generateConsumables(s, data); // 10-12
		generateGems(); // 13-15
		generateArtifacts(s, data); // 16-18
	}

	private ShopContents(HashMap<Integer, ShopItem> shopItems) {
		this.shopItems = shopItems;
	}

	public ShopItem get(int idx) {
		return shopItems.get(idx);
	}

	private void generateEquips(Session s, PlayerSessionData data) {
		EquipmentClass ec = data.getPlayerClass();
		// Create shop contents
		ArrayList<Equipment> equips = new ArrayList<Equipment>();
		equips.addAll(Equipment.getDrop(s.getAreasCompleted() + 0, ShopInstance.NUM_ITEMS / 2, ec, EquipmentClass.SHOP, EquipmentClass.CLASSLESS));
		equips.addAll(Equipment.getDrop(s.getAreasCompleted() + 1, ShopInstance.NUM_ITEMS / 2, ec, EquipmentClass.SHOP, EquipmentClass.CLASSLESS));
		
		// Generate 2 random unique sale slots
		HashSet<Integer> saleSlots = new HashSet<Integer>(2);
		while (saleSlots.size() < 2) {
			saleSlots.add(NeoRogue.gen.nextInt(equips.size()));
		}

		// First row of items
		for (int i = 0; i < 5; i++) {
			boolean sale = saleSlots.contains(i);
			int price = NeoRogue.gen.nextInt(100, 150);
			if (sale)
				price = (int) (price * NeoRogue.gen.nextDouble(0.4, 0.8));
			shopItems.put(i, new ShopItem(equips.get(i), price, sale));
		}

		// Second row, more expensive
		for (int i = 5; i < 10; i++) {
			boolean sale = saleSlots.contains(i);
			int price = NeoRogue.gen.nextInt(150, 200);
			if (sale)
				price = (int) (price * NeoRogue.gen.nextDouble(0.4, 0.8));
			shopItems.put(i, new ShopItem(equips.get(i), price, sale));
		}
	}

	private void generateConsumables(Session s, PlayerSessionData data) {
		EquipmentClass ec = data.getPlayerClass();
		int idx = 10;
		for (Consumable cons : Equipment.getConsumable(s.getAreasCompleted(), 3, ec, EquipmentClass.SHOP, EquipmentClass.CLASSLESS)) {
			int price = NeoRogue.gen.nextInt(30, 60);
			shopItems.put(idx++, new ShopItem(cons, price, false));
		}
	}

	private void generateGems() {
		int idx = 13;
		for (Equipment gem : GEMS) {
			int price = NeoRogue.gen.nextInt(100, 200);
			shopItems.put(idx++, new ShopItem(gem, price, false));
		}
	}

	private void generateArtifacts(Session s, PlayerSessionData data) {
		EquipmentClass ec = data.getPlayerClass();
		int idx = 16;
		for (Artifact art : Equipment.getArtifact(data.getArtifactDroptable(), s.getAreasCompleted() + 2, 3, ec, EquipmentClass.SHOP, EquipmentClass.CLASSLESS)) {
			int price = NeoRogue.gen.nextInt(150, 250);
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
			String[] subsplit = str.split(":");
			int idx = Integer.parseInt(subsplit[0]);
			ShopItem item = ShopItem.deserialize(subsplit[1]);
			contents.put(idx, item);
		}
		return new ShopContents(contents);
	}
}
