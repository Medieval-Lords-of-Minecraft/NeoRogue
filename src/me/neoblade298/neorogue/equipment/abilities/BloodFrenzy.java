package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
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

public class BloodFrenzy extends Equipment {
	private static final String ID = "BloodFrenzy";
	private int strength, atkSpeed;
	private static final int CUTOFF_STRENGTH = 15, CUTOFF_ATK_SPEED = 20, THRES_ATK_SPEED = 5;
	private static final ParticleContainer pc = new ParticleContainer(Particle.FLAME).count(20).speed(0.01).offsetY(1);
	
	public BloodFrenzy(boolean isUpgraded) {
		super(ID, "Blood Frenzy", isUpgraded, Rarity.RARE, EquipmentClass.WARRIOR,
				EquipmentType.ABILITY, EquipmentProperties.none());
		strength = isUpgraded ? 20 : 15;
		atkSpeed = isUpgraded ? 13 : 10;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, Trigger.KILL, (pdata, in) -> {
			Player p = data.getPlayer();
			if (data.getStatus(StatusType.BERSERK).getStacks() < CUTOFF_STRENGTH) {
				data.applyStatus(StatusType.BERSERK, data, 1, -1);
			}
			else {
				pc.play(p, p);
				Sounds.fire.play(p, p);
				data.applyStatus(StatusType.STRENGTH, data, strength, -1);
			}
			return TriggerResult.keep();
		});

		
		data.addTrigger(ID, Trigger.WEAPON_SWING, (pdata, in) -> {
			int mult = Math.min(CUTOFF_ATK_SPEED / 5, data.getStatus(StatusType.BERSERK).getStacks() / THRES_ATK_SPEED);
			WeaponSwingEvent ev = (WeaponSwingEvent) in;
			ev.getAttackSpeedBuffList().add(new Buff(data, 0, mult * atkSpeed * 0.01, BuffStatTracker.ignored(this)));
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.REDSTONE_ORE,
				"On kill, if below <white>" + CUTOFF_STRENGTH + "</white> stacks of " + GlossaryTag.BERSERK.tag(this) + ", gain " + GlossaryTag.BERSERK.tag(this, 1, false)
				+ ". Otherwise, gain " + GlossaryTag.STRENGTH.tag(this, strength, true) + ". Also, for every " + GlossaryTag.BERSERK.tag(this, THRES_ATK_SPEED, false) + " you have, up to <white>30</white>, increase your attack speed by"
				+ " <yellow>" + atkSpeed + "%</yellow>.");
	}
}
