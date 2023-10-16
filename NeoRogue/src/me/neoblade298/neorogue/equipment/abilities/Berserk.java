package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.particles.ParticleUtil;
import me.neoblade298.neorogue.equipment.Ability;
import me.neoblade298.neorogue.equipment.EquipmentClass;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.Trigger;
import me.neoblade298.neorogue.session.fights.BuffType;
import me.neoblade298.neorogue.session.fights.PlayerFightData;

public class Berserk extends Ability {
	private int seconds;
	
	public Berserk(boolean isUpgraded) {
		super("berserk", isUpgraded, Rarity.RARE, EquipmentClass.WARRIOR);
		display = "Berserk";
		seconds = isUpgraded ? 60 : 30;
		item = createItem(this, Material.REDSTONE, null,
				"&7Passive. Increase your damage by 1 every 10 basic attacks. In exchange, take "
				+ "50% increased damage for the first &e" + seconds + "s&7 of a fight.");
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, int hotbar) {
		data.addBuff(p.getUniqueId(), id, false, true, BuffType.GENERAL, 1, seconds);
		data.addTrigger(id, Trigger.BASIC_ATTACK, (inputs) -> {
			p.playSound(p, Sound.ENTITY_BLAZE_SHOOT, 1F, 1F);
			ParticleUtil.spawnParticle(p, false, p.getLocation(), Particle.FLAME, 25, 0.5, 0.5, 0.5, 0.1, null);
			data.addBuff(p.getUniqueId(), true, false, BuffType.GENERAL, 1);
			return true;
		});
	}
}
