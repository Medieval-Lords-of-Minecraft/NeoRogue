package me.neoblade298.neorogue.equipment.offhands;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.effects.Cone;
import me.neoblade298.neocore.bukkit.effects.LocalAxes;
import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetType;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.buff.StatTracker;
import me.neoblade298.neorogue.session.fight.status.Status;
import me.neoblade298.neorogue.session.fight.status.Status.GenericStatusType;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.PreDealDamageEvent;

public class IcewindTome extends Equipment {
	private static final String ID = "IcewindTome";
	private static final int MANA_COST = 15;
	private static final int COOLDOWN_SECONDS = 12;
	private static final int MARK_DURATION_TICKS = 100;
	private static final double KNOCKBACK = 1;
	private static final TargetProperties TARGETS = TargetProperties.cone(75, 5, false, TargetType.ENEMY);
	private static final Cone CAST_CONE = new Cone(TARGETS.range, TARGETS.arc);
	private static final ParticleContainer CONE_EDGE = new ParticleContainer(Particle.DUST).count(1).spread(0, 0)
			.speed(0).dustOptions(new DustOptions(Color.fromRGB(125, 220, 255), 1F));
	private static final ParticleContainer CONE_FILL = new ParticleContainer(Particle.SNOWFLAKE).count(1)
			.spread(0.1, 0).speed(0);
	private static final ParticleContainer MARK_CONSUME = new ParticleContainer(Particle.BLOCK)
			.blockData(Material.BLUE_ICE.createBlockData()).count(12).spread(0.1, 0.1).offsetY(0.8).speed(0.01);
	private int frost;
	private double iceIncrease;

	public IcewindTome(boolean isUpgraded) {
		super(ID, "Icewind Tome", isUpgraded, Rarity.COMMON, EquipmentClass.ARCHER, EquipmentType.OFFHAND,
				EquipmentProperties.ofUsable(MANA_COST, 0, COOLDOWN_SECONDS, TARGETS.range));
		frost = isUpgraded ? 5 : 3;
		iceIncrease = isUpgraded ? 0.5 : 0.3;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		String markId = data.getPlayer().getUniqueId() + "-" + id + "-" + slot;
		data.addTrigger(id, bind, new EquipmentInstance(data, sessionEq, slot, es, (pdata, in) -> {
			Player player = data.getPlayer();
			player.swingOffHand();
			Sounds.attackSweep.play(player, player);
			CAST_CONE.play(CONE_EDGE, player.getLocation(), LocalAxes.usingEyeLocation(player), CONE_FILL);
			for (LivingEntity target : TargetHelper.getEntitiesInCone(player, TARGETS)) {
				FightInstance.knockback(player, target, KNOCKBACK);
				FightInstance.applyStatus(target, StatusType.FROST, data, frost, -1, this);

				FightData targetData = FightInstance.getFightData(target);
				Status mark = Status.createByGenericType(GenericStatusType.BASIC, markId, targetData, true);
				targetData.applyStatus(mark, data, 1, MARK_DURATION_TICKS, this);
			}
			return TriggerResult.keep();
		}));

		data.addTrigger(id, Trigger.PRE_DEAL_DAMAGE, (pdata, in) -> {
			PreDealDamageEvent event = (PreDealDamageEvent) in;
			if (!event.getMeta().containsType(DamageCategory.ICE)) return TriggerResult.keep();

			FightData targetData = FightInstance.getFightData(event.getTarget());
			if (!targetData.hasStatus(markId)) return TriggerResult.keep();

			targetData.removeStatus(markId);
			event.getMeta().addDamageBuff(DamageBuffType.of(DamageCategory.ICE), Buff.multiplier(data, iceIncrease,
					StatTracker.damageBuffAlly(id + slot, this)));
			Player player = data.getPlayer();
			MARK_CONSUME.play(player, event.getTarget().getLocation());
			Sounds.glass.play(player, event.getTarget());
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.BOOK, "On cast, knock enemies back and apply " + GlossaryTag.FROST.tag(this, frost)
				+ " in a cone. Affected enemies are marked [" + DescUtil.white("5s") + "]. The next time you deal "
				+ GlossaryTag.ICE.tag(this) + " damage to each marked enemy, increase that ice damage by "
				+ DescUtil.yellow((int) (iceIncrease * 100) + "%") + " and consume their mark.");
	}
}