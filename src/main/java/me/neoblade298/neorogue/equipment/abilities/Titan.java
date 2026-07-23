package me.neoblade298.neorogue.equipment.abilities;
import org.bukkit.Material;
import org.bukkit.Particle;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.equipment.Power;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.BuffStatTracker;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.CastUsableEvent;
import me.neoblade298.neorogue.session.fight.trigger.event.PreCastUsableEvent;

public class Titan extends Equipment implements Power {
	private static final String ID = "Titan";
	private static final ParticleContainer pc = new ParticleContainer(Particle.CLOUD);
	private int staminaReduction;
	private static final int CUTOFF = 15;

	public Titan(boolean isUpgraded) {
		super(ID, "Titan", isUpgraded, Rarity.UNCOMMON, EquipmentClass.WARRIOR, EquipmentType.ABILITY,
				EquipmentProperties.none());
		pc.count(50).spread(0.5, 0.5).speed(0.2);
		staminaReduction = isUpgraded ? 15 : 10;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		data.addTrigger(id, Trigger.CAST_USABLE, (pdata, in) -> {
			CastUsableEvent cev = (CastUsableEvent) in;
			if (cev.getInstance().getStaminaCost() < CUTOFF) return TriggerResult.keep();

			if (activatePower(data, slot, es)) return TriggerResult.remove();
			return TriggerResult.keep();
		});
	}

	@Override
	public void onPowerActivated(PlayerFightData data, int slot, EquipSlot es) {
		data.addTrigger(id, Trigger.PRE_CAST_USABLE, (pdata2, in2) -> {
			PreCastUsableEvent ev = (PreCastUsableEvent) in2;
			if (ev.getInstance().getStaminaCost() < CUTOFF)
				return TriggerResult.keep();
			ev.addBuff(PropertyType.STAMINA_COST, id,
					new Buff(data, staminaReduction, 0, BuffStatTracker.ignored(this)));
			return TriggerResult.keep();
		});
	}


	@Override
	public void setupItem() {
		item = createItem(Material.DEAD_BUSH, GlossaryTag.PASSIVE.tag(this) + " " + GlossaryTag.POWER.tag(this) + ". Activates after casting an ability that costs " + DescUtil.val(CUTOFF) + "+ stamina. Abilities that cost at least " + DescUtil.val(CUTOFF)
				+ " stamina have their stamina cost reduced by " + DescUtil.val(staminaReduction) + ".");
	}
}
