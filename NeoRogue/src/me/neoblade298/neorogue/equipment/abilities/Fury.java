package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.particles.ParticleContainer;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class Fury extends Equipment {
	private int damage, berserk;
	private ParticleContainer pc = new ParticleContainer(Particle.CLOUD),
			hit = new ParticleContainer(Particle.REDSTONE),
			explode = new ParticleContainer(Particle.EXPLOSION_NORMAL);
	
	public Fury(boolean isUpgraded) {
		super("fury", "Fury", isUpgraded, Rarity.RARE, EquipmentClass.WARRIOR,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(0, 25, 5, 0));
		damage = 125;
		berserk = isUpgraded ? 10 : 15;
		pc.count(50).spread(0.5, 0.5).speed(0.2);
		hit.count(50).spread(0.5, 0.5);
		explode.count(25).spread(0.5, 0.5).speed(0.1);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, bind, new FuryInstance(this, p, damage, bind));
	}
	
	private class FuryInstance extends EquipmentInstance {
		private boolean isBerserk;
		public FuryInstance(Equipment eq, Player p, int damage, Trigger bind) {
			super(eq);
			
			this.action = (data, in) -> {
				Util.playSound(p, Sound.ITEM_ARMOR_EQUIP_CHAIN, 1F, 1F, false);
				pc.spawn(p);
				data.addTrigger(id, Trigger.BASIC_ATTACK, (pdata, in2) -> {
					Damageable target = (Damageable) in[0];
					FightInstance.dealDamage(p, DamageType.SLASHING, damage, target);
					hit.spawn(((Damageable) in[0]).getLocation());
					Util.playSound(p, Sound.BLOCK_ANVIL_LAND, 1F, 1F, false);
					if (isBerserk) {
						Util.playSound(p, target.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1F, 1F, false);
						explode.spawn(target.getLocation());
					}
					return TriggerResult.remove();
				});
				return TriggerResult.keep();
			};
		}
		
		@Override
		public boolean canTrigger(Player p, PlayerFightData data) {
			boolean isBerserkAfter = data.hasStatus("BERSERK") && data.getStatus("BERSERK").getStacks() >= berserk;
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
	}

	@Override
	public void setupItem() {
		item = createItem(Material.FLINT,
				"On cast, your next basic attack deals <yellow>" + damage + " </yellow>damage and grants a stack of Berserk. " +
				"At <yellow>" + berserk + " </yellow>stacks, the cooldown of this skill is halved and the cost is removed.");
	}
}
