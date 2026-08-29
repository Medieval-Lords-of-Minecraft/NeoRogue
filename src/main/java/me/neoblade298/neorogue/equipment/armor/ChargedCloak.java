package me.neoblade298.neorogue.equipment.armor;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import me.neoblade298.neocore.bukkit.effects.Circle;
import me.neoblade298.neocore.bukkit.effects.LocalAxes;
import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neocore.bukkit.effects.SoundContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
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
import me.neoblade298.neorogue.session.fight.trigger.event.ApplyStatusEvent;

public class ChargedCloak extends Equipment {
	private static final String ID = "ChargedCloak";
	private static final int DIRECT_REDUCTION = 1, SPEED_DURATION = 40, SPEED_AMPLIFIER = 1;
	private static final Circle THRESHOLD_RING = new Circle(1.1);
	private static final ParticleContainer THRESHOLD_PARTICLE = new ParticleContainer(Particle.DUST).count(1)
			.spread(0, 0).speed(0).dustOptions(new DustOptions(Color.fromRGB(80, 200, 255), 0.9F));
	private static final ParticleContainer SPEED_PARTICLE = new ParticleContainer(Particle.FIREWORK).count(8)
			.spread(0.1, 0.45).offsetY(0.8).speed(0.01);
	private static final SoundContainer THRESHOLD_SOUND = new SoundContainer(Sound.BLOCK_AMETHYST_BLOCK_CHIME,
			0.45F, 1.6F);
	private static final SoundContainer SPEED_SOUND = new SoundContainer(Sound.ENTITY_BREEZE_WIND_BURST,
			0.35F, 1.45F);
	private int threshold;

	public ChargedCloak(boolean isUpgraded) {
		super(ID, "Charged Cloak", isUpgraded, Rarity.UNCOMMON, EquipmentClass.THIEF, EquipmentType.ARMOR);
		threshold = isUpgraded ? 6 : 9;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		data.addDefenseBuff(DamageBuffType.of(DamageCategory.DIRECT), Buff.increase(data, DIRECT_REDUCTION,
				StatTracker.defenseBuffAlly(id + slot, this)));

		ActionMeta appliedStacks = new ActionMeta();
		data.addTrigger(id, Trigger.APPLY_STATUS, (pdata, in) -> {
			ApplyStatusEvent event = (ApplyStatusEvent) in;
			if (!event.isStatus(StatusType.ELECTRIFIED) || event.getStacks() <= 0) return TriggerResult.keep();

			int total = appliedStacks.getCount() + event.getStacks();
			int activations = total / threshold;
			appliedStacks.setCount(total % threshold);
			if (activations > 0) {
				Player p = data.getPlayer();
				THRESHOLD_RING.play(p, THRESHOLD_PARTICLE, p.getLocation().add(0, 0.15, 0), LocalAxes.xz(), null);
				SPEED_PARTICLE.play(p, p);
				THRESHOLD_SOUND.play(p, p);
				SPEED_SOUND.play(p, p);
				for (int i = 0; i < activations; i++) {
					p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, SPEED_DURATION, SPEED_AMPLIFIER));
				}
			}
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.LEATHER_CHESTPLATE,
				"Reduce incoming " + GlossaryTag.DIRECT.tag(this) + " damage by "
				+ DescUtil.white(DIRECT_REDUCTION) + ". For every "
				+ GlossaryTag.ELECTRIFIED.tag(this, threshold) + " you apply, gain "
				+ DescUtil.potion("Speed", SPEED_AMPLIFIER, SPEED_DURATION / 20) + ".");
	}
}