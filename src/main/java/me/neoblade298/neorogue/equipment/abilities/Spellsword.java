package me.neoblade298.neorogue.equipment.abilities;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neocore.bukkit.effects.SoundContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Power;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.DamageSlice;
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.DealDamageEvent;
import me.neoblade298.neorogue.session.fight.trigger.event.PreBasicAttackEvent;

public class Spellsword extends Equipment implements Power {
	private static final String ID = "Spellsword";
	private static final int ACTIVATION_THRES = 3;
	private int damage;
	private static final ParticleContainer pc = new ParticleContainer(Particle.CLOUD);
	private static final ParticleContainer hitGlow = new ParticleContainer(Particle.DUST)
			.dustOptions(new DustOptions(Color.fromRGB(255, 215, 80), 1.1F))
			.count(18).spread(0.1, 0.1).offsetY(0.8).speed(0);
	private static final ParticleContainer hitSpark = new ParticleContainer(Particle.FIREWORK)
			.count(12).spread(0.08, 0.08).offsetY(0.8).speed(0.01);
	private static final SoundContainer hitChime = new SoundContainer(Sound.BLOCK_AMETHYST_BLOCK_BREAK, 0.7F, 1.35F),
			hitImpact = new SoundContainer(Sound.ENTITY_PLAYER_ATTACK_CRIT, 0.55F, 1.15F);

	public Spellsword(boolean isUpgraded) {
		super(ID, "Spellsword", isUpgraded, Rarity.RARE, EquipmentClass.WARRIOR, EquipmentType.ABILITY,
				EquipmentProperties.none());
		damage = isUpgraded ? 260 : 175;
		pc.count(50).spread(0.5, 0.5).speed(0.2);
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		ActionMeta activation = new ActionMeta();
		data.addTrigger(id, Trigger.DEAL_DAMAGE, (pdata, in) -> {
			DealDamageEvent ev = (DealDamageEvent) in;
			if (!ev.getMeta().containsType(DamageCategory.MAGICAL)) return TriggerResult.keep();
			activation.addCount(1);
			if (activation.getCount() < ACTIVATION_THRES) return TriggerResult.keep();
			if (activatePower(data, slot, es)) return TriggerResult.remove();
			return TriggerResult.keep();
		});
	}

	@Override
	public void onPowerActivated(PlayerFightData data, int slot, EquipSlot es) {
		ActionMeta empowered = new ActionMeta();
		data.addTrigger(id + "-active", Trigger.CAST_USABLE, (pdata, in) -> {
			empowered.setBool(true);
			Player p = data.getPlayer();
			Sounds.equip.play(p, p);
			pc.play(p, p);
			return TriggerResult.keep();
		});

		data.addTrigger(id + "-attack", Trigger.PRE_BASIC_ATTACK, (pdata, in) -> {
			if (!empowered.getBool()) return TriggerResult.keep();
			empowered.setBool(false);
			Player p = data.getPlayer();
			PreBasicAttackEvent ev = (PreBasicAttackEvent) in;
			ev.getMeta().addDamageSlice(
					new DamageSlice(data, damage, DamageType.LIGHT, DamageStatTracker.of(id + slot, this)));
			hitGlow.play(p, ev.getTarget());
			hitSpark.play(p, ev.getTarget());
			hitChime.play(p, ev.getTarget());
			hitImpact.play(p, ev.getTarget());
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.FLINT,
				GlossaryTag.PASSIVE.tag(this) + " " + GlossaryTag.POWER.tag(this) + ". Activates after dealing "
						+ GlossaryTag.MAGICAL.tag(this) + " damage " + DescUtil.val(ACTIVATION_THRES)
						+ " times. Casting an ability empowers your next basic attack to deal an additional "
						+ GlossaryTag.LIGHT.tag(this, damage) + " damage.");
	}
}
