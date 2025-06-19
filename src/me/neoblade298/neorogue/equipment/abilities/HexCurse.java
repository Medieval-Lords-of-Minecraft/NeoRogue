package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neocore.bukkit.effects.SoundContainer;
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
import me.neoblade298.neorogue.session.fight.trigger.event.DealtDamageEvent;
import me.neoblade298.neorogue.session.fight.trigger.event.PreBasicAttackEvent;

public class HexCurse extends Equipment {
	private static final String ID = "hexCurse";
	private static final ParticleContainer pc = new ParticleContainer(Particle.SMOKE).offsetY(1).spread(0.5, 0.5).count(30),
			cons = pc.clone().particle(Particle.SOUL);
	private static final SoundContainer sc = new SoundContainer(Sound.ENTITY_GUARDIAN_HURT);
	private int damage;
	
	public HexCurse(boolean isUpgraded) {
		super(ID, "Hex Curse", isUpgraded, Rarity.COMMON, EquipmentClass.MAGE,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(5, 0, 14, 0));
				damage = isUpgraded ? 105 : 70;
	}

	public void setupReforges() {
		addReforge(CalculatingGaze.get(), HexCurse2.get(), CollectionHex.get());
		addReforge(Intuition.get(), GrowingHex.get());
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
				Status s = Status.createByGenericType(GenericStatusType.BASIC, statusName, fd, true);
				ev.getMeta().addDamageBuff(DamageBuffType.of(DamageCategory.GENERAL),
						Buff.increase(data, damage, BuffStatTracker.damageBuffAlly(id, this)));
				fd.applyStatus(s, data, -1, -1);
			}
			return TriggerResult.keep();
		});

		data.addTrigger(id, Trigger.DEALT_DAMAGE, (pdata, in) -> {
			DealtDamageEvent ev = (DealtDamageEvent) in;
			if (ev.getMeta().isBasicAttack() || !am.getBool()) return TriggerResult.keep();
			
			Location loc = ev.getTarget().getLocation();
			FightData fd = FightInstance.getFightData(ev.getTarget());
			if (!fd.hasStatus(statusName)) {
				Sounds.infect.play(p, loc);
				pc.play(p, loc);
				Status s = Status.createByGenericType(GenericStatusType.BASIC, statusName, fd, false);
				fd.applyStatus(s, data, 1, 160);
			}
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.SCULK_SENSOR,
				"On cast, for the next <white>10s</white>, your non-basic attack damage marks enemies for <white>8s</white>. Dealing to marked enemies basic attack damage deals " +
				GlossaryTag.DARK.tag(this, damage, true) + " damage and consumes the mark. Marks do not stack.");
	}
}
