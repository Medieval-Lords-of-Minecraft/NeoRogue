package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neocore.bukkit.effects.ParticleUtil;
import me.neoblade298.neocore.bukkit.effects.SoundContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Power;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageMeta.DamageOrigin;
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

public class Starborn extends Equipment implements Power {
	private static final String ID = "Starborn";
	private static final TargetProperties RIFT_RANGE = TargetProperties.radius(8, false, TargetType.ENEMY);
	private static final ParticleContainer BEAM = new ParticleContainer(Particle.DUST)
			.dustOptions(new DustOptions(Color.fromRGB(205, 185, 255), 1.1F)).count(1).spread(0.02, 0.02).speed(0);
	private static final ParticleContainer BEAM_SPARK = new ParticleContainer(Particle.FIREWORK)
			.count(1).spread(0.02, 0.02).speed(0.01);
	private static final ParticleContainer STAR_BURST = new ParticleContainer(Particle.FIREWORK)
			.count(8).spread(0.1, 0.1).speed(0.01).offsetY(0.8);
	private static final SoundContainer STAR_PULSE = new SoundContainer(Sound.BLOCK_AMETHYST_BLOCK_RESONATE, 0.6F, 1.45F);
	private static final SoundContainer STAR_HIT = new SoundContainer(Sound.BLOCK_AMETHYST_BLOCK_CHIME, 0.35F, 1.75F);
	private int activationRifts, intervalSeconds, riftDurationTicks, damage;

	public Starborn(boolean isUpgraded) {
		super(ID, "Starborn", isUpgraded, Rarity.RARE, EquipmentClass.MAGE,
				EquipmentType.ABILITY, EquipmentProperties.none());
		activationRifts = 3;
		intervalSeconds = 7;
		riftDurationTicks = 200;
		damage = isUpgraded ? 120 : 80;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		ActionMeta count = new ActionMeta();
		data.addTrigger(id, Trigger.CREATE_RIFT, (pdata, in) -> {
			if (count.addCount(1) < activationRifts) return TriggerResult.keep();
			if (activatePower(data, slot, es)) return TriggerResult.remove();
			return TriggerResult.keep();
		});
	}

	@Override
	public void onPowerActivated(PlayerFightData data, int slot, EquipSlot es) {
		ActionMeta timer = new ActionMeta();
		data.addTrigger(id + "-active", Trigger.PLAYER_TICK, (pdata, in) -> {
			if (timer.addCount(1) < intervalSeconds) return TriggerResult.keep();
			timer.setCount(0);
			Player p = data.getPlayer();
			data.addRift(new Rift(data, p.getLocation().clone(), riftDurationTicks, this));
			STAR_PULSE.play(p, p);
			for (Rift rift : data.getRifts().values()) {
				LivingEntity target = TargetHelper.getNearest(p, rift.getLocation(), RIFT_RANGE);
				if (target == null) continue;
				Location end = target.getLocation().add(0, 0.8, 0);
				Location start = rift.getLocation().clone().add(0, 0.3, 0);
				ParticleUtil.drawLine(p, BEAM, start, end, 0.25);
				ParticleUtil.drawLine(p, BEAM_SPARK, start, end, 0.5);
				STAR_BURST.play(p, target.getLocation());
				STAR_HIT.play(p, target.getLocation());
				FightInstance.dealDamage(new DamageMeta(data, damage, DamageType.DARK,
						DamageStatTracker.of(id + slot, this), DamageOrigin.RIFT), target);
			}
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.NETHER_STAR,
				GlossaryTag.PASSIVE.tag(this) + " " + GlossaryTag.POWER.tag(this) + ". Activates after creating "
				+ DescUtil.val(activationRifts) + " " + GlossaryTag.RIFT.tagPlural(this) + ". Every "
				+ DescUtil.val(intervalSeconds + "s") + ", create a " + GlossaryTag.RIFT.tag(this)
				+ ", then every rift deals " + GlossaryTag.DARK.tag(this, damage)
				+ " damage to its nearest enemy within " + DescUtil.val((int) RIFT_RANGE.range) + " blocks.");
	}
}
