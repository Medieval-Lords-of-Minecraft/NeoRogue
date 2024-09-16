package me.neoblade298.neorogue.equipment;

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
	public void onTick(Player p, ProjectileInstance proj, boolean interpolation) {}
	public void onHit(ProjectileInstance inst, LivingEntity target) {}
	public void modifyDamage(DamageMeta meta) {}
	public double modifyVelocity(double velocity) { return velocity; }

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		if (data.getAmmunition() == null) {
			equip(p, data);
		}

		data.addTrigger(id, bind, (pdata, in) -> {
			equip(p, data);
			return TriggerResult.keep();
		});
	}

	private void equip(Player p, PlayerFightData data) {
		data.setAmmunition(this);
		Sounds.equip.play(p, p);
		Util.msg(p, Component.text("You equipped ", NamedTextColor.GRAY).append(this.getDisplay()));
	}
}
