package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Power;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.DamageSlice;
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.PreDealDamageEvent;

public class StonePlating extends Equipment implements Power {
	private static final String ID = "StonePlating";
	private int shields;
	private double bonusPercent;

	public StonePlating(boolean isUpgraded) {
		super(ID, "Stone Plating", isUpgraded, Rarity.UNCOMMON, EquipmentClass.MAGE,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(0, 0, 0, 0));
		shields = isUpgraded ? 10 : 6;
		bonusPercent = isUpgraded ? 0.25 : 0.15;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, Trigger.PLAYER_TICK, (pdata, in) -> {
			if (data.getStamina() < data.getMaxStamina() * 0.5) return TriggerResult.keep();

			if (activatePower(data, slot, es)) return TriggerResult.remove();
			return TriggerResult.keep();
		});
	}

	@Override
	public void onPowerActivated(PlayerFightData data, int slot, EquipSlot es) {
		Player p = data.getPlayer();
		data.addPermanentShield(p.getUniqueId(), shields);

		// Add persistent trigger for bonus blunt damage on magical hits while shielded
		data.addTrigger(id, Trigger.PRE_DEAL_DAMAGE, (pdata2, in2) -> {
			if (data.getShields().isEmpty()) return TriggerResult.keep();
			PreDealDamageEvent ev = (PreDealDamageEvent) in2;
			if (!ev.getMeta().containsType(DamageCategory.MAGICAL)) return TriggerResult.keep();

			// Sum up magical damage from post-mitigation slices
			double magicalDamage = 0;
			for (java.util.Map.Entry<DamageType, Double> entry : ev.getMeta().getPostMitigationDamage().entrySet()) {
				if (DamageCategory.MAGICAL.hasType(entry.getKey())) {
					magicalDamage += entry.getValue();
				}
			}
			if (magicalDamage <= 0) return TriggerResult.keep();

			double bonusDamage = magicalDamage * bonusPercent;
			ev.getMeta().addDamageSlice(new DamageSlice(data, bonusDamage, DamageType.BLUNT, DamageStatTracker.of(id + slot, this)));
			return TriggerResult.keep();
		});
	}


	@Override
	public void setupItem() {
		item = createItem(Material.DEEPSLATE,
				GlossaryTag.POWER.tag(this) + ". Activates after reaching " + DescUtil.white("50%") + " stamina. Gain " + GlossaryTag.SHIELDS.tag(this, shields, true)
				+ ". While you have " + GlossaryTag.SHIELDS.tag(this)
				+ ", deal " + DescUtil.yellow((int) (bonusPercent * 100) + "%")
				+ " of " + GlossaryTag.MAGICAL.tag(this) + " damage dealt as additional "
				+ GlossaryTag.BLUNT.tag(this) + " damage.");
	}
}
