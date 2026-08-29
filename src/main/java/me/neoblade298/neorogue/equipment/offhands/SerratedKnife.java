package me.neoblade298.neorogue.equipment.offhands;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
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
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.LaunchProjectileGroupEvent;
import me.neoblade298.neorogue.session.fight.trigger.event.LeftClickHitEvent;

public class SerratedKnife extends Equipment {
	private static final String ID = "SerratedKnife";
	private static final int COOLDOWN = 5;
	private static final ParticleContainer SLASH = new ParticleContainer(Particle.SWEEP_ATTACK)
			.count(1).spread(0, 0).offsetY(1).speed(0);
	private static final ParticleContainer HIT = new ParticleContainer(Particle.DUST)
			.dustOptions(new DustOptions(Color.fromRGB(175, 35, 30), 0.9F))
			.count(6).spread(0.1, 0.1).offsetY(1).speed(0.01);
	private final int damage, damagePerRend;

	public SerratedKnife(boolean isUpgraded) {
		super(ID, "Serrated Knife", isUpgraded, Rarity.UNCOMMON, EquipmentClass.ARCHER,
				EquipmentType.OFFHAND, EquipmentProperties.ofUsable(0, 0, COOLDOWN, 0));
		damage = isUpgraded ? 90 : 60;
		damagePerRend = isUpgraded ? 4 : 3;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		ActionMeta aftershots = new ActionMeta();
		data.addTrigger(id, Trigger.LAUNCH_PROJECTILE_GROUP, (pdata, in) -> {
			LaunchProjectileGroupEvent event = (LaunchProjectileGroupEvent) in;
			if (!event.isBasicAttack() || aftershots.getCount() <= 0) return TriggerResult.keep();
			aftershots.addCount(-1);
			data.addAftershot(event.getGroup());
			return TriggerResult.keep();
		});

		data.addTrigger(id, Trigger.LEFT_CLICK_HIT,
				new EquipmentInstance(data, sessionEq, slot, es, (pdata, in) -> {
			LeftClickHitEvent event = (LeftClickHitEvent) in;
			Player player = data.getPlayer();
			FightData targetData = FightInstance.getFightData(event.getTarget());
			int rend = targetData == null ? 0 : targetData.getStatus(StatusType.REND).getStacks();
			int totalDamage = damage + damagePerRend * rend;

			player.swingOffHand();
			Sounds.attackSweep.play(player, player);
			FightInstance.dealDamage(new DamageMeta(data, totalDamage, DamageType.SLASHING,
					DamageStatTracker.of(id + slot, this)), event.getTarget());
			aftershots.addCount(1);
			SLASH.play(player, event.getTarget());
			HIT.play(player, event.getTarget());
			Sounds.crit.play(player, event.getTarget());
			return TriggerResult.keep();
		}));
	}

	@Override
	public void setupItem() {
		item = createItem(Material.SHEARS,
				"Left click an enemy to deal " + GlossaryTag.SLASHING.tag(this, damage) + " damage plus "
				+ GlossaryTag.SLASHING.tag(this, damagePerRend) + " damage per "
				+ GlossaryTag.REND.tag(this) + ". Your next basic attack gains "
				+ GlossaryTag.AFTERSHOT.tag(this) + ".");
	}
}
