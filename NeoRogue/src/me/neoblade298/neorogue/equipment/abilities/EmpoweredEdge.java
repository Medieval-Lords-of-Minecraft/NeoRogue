package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.Particle.DustOptions;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.particles.ParticleUtil;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neorogue.equipment.Ability;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.Trigger;
import me.neoblade298.neorogue.session.fights.DamageType;
import me.neoblade298.neorogue.session.fights.FightData;
import me.neoblade298.neorogue.session.fights.FightInstance;

public class EmpoweredEdge extends Ability {
	
	public EmpoweredEdge(boolean isUpgraded) {
		super("empoweredEdge", isUpgraded, Rarity.COMMON);
		display = "Empowered Edge";
		cooldown = isUpgraded ? 5 : 7;
		int damage = isUpgraded ? 100 : 75;
		item = Ability.createItem(this, Material.FLINT, null,
				"&7On cast, your next basic attack deals &e" + damage + " &7damage.");
	}

	@Override
	public void initialize(Player p, FightData data, FightInstance inst, Trigger bind) {
		Util.playSound(p, Sound.ITEM_ARMOR_EQUIP_CHAIN, 1F, 1F, false);
		ParticleUtil.spawnParticle(p, false, p.getLocation(), Particle.CLOUD, 50, 0.5, 0.5, 0.5, 0.2);
		data.addTrigger(id, Trigger.BASIC_ATTACK, (inputs) -> {
			inst.dealDamage(p, DamageType.SLASHING, isUpgraded ? 100 : 75, (Damageable) inputs[1]);
			return false;
		});
	}
	
	private class EmpoweredEdgeInstance extends EquipmentInstance {
		private Player p;
		private FightInstance inst;
		public WoodenSwordInstance(Player p, FightData data, FightInstance inst, Trigger bind) {
			this.inst = inst;
			this.p = p;
		}
		
		@Override
		public boolean trigger(Object[] inputs) {
			inst.dealDamage(p, DamageType.SLASHING, damage, ((Damageable) inputs[1]));
			return true;
		}
	}
}
