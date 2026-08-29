package me.neoblade298.neorogue.equipment.abilities;

import java.util.HashSet;

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
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Power;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.DamageSlice;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.BuffStatTracker;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.DealDamageEvent;

public class SageOfTheSixPaths extends Equipment implements Power {
	private static final String ID = "SageOfTheSixPaths";
	private static final Circle AWAKENING_RING = new Circle(1.5);
	private static final ParticleContainer AWAKENING_EDGE = new ParticleContainer(Particle.DUST)
			.dustOptions(new DustOptions(Color.fromRGB(235, 205, 255), 1.2F)).count(1).spread(0, 0).speed(0);
	private static final ParticleContainer AWAKENING_BURST = new ParticleContainer(Particle.FIREWORK)
			.count(14).spread(0.1, 0.1).speed(0.01).offsetY(1);
	private static final SoundContainer AWAKENING_SOUND = new SoundContainer(Sound.BLOCK_BEACON_ACTIVATE, 0.75F, 1.4F);
	private int requiredTypes, magicIncrease, shields;
	private double magicMultiplier;

	public SageOfTheSixPaths(boolean isUpgraded) {
		super(ID, "Sage of the Six Paths", isUpgraded, Rarity.EPIC, EquipmentClass.MAGE,
				EquipmentType.ABILITY, EquipmentProperties.none());
		requiredTypes = 6;
		magicIncrease = isUpgraded ? 150 : 100;
		magicMultiplier = magicIncrease / 100.0;
		shields = 50;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		HashSet<DamageType> types = new HashSet<>();
		data.addTrigger(id, Trigger.DEAL_DAMAGE, (pdata, in) -> {
			DealDamageEvent ev = (DealDamageEvent) in;
			for (DamageSlice slice : ev.getMeta().getSlices()) types.add(slice.getPostBuffType());
			if (types.size() < requiredTypes) return TriggerResult.keep();
			if (activatePower(data, slot, es)) return TriggerResult.remove();
			return TriggerResult.keep();
		});
	}

	@Override
	public void onPowerActivated(PlayerFightData data, int slot, EquipSlot es) {
		Player p = data.getPlayer();
		AWAKENING_RING.play(AWAKENING_EDGE, p.getLocation().clone().add(0, 0.1, 0), LocalAxes.xz(), null);
		AWAKENING_BURST.play(p, p.getLocation());
		AWAKENING_SOUND.play(p, p);
		String buffId = id + slot;
		data.addDamageBuff(DamageBuffType.of(DamageCategory.MAGICAL),
				Buff.multiplier(data, magicMultiplier, BuffStatTracker.damageBuffAlly(buffId, this)));
		data.addPermanentShield(p.getUniqueId(), shields, this);
	}

	@Override
	public void setupItem() {
		item = createItem(Material.NETHER_STAR,
				GlossaryTag.PASSIVE.tag(this) + " " + GlossaryTag.POWER.tag(this) + ". After dealing "
				+ DescUtil.val(requiredTypes) + " different damage types, increase magical damage by "
				+ DescUtil.val(magicIncrease + "%") + " and gain " + GlossaryTag.SHIELDS.tag(this, shields) + ".");
	}
}
