package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.effects.Circle;
import me.neoblade298.neocore.bukkit.effects.LocalAxes;
import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neocore.bukkit.effects.SoundContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageSlice;
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.Rift;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.PreBasicAttackEvent;

public class EnergyHarvest extends Equipment {
	private static final String ID = "EnergyHarvest";
	private static final Circle HARVEST_RING = new Circle(1.25);
	private static final ParticleContainer HARVEST_EDGE = new ParticleContainer(Particle.DUST)
			.dustOptions(new DustOptions(Color.fromRGB(85, 35, 130), 1.1F)).count(1).spread(0, 0).speed(0);
	private static final ParticleContainer HARVEST_SPARK = new ParticleContainer(Particle.REVERSE_PORTAL)
			.count(8).spread(0.1, 0.1).speed(0.01).offsetY(0.5);
	private static final SoundContainer RIFT_SOUND = new SoundContainer(Sound.BLOCK_SCULK_CATALYST_BLOOM, 0.55F, 1.25F);
	private int damage, attacksPerRift, riftDurationTicks;

	public EnergyHarvest(boolean isUpgraded) {
		super(ID, "Energy Harvest", isUpgraded, Rarity.UNCOMMON, EquipmentClass.MAGE,
				EquipmentType.ABILITY, EquipmentProperties.none());
		damage = isUpgraded ? 15 : 10;
		attacksPerRift = isUpgraded ? 6 : 7;
		riftDurationTicks = 200;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		ActionMeta attacks = new ActionMeta();
		data.addTrigger(id, Trigger.PRE_BASIC_ATTACK, (pdata, in) -> {
			PreBasicAttackEvent ev = (PreBasicAttackEvent) in;
			ev.getMeta().addDamageSlice(new DamageSlice(data, damage, DamageType.DARK,
					DamageStatTracker.of(id + slot, this)));
			if (attacks.addCount(1) >= attacksPerRift) {
				attacks.setCount(0);
				Player p = data.getPlayer();
				org.bukkit.Location location = p.getLocation().clone();
				data.addRift(new Rift(data, location, riftDurationTicks, this));
				HARVEST_RING.play(HARVEST_EDGE, location.clone().add(0, 0.08, 0), LocalAxes.xz(), null);
				HARVEST_SPARK.play(p, location);
				RIFT_SOUND.play(p, location);
			}
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.SCULK,
				GlossaryTag.PASSIVE.tag(this) + ". Basic attacks deal an additional "
				+ GlossaryTag.DARK.tag(this, damage) + " damage. Every " + DescUtil.val(attacksPerRift)
				+ " basic attacks, create a " + GlossaryTag.RIFT.tag(this) + ".");
	}
}
