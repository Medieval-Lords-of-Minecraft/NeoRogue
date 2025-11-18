package me.neoblade298.neorogue.equipment.abilities;

import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.StandardEquipmentInstance;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.buff.StatTracker;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerCondition;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class Gambit extends Equipment {
	private static final String ID = "Gambit";
	private static final ParticleContainer pc = new ParticleContainer(Particle.DUST).count(50).spread(1, 1).offsetY(1);
	private static final TriggerCondition cond = (p, pdata, in) -> {
		if (!pdata.hasStatus(StatusType.FOCUS)) {
			Util.msg(p, "You need Focus to use this ability!");
			return false;
		}
		return true;
	};
	private int damage;
	
	public Gambit(boolean isUpgraded) {
		super(ID, "Gambit", isUpgraded, Rarity.UNCOMMON, EquipmentClass.ARCHER,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(15, 5, 20, 0));
		damage = isUpgraded ? 30 : 20;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		String buffId = UUID.randomUUID().toString();
		StandardEquipmentInstance inst = new StandardEquipmentInstance(data, this, slot, es);
		inst.setAction((pdata, inputs) -> {
			Sounds.equip.play(p, p);
			inst.setBool(true);
			inst.setCount(0);
			data.addTask(new BukkitRunnable() {
				public void run() {
					inst.setBool(false);
				}
			}.runTaskLater(NeoRogue.inst(), 60L));
			return TriggerResult.keep();
		});
		inst.setCondition(cond);
		data.addTrigger(id, bind, inst);

		data.addTrigger(id, Trigger.KILL, (pdata, in) -> {
			if (inst.getBool()) {
				inst.addCount(1);
				if (inst.getCount() >= 2) {
					inst.setBool(false);
					inst.setCount(0);
					Sounds.roar.play(p, p);
					pc.play(p, p);
					data.addDamageBuff(DamageBuffType.of(DamageCategory.GENERAL), new Buff(data, damage, 0, StatTracker.damageBuffAlly(buffId, this)));
				}
			}
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.DRAGON_HEAD,
				"Requires " + GlossaryTag.FOCUS.tag(this) + ". On cast, lose " + GlossaryTag.FOCUS.tag(this, 1, false) + ". If you kill <white>2</white> " +
				"enemies within <white>3s</white>, permanently increase your damage by " + DescUtil.yellow(damage) + ".");
	}
}
