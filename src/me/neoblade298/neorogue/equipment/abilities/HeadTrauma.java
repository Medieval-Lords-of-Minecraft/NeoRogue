package me.neoblade298.neorogue.equipment.abilities;

import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neocore.bukkit.effects.SoundContainer;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.ActionMeta;
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
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class HeadTrauma extends Equipment {
	private static final String ID = "HeadTrauma";
	private int damage, reducStr;
	private double reduc;
	private static final int THRESHOLD = 15;
	private static final ParticleContainer pc = new ParticleContainer(Particle.BLOCK).blockData(Material.DIRT.createBlockData()).count(20).spread(1, 1).offsetY(1);
	private static final SoundContainer sc = new SoundContainer(Sound.BLOCK_ROOTED_DIRT_BREAK);
	
	public HeadTrauma(boolean isUpgraded) {
		super(ID, "Head Trauma", isUpgraded, Rarity.UNCOMMON, EquipmentClass.MAGE,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(0, 0, 0, 0));
		reduc = isUpgraded ? 0.6 : 0.4;
		reducStr = (int) (100 * reduc);
		damage = isUpgraded ? 300 : 200;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	private static final int ACTIVATION_THRES = 15;

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ActionMeta am = new ActionMeta();
		data.addTrigger(id, Trigger.APPLY_STATUS, (pdata, in) -> {
			ApplyStatusEvent ev = (ApplyStatusEvent) in;
			if (!ev.isStatus(StatusType.CONCUSSED)) return TriggerResult.keep();
			am.addCount(ev.getStacks());
			if (am.getCount() < ACTIVATION_THRES) return TriggerResult.keep();

			Player p = data.getPlayer();
			Sounds.fire.play(p, p);
			Util.msg(p, hoverable.append(Component.text(" was activated", NamedTextColor.GRAY)));

			String statusName = p.getName() + "-headTrauma";
			String buffId = UUID.randomUUID().toString();
			data.addTask(new BukkitRunnable() {
				public void run() {
					data.addTrigger(id + "-active", Trigger.APPLY_STATUS, (pdata2, in2) -> {
						ApplyStatusEvent ev2 = (ApplyStatusEvent) in2;
						if (!ev2.isStatus(StatusType.CONCUSSED)) return TriggerResult.keep();
						FightData fd = ev2.getTarget();
						if (fd.getStatus(StatusType.CONCUSSED).getStacks() < THRESHOLD) return TriggerResult.keep();
						if (fd.hasStatus(statusName)) return TriggerResult.keep();
						Status s = new BasicStatus(statusName, data, StatusClass.NONE, true);
						fd.applyStatus(s, data, 1, -1);
						Player p2 = data.getPlayer();
						FightInstance.dealDamage(new DamageMeta(data, damage, DamageType.EARTHEN, DamageStatTracker.of(id + slot, HeadTrauma.this)), fd.getEntity());
						fd.addDefenseBuff(DamageBuffType.of(DamageCategory.GENERAL), Buff.multiplier(data, -reduc, BuffStatTracker.defenseDebuffEnemy(buffId, HeadTrauma.this, false)));
						pc.play(p2, fd.getEntity());
						sc.play(p2, fd.getEntity());
						return TriggerResult.keep();
					});
				}
			}.runTask(NeoRogue.inst()));

			return TriggerResult.remove();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.ARMOR_STAND,
				GlossaryTag.POWER.tag(this) + ". Activates after applying " + DescUtil.white(THRESHOLD) + " " + GlossaryTag.CONCUSSED.tag(this) + " stacks. Once per enemy, applying " + GlossaryTag.CONCUSSED.tag(this, THRESHOLD, false) +
				" to an enemy will deal " + GlossaryTag.EARTHEN.tag(this, damage, true) + " damage to them and reduce their defense by " + 
				DescUtil.yellow(reducStr + "%") + " permanently.");
	}
}
