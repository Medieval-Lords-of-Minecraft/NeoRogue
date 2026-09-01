package me.neoblade298.neorogue.equipment.armor;

import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import me.neoblade298.neocore.bukkit.effects.LocalAxes;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.equipment.mechanics.Barrier;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.buff.StatTracker;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.ApplyStatusEvent;

public class WraithMantle extends Equipment {
	private static final String ID = "WraithMantle";
	private static final int INSANITY_THRESHOLD = 150, CLEAR_RADIUS = 4;
	private static final Vector[] CARDINAL_DIRECTIONS = {
			new Vector(0, 0, -1), new Vector(1, 0, 0), new Vector(0, 0, 1), new Vector(-1, 0, 0)
	};
	private final int damageReduction;

	public WraithMantle(boolean isUpgraded) {
		super(ID, "Wraith Mantle", isUpgraded, Rarity.EPIC, EquipmentClass.THIEF, EquipmentType.ARMOR);
		damageReduction = isUpgraded ? 6 : 4;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		data.addDefenseBuff(DamageBuffType.of(DamageCategory.MAGICAL), Buff.increase(data, damageReduction,
				StatTracker.defenseBuffAlly(UUID.randomUUID().toString(), this)));
		ActionMeta insanityApplied = new ActionMeta();
		data.addTrigger(id, Trigger.APPLY_STATUS, (pdata, in) -> {
			ApplyStatusEvent ev = (ApplyStatusEvent) in;
			if (!ev.isStatus(StatusType.INSANITY) || ev.getStacks() <= 0) return TriggerResult.keep();
			if (insanityApplied.getCount() >= INSANITY_THRESHOLD) {
				Player player = data.getPlayer();
				createProjectileBarriers(data, player);
			}
			insanityApplied.addCount(ev.getStacks());
			return TriggerResult.keep();
		});
	}

	private void createProjectileBarriers(PlayerFightData data, Player player) {
		Vector up = new Vector(0, 1, 0);
		for (Vector forward : CARDINAL_DIRECTIONS) {
			Vector left = up.clone().crossProduct(forward);
			Location center = player.getLocation().add(forward.clone().multiply(CLEAR_RADIUS / 2D))
					.add(0, CLEAR_RADIUS, 0);
			Barrier barrier = Barrier.invisibleStationary(player, CLEAR_RADIUS * 2, CLEAR_RADIUS,
					CLEAR_RADIUS * 2, center, new LocalAxes(left, up, forward), null, true);
			UUID barrierId = data.addBarrier(barrier);
			data.addGuaranteedTask(barrierId, () -> data.removeBarrier(barrierId), 2);
		}
	}

	@Override
	public void setupItem() {
		item = createItem(Material.NETHERITE_CHESTPLATE, "Reduce " + GlossaryTag.MAGICAL.tag(this) + " damage by "
				+ DescUtil.val(damageReduction) + ". After applying " + GlossaryTag.INSANITY.tag(this, INSANITY_THRESHOLD)
				+ ", each subsequent application clears nearby projectiles within " + DescUtil.val(CLEAR_RADIUS) + " blocks.");
	}
}