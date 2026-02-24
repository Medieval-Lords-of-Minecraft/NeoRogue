package me.neoblade298.neorogue.equipment.abilities;

import java.util.HashSet;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neocore.bukkit.effects.SoundContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetType;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.BuffStatTracker;
import me.neoblade298.neorogue.session.fight.status.BasicStatus;
import me.neoblade298.neorogue.session.fight.status.Status;
import me.neoblade298.neorogue.session.fight.status.Status.StatusClass;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.ApplyStatusEvent;
import me.neoblade298.neorogue.session.fight.trigger.event.KillEvent;
import me.neoblade298.neorogue.session.fight.trigger.event.PreApplyStatusEvent;

public class FrozenTomb extends Equipment {
	private static final String ID = "FrozenTomb";
	private static final int DAMAGE = 400;
	private static final int SPREAD_FROST = 300;
	private static final TargetProperties tp = TargetProperties.radius(6, false, TargetType.ENEMY);
	private double frostIncrease;
	private int threshold;
	private static final ParticleContainer tomb = new ParticleContainer(Particle.BLOCK)
			.blockData(Material.BLUE_ICE.createBlockData()).count(100).spread(1.5, 1.5).offsetY(1);
	private static final ParticleContainer spread = new ParticleContainer(Particle.SNOWFLAKE)
			.count(50).spread(1, 1).offsetY(1);
	private static final SoundContainer freeze = new SoundContainer(Sound.BLOCK_GLASS_BREAK);
	
	public FrozenTomb(boolean isUpgraded) {
		super(ID, "Frozen Tomb", isUpgraded, Rarity.RARE, EquipmentClass.ARCHER,
				EquipmentType.ABILITY, EquipmentProperties.none());
		frostIncrease = isUpgraded ? 0.3 : 0.2;
		threshold = isUpgraded ? 300 : 400;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		Player p = data.getPlayer();
		String statusName = p.getName() + "-frozenTomb";
		HashSet<UUID> tombTriggered = new HashSet<>();
		
		// Increase all frost applications by percentage
		data.addTrigger(id, Trigger.PRE_APPLY_STATUS, (pdata, in) -> {
			PreApplyStatusEvent ev = (PreApplyStatusEvent) in;
			if (!ev.isStatus(StatusType.FROST)) return TriggerResult.keep();
			ev.getStacksBuffList().add(Buff.multiplier(data, frostIncrease, BuffStatTracker.ignored(this)));
			return TriggerResult.keep();
		});
		
		// Once per enemy, when applying frost and they reach threshold, deal damage
		data.addTrigger(id, Trigger.APPLY_STATUS, (pdata, in) -> {
			ApplyStatusEvent ev = (ApplyStatusEvent) in;
			if (!ev.isStatus(StatusType.FROST)) return TriggerResult.keep();
			FightData fd = ev.getTarget();
			
			// Check if already triggered for this enemy
			if (fd.hasStatus(statusName)) return TriggerResult.keep();
			
			// Check if reached threshold
			if (fd.getStatus(StatusType.FROST).getStacks() < threshold) return TriggerResult.keep();
			
			// Mark as triggered for this enemy
			Status s = new BasicStatus(statusName, data, StatusClass.NONE, true);
			fd.applyStatus(s, data, 1, -1);
			
			// Track for potential kill spread
			tombTriggered.add(fd.getEntity().getUniqueId());
			
			// Deal damage
			tomb.play(p, fd.getEntity());
			freeze.play(p, fd.getEntity());
			FightInstance.dealDamage(new DamageMeta(data, DAMAGE, DamageType.ICE, 
					DamageStatTracker.of(id + slot, this)), fd.getEntity());
			
			return TriggerResult.keep();
		});
		
		// If the damage kills the enemy, spread frost to nearby enemies
		data.addTrigger(id, Trigger.KILL, (pdata, in) -> {
			KillEvent ev = (KillEvent) in;
			UUID killedId = ev.getTarget().getUniqueId();
			
			// Check if this enemy had tomb triggered
			if (!tombTriggered.contains(killedId)) return TriggerResult.keep();
			
			// Remove from tracking set
			tombTriggered.remove(killedId);
			
			// Spread frost to nearby enemies
			spread.play(p, ev.getTarget().getLocation());
			for (LivingEntity ent : TargetHelper.getEntitiesInRadius(ev.getTarget(), tp)) {
				FightInstance.applyStatus(ent, StatusType.FROST, data, SPREAD_FROST, -1);
			}
			
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.PACKED_ICE,
				"Passive. All applications of " + GlossaryTag.FROST.tag(this) + " are increased by " + 
				DescUtil.yellow((int)(frostIncrease * 100) + "%") + ". Once per enemy, when you apply " +
				GlossaryTag.FROST.tag(this) + " to them and they reach over " + DescUtil.white(threshold) + 
				" " + GlossaryTag.FROST.tag(this) + ", deal " + GlossaryTag.ICE.tag(this, DAMAGE, false) + 
				" damage to them. If this kills them, apply " + GlossaryTag.FROST.tag(this, SPREAD_FROST, false) + 
				" to nearby enemies.");
	}
}
