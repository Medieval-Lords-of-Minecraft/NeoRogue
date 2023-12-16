package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.particles.ParticleContainer;
import me.neoblade298.neorogue.equipment.Ability;
import me.neoblade298.neorogue.equipment.EquipmentClass;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.BuffType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class DarkPact extends Ability {
	private ParticleContainer pc = new ParticleContainer(Particle.FLAME);
	private int seconds;
	
	public DarkPact(boolean isUpgraded) {
		super("darkPact", "Dark Pact", isUpgraded, Rarity.RARE, EquipmentClass.WARRIOR);
		seconds = isUpgraded ? 40 : 25;
		pc.count(25).spread(0.5, 0.5).speed(0.1);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, int slot) {
		data.addBuff(p.getUniqueId(), id, false, true, BuffType.GENERAL, 1, seconds);
		data.addTrigger(id, Trigger.BASIC_ATTACK, (pdata, inputs) -> {
			p.playSound(p, Sound.ENTITY_BLAZE_SHOOT, 1F, 1F);
			pc.spawn(p);
			data.addBuff(p.getUniqueId(), true, false, BuffType.GENERAL, 1);
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(this, Material.REDSTONE, null,
				"Passive. Increase your damage by 1 every 10 basic attacks. In exchange, take "
				+ "50% increased damage for the first <yellow>" + seconds + "s</yellow> of a fight.");
	}
}
