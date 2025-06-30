package me.neoblade298.neorogue.equipment.weapons;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.ProjectileLaunchEvent;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.Bow;
import me.neoblade298.neorogue.equipment.BowProjectile;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileGroup;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileInstance;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class CompositeBow extends Bow {
	private static final String ID = "compositeBow";
	private int thres, damage;
	
	public CompositeBow(boolean isUpgraded) {
		super(ID, "Composite Bow", isUpgraded, Rarity.UNCOMMON, EquipmentClass.ARCHER,
				EquipmentType.WEAPON,
				EquipmentProperties.ofBow(50, 1, 0, 12, 0, 1.4));
			thres = 2;
			damage = isUpgraded ? 30 : 15;
	}

	@Override
	public void onTick(Player p, ProjectileInstance proj, int interpolation) {
		BowProjectile.tick.play(p, proj.getLocation());
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addSlotBasedTrigger(id, slot, Trigger.VANILLA_PROJECTILE, (pdata, in) -> {
			if (!canShoot(data)) return TriggerResult.keep();
			useBow(data);

			ProjectileLaunchEvent ev = (ProjectileLaunchEvent) in;
			boolean hasBonus = data.getStatus(StatusType.FOCUS).getStacks() >= thres;
			ProjectileGroup proj = new ProjectileGroup(new BowProjectile(data, ev.getEntity().getVelocity(), this, id + slot).setDamageBonus(hasBonus ? damage : 0));
			proj.start(data);
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.BOW, "At " + GlossaryTag.FOCUS.tag(this, thres, false) + ", deal an additional " + DescUtil.yellow(damage) + " damage");
	}
}
