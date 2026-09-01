package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neocore.bukkit.effects.Circle;
import me.neoblade298.neocore.bukkit.effects.LocalAxes;
import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neocore.bukkit.effects.ParticleUtil;
import me.neoblade298.neocore.bukkit.effects.SoundContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class Lacerate extends Equipment {
	private static final String ID = "Lacerate";
	private static final int DAMAGE_DELAY = 20;
	private static final double LINE_TOLERANCE = 2;
	private static final TargetProperties TARGETS = TargetProperties.line(0, LINE_TOLERANCE, TargetType.ENEMY);
	private static final Circle CAST_RING = new Circle(0.75);
	private static final ParticleContainer CAST_PARTICLE = new ParticleContainer(Particle.CLOUD).count(1)
			.spread(0, 0).speed(0);
	private static final ParticleContainer DASH_PARTICLE = new ParticleContainer(Particle.CLOUD).count(8)
			.spread(0.1, 0.25).offsetY(0.5).speed(0.01);
	private static final ParticleContainer SLASH_LINE = new ParticleContainer(Particle.DUST).count(1)
			.spread(0.03, 0.03).speed(0).dustOptions(new DustOptions(Color.fromRGB(220, 235, 245), 0.9F));
	private static final ParticleContainer SLASH_FLASH = new ParticleContainer(Particle.SWEEP_ATTACK).count(1)
			.spread(0, 0).speed(0);
	private static final ParticleContainer TARGET_IMPACT = new ParticleContainer(Particle.ENCHANTED_HIT).count(10)
			.spread(0.1, 0.4).offsetY(1).speed(0.01);
	private static final SoundContainer CAST_SOUND = new SoundContainer(Sound.ENTITY_PLAYER_ATTACK_SWEEP, 0.45F, 1.45F);
	private static final SoundContainer SLASH_SOUND = new SoundContainer(Sound.ENTITY_PLAYER_ATTACK_CRIT, 0.7F, 0.85F);
	private int damage;

	public Lacerate(boolean isUpgraded) {
		super(ID, "Lacerate", isUpgraded, Rarity.UNCOMMON, EquipmentClass.THIEF, EquipmentType.ABILITY,
				EquipmentProperties.ofUsable(0, 20, 7, 0));
		damage = isUpgraded ? 120 : 80;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		data.addTrigger(id, bind, new EquipmentInstance(data, sessionEq, slot, es, (pdata, in) -> {
			Player p = data.getPlayer();
			Location castLocation = p.getLocation().clone();
			CAST_RING.play(p, CAST_PARTICLE, castLocation.clone().add(0, 0.12, 0), LocalAxes.xz(), null);
			DASH_PARTICLE.play(p, p);
			CAST_SOUND.play(p, p);
			data.dash();
			data.addTask(new BukkitRunnable() {
				@Override
				public void run() {
					Player p = data.getPlayer();
					if (p.getWorld() != castLocation.getWorld()) return;
					Location start = castLocation.clone().add(0, 1, 0);
					Location end = p.getLocation().clone().add(0, 1, 0);
					ParticleUtil.drawLine(p, SLASH_LINE, start, end, 0.25);
					Location midpoint = start.clone().add(end.toVector().subtract(start.toVector()).multiply(0.5));
					SLASH_FLASH.play(p, midpoint);
					SLASH_SOUND.play(p, midpoint);
					for (LivingEntity target : TargetHelper.getEntitiesInLine(p, start, end, TARGETS)) {
						TARGET_IMPACT.play(p, target);
						FightInstance.dealDamage(new DamageMeta(data, damage, DamageType.PIERCING,
								DamageStatTracker.of(id + slot, Lacerate.this)), target);
					}
				}
			}.runTaskLater(NeoRogue.inst(), DAMAGE_DELAY));
			return TriggerResult.keep();
		}));
	}

	@Override
	public void setupItem() {
		item = createItem(Material.SHEARS,
				"On cast, save your location and " + GlossaryTag.DASH.tag(this) + " forward. After "
				+ DescUtil.val("1s") + ", deal " + GlossaryTag.PIERCING.tag(this, damage)
				+ " damage to enemies in a line from the saved location to your current location.");
	}
}