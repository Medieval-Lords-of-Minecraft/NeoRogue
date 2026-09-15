package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Power;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetType;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.ApplyStatusEvent;
import me.neoblade298.neorogue.session.fight.trigger.event.BasicAttackEvent;

public class WeaponEnchantmentElectrified2 extends Equipment implements Power {
	private static final String ID = "WeaponEnchantmentElectrified2";
	private static final int AREA_OF_EFFECT = 3;
	private static final ParticleContainer PART = new ParticleContainer(Particle.FIREWORK)
			.count(50).spread(0.2, 3).offsetY(2);
	private static final TargetProperties TARGETS = TargetProperties.radius(AREA_OF_EFFECT, false, TargetType.ENEMY);
	private final int activationThreshold, attackThreshold, damage, electrifiedMultiplier;

	public WeaponEnchantmentElectrified2(boolean isUpgraded) {
		super(ID, "Weapon Enchantment: Electrified II", isUpgraded, Rarity.RARE, EquipmentClass.MAGE,
				EquipmentType.ABILITY, EquipmentProperties.none());
		activationThreshold = 10;
		attackThreshold = isUpgraded ? 4 : 5;
		damage = 90;
		electrifiedMultiplier = 10;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		ActionMeta activationProgress = new ActionMeta();
		data.addTrigger(id, Trigger.APPLY_STATUS, (pdata, in) -> {
			ApplyStatusEvent event = (ApplyStatusEvent) in;
			if (!event.isStatus(StatusType.ELECTRIFIED) || event.getStacks() <= 0) return TriggerResult.keep();
			if (activationProgress.addCount(1) < activationThreshold) return TriggerResult.keep();

			if (activatePower(data, slot, es)) return TriggerResult.remove();
			return TriggerResult.keep();
		});
	}

	@Override
	public void onPowerActivated(PlayerFightData data, int slot, EquipSlot es) {
		ActionMeta attackProgress = new ActionMeta();
		data.addTrigger(id + "-active", Trigger.BASIC_ATTACK, (pdata, in) -> {
			BasicAttackEvent event = (BasicAttackEvent) in;
			LivingEntity target = event.getTarget();
			if (target == null || attackProgress.addCount(1) < attackThreshold) return TriggerResult.keep();
			attackProgress.setCount(0);

			FightData targetData = FightInstance.getFightData(target);
			int electrifiedStacks = targetData.hasStatus(StatusType.ELECTRIFIED)
					? targetData.getStatus(StatusType.ELECTRIFIED).getStacks() : 0;
			int overloadDamage = damage + electrifiedStacks * electrifiedMultiplier;
			Player player = data.getPlayer();
			for (LivingEntity enemy : TargetHelper.getEntitiesInRadius(target, TARGETS)) {
				FightInstance.dealDamage(new DamageMeta(data, overloadDamage, DamageType.LIGHTNING,
						DamageStatTracker.of(id + slot, this)), enemy);
				PART.play(player, enemy);
			}
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.LIGHTNING_ROD,
				GlossaryTag.PASSIVE.tag(this) + " " + GlossaryTag.POWER.tag(this)
						+ ". Activates after applying " + GlossaryTag.ELECTRIFIED.tag(this) + " "
						+ DescUtil.val(activationThreshold) + " times. Every " + DescUtil.val(attackThreshold)
						+ " basic attacks, overload the basic attack target, dealing "
						+ GlossaryTag.LIGHTNING.tag(this, damage) + " damage + the target's number of "
						+ GlossaryTag.ELECTRIFIED.tag(this) + " stacks multiplied by "
						+ DescUtil.val(electrifiedMultiplier) + " to enemies within "
						+ DescUtil.val(AREA_OF_EFFECT) + " blocks of it.");
	}
}