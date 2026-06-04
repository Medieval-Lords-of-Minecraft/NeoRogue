package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Power;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.BuffStatTracker;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.WeaponSwingEvent;

public class BloodFrenzy extends Equipment implements Power {
	private static final String ID = "BloodFrenzy";
	private int strength, atkSpeed;
	private static final int CUTOFF_STRENGTH = 15, CUTOFF_ATK_SPEED = 20, THRES_ATK_SPEED = 5;
	private static final ParticleContainer pc = new ParticleContainer(Particle.FLAME).count(20).speed(0.01).offsetY(1);
	
	public BloodFrenzy(boolean isUpgraded) {
		super(ID, "Blood Frenzy", isUpgraded, Rarity.RARE, EquipmentClass.WARRIOR,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(0, 0, 0, 0));
		strength = isUpgraded ? 17 : 13;
		atkSpeed = isUpgraded ? 11 : 8;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	private static final int ACTIVATION_THRES = 5, ACTIVATION_BERSERK = 3;

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ActionMeta am = new ActionMeta();
		data.addTrigger(id, Trigger.PRE_BASIC_ATTACK, (pdata, in) -> {
			if (data.getStatus(StatusType.BERSERK).getStacks() < ACTIVATION_BERSERK) return TriggerResult.keep();
			am.addCount(1);
			if (am.getCount() < ACTIVATION_THRES) return TriggerResult.keep();

			if (activatePower(data, slot, es)) return TriggerResult.remove();
			return TriggerResult.keep();
		});
	}

	@Override
	public void onPowerActivated(PlayerFightData data, int slot, EquipSlot es) {
		data.addTrigger(id, Trigger.KILL, (pdata2, in2) -> {
			Player p2 = data.getPlayer();
			if (data.getStatus(StatusType.BERSERK).getStacks() < CUTOFF_STRENGTH) {
				data.applyStatus(StatusType.BERSERK, data, 1, -1);
			}
			else {
				pc.play(p2, p2);
				Sounds.fire.play(p2, p2);
				data.applyStatus(StatusType.STRENGTH, data, strength, -1);
			}
			return TriggerResult.keep();
		});

		data.addTrigger(ID, Trigger.WEAPON_SWING, (pdata3, in3) -> {
			int mult = Math.min(CUTOFF_ATK_SPEED / 5, data.getStatus(StatusType.BERSERK).getStacks() / THRES_ATK_SPEED);
			WeaponSwingEvent ev = (WeaponSwingEvent) in3;
			ev.getAttackSpeedBuffList().add(new Buff(data, 0, mult * atkSpeed * 0.01, BuffStatTracker.ignored(this)));
			return TriggerResult.keep();
		});
	}


	@Override
	public void setupItem() {
	item = createItem(Material.REDSTONE_ORE,
			GlossaryTag.POWER.tag(this) + ". Activates after basic attacking " + DescUtil.white(ACTIVATION_THRES) + " times with " + DescUtil.white(ACTIVATION_BERSERK) + "+ " + GlossaryTag.BERSERK.tag(this) + ". On kill, if below " + DescUtil.white(CUTOFF_STRENGTH) + " stacks of " + GlossaryTag.BERSERK.tag(this) + ", gain " + GlossaryTag.BERSERK.tag(this, 1, false)
			+ ". Otherwise, gain " + GlossaryTag.STRENGTH.tag(this, strength, true) + ". Also, for every " + GlossaryTag.BERSERK.tag(this, THRES_ATK_SPEED, false) + " you have, up to " + DescUtil.white(CUTOFF_ATK_SPEED) +
			", increase your attack speed by " + DescUtil.yellow(atkSpeed + "%") + ".");
	}
}
