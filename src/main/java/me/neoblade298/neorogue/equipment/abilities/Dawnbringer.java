package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neocore.bukkit.effects.SoundContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.equipment.mechanics.Barrier;
import me.neoblade298.neorogue.equipment.mechanics.Projectile;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileGroup;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileInstance;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageSlice;
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.buff.StatTracker;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class Dawnbringer extends Equipment {
	private static final String ID = "Dawnbringer";
	private static final int RANGE = 10, WIDTH = 4, SANCTIFIED_THRESHOLD = 10;
	private static final ParticleContainer chargeParticles = new ParticleContainer(Particle.DUST)
			.dustOptions(new DustOptions(Color.fromRGB(255, 220, 105), 1F)).count(12).spread(0.5, 0.8).speed(0.01).offsetY(1);
	private static final ParticleContainer waveCore =
			new ParticleContainer(Particle.END_ROD).count(1).spread(0, 0).speed(0);
	private static final ParticleContainer waveEdge = new ParticleContainer(Particle.DUST)
			.dustOptions(new DustOptions(Color.fromRGB(255, 220, 105), 1.2F)).count(1).spread(0, 0).speed(0);
	private static final ParticleContainer impactParticles =
			new ParticleContainer(Particle.END_ROD).count(8).spread(0.1, 0.1).speed(0.01);
	private static final ParticleContainer sanctifiedImpact =
			new ParticleContainer(Particle.FIREWORK).count(12).spread(0.1, 0.1).speed(0.01);
	private static final ParticleContainer buffPulse = new ParticleContainer(Particle.DUST)
			.dustOptions(new DustOptions(Color.fromRGB(255, 220, 105), 1.4F)).count(10).spread(0.1, 0.1).speed(0.01).offsetY(1);
	private static final SoundContainer launchSound =
			new SoundContainer(Sound.BLOCK_AMETHYST_BLOCK_CHIME, 0.75F, 1.25F);
	private static final SoundContainer impactSound =
			new SoundContainer(Sound.BLOCK_GLASS_BREAK, 0.55F, 1.4F);
	private static final SoundContainer qualifySound =
			new SoundContainer(Sound.BLOCK_AMETHYST_BLOCK_RESONATE, 0.7F, 1.15F);
	private int damage, lightBuff;

	public Dawnbringer(boolean isUpgraded) {
		super(ID, "Dawnbringer", isUpgraded, Rarity.UNCOMMON, EquipmentClass.WARRIOR,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(15, 10, 5, RANGE));
		damage = isUpgraded ? 300 : 200;
		lightBuff = isUpgraded ? 30 : 20;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		ProjectileGroup projectiles = new ProjectileGroup(new DawnbringerProjectile(data, this, slot));
		data.addTrigger(id, bind, new EquipmentInstance(data, sessionEq, slot, es, (pdata, in) -> {
			Player player = data.getPlayer();
			Sounds.enchant.play(player, player);
			chargeParticles.play(player, player);
			data.charge(20);
			data.addTask(new BukkitRunnable() {
				@Override
				public void run() {
					projectiles.start(data);
				}
			}.runTaskLater(NeoRogue.inst(), 20));
			return TriggerResult.keep();
		}));
	}

	@Override
	public void setupItem() {
		item = createItem(Material.GOLDEN_SWORD,
				"On cast, " + DescUtil.charge(this, 1, 1) + " before firing a " + DescUtil.val(WIDTH)
				+ " block wide piercing projectile that deals " + GlossaryTag.LIGHT.tag(this, damage)
				+ " damage. For each enemy hit with at least " + DescUtil.white(SANCTIFIED_THRESHOLD) + " "
				+ GlossaryTag.SANCTIFIED.tag(this) + ", permanently increase all "
				+ GlossaryTag.LIGHT.tag(this) + " damage by " + DescUtil.yellow(lightBuff) + ".");
	}

	private class DawnbringerProjectile extends Projectile {
		private final PlayerFightData data;
		private final Equipment eq;
		private final int slot;

		public DawnbringerProjectile(PlayerFightData data, Equipment eq, int slot) {
			super(properties.get(PropertyType.RANGE), 2);
			this.size(WIDTH, 1).pierce(-1);
			this.blocksPerTick(2);
			this.data = data;
			this.eq = eq;
			this.slot = slot;
		}

		@Override
		public void onStart(ProjectileInstance proj) {
			Player player = data.getPlayer();
			Sounds.shoot.play(player, player);
			launchSound.play(player, player);
			proj.getMeta().addDamageSlice(new DamageSlice(data, damage, DamageType.LIGHT,
					DamageStatTracker.of(ID + slot, eq)));
		}

		@Override
		public void onTick(ProjectileInstance proj, int interpolation) {
			Player player = data.getPlayer();
			Vector velocity = proj.getVelocity().clone().normalize();
			Vector side = velocity.clone().rotateAroundY(Math.PI / 2);
			Location center = proj.getLocation();
			waveCore.play(player, center);
			waveCore.play(player, center.clone().add(side.clone()));
			waveCore.play(player, center.clone().subtract(side.clone()));
			waveEdge.play(player, center.clone().add(side.clone().multiply(2)));
			waveEdge.play(player, center.clone().subtract(side.clone().multiply(2)));
		}

		@Override
		public void onHit(FightData hit, Barrier hitBarrier, DamageMeta meta, ProjectileInstance proj) {
			Player player = data.getPlayer();
			Location hitLocation = hit.getEntity().getLocation().add(0, 1, 0);
			impactParticles.play(player, hitLocation);
			impactSound.play(player, hit.getEntity());
			if (hit.getStatus(StatusType.SANCTIFIED).getStacks() < SANCTIFIED_THRESHOLD) return;

			data.addDamageBuff(DamageBuffType.of(DamageCategory.LIGHT),
					Buff.increase(data, lightBuff, StatTracker.damageBuffAlly(ID + slot, eq)));
			sanctifiedImpact.play(player, hitLocation);
			buffPulse.play(player, player);
			qualifySound.play(player, hit.getEntity());
			Sounds.success.play(player, player);
		}
	}
}