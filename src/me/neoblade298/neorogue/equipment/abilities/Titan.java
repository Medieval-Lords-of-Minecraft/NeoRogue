package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.BuffStatTracker;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.CastUsableEvent;
import me.neoblade298.neorogue.session.fight.trigger.event.PreCastUsableEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class Titan extends Equipment {
	private static final String ID = "Titan";
	private static final ParticleContainer pc = new ParticleContainer(Particle.CLOUD);
	private int staminaReduction;
	private static final int CUTOFF = 15;

	public Titan(boolean isUpgraded) {
		super(ID, "Titan", isUpgraded, Rarity.UNCOMMON, EquipmentClass.WARRIOR, EquipmentType.ABILITY,
				EquipmentProperties.ofUsable(0, 0, 0, 0));
		pc.count(50).spread(0.5, 0.5).speed(0.2);
		staminaReduction = isUpgraded ? 15 : 10;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, Trigger.CAST_USABLE, (pdata, in) -> {
			CastUsableEvent cev = (CastUsableEvent) in;
			if (cev.getInstance().getStaminaCost() < CUTOFF) return TriggerResult.keep();

			Player p = data.getPlayer();
			Sounds.fire.play(p, p);
			Util.msg(p, hoverable.append(Component.text(" was activated", NamedTextColor.GRAY)));

			data.addTrigger(id, Trigger.PRE_CAST_USABLE, (pdata2, in2) -> {
				PreCastUsableEvent ev = (PreCastUsableEvent) in2;
				if (ev.getInstance().getStaminaCost() < CUTOFF)
					return TriggerResult.keep();
				ev.addBuff(PropertyType.STAMINA_COST, id,
						new Buff(data, staminaReduction, 0, BuffStatTracker.ignored(this)));
				return TriggerResult.keep();
			});

			return TriggerResult.remove();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.DEAD_BUSH, GlossaryTag.POWER.tag(this) + ". Abilities that cost at least " + DescUtil.white(CUTOFF)
				+ " stamina have their stamina cost reduced by " + DescUtil.yellow(staminaReduction) + ".");
	}
}
