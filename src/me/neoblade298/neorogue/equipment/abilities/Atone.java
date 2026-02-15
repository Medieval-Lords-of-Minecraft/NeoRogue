package me.neoblade298.neorogue.equipment.abilities;

import java.util.LinkedList;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neocore.bukkit.effects.Circle;
import me.neoblade298.neocore.bukkit.effects.LocalAxes;
import me.neoblade298.neocore.bukkit.effects.ParticleAnimation;
import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neocore.bukkit.effects.ParticleShapeMemory;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetType;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class Atone extends Equipment {
	private static final String ID = "Atone";
	private int damage, sanct;
	private static final TargetProperties tp = TargetProperties.radius(5, false, TargetType.ENEMY);
	private static final ParticleAnimation anim;
	private static final ParticleContainer circPart = new ParticleContainer(Particle.FIREWORK);
	private static final Circle circ = new Circle(tp.range);
	
	static {
		anim = new ParticleAnimation(circPart, (loc, tick) -> {
			ParticleShapeMemory mem = circ.calculate(loc.add(0, 2 - (0.5 * tick), 0), LocalAxes.xz());
			LinkedList<Location> partLocs = mem.getEdges();
			if (tick == 4) partLocs.addAll(mem.getFill());
			return partLocs;
		}, 5);
	}
	
	public Atone(boolean isUpgraded) {
		super(ID, "Atone", isUpgraded, Rarity.UNCOMMON, EquipmentClass.WARRIOR,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(15, 10, 5, 7));
		
		damage = isUpgraded ? 300 : 200;
		sanct = isUpgraded ? 10 : 7;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		Equipment eq = this;
		data.addTrigger(id, bind, new EquipmentInstance(data, this, slot, es, (pd, in) -> {
			Player p = data.getPlayer();
			p.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 20, 0));
			data.addTask(new BukkitRunnable() {
				public void run() {
					anim.play(p, p);
				}
			}.runTaskLater(NeoRogue.inst(), 20L));
			
			data.addTask(new BukkitRunnable() {
				public void run() {
					Sounds.firework.play(p, p);
					for (LivingEntity ent : TargetHelper.getEntitiesInRadius(p, tp)) {
						double increase = FightInstance.getFightData(ent).getStatus(StatusType.SANCTIFIED).getStacks() * sanct;
						FightInstance.dealDamage(new DamageMeta(data, damage + increase, DamageType.LIGHT, DamageStatTracker.of(id + slot, eq)), ent);
					}
				}
			}.runTaskLater(NeoRogue.inst(), 25L));
			return TriggerResult.keep();
		}));
	}

	@Override
	public void setupItem() {
		item = createItem(Material.BLADE_POTTERY_SHERD,
				"On cast, " + DescUtil.charge(this, 1, 1) + " before dealing " + GlossaryTag.LIGHT.tag(this, damage, true) + " to nearby enemies. "
				+ "Increase damage dealt by number of " + GlossaryTag.SANCTIFIED.tag(this) + " stacks on the enemy multiplied by <yellow>" + sanct + "</yellow>.");
	}
}
