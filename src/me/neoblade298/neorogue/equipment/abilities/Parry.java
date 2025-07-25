package me.neoblade298.neorogue.equipment.abilities;

import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.buff.StatTracker;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerAction;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.PreBasicAttackEvent;

public class Parry extends Equipment {
	private static final String ID = "parry";
	private int shields, damage;
	private static final ParticleContainer pc = new ParticleContainer(Particle.CLOUD),
			bpc = new ParticleContainer(Particle.FLAME),
			hit = new ParticleContainer(Particle.DUST);
	
	public Parry(boolean isUpgraded) {
		super(ID, "Parry", isUpgraded, Rarity.UNCOMMON, EquipmentClass.WARRIOR,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(0, 25, 5, 0));
		shields = 15;
		damage = isUpgraded ? 60 : 40;
		pc.count(10).spread(0.5, 0.5).speed(0.2);
		bpc.count(20).spread(0.5, 0.5).speed(0.1);
		hit.count(50).spread(0.5, 0.5);
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, bind, new EquipmentInstance(data, this, slot, es, (pdata, in) -> {
			pc.play(p, p);
			data.addSimpleShield(p.getUniqueId(), shields, 100);
			Sounds.equip.play(p, p);
			data.addTrigger(id, Trigger.PRE_RECEIVE_DAMAGE, new ParryBlock(p, this, slot));
			return TriggerResult.keep();
		}));
	}
	
	private class ParryBlock implements TriggerAction {
		private long createTime;
		private Player p;
		private Equipment eq;
		private int slot;
		private String buffId = UUID.randomUUID().toString();
		public ParryBlock(Player p, Equipment eq, int slot) {
			this.p = p;
			this.eq = eq;
			createTime = System.currentTimeMillis();
		}
		@Override
		public TriggerResult trigger(PlayerFightData data, Object inputs) {
			if (System.currentTimeMillis() - createTime > 5000) return TriggerResult.remove();
			bpc.play(p, p);
			Sounds.fire.play(p, p);
			data.addTrigger(id, Trigger.PRE_BASIC_ATTACK, (pdata, in) -> {
				PreBasicAttackEvent ev = (PreBasicAttackEvent) in;
				ev.getMeta().addDamageBuff(DamageBuffType.of(DamageCategory.GENERAL), new Buff(data, damage, 0, StatTracker.damageBuffAlly(buffId, eq)));
				FightInstance.dealDamage(data, DamageType.SLASHING, damage, ev.getTarget(), DamageStatTracker.of(id + slot, eq));
				hit.play(p, ev.getTarget().getLocation());
				Sounds.anvil.play(p, p);
				return TriggerResult.remove();
			});
			return TriggerResult.keep();
		}
	}

	@Override
	public void setupItem() {
		item = createItem(Material.NETHER_QUARTZ_ORE,
				"On cast, gain <white>" + shields + " </white>" + GlossaryTag.SHIELDS.tag(this) + " for <white>2</white> seconds. Taking damage during this "
						+ "increases your next basic attack's damage by <yellow>" + damage + "</yellow> once per cast.");
	}
}
