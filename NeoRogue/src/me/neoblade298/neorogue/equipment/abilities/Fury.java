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
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class Fury extends Ability {
	private int damage, berserk;
	private ParticleContainer pc = new ParticleContainer(Particle.CLOUD),
			hit = new ParticleContainer(Particle.REDSTONE),
			explode = new ParticleContainer(Particle.EXPLOSION_NORMAL);
	
	public Fury(boolean isUpgraded) {
		super("fury", "Fury", isUpgraded, Rarity.RARE, EquipmentClass.WARRIOR);
		setBaseProperties(5, 0, 50);
		damage = 200;
		berserk = isUpgraded ? 40 : 60;
		item = createItem(this, Material.FLINT, null,
				"On cast, your next basic attack deals <yellow>" + damage + " </yellow>damage and grants a stack of Berserk. " +
				"At <yellow>" + berserk + " </yellow>stacks, the cooldown of this skill is halved and the cost is removed.");
		pc.count(50).offset(0.5, 0.5).speed(0.2);
		hit.count(50).offset(0.5, 0.5);
		explode.count(25).offset(0.5, 0.5).speed(0.1);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, int slot) {
		data.addHotbarTrigger(id, slot, bind, new EmpoweredEdgeInstance(this, p, damage, bind));
	}
	
	private class EmpoweredEdgeInstance extends UsableInstance {
		private Player p;
		private boolean isBerserk;
		public EmpoweredEdgeInstance(Ability a, Player p, int damage, Trigger bind) {
			super(a);
			this.p = p;
		}
		
		@Override
		public boolean canTrigger(Player p, PlayerFightData data) {
			boolean isBerserkAfter = data.getStatus("BERSERK").getStacks() >= berserk;
			if (!isBerserk && isBerserkAfter) {
				this.cooldown = 2.5;
				this.staminaCost = 0;
			}
			else if (isBerserk && !isBerserkAfter) {
				this.cooldown = 5;
				this.staminaCost = 50;
			}
			return super.canTrigger(p, data);
		}
		
		@Override
		public TriggerResult run(PlayerFightData data, Object[] inputs) {
			Util.playSound(p, Sound.ITEM_ARMOR_EQUIP_CHAIN, 1F, 1F, false);
			pc.spawn(p);
			data.addTrigger(id, Trigger.BASIC_ATTACK, (pdata, in) -> {
				Damageable target = (Damageable) in[1];
				FightInstance.dealDamage(p, DamageType.SLASHING, damage, target);
				hit.spawn(((Damageable) in[1]).getLocation());
				Util.playSound(p, Sound.BLOCK_ANVIL_LAND, 1F, 1F, false);
				if (isBerserk) {
					Util.playSound(p, target.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1F, 1F, false);
					explode.spawn(target.getLocation());
				}
				return TriggerResult.remove();
			});
			return TriggerResult.keep();
		}
	}
}
