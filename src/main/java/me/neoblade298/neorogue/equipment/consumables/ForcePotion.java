package me.neoblade298.neorogue.equipment.consumables;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.PotionMeta;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.Consumable;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.buff.StatTracker;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class ForcePotion extends Consumable {
	private static final String ID = "ForcePotion";
	private double damageBuff;

	public ForcePotion(boolean isUpgraded) {
		super(ID, "Force Potion", isUpgraded, Rarity.UNCOMMON, EquipmentClass.CLASSLESS);
		damageBuff = isUpgraded ? 0.6 : 0.4;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public TriggerResult runConsumableEffects(Player p, PlayerFightData data, int slot) {
		data.addDamageBuff(DamageBuffType.of(DamageCategory.GENERAL),
				Buff.multiplier(data, damageBuff, StatTracker.damageBuffAlly(id, this)), 400);
		return TriggerResult.remove();
	}

	@Override
	public void setupItem() {
		int buffPct = (int) (damageBuff * 100);
		item = createItem(Material.POTION,
				"Increases " + GlossaryTag.GENERAL.tag(this) + " damage by " + DescUtil.val(buffPct + "%") + " for [<white>20s</white>]. Consumed on first use.");
		PotionMeta meta = (PotionMeta) item.getItemMeta();
		meta.setColor(Color.fromRGB(255, 69, 0));
		item.setItemMeta(meta);
	}
}
