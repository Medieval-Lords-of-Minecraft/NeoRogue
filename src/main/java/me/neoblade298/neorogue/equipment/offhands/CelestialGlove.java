package me.neoblade298.neorogue.equipment.offhands;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.effects.Circle;
import me.neoblade298.neocore.bukkit.effects.Cone;
import me.neoblade298.neocore.bukkit.effects.LocalAxes;
import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.ActionMeta;
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
import me.neoblade298.neorogue.session.fight.trigger.event.CastUsableEvent;

public class CelestialGlove extends Equipment {
	private static final String ID = "CelestialGlove";
	private static final int STACKS_REQUIRED = 5, RIFT_DURATION = 200;
	private static final TargetProperties TARGETS = TargetProperties.cone(70, 7, false, TargetType.ENEMY);
	private static final Cone CONE = new Cone(TARGETS.range, TARGETS.arc);
	private static final Circle RIFT_RING = new Circle(1.25);
	private static final ParticleContainer CONE_EDGE = new ParticleContainer(Particle.DUST)
			.dustOptions(new DustOptions(Color.fromRGB(105, 45, 155), 1.1F)).count(1).spread(0, 0).speed(0);
	private static final ParticleContainer CONE_FILL = new ParticleContainer(Particle.REVERSE_PORTAL)
			.count(1).spread(0.05, 0).speed(0);
	private static final ParticleContainer RIFT_EDGE = new ParticleContainer(Particle.DUST)
			.dustOptions(new DustOptions(Color.fromRGB(35, 10, 55), 1.25F)).count(1).spread(0, 0).speed(0);
	private static final ParticleContainer RIFT_BURST = new ParticleContainer(Particle.REVERSE_PORTAL)
			.count(18).spread(0.1, 0.1).speed(0.01).offsetY(0.2);
	private int damage;

	public CelestialGlove(boolean isUpgraded) {
		super(ID, "Celestial Glove", isUpgraded, Rarity.RARE, EquipmentClass.MAGE, EquipmentType.OFFHAND,
				EquipmentProperties.ofUsable(35, 0, 0, TARGETS.range));
		damage = isUpgraded ? 220 : 180;
	}

	public static Equipment get() { return Equipment.get(ID, false); }

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		ActionMeta stacks = new ActionMeta();
		EquipmentInstance instance = new EquipmentInstance(data, sessionEq, slot, es, (pdata, in) -> {
			stacks.addCount(-STACKS_REQUIRED);
			Player player = data.getPlayer();
			player.swingOffHand();
			CONE.play(CONE_EDGE, player.getLocation(), LocalAxes.usingEyeLocation(player), CONE_FILL);
			RIFT_RING.play(RIFT_EDGE, player.getLocation().add(0, 0.15, 0), LocalAxes.xz(), null);
			RIFT_BURST.play(player, player);
			Sounds.wither.play(player, player);
			for (LivingEntity target : TargetHelper.getEntitiesInCone(player, TARGETS)) {
				FightInstance.dealDamage(new DamageMeta(data, damage, DamageType.DARK,
						DamageStatTracker.of(id + slot, this)), target);
			}
			data.addRift(new Rift(data, player.getLocation(), RIFT_DURATION, this));
			return TriggerResult.keep();
		}, (player, pdata, in) -> stacks.getCount() >= STACKS_REQUIRED);
		data.addTrigger(id, Trigger.CAST_USABLE, (pdata, in) -> {
			CastUsableEvent event = (CastUsableEvent) in;
			if (event.getInstance().getEquipment().getType() == EquipmentType.ABILITY) stacks.addCount(1);
			return TriggerResult.keep();
		});
		data.addTrigger(id, Trigger.RIGHT_CLICK, instance);
	}

	@Override
	public void setupItem() {
		item = createItem(Material.IRON_SWORD, "Casting an ability grants a stack. At "
				+ DescUtil.white(STACKS_REQUIRED) + " stacks, use this to consume them, deal "
				+ GlossaryTag.DARK.tag(this, damage) + " damage in a cone, and create a "
				+ GlossaryTag.RIFT.tag(this) + " at your feet " + DescUtil.duration(RIFT_DURATION / 20) + ".");
	}
}