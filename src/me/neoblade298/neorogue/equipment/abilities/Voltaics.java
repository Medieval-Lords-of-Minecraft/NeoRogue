package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neocore.bukkit.effects.SoundContainer;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.ActionMeta;
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

public class Voltaics extends Equipment {
	private static final String ID = "Voltaics";
	private static final int TICK_INTERVAL = 5;
	private static final int BASE_ELECTRIFIED_STACKS = 3;
	private static final int BASE_DURATION_SECONDS = 5;
	private static final TargetProperties tp = TargetProperties.line(16, 2, TargetType.ENEMY);
	private static final ParticleContainer lightning = new ParticleContainer(Particle.ELECTRIC_SPARK)
			.count(20).spread(0.3, 0.3).speed(0.1);
	private static final SoundContainer voltSound = new SoundContainer(Sound.ENTITY_LIGHTNING_BOLT_IMPACT);
	private int damage;
	
	public Voltaics(boolean isUpgraded) {
		super(ID, "Voltaics", isUpgraded, Rarity.RARE, EquipmentClass.MAGE,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(20, 60, 0, tp.range));
		damage = isUpgraded ? 120 : 80;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ActionMeta tickCounter = new ActionMeta();
		ActionMeta totalElectrified = new ActionMeta();
		
		// Every 5 ticks, fire a projectile
		data.addTrigger(id, Trigger.PLAYER_TICK, (pdata, in) -> {
			tickCounter.addCount(1);
			if (tickCounter.getCount() >= TICK_INTERVAL) {
				tickCounter.setCount(0);
				
				Player p = data.getPlayer();
				LivingEntity target = TargetHelper.getNearestInSight(p, tp);
				
				if (target != null) {
					FightData targetData = FightInstance.getFightData(target.getUniqueId());
					if (targetData != null) {
						// Deal damage
						FightInstance.dealDamage(new DamageMeta(data, damage, DamageType.LIGHTNING, 
								DamageStatTracker.of(id + slot, this)), target);
						
						// Calculate duration based on cumulative electrified applied
						int baseDurationTicks = BASE_DURATION_SECONDS * 20;
						int scaledDurationTicks = baseDurationTicks + (totalElectrified.getCount() * 2);
						
						// Apply electrified status
						targetData.applyStatus(StatusType.ELECTRIFIED, data, BASE_ELECTRIFIED_STACKS, scaledDurationTicks);
						totalElectrified.addCount(BASE_ELECTRIFIED_STACKS);
						
						// Play effects
						Sounds.thunder.play(p, target);
						voltSound.play(p, target);
						lightning.play(p, target.getLocation());
					}
				}
			}
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.AMETHYST_SHARD,
				"Passive. Every <white>" + TICK_INTERVAL + "</white> ticks, fire a bolt of lightning at the nearest enemy in sight that deals " + 
				GlossaryTag.LIGHTNING.tag(this, damage, true) + " and applies " + GlossaryTag.ELECTRIFIED.tag(this, BASE_ELECTRIFIED_STACKS, false) + 
				". The duration of applied " + GlossaryTag.ELECTRIFIED.tag(this) + " increases by <white>2 ticks</white> for every " + 
				GlossaryTag.ELECTRIFIED.tag(this) + " you've applied this fight.");
	}
}
