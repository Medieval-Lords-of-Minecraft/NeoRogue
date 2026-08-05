package me.neoblade298.neorogue.equipment.weapons;
import org.bukkit.Material;
import org.bukkit.Sound;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.equipment.abilities.BasicManaManipulation;
import me.neoblade298.neorogue.equipment.abilities.Resourcefulness;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageSlice;
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.LeftClickHitEvent;
import me.neoblade298.neorogue.session.fight.trigger.event.PreBasicAttackEvent;

public class SparkKnife extends Equipment {
	private static final String ID = "SparkKnife";
	private static final int HITS_PER_ELECTRIFIED = 5;
	private int elec, bonusDamage;
	
	public SparkKnife(boolean isUpgraded) {
		super(ID, "Spark Knife", isUpgraded, Rarity.COMMON, EquipmentClass.THIEF,
				EquipmentType.WEAPON,
				EquipmentProperties.ofWeapon(isUpgraded ? 35 : 30, 1, 0.2, DamageType.SLASHING, Sound.ENTITY_PLAYER_ATTACK_SWEEP));
		elec = isUpgraded ? 3 : 2;
		bonusDamage = isUpgraded ? 10 : 5;
	}

	@Override
	public void setupReforges() {
		addReforge(BasicManaManipulation.get(), ElectromagneticKnife.get());
		addReforge(Resourcefulness.get(), SparkdrainKnife.get(), LightningCutter.get());
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		int[] hits = { 0 };
		data.addSlotBasedTrigger(id, slot, Trigger.LEFT_CLICK_HIT, (pdata, inputs) -> {
			LeftClickHitEvent ev = (LeftClickHitEvent) inputs;
			weaponSwingAndDamage(pdata.getPlayer(), data, ev.getTarget());
			if (++hits[0] >= HITS_PER_ELECTRIFIED) {
				FightInstance.applyStatus(ev.getTarget(), StatusType.ELECTRIFIED, data, elec, -1, this);
				hits[0] = 0;
			}
			return TriggerResult.keep();
		});

		data.addTrigger(id, Trigger.PRE_BASIC_ATTACK, (pdata, inputs) -> {
			PreBasicAttackEvent ev = (PreBasicAttackEvent) inputs;
			if (!ev.getWeapon().equals(this)) return TriggerResult.keep();
			FightData target = FightInstance.getFightData(ev.getTarget());
			if (target != null && target.hasStatus(StatusType.ELECTRIFIED)) {
				ev.getMeta().addDamageSlice(new DamageSlice(pdata, bonusDamage, DamageType.SLASHING,
						DamageStatTracker.of(id + slot, this)));
			}
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.STONE_SWORD,
				"Every " + DescUtil.white(HITS_PER_ELECTRIFIED) + " basic attacks apply "
						+ GlossaryTag.ELECTRIFIED.tag(this, elec, true) + ". Deal "
						+ DescUtil.yellow(bonusDamage) + " additional damage to "
						+ GlossaryTag.ELECTRIFIED.tag(this) + " enemies.");
	}
}
