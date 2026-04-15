package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.Sounds;
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
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.BasicAttackEvent;

public class LaceratingWave extends Equipment {
	private static final String ID = "LaceratingWave";
	private int damage, rendThreshold;
	private static final ParticleContainer wave = new ParticleContainer(Particle.SWEEP_ATTACK)
			.count(20).spread(2, 0.2).offsetY(0.3);
	private static final ParticleContainer blood = new ParticleContainer(Particle.DUST)
			.count(30).spread(2, 0.2).offsetY(0.3);
	private static final TargetProperties tp = TargetProperties.radius(3, false, TargetType.ENEMY);
	
	public LaceratingWave(boolean isUpgraded) {
		super(ID, "Lacerating Wave", isUpgraded, Rarity.RARE, EquipmentClass.ARCHER,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(0, 0, 0, tp.range));
		damage = isUpgraded ? 60 : 40;
		rendThreshold = isUpgraded ? 25 : 40;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, Trigger.BASIC_ATTACK, (pdata, in) -> {
			BasicAttackEvent ev = (BasicAttackEvent) in;
			LivingEntity target = ev.getTarget();
			if (target == null) return TriggerResult.keep();
			
			FightData fd = FightInstance.getFightData(target);
			if (fd == null) return TriggerResult.keep();
			
			// Check if target has over the rend threshold
			int rendStacks = fd.getStatus(StatusType.REND).getStacks();
			if (rendStacks < rendThreshold) return TriggerResult.keep();
			
			Player p = data.getPlayer();
			wave.play(p, target.getLocation());
			blood.play(p, target.getLocation());
			Sounds.attackSweep.play(p, target.getLocation());
			
			// Deal AOE damage around the target
			for (LivingEntity ent : TargetHelper.getEntitiesInRadius(target, tp)) {
				FightInstance.dealDamage(new DamageMeta(data, damage, DamageType.SLASHING, 
						DamageStatTracker.of(id + slot, this)), ent);
			}
			
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.IRON_SWORD,
				"Passive. Dealing basic attack damage to an enemy with over " + 
				DescUtil.yellow(rendThreshold) + " " + GlossaryTag.REND.tag(this) + " deals " + 
				GlossaryTag.SLASHING.tag(this, damage, true) + " damage in an area around them.");
	}
}
