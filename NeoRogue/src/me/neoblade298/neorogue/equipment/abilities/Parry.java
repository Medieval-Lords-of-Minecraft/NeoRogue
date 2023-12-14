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
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerAction;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class Parry extends Ability {
	private int shields, damage;
	private ParticleContainer pc = new ParticleContainer(Particle.CLOUD),
			bpc = new ParticleContainer(Particle.FLAME),
			hit = new ParticleContainer(Particle.REDSTONE);
	
	public Parry(boolean isUpgraded) {
		super("parry", "Parry", isUpgraded, Rarity.RARE, EquipmentClass.WARRIOR);
		setBaseProperties(20, 0, 100, 0);
		shields = 30;
		damage = isUpgraded ? 600 : 400;
		item = createItem(this, Material.FLINT, null,
				"On cast, gain <yellow>" + shields + " </yellow>shields for 2 seconds. Taking damage during this "
						+ "increases your next basic attack's damage by <yellow>" + damage + "</yellow>.");
		pc.count(10).spread(0.5, 0.5).speed(0.2);
		bpc.count(20).spread(0.5, 0.5).speed(0.1);
		hit.count(50).spread(0.5, 0.5);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, int slot) {
		data.addTrigger(id, bind, (fd, in) -> {
			pc.spawn(p);
			data.addShield(p.getUniqueId(), shields, true, 100, 100, 0, 1);
			Util.playSound(p, Sound.ITEM_ARMOR_EQUIP_CHAIN, 1F, 1F, false);
			data.addTrigger(id, Trigger.RECEIVED_DAMAGE, new ParryInstance(p));
			return TriggerResult.keep();
		});
	}
	
	// Change reforgeoptions to allow multiple alternatives
	// Add an equipment type that's just reforge material
	private class ParryInstance implements TriggerAction {
		private long createTime;
		private Player p;
		public ParryInstance(Player p) {
			this.p = p;
			createTime = System.currentTimeMillis();
		}
		@Override
		public TriggerResult trigger(PlayerFightData data, Object[] inputs) {
			if (System.currentTimeMillis() - createTime > 5000) return TriggerResult.remove();
			bpc.spawn(p);
			Util.playSound(p, Sound.ENTITY_BLAZE_SHOOT, 1F, 1F, false);
			data.addTrigger(id, Trigger.BASIC_ATTACK, (pdata, in) -> {
				FightInstance.dealDamage(p, DamageType.SLASHING, damage, (Damageable) in[1]);
				hit.spawn(((Damageable) in[1]).getLocation());
				Util.playSound(p, Sound.BLOCK_ANVIL_LAND, 1F, 1F, false);
				return TriggerResult.remove();
			});
			return TriggerResult.keep();
		}
		
	}
}
