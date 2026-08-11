package me.neoblade298.neorogue.session.chance.builtin;

import org.bukkit.Material;

import me.neoblade298.neorogue.region.RegionType;
import me.neoblade298.neorogue.session.chance.ChanceChoice;
import me.neoblade298.neorogue.session.chance.ChanceSet;
import me.neoblade298.neorogue.session.chance.ChanceStage;
import me.neoblade298.neorogue.session.instances.ShopInstance;

public class TravellingMerchantChance extends ChanceSet {

	public TravellingMerchantChance() {
		super(new RegionType[] { RegionType.LOW_DISTRICT, RegionType.HARVEST_FIELDS, RegionType.FROZEN_WASTES },
				Material.EMERALD, "TravellingMerchant", "Travelling Merchant");

		ChanceStage stage = new ChanceStage(this, INIT_ID,
				"A travelling merchant waves your party over to a cart stocked with unusual wares.");
		stage.addChoice(new ChanceChoice(Material.CHEST, "Browse the wares",
				"Visit the merchant's shop.",
				(s, inst, data) -> {
					inst.setNextInstance(new ShopInstance(s));
					return null;
				}));
		stage.addChoice(new ChanceChoice(Material.BARRIER, "Turn down the merchant",
				"Man, I JUST had a shop, what are you doing here?",
				(s, inst, data) -> null));
	}
}