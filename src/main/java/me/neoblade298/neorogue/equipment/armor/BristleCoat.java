package me.neoblade298.neorogue.equipment.armor;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.DamageMeta.DamageOrigin;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.buff.StatTracker;
import me.neoblade298.neorogue.session.fight.status.Status.StatusClass;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.ApplyStatusEvent;
import me.neoblade298.neorogue.session.fight.trigger.event.DealDamageEvent;

public class BristleCoat extends Equipment {
	private static final String ID = "BristleCoat";
	private static final int DIRECT_REDUCTION = 2, STATUS_DURATION = 12, STATUS_STACKS = 1;
	private static final ParticleContainer PROTECT_PROC = new ParticleContainer(Particle.CRIT).count(8)
			.spread(0.35, 0.45).offsetY(1).speed(0.01);
	private static final ParticleContainer SHELL_PROC = new ParticleContainer(Particle.DUST)
			.dustOptions(new DustOptions(Color.fromRGB(145, 225, 255), 1F)).count(8)
			.spread(0.35, 0.45).offsetY(1).speed(0.01);
	private int projectileThreshold, negativeStatusThreshold;

	public BristleCoat(boolean isUpgraded) {
		super(ID, "Bristle Coat", isUpgraded, Rarity.RARE, EquipmentClass.ARCHER, EquipmentType.ARMOR);
		projectileThreshold = isUpgraded ? 6 : 8;
		negativeStatusThreshold = isUpgraded ? 5 : 7;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		ActionMeta projectileHits = new ActionMeta();
		ActionMeta negativeStacks = new ActionMeta();
		data.addDefenseBuff(DamageBuffType.of(DamageCategory.DIRECT), Buff.increase(data, DIRECT_REDUCTION,
				StatTracker.defenseBuffAlly(id + slot, this)));

		data.addTrigger(id, Trigger.DEAL_DAMAGE, (pdata, in) -> {
			DealDamageEvent event = (DealDamageEvent) in;
			if (!event.getMeta().hasOrigin(DamageOrigin.PROJECTILE)) return TriggerResult.keep();
			int activations = projectileHits.addCount(1) / projectileThreshold;
			projectileHits.setCount(projectileHits.getCount() % projectileThreshold);
			if (activations > 0) {
				data.applyStatus(StatusType.PROTECT, data, STATUS_STACKS * activations, STATUS_DURATION * 20, this);
				Player player = data.getPlayer();
				PROTECT_PROC.play(player, player);
				Sounds.enchant.play(player, player);
			}
			return TriggerResult.keep();
		});

		data.addTrigger(id, Trigger.APPLY_STATUS, (pdata, in) -> {
			ApplyStatusEvent event = (ApplyStatusEvent) in;
			if (event.getStatusClass() != StatusClass.NEGATIVE) return TriggerResult.keep();
			int total = negativeStacks.getCount() + event.getStacks();
			int activations = total / negativeStatusThreshold;
			negativeStacks.setCount(total % negativeStatusThreshold);
			if (activations > 0) {
				data.applyStatus(StatusType.SHELL, data, STATUS_STACKS * activations, STATUS_DURATION * 20, this);
				Player player = data.getPlayer();
				SHELL_PROC.play(player, player);
				Sounds.enchant.play(player, player);
			}
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.LEATHER_CHESTPLATE, "Reduce " + GlossaryTag.DIRECT.tag(this) + " damage taken by "
				+ DescUtil.white(DIRECT_REDUCTION) + ". Every " + DescUtil.yellow(projectileThreshold)
				+ " times you deal projectile damage, gain " + GlossaryTag.PROTECT.tag(this, STATUS_STACKS) + " ["
				+ DescUtil.white(STATUS_DURATION + "s") + "]. For every " + DescUtil.yellow(negativeStatusThreshold)
				+ " negative status stacks you apply, gain " + GlossaryTag.SHELL.tag(this, STATUS_STACKS) + " ["
				+ DescUtil.white(STATUS_DURATION + "s") + "].");
	}
}