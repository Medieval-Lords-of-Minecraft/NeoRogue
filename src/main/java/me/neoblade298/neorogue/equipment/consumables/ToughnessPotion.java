package me.neoblade298.neorogue.equipment.consumables;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.Consumable;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.BuffStatTracker;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class ToughnessPotion extends Consumable {
	private static final String ID = "ToughnessPotion";
	private int reduc;

	public ToughnessPotion(boolean isUpgraded) {
		super(ID, "Toughness Potion", isUpgraded, Rarity.UNCOMMON, EquipmentClass.CLASSLESS);
		reduc = isUpgraded ? 5 : 3;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public TriggerResult runConsumableEffects(Player p, PlayerFightData data, int slot) {
		data.addDefenseBuff(DamageBuffType.of(DamageCategory.DIRECT),
				Buff.increase(data, reduc, BuffStatTracker.defenseBuffAlly(id + slot, this)));
		return TriggerResult.remove();
	}

	@Override
	public void setupItem() {
		item = createItem(Material.POTION,
				"Reduces " + GlossaryTag.DIRECT.tag(this) + " damage taken by " + DescUtil.val(reduc) + " for the rest of combat. Consumed on first use.");
	}

	@Override
	protected void modifyMeta(ItemMeta meta) {
		((PotionMeta) meta).setColor(Color.fromRGB(139, 90, 43));
	}
}
