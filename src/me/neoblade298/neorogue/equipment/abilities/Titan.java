package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.BuffStatTracker;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.CastUsableEvent;

public class Titan extends Equipment {
	private static final String ID = "titan";
	private static final ParticleContainer pc = new ParticleContainer(Particle.CLOUD);
	private int staminaReduction;
	private static final int CUTOFF = 20;
	
	public Titan(boolean isUpgraded) {
		super(ID, "Titan", isUpgraded, Rarity.UNCOMMON, EquipmentClass.WARRIOR,
				EquipmentType.ABILITY, EquipmentProperties.none());
		pc.count(50).spread(0.5, 0.5).speed(0.2);
		staminaReduction = isUpgraded ? 15 : 10;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, Trigger.PRE_CAST_USABLE, (pdata, in) -> {
			CastUsableEvent ev = (CastUsableEvent) in;
			EquipmentInstance inst = ev.getInstance();
			if (inst.getStaminaCost() < CUTOFF) return TriggerResult.keep();
			ev.addBuff(PropertyType.STAMINA_COST, id, new Buff(data, staminaReduction, 0, BuffStatTracker.ignored(this)));
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.DEAD_BUSH,
				"Passive. Abilities that cost at least <white>" + CUTOFF + "</white> stamina have their stamina cost reduced by <yellow>" + staminaReduction + "</yellow>.");
	}
}
