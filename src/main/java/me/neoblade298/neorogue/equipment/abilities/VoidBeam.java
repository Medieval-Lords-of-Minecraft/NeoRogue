package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neocore.bukkit.effects.ParticleUtil;
import me.neoblade298.neocore.bukkit.effects.SoundContainer;
import me.neoblade298.neorogue.DescUtil;
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
import me.neoblade298.neorogue.session.fight.Rift;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class VoidBeam extends Equipment {
	private static final String ID = "VoidBeam";
	private static final TargetProperties LINE = TargetProperties.line(8, 1, TargetType.ENEMY);
	private static final ParticleContainer BEAM = new ParticleContainer(Particle.DUST)
			.dustOptions(new DustOptions(Color.fromRGB(90, 35, 145), 1.2F)).count(1).spread(0.02, 0.02).speed(0);
	private static final ParticleContainer BEAM_CORE = new ParticleContainer(Particle.REVERSE_PORTAL)
			.count(1).spread(0.03, 0.03).speed(0);
	private static final ParticleContainer CHARGE = new ParticleContainer(Particle.REVERSE_PORTAL)
			.count(8).spread(0.1, 0.1).speed(0.01).offsetY(1.2);
	private static final ParticleContainer IMPACT = new ParticleContainer(Particle.SOUL)
			.count(6).spread(0.1, 0.1).speed(0.01).offsetY(0.8);
	private static final SoundContainer CHARGE_SOUND = new SoundContainer(Sound.BLOCK_RESPAWN_ANCHOR_CHARGE, 0.55F, 1.35F);
	private static final SoundContainer BEAM_SOUND = new SoundContainer(Sound.ENTITY_WITHER_SHOOT, 0.65F, 1.25F);
	private static final SoundContainer RIFT_BONUS_SOUND = new SoundContainer(Sound.BLOCK_AMETHYST_BLOCK_CHIME, 0.45F, 1.55F);
	private int chargeTicks, damage, cooldownReduction;

	public VoidBeam(boolean isUpgraded) {
		super(ID, "Void Beam", isUpgraded, Rarity.UNCOMMON, EquipmentClass.MAGE,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(15, 0, 12, LINE.range));
		chargeTicks = 20;
		damage = isUpgraded ? 150 : 100;
		cooldownReduction = 5;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		EquipmentInstance inst = new EquipmentInstance(data, sessionEq, slot, es);
		inst.setAction((pdata, in) -> {
			Player caster = data.getPlayer();
			CHARGE.play(caster, caster.getLocation());
			CHARGE_SOUND.play(caster, caster);
			data.charge(chargeTicks).then(() -> {
				Player p = data.getPlayer();
				Location start = p.getEyeLocation();
				Vector direction = start.getDirection().normalize();
				Location end = start.clone().add(direction.multiply(LINE.range));
				ParticleUtil.drawLine(p, BEAM, start, end, 0.2);
				ParticleUtil.drawLine(p, BEAM_CORE, start, end, 0.4);
				BEAM_SOUND.play(p, p);
				for (LivingEntity target : TargetHelper.getEntitiesInLine(p, start, end, LINE)) {
					IMPACT.play(p, target.getLocation());
					FightInstance.dealDamage(new DamageMeta(data, damage, DamageType.DARK,
							DamageStatTracker.of(id + slot, this)), target);
				}
				if (nearRift(data, p.getLocation())) {
					inst.addCooldown(-cooldownReduction);
					RIFT_BONUS_SOUND.play(p, p);
				}
			});
			return TriggerResult.keep();
		});
		data.addTrigger(id, bind, inst);
	}

	private boolean nearRift(PlayerFightData data, Location location) {
		for (Rift rift : data.getRifts().values()) {
			if (rift.getLocation().getWorld().equals(location.getWorld())
					&& rift.getLocation().distanceSquared(location) <= LINE.range * LINE.range) return true;
		}
		return false;
	}

	@Override
	public void setupItem() {
		item = createItem(Material.ENDER_PEARL,
				DescUtil.charge(this, 0, 1) + ", then deal " + GlossaryTag.DARK.tag(this, damage)
				+ " damage in a line. If within " + DescUtil.val((int) LINE.range) + " blocks of a "
				+ GlossaryTag.RIFT.tag(this) + ", reduce this ability's cooldown by "
				+ DescUtil.val(cooldownReduction + "s") + ".");
	}
}
