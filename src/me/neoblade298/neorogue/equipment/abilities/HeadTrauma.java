package me.neoblade298.neorogue.equipment.abilities;

import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neocore.bukkit.effects.SoundContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.BuffStatTracker;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.status.BasicStatus;
import me.neoblade298.neorogue.session.fight.status.Status;
import me.neoblade298.neorogue.session.fight.status.Status.StatusClass;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.ApplyStatusEvent;

public class HeadTrauma extends Equipment {
	private static final String ID = "HeadTrauma";
	private int damage, reducStr;
	private double reduc;
	private static final int THRESHOLD = 100;
	private static final ParticleContainer pc = new ParticleContainer(Particle.BLOCK).blockData(Material.DIRT.createBlockData()).count(20).spread(1, 1).offsetY(1);
	private static final SoundContainer sc = new SoundContainer(Sound.BLOCK_ROOTED_DIRT_BREAK);
	
	public HeadTrauma(boolean isUpgraded) {
		super(ID, "Head Trauma", isUpgraded, Rarity.UNCOMMON, EquipmentClass.MAGE,
				EquipmentType.ABILITY, EquipmentProperties.none());
		reduc = isUpgraded ? 0.6 : 0.4;
		reducStr = (int) (100 * reduc);
		damage = isUpgraded ? 300 : 200;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		String statusName = p.getName() + "-headTrauma";
		String buffId = UUID.randomUUID().toString();
		data.addTrigger(id, Trigger.APPLY_STATUS, (pdata, in) -> {
			ApplyStatusEvent ev = (ApplyStatusEvent) in;
			if (!ev.isStatus(StatusType.CONCUSSED)) return TriggerResult.keep();
			FightData fd = ev.getTarget();
			if (fd.getStatus(StatusType.CONCUSSED).getStacks() < THRESHOLD) return TriggerResult.keep();
			if (fd.hasStatus(statusName)) return TriggerResult.keep();
			Status s = new BasicStatus(statusName, data, StatusClass.NONE, true);
			fd.applyStatus(s, data, 1, -1);
			FightInstance.dealDamage(new DamageMeta(data, damage, DamageType.EARTHEN, DamageStatTracker.of(id + slot, this)), fd.getEntity());
			fd.addDefenseBuff(DamageBuffType.of(DamageCategory.GENERAL), Buff.multiplier(data, -reduc, BuffStatTracker.defenseDebuffEnemy(buffId, this, false)));
			pc.play(p, fd.getEntity());
			sc.play(p, fd.getEntity());
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.ARMOR_STAND,
				"Passive. Once per enemy, applying " + GlossaryTag.CONCUSSED.tag(this, THRESHOLD, false) +
				" to an enemy will deal " + GlossaryTag.EARTHEN.tag(this, damage, true) + " damage to them and reduce their defense by " + 
				DescUtil.yellow(reducStr + "%") + " permanently.");
	}
}
