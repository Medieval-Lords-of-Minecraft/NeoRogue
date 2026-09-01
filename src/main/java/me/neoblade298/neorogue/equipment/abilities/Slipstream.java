package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neocore.bukkit.effects.Circle;
import me.neoblade298.neocore.bukkit.effects.LocalAxes;
import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neocore.bukkit.effects.SoundContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Power;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.buff.StatTracker;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class Slipstream extends Equipment implements Power {
	private static final String ID = "Slipstream";
	private static final int DASHES_TO_ACTIVATE = 3, BUFF_DURATION = 120;
	private static final Circle UNLOCK_RING = new Circle(1.4);
	private static final ParticleContainer UNLOCK_PARTICLE = new ParticleContainer(Particle.DUST).count(1)
			.spread(0, 0).speed(0).dustOptions(new DustOptions(Color.fromRGB(110, 220, 255), 1F));
	private static final ParticleContainer UNLOCK_SPARK = new ParticleContainer(Particle.FIREWORK).count(14)
			.spread(0.1, 0.7).offsetY(0.8).speed(0.01);
	private static final ParticleContainer STACK_WIND = new ParticleContainer(Particle.CLOUD).count(5)
			.spread(0.1, 0.2).offsetY(0.45).speed(0.01);
	private static final ParticleContainer STACK_SPARK = new ParticleContainer(Particle.FIREWORK).count(3)
			.spread(0.08, 0.25).offsetY(0.65).speed(0.01);
	private static final SoundContainer UNLOCK_SOUND = new SoundContainer(Sound.BLOCK_AMETHYST_BLOCK_CHIME,
			0.7F, 1.35F);
	private static final SoundContainer STACK_SOUND = new SoundContainer(Sound.ENTITY_BREEZE_WIND_BURST,
			0.25F, 1.65F);
	private double damageIncrease;

	public Slipstream(boolean isUpgraded) {
		super(ID, "Slipstream", isUpgraded, Rarity.UNCOMMON, EquipmentClass.THIEF, EquipmentType.ABILITY,
				EquipmentProperties.none());
		damageIncrease = isUpgraded ? 0.05 : 0.03;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		ActionMeta activation = new ActionMeta();
		data.addTrigger(id, Trigger.DASH, (pdata, in) -> {
			if (activation.getBool()) return TriggerResult.keep();
			if (activation.addCount(1) < DASHES_TO_ACTIVATE) return TriggerResult.keep();
			if (activatePower(data, slot, es)) {
				activation.setBool(true);
				playUnlock(data);
				grantStack(data, slot, false);
				return TriggerResult.remove();
			}
			return TriggerResult.keep();
		});

		data.addTrigger(id, Trigger.EVADE, (pdata, in) -> {
			if (activation.getBool()) return TriggerResult.keep();
			if (activatePower(data, slot, es)) {
				activation.setBool(true);
				playUnlock(data);
				grantStack(data, slot, false);
				return TriggerResult.remove();
			}
			return TriggerResult.keep();
		});
	}

	@Override
	public void onPowerActivated(PlayerFightData data, int slot, EquipSlot es) {
		data.addTask(new BukkitRunnable() {
			@Override
			public void run() {
				ActionMeta queuedProc = new ActionMeta();
				Runnable queueStack = () -> {
					if (queuedProc.getBool()) return;
					queuedProc.setBool(true);
					data.addTask(new BukkitRunnable() {
						@Override
						public void run() {
							queuedProc.setBool(false);
							grantStack(data, slot, true);
						}
					}.runTask(NeoRogue.inst()));
				};

				data.addTrigger(id + "-active", Trigger.DASH, (pdata2, in2) -> {
					queueStack.run();
					return TriggerResult.keep();
				});
				data.addTrigger(id + "-active", Trigger.EVADE, (pdata2, in2) -> {
					queueStack.run();
					return TriggerResult.keep();
				});
			}
		}.runTask(NeoRogue.inst()));
	}

	private static void playUnlock(PlayerFightData data) {
		Player p = data.getPlayer();
		UNLOCK_RING.play(p, UNLOCK_PARTICLE, p.getLocation().add(0, 0.15, 0), LocalAxes.xz(), null);
		UNLOCK_SPARK.play(p, p);
		UNLOCK_SOUND.play(p, p);
	}

	private void grantStack(PlayerFightData data, int slot, boolean playFeedback) {
		data.addDamageBuff(DamageBuffType.of(DamageCategory.PHYSICAL),
				Buff.multiplier(data, damageIncrease, StatTracker.damageBuffAlly(id + slot, this, true)),
				BUFF_DURATION);
		if (playFeedback) {
			Player p = data.getPlayer();
			STACK_WIND.play(p, p);
			STACK_SPARK.play(p, p);
			STACK_SOUND.play(p, p);
		}
	}

	@Override
	public void setupItem() {
		item = createItem(Material.FEATHER,
				GlossaryTag.PASSIVE.tag(this) + " " + GlossaryTag.POWER.tag(this) + ". Activates after "
				+ DescUtil.val(DASHES_TO_ACTIVATE) + " dashes or " + DescUtil.val(1) + " evade. Once activated, dashes and evades increase your "
				+ GlossaryTag.PHYSICAL.tag(this) + " damage by "
				+ DescUtil.val((int) (damageIncrease * 100) + "%") + " " + DescUtil.duration(6)
				+ ", stackable.");
	}
}