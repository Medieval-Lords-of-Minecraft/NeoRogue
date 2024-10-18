package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.BasicAttackEvent;

public class Fury extends Equipment {
	private static final String ID = "fury";
	private int damage, berserk, heal, berserkHeal;
	private static final ParticleContainer pc = new ParticleContainer(Particle.CLOUD),
			hit = new ParticleContainer(Particle.REDSTONE),
			explode = new ParticleContainer(Particle.EXPLOSION_NORMAL);
	
	public Fury(boolean isUpgraded) {
		super(ID, "Fury", isUpgraded, Rarity.UNCOMMON, EquipmentClass.WARRIOR,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(0, 30, 5, 0));
		damage = 90;
		berserk = isUpgraded ? 10 : 15;
		heal = 1;
		berserkHeal = isUpgraded ? 3 : 2;
		pc.count(50).spread(0.5, 0.5).speed(0.2);
		hit.count(50).spread(0.5, 0.5);
		explode.count(25).spread(0.5, 0.5).speed(0.1);
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, bind, new FuryInstance(this, data, damage, bind, slot, es));
	}
	
	private class FuryInstance extends EquipmentInstance {
		private boolean isBerserk;
		public FuryInstance(Equipment eq, PlayerFightData data, int damage, Trigger bind, int slot, EquipSlot es) {
			super(data, eq, slot, es);
			
			this.action = (pdata, in) -> {
				Player p = data.getPlayer();
				Sounds.equip.play(p, p);
				pc.play(p, p);
				data.addTrigger(id, Trigger.BASIC_ATTACK, (pdata2, in2) -> {
					BasicAttackEvent ev = (BasicAttackEvent) in2;
					LivingEntity target = ev.getTarget();
					FightInstance.dealDamage(data, DamageType.SLASHING, damage, target);
					hit.play(p, target);
					Sounds.anvil.play(p, target);
					data.applyStatus(StatusType.BERSERK, data, 1, -1);
					if (isBerserk) {
						Sounds.explode.play(p, target);
						explode.play(p, target);
						FightInstance.giveHeal(p, berserkHeal, p);
					}
					else {
						FightInstance.giveHeal(p, heal, p);
					}
					return TriggerResult.remove();
				});
				return TriggerResult.keep();
			};
		}
		
		@Override
		public boolean canTrigger(Player p, PlayerFightData data) {
			boolean isBerserkAfter = data.hasStatus(StatusType.BERSERK) && data.getStatus(StatusType.BERSERK).getStacks() >= berserk;
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
		item = createItem(Material.BLAZE_ROD,
				"On cast, your next basic attack deals <white>" + damage + " </white>" + GlossaryTag.SLASHING.tag(this) + " damage, heals for <white>"
				+ heal + "</white>, and grants"
						+ " a stack of " + GlossaryTag.BERSERK.tag(this) + ". " +
				"At <yellow>" + berserk + " </yellow>stacks, the cooldown of this skill is halved and the cost is removed. The heal is increased to <yellow>"
				+ berserkHeal + "</yellow>.");
	}
}
