package me.neoblade298.neorogue.equipment.abilities;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neocore.bukkit.effects.Circle;
import me.neoblade298.neocore.bukkit.effects.LocalAxes;
import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.SessionEquipment;
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

public class Expunge extends Equipment {
	private static final String ID = "Expunge";
	private int stacks, bonus, poisonDuration;
	private static final TargetProperties tp = TargetProperties.radius(5, false, TargetType.ENEMY);
	private static final ParticleContainer circPart = new ParticleContainer(Particle.DUST)
			.dustOptions(new DustOptions(Color.GREEN, 1F));
	private static final Circle circ = new Circle(tp.range);
	
	public Expunge(boolean isUpgraded) {
		super(ID, "Expunge", isUpgraded, Rarity.UNCOMMON, EquipmentClass.THIEF,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(25, 0, 12, 7));
		
		stacks = isUpgraded ? 30 : 20;
		bonus = isUpgraded ? 12 : 8;
		poisonDuration = 60;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		data.addTrigger(id, bind, new EquipmentInstance(data, sessionEq, slot, es, (pd, in) -> {
			data.charge(20, 0);
			
			data.addTask(new BukkitRunnable() {
				public void run() {
					Player p = data.getPlayer();
					Sounds.extinguish.play(p, p);
					circ.play(circPart, p.getLocation(), LocalAxes.xz(), null);
					for (LivingEntity ent : TargetHelper.getEntitiesInRadius(p, tp)) {
						FightData fd = FightInstance.getFightData(ent);
						fd.applyStatus(StatusType.POISON, data, stacks, poisonDuration, Expunge.this);
						double dmg = FightInstance.getFightData(ent).getStatus(StatusType.POISON).getStacks() * bonus;
						FightInstance.dealDamage(new DamageMeta(data, dmg, DamageType.POISON, DamageStatTracker.of(id + slot, Expunge.this)), ent);
					}
				}
			}.runTaskLater(NeoRogue.inst(), 25L));
			return TriggerResult.keep();
		}));
	}

	@Override
	public void setupItem() {
		item = createItem(Material.CACTUS,
				"On cast, " + DescUtil.charge(this, 1, 1) + " before applying " + GlossaryTag.POISON.tag(this, stacks, true) + " [" + DescUtil.white(poisonDuration / 20 + "s") + "] to nearby enemies. "
				+ "Then, deal " + GlossaryTag.POISON.tag(this) + " damage based on " + GlossaryTag.POISON.tag(this) + " stacks on the enemy multiplied by " + DescUtil.yellow(bonus) + ".");
	}
}
