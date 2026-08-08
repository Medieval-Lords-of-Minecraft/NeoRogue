package me.neoblade298.neorogue.equipment.abilities;

import java.util.UUID;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import me.neoblade298.neocore.bukkit.effects.Circle;
import me.neoblade298.neocore.bukkit.effects.LocalAxes;
import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.buff.StatTracker;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class Berserk extends Equipment {
	private static final String ID = "Berserk";
	private static final double DAMAGE_REDUCTION = 0.5;
	private static final double PHYSICAL_INCREASE = 1;
	private static final Circle TRANSFORMATION_RING = new Circle(2);
	private static final ParticleContainer CRIMSON_RING = new ParticleContainer(Particle.DUST).count(1).spread(0, 0)
			.speed(0).dustOptions(new DustOptions(Color.fromRGB(145, 12, 20), 1.4F));
	private static final ParticleContainer DARKEN = new ParticleContainer(Particle.SOUL).count(30).spread(0.6, 1)
			.offsetY(1).speed(0.01);
	private int threshold;

	public Berserk(boolean isUpgraded) {
		super(ID, "Berserk", isUpgraded, Rarity.EPIC, EquipmentClass.WARRIOR, EquipmentType.ABILITY,
				EquipmentProperties.none());
		threshold = isUpgraded ? 30 : 40;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		ActionMeta activated = new ActionMeta();
		data.addTrigger(id, Trigger.RECEIVE_STATUS, (pdata, in) -> {
			if (activated.getBool() || data.getStatus(StatusType.BERSERK).getStacks() < threshold) {
				return TriggerResult.keep();
			}
			activated.setBool(true);
			String buffId = UUID.randomUUID().toString();
			data.addDefenseBuff(DamageBuffType.of(DamageCategory.DIRECT), Buff.multiplier(data,
					DAMAGE_REDUCTION, StatTracker.defenseBuffAlly(buffId, this)));
			data.addDamageBuff(DamageBuffType.of(DamageCategory.PHYSICAL), Buff.multiplier(data,
					PHYSICAL_INCREASE, StatTracker.damageBuffAlly(buffId, this)));
			Player p = data.getPlayer();
			p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, PotionEffect.INFINITE_DURATION, 0));
			TRANSFORMATION_RING.play(CRIMSON_RING, p.getLocation(), LocalAxes.xz(), null);
			DARKEN.play(p, p);
			Sounds.roar.play(p, p);
			Sounds.breaks.play(p, p);
			return TriggerResult.remove();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.FIRE_CHARGE,
				GlossaryTag.PASSIVE.tag(this) + ". Upon reaching " + GlossaryTag.BERSERK.tag(this, threshold)
						+ ", permanently reduce all damage taken by " + DescUtil.white("50%") + ", increase "
						+ GlossaryTag.PHYSICAL.tag(this) + " damage dealt by " + DescUtil.white("100%")
						+ ", and gain Blindness for the remainder of the fight. Activates once per fight.");
	}
}