package me.neoblade298.neorogue.equipment.weapons;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neocore.bukkit.effects.ParticleUtil;
import me.neoblade298.neocore.bukkit.effects.SoundContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
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

public class MageMace extends Equipment {
	private static final String ID = "MageMace";
	private static final double ATTACK_SPEED = 0.5, RANGE = 4, ARC = 90;
	private static final int SWEEP_STEPS = 5;
	private static final TargetProperties LINE = TargetProperties.line(RANGE, 0.75, TargetType.ENEMY);
	private static final ParticleContainer SWEEP = new ParticleContainer(Particle.BLOCK)
			.blockData(Material.TUFF.createBlockData()).count(1).spread(0, 0).speed(0);
	private static final ParticleContainer IMPACT = SWEEP.clone().count(6).spread(0.1, 0.1).speed(0.01);
	private static final SoundContainer IMPACT_SOUND = new SoundContainer(Sound.BLOCK_DEEPSLATE_BREAK, 0.6F, 0.75F);
	private final int damage;

	public MageMace(boolean isUpgraded) {
		super(ID, "Mage Mace", isUpgraded, Rarity.RARE, EquipmentClass.MAGE, EquipmentType.WEAPON,
				EquipmentProperties.ofWeapon(isUpgraded ? 100 : 80, ATTACK_SPEED, DamageType.BLUNT,
						Sound.ENTITY_PLAYER_ATTACK_SWEEP));
		damage = (int) properties.get(PropertyType.DAMAGE);
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		data.addSlotBasedTrigger(id, slot, Trigger.LEFT_CLICK, (pdata, in) -> {
			if (!canUseWeapon(data) || !data.canBasicAttack(EquipSlot.HOTBAR)) return TriggerResult.keep();
			Player player = data.getPlayer();
			weaponSwing(player, data);
			Set<UUID> hitTargets = new HashSet<UUID>();
			Vector facing = player.getEyeLocation().getDirection().setY(0).normalize();
			data.addTask(new BukkitRunnable() {
				private int step;

				@Override
				public void run() {
					Player currentPlayer = data.getPlayer();
					if (!currentPlayer.isValid() || step >= SWEEP_STEPS) {
						cancel();
						return;
					}
					double angle = -ARC / 2 + ARC * step / (SWEEP_STEPS - 1);
					Vector direction = facing.clone().rotateAroundY(Math.toRadians(angle));
					Location start = currentPlayer.getLocation().add(0, 1, 0);
					ParticleUtil.drawLine(currentPlayer, SWEEP, start, start.clone().add(direction.clone().multiply(RANGE)), 0.4);
					for (LivingEntity target : TargetHelper.getEntitiesInLine(currentPlayer, start, direction, LINE)) {
						if (!hitTargets.add(target.getUniqueId())) continue;
						FightData targetData = FightInstance.getFightData(target);
						int stacks = targetData != null && targetData.hasStatus(StatusType.CONCUSSED)
								? targetData.getStatus(StatusType.CONCUSSED).getStacks() : 0;
						DamageMeta meta = new DamageMeta(data, damage + stacks, DamageType.BLUNT,
								DamageStatTracker.of(id + slot, MageMace.this));
						meta.isBasicAttack(MageMace.this, true);
						FightInstance.dealDamage(meta, target);
						IMPACT.play(currentPlayer, target.getLocation().add(0, 1, 0));
						IMPACT_SOUND.play(currentPlayer, target);
					}
					step++;
				}
			}.runTaskTimer(NeoRogue.inst(), 0L, 1L));
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.MACE, "Sweep a line from left to right through " + DescUtil.val("90 degrees")
				+ ", dealing " + GlossaryTag.BLUNT.tag(this, damage) + " damage plus "
				+ DescUtil.val(1) + " per current " + GlossaryTag.CONCUSSED.tag(this) + " stack.");
	}
}