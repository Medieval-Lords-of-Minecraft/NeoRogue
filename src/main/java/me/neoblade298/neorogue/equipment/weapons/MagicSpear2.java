package me.neoblade298.neorogue.equipment.weapons;

import java.util.LinkedList;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neocore.bukkit.effects.ParticleUtil;
import me.neoblade298.neocore.bukkit.effects.SoundContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
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
import me.neoblade298.neorogue.session.fight.trigger.event.LeftClickHitEvent;

public class MagicSpear2 extends Equipment {
	private static final String ID = "MagicSpear2";
	private static final TargetProperties SPEAR_HIT = TargetProperties.line(5, 1, TargetType.ENEMY);
	private static final ParticleContainer SPEAR_TRAIL = new ParticleContainer(Particle.DUST)
			.dustOptions(new DustOptions(Color.fromRGB(150, 115, 70), 0.8F)).count(1).spread(0, 0).speed(0);
	private static final ParticleContainer IMPACT = new ParticleContainer(Particle.BLOCK)
			.blockData(Material.PACKED_MUD.createBlockData()).count(7).spread(0.1, 0.1).speed(0.01);
	private static final SoundContainer IMPACT_SOUND = new SoundContainer(Sound.BLOCK_DEEPSLATE_BREAK, 0.55F, 1.25F);
	private final int damagePerConcussed;

	public MagicSpear2(boolean isUpgraded) {
		super(ID, "Magic Spear II", isUpgraded, Rarity.RARE, EquipmentClass.MAGE, EquipmentType.WEAPON,
				EquipmentProperties.ofWeapon(0, 1, 50, 0.75, 0.2, DamageType.EARTHEN,
						Sound.ENTITY_PLAYER_ATTACK_CRIT));
		damagePerConcussed = isUpgraded ? 3 : 2;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		data.addSlotBasedTrigger(id, slot, Trigger.LEFT_CLICK_HIT, (pdata, in) -> {
			LeftClickHitEvent event = (LeftClickHitEvent) in;
			weaponSwingAndDamage(data.getPlayer(), data, event.getTarget(), getDamage(event.getTarget()));
			return TriggerResult.keep();
		});
		data.addSlotBasedTrigger(id, slot, Trigger.LEFT_CLICK_NO_HIT, (pdata, in) -> {
			if (!data.canBasicAttack()) return TriggerResult.keep();
			Player player = data.getPlayer();
			LinkedList<LivingEntity> targets = TargetHelper.getEntitiesInSight(player, SPEAR_HIT);
			if (targets.isEmpty()) return TriggerResult.keep();
			LivingEntity target = targets.getFirst();
			ParticleUtil.drawLine(player, SPEAR_TRAIL, player.getEyeLocation().add(0, -0.2, 0),
					target.getLocation().add(0, target.getHeight() * 0.5, 0), 0.35);
			IMPACT.play(player, target.getLocation().add(0, target.getHeight() * 0.5, 0));
			IMPACT_SOUND.play(player, target.getLocation());
			weaponSwingAndDamage(player, data, target, getDamage(target));
			return TriggerResult.keep();
		});
	}

	private double getDamage(LivingEntity target) {
		FightData targetData = FightInstance.getFightData(target);
		int concussed = targetData != null && targetData.hasStatus(StatusType.CONCUSSED)
				? targetData.getStatus(StatusType.CONCUSSED).getStacks() : 0;
		return properties.get(PropertyType.DAMAGE) + concussed * damagePerConcussed;
	}

	@Override
	public void setupItem() {
		item = createItem(Material.TRIDENT, "Melee range +2. Deals an additional "
				+ DescUtil.val(damagePerConcussed) + " damage per " + GlossaryTag.CONCUSSED.tag(this)
				+ " on the enemy.");
	}
}