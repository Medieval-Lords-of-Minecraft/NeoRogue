package me.neoblade298.neorogue.equipment.abilities;

import java.util.LinkedList;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetType;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.buff.StatTracker;
import me.neoblade298.neorogue.session.fight.status.Status;
import me.neoblade298.neorogue.session.fight.status.Status.GenericStatusType;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.PreDealDamageEvent;

public class RelentlessHunt extends Equipment {
	private static final String ID = "RelentlessHunt";
	private static final TargetProperties tp = TargetProperties.radius(8, false, TargetType.ENEMY);
	private static final ParticleContainer mark = new ParticleContainer(Particle.ENCHANTED_HIT).count(50).spread(0.3, 0.3).offsetY(2);
	private int shields;
	private double damageIncrease;
	
	public RelentlessHunt(boolean isUpgraded) {
		super(ID, "Relentless Hunt", isUpgraded, Rarity.RARE, EquipmentClass.ARCHER,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(15, 0, isUpgraded ? 10 : 20, tp.range));
		shields = isUpgraded ? 3 : 2;
		damageIncrease = isUpgraded ? 0.3 : 0.2;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		String statusName = data.getPlayer().getName() + "-relentlesshunt";
		ActionMeta am = new ActionMeta();
		
		// Mark the first enemy in a line on cast
		data.addTrigger(id, bind, new EquipmentInstance(data, this, slot, es, (pdata, in) -> {
			Player p = data.getPlayer();
			
			// Get first enemy in line
			Location start = p.getEyeLocation();
			Vector dir = start.getDirection();
			Location end = start.clone().add(dir.clone().multiply(properties.get(PropertyType.RANGE)));
			LinkedList<LivingEntity> enemies = TargetHelper.getEntitiesInLine(p, start, end, tp);
			
			if (enemies.isEmpty()) return TriggerResult.keep();
			LivingEntity trg = enemies.getFirst();
			
			// Remove mark from previous target if exists
			if (am.getEntity() != null && am.getEntity().isValid()) {
				FightData oldFd = FightInstance.getFightData(am.getEntity());
				if (oldFd != null && oldFd.hasStatus(statusName)) {
					oldFd.removeStatus(statusName);
				}
			}
			
			// Mark new target
			mark.play(p, trg);
			Sounds.infect.play(p, trg);
			FightData fd = FightInstance.getFightData(trg);
			Status s = Status.createByGenericType(GenericStatusType.BASIC, statusName, fd, true);
			fd.applyStatus(s, data, 1, -1); // Permanent mark
			am.setEntity(trg);
			
			return TriggerResult.keep();
		}));
		
		// Grant shields every second while marked enemy is alive
		data.addTask(new BukkitRunnable() {
			public void run() {
				if (am.getEntity() == null) return;
				if (!am.getEntity().isValid()) {
					am.setEntity(null);
					return;
				}
				Player p = data.getPlayer();
				data.addPermanentShield(p.getUniqueId(), shields);
			}
		}.runTaskTimer(NeoRogue.inst(), 20, 40));
		
		// Increase non-basic damage to marked enemy based on focus
		data.addTrigger(id, Trigger.PRE_DEAL_DAMAGE, (pdata, in) -> {
			PreDealDamageEvent ev = (PreDealDamageEvent) in;
			
			// Only apply to marked enemy
			if (am.getEntity() == null || !ev.getTarget().getUniqueId().equals(am.getEntity().getUniqueId())) {
				return TriggerResult.keep();
			}
			
			// Don't apply to basic attacks
			if (ev.getMeta().isBasicAttack()) return TriggerResult.keep();
			
			// Get focus stacks and apply damage increase
			int focusStacks = data.getStatus(StatusType.FOCUS).getStacks();
			if (focusStacks > 0) {
				double mult = focusStacks * damageIncrease;
				ev.getMeta().addDamageBuff(DamageBuffType.of(DamageCategory.GENERAL),
						Buff.multiplier(data, mult, StatTracker.damageBuffAlly(id + slot, this)));
			}
			
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.ENDER_EYE,
				"On cast, mark the first enemy in a line. Only one enemy can be marked at a time. " +
				"While the marked enemy is alive, gain " + GlossaryTag.SHIELDS.tag(this, shields, true) + " every <white>2s</white>. " +
				"Non-basic damage dealt to the marked enemy is increased by " + 
				DescUtil.yellow((int)(damageIncrease * 100)) + "% per stack of " + GlossaryTag.FOCUS.tag(this) + ".");
	}
}
