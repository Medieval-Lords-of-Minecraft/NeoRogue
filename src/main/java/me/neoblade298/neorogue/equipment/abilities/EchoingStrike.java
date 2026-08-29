package me.neoblade298.neorogue.equipment.abilities;

import java.util.UUID;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neocore.bukkit.effects.Circle;
import me.neoblade298.neocore.bukkit.effects.LocalAxes;
import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neocore.bukkit.effects.SoundContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageSlice;
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.PreBasicAttackEvent;

public class EchoingStrike extends Equipment {
	private static final String ID = "EchoingStrike";
	private static final int MANA_COST = 0;
	private static final int STAMINA_COST = 15;
	private static final int COOLDOWN_SECONDS = 10;
	private static final int IMMEDIATE_DAMAGE = 40;
	private static final int DELAY_TICKS = 40;
	private static final int DELAY_SECONDS = 2;
	private static final Circle CAST_RING = new Circle(0.9), ECHO_RING = new Circle(0.65);
	private static final ParticleContainer CAST_PARTICLE = new ParticleContainer(Particle.DUST).count(1)
			.spread(0, 0).speed(0).dustOptions(new DustOptions(Color.fromRGB(190, 205, 220), 1F));
	private static final ParticleContainer FIRST_HIT = new ParticleContainer(Particle.ENCHANTED_HIT).count(7)
			.spread(0.1, 0.3).speed(0.01).offsetY(1);
	private static final ParticleContainer ECHO_MARK = new ParticleContainer(Particle.DUST).count(1)
			.spread(0, 0).speed(0).dustOptions(new DustOptions(Color.fromRGB(120, 140, 165), 0.8F));
	private static final ParticleContainer DELAYED_HIT = new ParticleContainer(Particle.SWEEP_ATTACK).count(1)
			.spread(0.1, 0.15).speed(0).offsetY(1);
	private static final ParticleContainer DELAYED_SPARKS = new ParticleContainer(Particle.ENCHANTED_HIT).count(10)
			.spread(0.1, 0.4).speed(0.01).offsetY(1);
	private static final SoundContainer CAST_SOUND = new SoundContainer(Sound.ITEM_ARMOR_EQUIP_IRON, 0.55F, 1.25F);
	private static final SoundContainer FIRST_HIT_SOUND = new SoundContainer(Sound.ENTITY_PLAYER_ATTACK_SWEEP, 0.6F, 1.15F);
	private static final SoundContainer DELAYED_HIT_SOUND = new SoundContainer(Sound.ENTITY_PLAYER_ATTACK_CRIT, 0.7F, 0.7F);
	private final int delayedDamage;

	public EchoingStrike(boolean isUpgraded) {
		super(ID, "Echoing Strike", isUpgraded, Rarity.COMMON, EquipmentClass.WARRIOR,
				EquipmentType.ABILITY,
				EquipmentProperties.ofUsable(MANA_COST, STAMINA_COST, COOLDOWN_SECONDS, 0));
		delayedDamage = isUpgraded ? 90 : 60;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		data.addTrigger(id, bind, new EquipmentInstance(data, sessionEq, slot, es, (pdata, in) -> {
			Player player = data.getPlayer();
			CAST_RING.play(CAST_PARTICLE, player.getLocation().add(0, 0.15, 0), LocalAxes.xz(), null);
			CAST_SOUND.play(player, player);
			data.addTrigger(id, Trigger.PRE_BASIC_ATTACK, (pdata2, in2) -> {
				PreBasicAttackEvent event = (PreBasicAttackEvent) in2;
				event.getMeta().addDamageSlice(new DamageSlice(data, IMMEDIATE_DAMAGE, DamageType.PIERCING,
						DamageStatTracker.of(id + slot, this)));
				Player current = data.getPlayer();
				Location targetLocation = event.getTarget().getLocation();
				FIRST_HIT.play(current, targetLocation);
				ECHO_RING.play(ECHO_MARK, targetLocation.clone().add(0, 1, 0), LocalAxes.xz(), null);
				FIRST_HIT_SOUND.play(current, targetLocation);
				UUID targetId = event.getTarget().getUniqueId();
				data.addTask(new BukkitRunnable() {
					@Override
					public void run() {
						FightData targetData = FightInstance.getFightData(targetId);
						if (targetData == null || targetData.getInstance() != data.getInstance()) return;
						LivingEntity target = targetData.getEntity();
						if (target == null || !target.isValid() || target.isDead()) return;
						Player current = data.getPlayer();
						DELAYED_HIT.play(current, target);
						DELAYED_SPARKS.play(current, target);
						DELAYED_HIT_SOUND.play(current, target);
						FightInstance.dealDamage(new DamageMeta(data, delayedDamage, DamageType.PIERCING,
								DamageStatTracker.of(id + slot, EchoingStrike.this)), target);
					}
				}.runTaskLater(NeoRogue.inst(), DELAY_TICKS));
				return TriggerResult.remove();
			});
			return TriggerResult.keep();
		}));
	}

	@Override
	public void setupItem() {
		item = createItem(Material.FLINT,
				"On cast, your next basic attack deals an additional "
						+ GlossaryTag.PIERCING.tag(this, IMMEDIATE_DAMAGE) + " damage, then deals "
						+ GlossaryTag.PIERCING.tag(this, delayedDamage) + " damage to the same enemy "
						+ DescUtil.duration(DELAY_SECONDS) + " later.");
	}
}