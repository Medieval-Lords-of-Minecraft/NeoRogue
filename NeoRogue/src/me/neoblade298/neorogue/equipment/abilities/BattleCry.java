package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.particles.ParticleUtil;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neorogue.equipment.Ability;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.Trigger;
import me.neoblade298.neorogue.session.fights.BuffType;
import me.neoblade298.neorogue.session.fights.FightData;
import me.neoblade298.neorogue.session.fights.FightInstance;

public class BattleCry extends Ability {
	
	public BattleCry(boolean isUpgraded) {
		super("battleCry", isUpgraded, Rarity.COMMON);
		display = "Battle Cry";
		cooldown = 30;
		staminaCost = 25;
		int strength = isUpgraded ? 20 : 14;
		item = Ability.createItem(this, Material.REDSTONE, null,
				"&7On cast, give yourself &e" + strength + " &7bonus physical damage for &e10&7 seconds.");
	}

	@Override
	public void initialize(Player p, FightData data, FightInstance inst, Trigger bind) {
		data.addTrigger(id, bind, new BattleCryInstance(this, p, data));
	}
	
	private class BattleCryInstance extends EquipmentInstance {
		private Player p;
		private FightData data;
		public BattleCryInstance(Ability a, Player p, FightData data) {
			super(a);
			this.p = p;
			this.cooldown = a.getCooldown();
			this.data = data;
		}
		
		@Override
		public boolean run(Object[] inputs) {
			Util.playSound(p, Sound.ENTITY_BLAZE_DEATH, 1F, 1F, false);
			ParticleUtil.spawnParticle(p, false, p.getLocation(), Particle.REDSTONE, 50, 0.5, 0.5, 0.5, 0, new DustOptions(Color.RED, 1F));
			data.addBuff(id, true, false, BuffType.PHYSICAL, isUpgraded ? 20 : 14, 10);
			return true;
		}
	}
}
