package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
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
	private int damage;
	
	public EmpoweredEdge(boolean isUpgraded) {
		super("empoweredEdge", isUpgraded, Rarity.COMMON);
		display = "Empowered Edge";
		cooldown = isUpgraded ? 5 : 7;
		damage = isUpgraded ? 100 : 75;
		item = Ability.createItem(this, Material.FLINT, null,
				"&7On cast, your next basic attack deals &e" + damage + " &7damage.");
	}

	@Override
	public void initialize(Player p, FightData data, Trigger bind, int hotbar) {
		data.addTrigger(id, bind, new EmpoweredEdgeInstance(this, p, damage, data, bind));
	}
	
	private class EmpoweredEdgeInstance extends EquipmentInstance {
		private Player p;
		private FightData data;
		private int damage;
		public EmpoweredEdgeInstance(Ability a, Player p, int damage, FightData data, Trigger bind) {
			super(a);
			this.p = p;
			this.cooldown = a.getCooldown();
			this.damage = damage;
			this.data = data;
		}
		
		@Override
		public boolean run(Object[] inputs) {
			Util.playSound(p, Sound.ITEM_ARMOR_EQUIP_CHAIN, 1F, 1F, false);
			ParticleUtil.spawnParticle(p, false, p.getLocation(), Particle.CLOUD, 50, 0.5, 0.5, 0.5, 0.2);
			data.addTrigger(id, Trigger.BASIC_ATTACK, (in) -> {
				FightInstance.dealDamage(p, DamageType.SLASHING, damage, (Damageable) in[1]);
				return false;
			});
			return true;
		}
	}
}