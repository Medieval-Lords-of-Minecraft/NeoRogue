package me.neoblade298.neorogue.equipment.weapons;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.util.Vector;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Bow;
import me.neoblade298.neorogue.equipment.BowProjectile;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileGroup;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileInstance;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.PreDealDamageEvent;

public class IcyBow extends Bow {
	private static final String ID = "IcyBow";
	private static final int ATTACK_THRESHOLD = 5;
	private static final int FROST = 3;
	private static final ParticleContainer FROST_PROC = new ParticleContainer(Particle.BLOCK)
			.blockData(Material.BLUE_ICE.createBlockData()).count(10).spread(0.1, 0.1).offsetY(0.8).speed(0.01);

	public IcyBow(boolean isUpgraded) {
		super(ID, "Icy Bow", isUpgraded, Rarity.COMMON, EquipmentClass.ARCHER,
				EquipmentType.WEAPON,
				EquipmentProperties.ofBow(isUpgraded ? 25 : 20, 1, 0, 12, 0, 0.6));
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void onTick(Player player, ProjectileInstance projectile, int interpolation) {
		BowProjectile.tick.play(player, projectile.getLocation());
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		ActionMeta attacks = new ActionMeta();
		data.addSlotBasedTrigger(id, slot, Trigger.VANILLA_PROJECTILE, (pdata, in) -> {
			ProjectileLaunchEvent event = (ProjectileLaunchEvent) in;
			Vector arrowVelocity = event.getEntity().getVelocity();
			if (!canShoot(data, arrowVelocity)) return TriggerResult.keep();
			useBow(data);

			new ProjectileGroup(new BowProjectile(data, arrowVelocity, this, id + slot)).start(data);
			return TriggerResult.keep();
		});

		data.addTrigger(id, Trigger.PRE_DEAL_DAMAGE, (pdata, in) -> {
			PreDealDamageEvent event = (PreDealDamageEvent) in;
			if (!event.getMeta().isBasicAttack() || event.getMeta().getWeapon() != this) return TriggerResult.keep();
			if (attacks.addCount(1) < ATTACK_THRESHOLD) return TriggerResult.keep();

			attacks.setCount(0);
			FightInstance.applyStatus(event.getTarget(), StatusType.FROST, data, FROST, -1, this);
			Player player = data.getPlayer();
			FROST_PROC.play(player, event.getTarget().getLocation());
			Sounds.glass.play(player, event.getTarget());
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.BOW, "Every " + DescUtil.val(ATTACK_THRESHOLD)
				+ " basic attacks made with this weapon applies " + GlossaryTag.FROST.tag(this, FROST) + ".");
	}
}