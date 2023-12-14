package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.particles.ParticleContainer;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neorogue.equipment.Ability;
import me.neoblade298.neorogue.equipment.EquipmentClass;
import me.neoblade298.neorogue.equipment.UsableInstance;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class BlessedEdge extends Ability {
	private int damage, sanct;
	private ParticleContainer pc = new ParticleContainer(Particle.CLOUD),
			hit = new ParticleContainer(Particle.FIREWORKS_SPARK);
	
	public BlessedEdge(boolean isUpgraded) {
		super("blessedEdge", "Blessed Edge", isUpgraded, Rarity.RARE, EquipmentClass.WARRIOR);
		setBaseProperties(5, 20, 20);
		damage = 125;
		sanct = isUpgraded ? 9 : 6;
		item = createItem(this, Material.FLINT, null,
				"On cast, your next basic attack deals <yellow>" + damage + " </yellow>damage and applies <yellow>" + sanct +
				"</yellow> sanctified.");
		pc.count(50).spread(0.5, 0.5).speed(0.2);
		hit.count(50).spread(0.5, 0.5);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, int slot) {
		data.addHotbarTrigger(id, slot, bind, new EmpoweredEdgeInstance(this, p, damage, bind));
	}
	
	private class EmpoweredEdgeInstance extends UsableInstance {
		private Player p;
		public EmpoweredEdgeInstance(Ability a, Player p, int damage, Trigger bind) {
			super(a);
			this.p = p;
			this.cooldown = a.getCooldown();
		}
		
		@Override
		public TriggerResult run(PlayerFightData data, Object[] inputs) {
			Util.playSound(p, Sound.ITEM_ARMOR_EQUIP_CHAIN, 1F, 1F, false);
			pc.spawn(p);
			data.addTrigger(id, Trigger.BASIC_ATTACK, (pdata, in) -> {
				Damageable target = (Damageable) in[1];
				FightInstance.dealDamage(p, DamageType.SLASHING, damage, target);
				FightInstance.getFightData(target.getUniqueId()).applyStatus(StatusType.SANCTIFIED, p.getUniqueId(), sanct, -1);
				hit.spawn(((Damageable) in[1]).getLocation());
				Util.playSound(p, Sound.BLOCK_ANVIL_LAND, 1F, 1F, false);
				return TriggerResult.remove();
			});
			return TriggerResult.keep();
		}
	}
}
