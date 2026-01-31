package me.neoblade298.neorogue.equipment.weapons;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageSlice;
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.LeftClickHitEvent;
import me.neoblade298.neorogue.session.fight.trigger.event.PreBasicAttackEvent;

public class ToxicRazor extends Equipment {
	private static final String ID = "ToxicRazor";
	private int threshold, bonus;
	
	public ToxicRazor(boolean isUpgraded) {
		super(ID, "Toxic Razor", isUpgraded, Rarity.RARE, EquipmentClass.THIEF,
				EquipmentType.WEAPON,
				EquipmentProperties.ofWeapon(50, 1, 0.2, DamageType.PIERCING, Sound.ENTITY_PLAYER_ATTACK_SWEEP));
		threshold = isUpgraded ? 150 : 200;
		bonus = 5;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addSlotBasedTrigger(id, slot, Trigger.LEFT_CLICK_HIT, (pdata, inputs) -> {
			LeftClickHitEvent ev = (LeftClickHitEvent) inputs;
			weaponSwingAndDamage(p, data, ev.getTarget());
			return TriggerResult.keep();
		});
		
		data.addTrigger(id, Trigger.PRE_BASIC_ATTACK, (pdata, in) -> {
			PreBasicAttackEvent ev = (PreBasicAttackEvent) in;
			if (!ev.getWeapon().equals(this)) return TriggerResult.keep();
			
			int poisonApplied = pdata.getStats().getStatusesApplied().getOrDefault(StatusType.POISON, 0);
			int extraDamage = (poisonApplied / threshold) * bonus;
			
			if (extraDamage > 0) {
				ev.getMeta().addDamageSlice(new DamageSlice(pdata, extraDamage, DamageType.PIERCING, DamageStatTracker.of(id + slot, this)));
			}
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.PRISMARINE_SHARD,
				"Deal an additional " + DescUtil.white(bonus) + " damage for every " + DescUtil.yellow(threshold) +
				" " + GlossaryTag.POISON.tag(this) + " stacks you have applied this fight.");
	}
}
