package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageSlice;
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.PreBasicAttackEvent;

public class StaticSurge extends Equipment {
	private static final String ID = "StaticSurge";
	private int damage, electrified;
	
	public StaticSurge(boolean isUpgraded) {
		super(ID, "Static Surge", isUpgraded, Rarity.RARE, EquipmentClass.THIEF, EquipmentType.ABILITY,
				EquipmentProperties.ofUsable(20, 10, 0, 0));
		damage = isUpgraded ? 50 : 30;
		electrified = isUpgraded ? 8 : 5;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, bind, new EquipmentInstance(data, this, slot, es, (pdata, in) -> {
			Sounds.equip.play(data.getPlayer(), data.getPlayer());

			ActionMeta am = new ActionMeta();
			data.addTrigger(id, Trigger.TOGGLE_SPRINT, (pdata2, in2) -> {
				Player p = data.getPlayer();
				if (p.isSprinting()) {
					am.setTime(System.currentTimeMillis());
				} else {
					am.setTime(0);
				}
				return TriggerResult.keep();
			});

			data.addTrigger(id, Trigger.PRE_BASIC_ATTACK, (pdata3, in3) -> {
				if (am.getTime() == 0 || System.currentTimeMillis() - am.getTime() < 1000) {
					return TriggerResult.keep();
				}
				PreBasicAttackEvent ev = (PreBasicAttackEvent) in3;
				ev.getMeta().addDamageSlice(
						new DamageSlice(data, damage, DamageType.LIGHTNING, DamageStatTracker.of(id + slot, this)));
				FightInstance.applyStatus(ev.getTarget(), StatusType.ELECTRIFIED, data, electrified, -1);
				return TriggerResult.keep();
			});

			return TriggerResult.remove();
		}));
	}

	@Override
	public void setupItem() {
		item = createItem(Material.LIGHTNING_ROD,
				GlossaryTag.POWER.tag(this) + ". If you have been sprinting for at least " + DescUtil.white("1s") + ", your basic attacks deal an additional " + 
				GlossaryTag.LIGHTNING.tag(this, damage, true) + " damage and apply " + 
				GlossaryTag.ELECTRIFIED.tag(this, electrified, true) + ".");
	}
}
