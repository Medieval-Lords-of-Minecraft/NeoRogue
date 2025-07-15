package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neocore.bukkit.effects.SoundContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.BuffStatTracker;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.status.Status;
import me.neoblade298.neorogue.session.fight.status.Status.GenericStatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.DealDamageEvent;
import me.neoblade298.neorogue.session.fight.trigger.event.PreBasicAttackEvent;

public class GrowingHex extends Equipment {
	private static final String ID = "growingHex";
	private static final ParticleContainer pc = new ParticleContainer(Particle.SMOKE).offsetY(0.5).spread(0.5, 0.5).count(30),
			cons = pc.clone().particle(Particle.SOUL);
	private static final SoundContainer sc = new SoundContainer(Sound.ENTITY_GUARDIAN_HURT);
	private int damage, growth;
	
	public GrowingHex(boolean isUpgraded) {
		super(ID, "Growing Hex", isUpgraded, Rarity.UNCOMMON, EquipmentClass.MAGE,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(15, 0, 14, 0));
				damage = 120;
				growth = isUpgraded ? 50 : 25;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ActionMeta am = new ActionMeta();
		String statusName = p.getName() + "-hexcurse";
		data.addTrigger(id, bind, new EquipmentInstance(data, this, slot, es, (pdata, in) -> {
			Sounds.fire.play(p, p);
			am.setBool(true);
			data.addTask(new BukkitRunnable() {
				public void run() {
					am.setBool(false);
				}
			}.runTaskLater(NeoRogue.inst(), 100));
			return TriggerResult.keep();
		}));

		data.addTrigger(id, Trigger.PRE_BASIC_ATTACK, (pdata, in) -> {
			PreBasicAttackEvent ev = (PreBasicAttackEvent) in;
			FightData fd = FightInstance.getFightData(ev.getTarget());
			if (fd.hasStatus(statusName)) {
				Location loc = ev.getTarget().getLocation();
				sc.play(p, loc);
				cons.play(p, loc);
				ev.getMeta().addDamageBuff(DamageBuffType.of(DamageCategory.GENERAL),
						Buff.increase(data, damage + am.getCount(), BuffStatTracker.damageBuffAlly(id, this)));
				am.addCount(growth);
				Status s = Status.createByGenericType(GenericStatusType.BASIC, statusName, fd, true);
				fd.applyStatus(s, data, -1, -1);
			}
			return TriggerResult.keep();
		});

		data.addTrigger(id, Trigger.DEAL_DAMAGE, (pdata, in) -> {
			DealDamageEvent ev = (DealDamageEvent) in;
			if (ev.getMeta().isBasicAttack() || !am.getBool()) return TriggerResult.keep();
			
			Location loc = ev.getTarget().getLocation();
			FightData fd = FightInstance.getFightData(ev.getTarget());
			if (!fd.hasStatus(statusName)) {
				Sounds.infect.play(p, loc);
				pc.play(p, loc);
				Status s = Status.createByGenericType(GenericStatusType.BASIC, statusName, fd, true);
				fd.applyStatus(s, data, 1, 160);
			}
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.SCULK_SENSOR,
				"On cast, for the next <white>10s</white>, your non-basic attack damage marks enemies for <white>8s</white>. Dealing to marked enemies basic attack damage deals " +
				GlossaryTag.DARK.tag(this, damage, false) + " damage plus " + DescUtil.yellow(growth) + " for every mark you've consumed this fight and consumes the mark. " +
				"Marks do not stack.");
	}
}
