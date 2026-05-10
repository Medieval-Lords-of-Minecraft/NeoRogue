package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.BuffStatTracker;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.WeaponSwingEvent;

public class Frenzy extends Equipment {
	private static final String ID = "Frenzy";
	private int atkSpeed;
	private static final int CUTOFF = 5;
	
	public Frenzy(boolean isUpgraded) {
		super(ID, "Frenzy", isUpgraded, Rarity.UNCOMMON, EquipmentClass.WARRIOR,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(15, 15, 0, 0));
		atkSpeed = isUpgraded ? 10 : 7;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void setupReforges() {
		addReforge(Bloodlust.get(), BloodFrenzy.get());
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, bind, new EquipmentInstance(data, this, slot, es, (pdata, in) -> {
			Sounds.equip.play(data.getPlayer(), data.getPlayer());

			data.addTrigger(id, Trigger.WEAPON_SWING, (pdata2, in2) -> {
				int mult = Math.min(4, data.getStatus(StatusType.BERSERK).getStacks() / CUTOFF);
				WeaponSwingEvent ev = (WeaponSwingEvent) in2;
				ev.getAttackSpeedBuffList().add(new Buff(data, 0, mult * atkSpeed * 0.01, BuffStatTracker.ignored(this)));
				return TriggerResult.keep();
			});

			return TriggerResult.remove();
		}));
	}

	@Override
	public void setupItem() {
		item = createItem(Material.TIPPED_ARROW,
				GlossaryTag.POWER.tag(this) + ". For every " + GlossaryTag.BERSERK.tag(this, CUTOFF, false) + " you have, up to " + DescUtil.white(20) + ", increase your attack speed by"
				+ " " + DescUtil.yellow(atkSpeed + "%") + ".");
	}
}
