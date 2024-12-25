package me.neoblade298.neorogue.equipment;

import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileInstance;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public abstract class Ammunition extends Equipment {
	public Ammunition(String id, String display, boolean isUpgraded, Rarity rarity, EquipmentClass ec, EquipmentType type, EquipmentProperties props) {
		super(id, display, isUpgraded, rarity, ec, type, props);
	}
	public void onTick(Player p, ProjectileInstance proj, int interpolation) {}
	public void onHit(ProjectileInstance inst, DamageMeta meta, LivingEntity target) {}
	public void onHitBlock(ProjectileInstance inst, Block b) {}
	public void onStart(ProjectileInstance inst) {}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		if (data.getAmmoInstance() == null) {
			equip(p, data, new AmmunitionInstance(data, this));
		}

		data.addTrigger(id, bind, (pdata, in) -> {
			equip(p, data, new AmmunitionInstance(data, this));
			return TriggerResult.keep();
		});
	}

	protected void equip(Player p, PlayerFightData data, AmmunitionInstance ammo) {
		data.setAmmoInstance(ammo);
		Sounds.equip.play(p, p);
		Util.msg(p, Component.text("You equipped ", NamedTextColor.GRAY).append(this.getDisplay()));
	}
}
