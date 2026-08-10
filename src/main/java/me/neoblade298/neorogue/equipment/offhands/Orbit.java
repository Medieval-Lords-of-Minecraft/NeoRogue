package me.neoblade298.neorogue.equipment.offhands;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neocore.bukkit.effects.SoundContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.equipment.mechanics.Barrier;
import me.neoblade298.neorogue.equipment.mechanics.Orbital;
import me.neoblade298.neorogue.equipment.mechanics.OrbitalInstance;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageSlice;
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.ApplyStatusEvent;

public class Orbit extends Equipment {
	private static final String ID = "Orbit";
	private static final double DURATION = 5, ROTATIONS_PER_SECOND = 0.5;
	private static final int SLOWNESS_TICKS = 40;
	private static final ParticleContainer ORBITAL_TRAIL = new ParticleContainer(Particle.BLOCK)
			.blockData(Material.END_STONE.createBlockData()).count(1).spread(0, 0).speed(0);
	private static final ParticleContainer CREATION = ORBITAL_TRAIL.clone().count(8).spread(0.1, 0.1).speed(0.01);
	private static final ParticleContainer IMPACT = ORBITAL_TRAIL.clone().count(6).spread(0.1, 0.1).speed(0.01);
	private static final SoundContainer CREATION_SOUND = new SoundContainer(Sound.BLOCK_AMETHYST_BLOCK_RESONATE, 0.6F, 0.75F);
	private static final SoundContainer IMPACT_SOUND = new SoundContainer(Sound.BLOCK_DEEPSLATE_HIT, 0.5F, 1.15F);
	private static final Orbital[] ORBITALS = {
			createOrbital(3, ROTATIONS_PER_SECOND),
			createOrbital(5, -ROTATIONS_PER_SECOND),
			createOrbital(7, ROTATIONS_PER_SECOND)
	};
	private final int damage;

	public Orbit(boolean isUpgraded) {
		super(ID, "Orbit", isUpgraded, Rarity.RARE, EquipmentClass.MAGE, EquipmentType.OFFHAND,
				EquipmentProperties.none());
		damage = isUpgraded ? 180 : 120;
	}

	private static Orbital createOrbital(double radius, double speed) {
		return new Orbital(speed, radius, DURATION) {
			@Override
			public void onTick(OrbitalInstance orbital, int interpolation) {
				if (orbital.getOwner().getEntity() instanceof Player) {
					ORBITAL_TRAIL.play((Player) orbital.getOwner().getEntity(), orbital.getLocation());
				}
			}

			@Override
			public void onHit(FightData hit, Barrier hitBarrier, DamageMeta meta, OrbitalInstance orbital) {
				if (hitBarrier == null) {
					hit.getEntity().addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, SLOWNESS_TICKS, 0));
					if (orbital.getOwner().getEntity() instanceof Player) {
						Player player = (Player) orbital.getOwner().getEntity();
						IMPACT.play(player, hit.getEntity().getLocation().add(0, 1, 0));
						IMPACT_SOUND.play(player, hit.getEntity());
					}
				}
			}

			@Override
			public void onFizzle(OrbitalInstance orbital) {
				boolean[] occupied = (boolean[]) orbital.getActionMeta().getObject();
				occupied[orbital.getActionMeta().getInt()] = false;
			}
		}.pierce(-1).ignore(true, true, false);
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		boolean[] occupied = new boolean[ORBITALS.length];
		data.addTrigger(id, Trigger.APPLY_STATUS, (pdata, in) -> {
			ApplyStatusEvent event = (ApplyStatusEvent) in;
			if (!event.isStatus(StatusType.CONCUSSED)) return TriggerResult.keep();
			for (int index = 0; index < occupied.length; index++) {
				if (occupied[index]) continue;
				occupied[index] = true;
				OrbitalInstance orbital = ORBITALS[index].start(data);
				orbital.getActionMeta().setObject(occupied);
				orbital.getActionMeta().setInt(index);
				orbital.addDamageSlice(new DamageSlice(data, damage, DamageType.EARTHEN,
						DamageStatTracker.of(id + slot, this)));
				Player player = data.getPlayer();
				CREATION.play(player, orbital.getLocation());
				CREATION_SOUND.play(player, player);
				break;
			}
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.ENDER_EYE, GlossaryTag.PASSIVE.tag(this) + ". Applying "
				+ GlossaryTag.CONCUSSED.tag(this) + " creates an orbital for " + DescUtil.white("5s")
				+ ", up to " + DescUtil.white(3) + " at radii " + DescUtil.white("3, 5, and 7")
				+ ". Each deals " + GlossaryTag.EARTHEN.tag(this, damage) + " damage once per enemy and applies "
				+ DescUtil.potion("Slowness", 1, 2) + ".");
	}
}