package me.neoblade298.neorogue.equipment;

import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileInstance;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class AmmunitionInstance {
	private PlayerFightData owner;
	private Ammunition ammo;
	private int count = -1, slot;
	private boolean isLimited = false;
	public AmmunitionInstance(PlayerFightData owner, Ammunition ammo, int slot) {
		this.owner = owner;
		this.ammo = ammo;
		this.slot = slot;

		if (ammo instanceof LimitedAmmunition) {
			this.count = ((LimitedAmmunition) ammo).uses;
			this.isLimited = true;
		}
	}

	public void use() {
		if (!isLimited) {
			// Tried to fix non-wooden ammo breaking but didn't work
			// This doesn't work when ammo hotbar 8 is selected but ammo hotbar 7 is used by vanilla bow
			// owner.getPlayer().getInventory().setItem(slot, icon);
		}
		else {
			if (--count == 0) {
				owner.setAmmoInstance(null);
				Util.msgRaw(owner.getPlayer(),
						Component.text("You ran out of ", NamedTextColor.GRAY).append(ammo.getDisplay()));
			}
			ItemStack item = owner.getPlayer().getInventory().getItem(slot);
			item.setAmount(count);
		}
	}
	public void onStart(ProjectileInstance inst) {
		ammo.onStart(inst);
		use();
	}
	public void onStart(ProjectileInstance inst, boolean useAmmo) {
		ammo.onStart(inst);
		if (useAmmo) use();
	}
	public void onTick(Player p, ProjectileInstance proj, int interpolation) {
		ammo.onTick(p, proj, interpolation);
	}
	public void onHit(ProjectileInstance inst, DamageMeta meta, LivingEntity target) {
		ammo.onHit(inst, meta, target);
	}
	public void onHitBlock(ProjectileInstance proj, Block b) {
		ammo.onHitBlock(proj, b);
	}
	public EquipmentProperties getProperties() {
		return ammo.getProperties();
	}
	public int getRemaining() {
		return ammo instanceof LimitedAmmunition ? count : -1;
	}
	public Ammunition getAmmo() {
		return ammo;
	}
}
