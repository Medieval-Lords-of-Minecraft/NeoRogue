package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.buff.StatTracker;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class Intuition extends Equipment {
	private static final String ID = "intuition";
	private int damage;
	private double regen;
	private static final int THRES = 30;
	
	public Intuition(boolean isUpgraded) {
		super(ID, "Intuition", isUpgraded, Rarity.COMMON, EquipmentClass.MAGE,
				EquipmentType.ABILITY, EquipmentProperties.none());
				damage = isUpgraded ? 15 : 10;
				regen = 0.3;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addDamageBuff(DamageBuffType.of(DamageCategory.MAGICAL), new Buff(data, damage, 0, StatTracker.damageBuffAlly(this)));
		data.addTrigger(id, Trigger.PLAYER_TICK, (pdata, in) -> {
			if (data.getMana() < THRES) data.addMana(regen);
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.ENDER_EYE,
				"Passive. Increase all " + GlossaryTag.MAGICAL.tag(this) + " damage by " + DescUtil.yellow(damage) + ". " +
				"While below " + DescUtil.white(THRES) + " mana, mana regen is increased by " + DescUtil.white(regen) + ".");
	}
}
