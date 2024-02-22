package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.particles.ParticleContainer;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.DamageMeta.BuffOrigin;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.BuffType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerAction;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.BasicAttackEvent;

public class Parry extends Equipment {
	private int shields, damage;
	private ParticleContainer pc = new ParticleContainer(Particle.CLOUD),
			bpc = new ParticleContainer(Particle.FLAME),
			hit = new ParticleContainer(Particle.REDSTONE);
	
	public Parry(boolean isUpgraded) {
		super("parry", "Parry", isUpgraded, Rarity.UNCOMMON, EquipmentClass.WARRIOR,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(0, 40, 5, 0));
		shields = 15;
		damage = isUpgraded ? 60 : 40;
		pc.count(10).spread(0.5, 0.5).speed(0.2);
		bpc.count(20).spread(0.5, 0.5).speed(0.1);
		hit.count(50).spread(0.5, 0.5);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, bind, new EquipmentInstance(p, this, slot, es, (pdata, in) -> {
			pc.spawn(p);
			data.addSimpleShield(p.getUniqueId(), shields, 100);
			Util.playSound(p, Sound.ITEM_ARMOR_EQUIP_CHAIN, 1F, 1F, false);
			data.addTrigger(id, Trigger.RECEIVED_DAMAGE, new ParryBlock(p));
			return TriggerResult.keep();
		}));
	}
	
	private class ParryBlock implements TriggerAction {
		private long createTime;
		private Player p;
		public ParryBlock(Player p) {
			this.p = p;
			createTime = System.currentTimeMillis();
		}
		@Override
		public TriggerResult trigger(PlayerFightData data, Object inputs) {
			if (System.currentTimeMillis() - createTime > 5000) return TriggerResult.remove();
			bpc.spawn(p);
			Util.playSound(p, Sound.ENTITY_BLAZE_SHOOT, 1F, 1F, false);
			data.addTrigger(id, Trigger.BASIC_ATTACK, (pdata, in) -> {
				BasicAttackEvent ev = (BasicAttackEvent) in;
				ev.getMeta().addBuff(BuffType.GENERAL, new Buff(p.getUniqueId(), damage, 0), BuffOrigin.NORMAL, true);
				FightInstance.dealDamage(data, DamageType.SLASHING, damage, ev.getTarget());
				hit.spawn(ev.getTarget().getLocation());
				Util.playSound(p, Sound.BLOCK_ANVIL_LAND, 1F, 1F, false);
				return TriggerResult.remove();
			});
			return TriggerResult.keep();
		}
	}

	@Override
	public void setupItem() {
		item = createItem(Material.FLINT,
				"On cast, gain <white>" + shields + " </white>" + GlossaryTag.SHIELDS.tag(this) + " for <white>2</white> seconds. Taking damage during this "
						+ "increases your next basic attack's damage by <yellow>" + damage + "</yellow> once per cast.");
	}
}
