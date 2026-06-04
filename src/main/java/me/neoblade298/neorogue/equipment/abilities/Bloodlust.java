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
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class Bloodlust extends Equipment implements Power {
	private static final String ID = "Bloodlust";
	private int strength;
	private static final int CUTOFF = 15;
	private static final ParticleContainer pc = new ParticleContainer(Particle.FLAME).count(20).speed(0.01).offsetY(1);
	
	public Bloodlust(boolean isUpgraded) {
		super(ID, "Bloodlust", isUpgraded, Rarity.UNCOMMON, EquipmentClass.WARRIOR,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(0, 0, 0, 0));
		strength = isUpgraded ? 15 : 10;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	private static final int ACTIVATION_THRES = 5;

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ActionMeta am = new ActionMeta();
		data.addTrigger(id, Trigger.PRE_BASIC_ATTACK, (pdata, in) -> {
			if (data.getStatus(StatusType.BERSERK).getStacks() < 1) return TriggerResult.keep();
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
			if (data.getStatus(StatusType.BERSERK).getStacks() < CUTOFF) {
				data.applyStatus(StatusType.BERSERK, data, 1, -1);
			}
			else {
				pc.play(p2, p2);
				Sounds.fire.play(p2, p2);
				data.applyStatus(StatusType.STRENGTH, data, strength, -1);
			}
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.REDSTONE_ORE,
				GlossaryTag.POWER.tag(this) + ". Activates after basic attacking " + DescUtil.white(ACTIVATION_THRES) + " times with " + DescUtil.white(1) + "+ " + GlossaryTag.BERSERK.tag(this) + ". On kill, if below " + DescUtil.white(CUTOFF) + " stacks of " + GlossaryTag.BERSERK.tag(this) + ", gain " + GlossaryTag.BERSERK.tag(this, 1, false)
				+ ". Otherwise, gain " + GlossaryTag.STRENGTH.tag(this, strength, true) + ".");
	}
}
