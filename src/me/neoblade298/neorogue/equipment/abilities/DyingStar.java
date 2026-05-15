package me.neoblade298.neorogue.equipment.abilities;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import me.neoblade298.neocore.bukkit.effects.Circle;
import me.neoblade298.neocore.bukkit.effects.LocalAxes;
import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neocore.bukkit.effects.SoundContainer;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.Rift;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class DyingStar extends Equipment {
	private static final String ID = "DyingStar";
	private static final TargetProperties tp = TargetProperties.radius(5, true, TargetType.ENEMY);
	private static final Circle circ = new Circle(tp.range);
	private static final ParticleContainer pull = new ParticleContainer(Particle.END_ROD).count(50).spread(2.5, 2.5).offsetY(1),
		explode = new ParticleContainer(Particle.DRAGON_BREATH).count(100).spread(2.5, 2.5).offsetY(1);
	private static final SoundContainer pullSound = new SoundContainer(Sound.ENTITY_ENDER_DRAGON_GROWL),
		explodeSound = new SoundContainer(Sound.ENTITY_GENERIC_EXPLODE);
	private int damage;
	
	public DyingStar(boolean isUpgraded) {
		super(ID, "Dying Star", isUpgraded, Rarity.RARE, EquipmentClass.MAGE,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(0, 0, 0, 0));
		damage = isUpgraded ? 450 : 300;
		properties.add(PropertyType.DAMAGE, damage);
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void setupItem() {
		item = createItem(Material.NETHER_STAR,
				GlossaryTag.POWER.tag(this) + ". Create a " + GlossaryTag.RIFT.tag(this) + ". Afterwards, when any " + GlossaryTag.RIFT.tag(this) +
				" expires, pull in nearby enemies, then explode " + DescUtil.white("1s") + " later dealing " +
				GlossaryTag.DARK.tag(this, damage, true) + " damage. If an enemy is killed, spawn a new " +
				GlossaryTag.RIFT.tag(this) + " [" + DescUtil.white("10s") + "] in the same place.");
	}

	private static final int ACTIVATION_THRES = 2;

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ActionMeta am = new ActionMeta();
		data.addTrigger(id, Trigger.CREATE_RIFT, (pdata, in) -> {
			am.addCount(1);
			if (am.getCount() < ACTIVATION_THRES) return TriggerResult.keep();

			Player p = data.getPlayer();
			Sounds.fire.play(p, p);
			Util.msg(p, hoverable.append(Component.text(" was activated", NamedTextColor.GRAY)));

			// Create a rift on activation
			data.addRift(new Rift(data, p.getLocation(), 100));

			// Track rift explosion locations and entities hit by them
			HashMap<UUID, Location> killedEntityToRiftLoc = new HashMap<>();

			// Handle rift expiration - pull enemies and explode
			data.addTrigger(id, Trigger.REMOVE_RIFT, (pdata2, in2) -> {
				Player p2 = data.getPlayer();
				Rift rift = (Rift) in2;
				Location riftLoc = rift.getLocation();

				// Pull enemies toward rift
				pullSound.play(p2, riftLoc);
				circ.play(pull, riftLoc, LocalAxes.xz(), null);
				for (LivingEntity ent : TargetHelper.getEntitiesInRadius(p2, riftLoc, tp)) {
					Vector direction = riftLoc.toVector().subtract(ent.getLocation().toVector());
					if (!direction.isZero()) {
						direction = direction.normalize().setY(0.3);
						ent.setVelocity(direction);
					}
				}

				// Explode 1 second later
				data.addTask(new BukkitRunnable() {
					public void run() {
						Player p3 = data.getPlayer();
						explodeSound.play(p3, riftLoc);
						circ.play(explode, riftLoc, LocalAxes.xz(), null);
						Sounds.explode.play(p3, riftLoc);

						for (LivingEntity ent : TargetHelper.getEntitiesInRadius(p3, riftLoc, tp)) {
							FightInstance.dealDamage(data, DamageType.DARK, damage, ent,
								DamageStatTracker.of(id + slot, DyingStar.this));
							killedEntityToRiftLoc.put(ent.getUniqueId(), riftLoc.clone());
						}

						// Clean up old entries after a short delay
						data.addTask(new BukkitRunnable() {
							public void run() {
								killedEntityToRiftLoc.entrySet().removeIf(entry ->
									entry.getValue().equals(riftLoc));
							}
						}.runTaskLater(NeoRogue.inst(), 10L));
					}
				}.runTaskLater(NeoRogue.inst(), 20L));

				return TriggerResult.keep();
			});

			// Handle kills to spawn new rifts
			data.addTrigger(id, Trigger.KILL_GLOBAL, (pdata2, in2) -> {
				LivingEntity ent = (LivingEntity) in2;
				Location riftLoc = killedEntityToRiftLoc.remove(ent.getUniqueId());
				if (riftLoc != null) {
					Player p2 = data.getPlayer();
					Sounds.equip.play(p2, riftLoc);
					data.addRift(new Rift(data, riftLoc, 200));
				}
				return TriggerResult.keep();
			});

			return TriggerResult.remove();
		});
	}
}
