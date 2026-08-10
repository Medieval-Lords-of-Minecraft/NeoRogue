package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

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

public class CrateringBlows extends Equipment {
	private static final String ID = "CrateringBlows";
	private static final double ROTATIONS_PER_SECOND = 1, RADIUS = 3, DURATION = 3;
	private static final ParticleContainer ORBITAL_PARTICLE = new ParticleContainer(Particle.BLOCK)
			.blockData(Material.DEEPSLATE.createBlockData()).count(1).spread(0.05, 0.05).speed(0);
	private static final ParticleContainer ORBITAL_DUST = new ParticleContainer(Particle.DUST_PLUME)
			.count(1).spread(0.05, 0.05).speed(0);
	private static final ParticleContainer HIT = new ParticleContainer(Particle.BLOCK)
			.blockData(Material.DEEPSLATE.createBlockData()).count(10).spread(0.1, 0.1).speed(0.01);
	private static final SoundContainer HIT_SOUND = new SoundContainer(Sound.BLOCK_DEEPSLATE_BREAK, 0.8F, 0.7F);
	private static final Orbital ORBITAL = new Orbital(ROTATIONS_PER_SECOND, RADIUS, DURATION) {
		@Override
		public void onTick(OrbitalInstance orbital, int interpolation) {
			Player p = ((PlayerFightData) orbital.getOwner()).getPlayer();
			ORBITAL_PARTICLE.play(p, orbital.getLocation());
			ORBITAL_DUST.play(p, orbital.getLocation());
		}

		@Override
		public void onHit(FightData hit, Barrier hitBarrier, DamageMeta meta, OrbitalInstance orbital) {
			Player p = ((PlayerFightData) orbital.getOwner()).getPlayer();
			HIT.play(p, orbital.getLocation());
			HIT_SOUND.play(p, orbital.getLocation());
		}
	}.pierce(-1);
	private int damage;

	public CrateringBlows(boolean isUpgraded) {
		super(ID, "Cratering Blows", isUpgraded, Rarity.UNCOMMON, EquipmentClass.WARRIOR,
				EquipmentType.ABILITY, EquipmentProperties.none());
		damage = isUpgraded ? 90 : 60;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		data.addTrigger(id, Trigger.APPLY_STATUS, (pdata, in) -> {
			ApplyStatusEvent ev = (ApplyStatusEvent) in;
			if (!ev.isStatus(StatusType.CONCUSSED)) return TriggerResult.keep();
			ORBITAL.start(data).addDamageSlice(new DamageSlice(data, damage, DamageType.EARTHEN,
					DamageStatTracker.of(id + slot, this)));
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.HEAVY_CORE, "Whenever you apply " + GlossaryTag.CONCUSSED.tag(this)
				+ ", create a " + DescUtil.white((int) DURATION + "s") + " orbital that deals "
				+ GlossaryTag.EARTHEN.tag(this, damage) + " damage at radius " + DescUtil.white((int) RADIUS) + ".");
	}
}