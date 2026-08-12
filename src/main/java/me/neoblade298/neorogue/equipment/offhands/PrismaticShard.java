package me.neoblade298.neorogue.equipment.offhands;

import java.util.HashSet;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neocore.bukkit.effects.Circle;
import me.neoblade298.neocore.bukkit.effects.LocalAxes;
import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neocore.bukkit.effects.ParticleUtil;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageSlice;
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.Rift;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetType;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.DealDamageEvent;

public class PrismaticShard extends Equipment {
	private static final String ID = "PrismaticShard";
	private static final int CHANNEL_TICKS = 20, ELECTRIFIED = 4, CONCUSSED = 4, RIFT_DURATION = 200;
	private static final TargetProperties TARGETS = TargetProperties.line(14, 2, TargetType.ENEMY);
	private static final Circle CHANNEL_RING = new Circle(1.15);
	private static final ParticleContainer[] CHANNEL_COLORS = {
			prismaticDust(Color.fromRGB(255, 95, 105), 1F), prismaticDust(Color.fromRGB(90, 205, 255), 1F),
			prismaticDust(Color.fromRGB(255, 225, 90), 1F), prismaticDust(Color.fromRGB(175, 105, 255), 1F)
	};
	private static final ParticleContainer BEAM = prismaticDust(Color.fromRGB(215, 245, 255), 0.9F);
	private static final ParticleContainer BEAM_SPARK = new ParticleContainer(Particle.FIREWORK)
			.count(1).spread(0, 0).speed(0.01);
	private static final ParticleContainer IMPACT = new ParticleContainer(Particle.FIREWORK)
			.count(12).spread(0.1, 0.1).speed(0.01).offsetY(0.8);
	private int damage, typesRequired;

	private static ParticleContainer prismaticDust(Color color, float size) {
		return new ParticleContainer(Particle.DUST).dustOptions(new DustOptions(color, size))
				.count(1).spread(0, 0).speed(0);
	}

	public PrismaticShard(boolean isUpgraded) {
		super(ID, "Prismatic Shard", isUpgraded, Rarity.EPIC, EquipmentClass.MAGE, EquipmentType.OFFHAND,
				EquipmentProperties.ofUsable(40, 0, 10, TARGETS.range));
		damage = isUpgraded ? 240 : 200;
		typesRequired = isUpgraded ? 3 : 4;
	}

	public static Equipment get() { return Equipment.get(ID, false); }

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		HashSet<DamageType> types = new HashSet<>();
		data.addTrigger(id, Trigger.DEAL_DAMAGE, (pdata, in) -> {
			DealDamageEvent event = (DealDamageEvent) in;
			if (!event.getMeta().getSlices().isEmpty()) {
				DamageSlice slice = event.getMeta().getSlices().getFirst();
				types.add(slice.getPostBuffType());
			}
			return TriggerResult.keep();
		});
		EquipmentInstance instance = new EquipmentInstance(data, sessionEq, slot, es, (pdata, in) -> {
			types.clear();
			Sounds.enchant.play(data.getPlayer(), data.getPlayer());
			data.addTask(new BukkitRunnable() {
				private int ticks;

				@Override
				public void run() {
					Player player = data.getPlayer();
					CHANNEL_RING.play(CHANNEL_COLORS[ticks % CHANNEL_COLORS.length],
							player.getLocation().add(0, 0.15 + ticks * 0.08, 0), LocalAxes.xz(), null);
					if (++ticks >= 5) cancel();
				}
			}.runTaskTimer(NeoRogue.inst(), 0L, 4L));
			data.channel(CHANNEL_TICKS).then(() -> {
				Player player = data.getPlayer();
				Location start = player.getEyeLocation();
				Location end = start.clone().add(player.getEyeLocation().getDirection()
						.multiply(properties.get(PropertyType.RANGE)));
				ParticleUtil.drawLine(player, BEAM, start, end, 0.25);
				ParticleUtil.drawLine(player, BEAM_SPARK, start, end, 0.5);
				Sounds.firework.play(player, player);
				for (LivingEntity target : TargetHelper.getEntitiesInLine(player, start, end, TARGETS)) {
					IMPACT.play(player, target);
					FightInstance.dealDamage(new DamageMeta(data, damage, DamageType.LIGHT,
							DamageStatTracker.of(id + slot, this)), target);
					FightInstance.applyStatus(target, StatusType.ELECTRIFIED, data, ELECTRIFIED, -1, this);
					FightInstance.applyStatus(target, StatusType.CONCUSSED, data, CONCUSSED, -1, this);
				}
				data.addRift(new Rift(data, player.getLocation(), RIFT_DURATION, this));
			});
			return TriggerResult.keep();
		}, (player, pdata, in) -> types.size() >= typesRequired);
		data.addTrigger(id, Trigger.RIGHT_CLICK, instance);
	}

	@Override
	public void setupItem() {
		item = createItem(Material.PRISMARINE_CRYSTALS, "Arms after dealing " + DescUtil.val(typesRequired)
				+ " different damage types and must rearm after every use. On use, " + GlossaryTag.CHANNEL.tag(this)
				+ " for " + DescUtil.white("1s") + ", then fire a beam for " + GlossaryTag.LIGHT.tag(this, damage)
				+ " damage, apply " + GlossaryTag.ELECTRIFIED.tag(this, ELECTRIFIED) + " and "
				+ GlossaryTag.CONCUSSED.tag(this, CONCUSSED) + ", and create a " + GlossaryTag.RIFT.tag(this) + ".");
	}
}