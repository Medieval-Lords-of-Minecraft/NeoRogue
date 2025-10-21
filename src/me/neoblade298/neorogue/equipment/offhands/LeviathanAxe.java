package me.neoblade298.neorogue.equipment.offhands;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neocore.bukkit.effects.SoundContainer;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusClass;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.ApplyStatusEvent;
import me.neoblade298.neorogue.session.fight.trigger.event.RightClickHitEvent;
import net.kyori.adventure.text.Component;

public class LeviathanAxe extends Equipment {
	private static final String ID = "LeviathanAxe";
	private static final ParticleContainer pc = new ParticleContainer(Particle.FLAME).count(25).spread(1, 1).speed(0.1);
	private int thres;
	public LeviathanAxe(boolean isUpgraded) {
		super(ID, "Leviathan Axe", isUpgraded, Rarity.RARE, EquipmentClass.WARRIOR,
				EquipmentType.OFFHAND, EquipmentProperties.ofWeapon(isUpgraded ? 200 : 150, 0.7, DamageType.BLUNT, new SoundContainer(Sound.ENTITY_PLAYER_ATTACK_SWEEP, 0.5F)));
		properties.addUpgrades(PropertyType.DAMAGE);
		thres = isUpgraded ? 600 : 500;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ItemStack icon = item.withType(Material.GOLD_INGOT);
		ActionMeta am = new ActionMeta();

		data.addTrigger(id, Trigger.APPLY_STATUS, (pdata, in) -> {
			ApplyStatusEvent ev = (ApplyStatusEvent) in;
			if (ev.getStatusClass() != StatusClass.NEGATIVE) return TriggerResult.keep();
			if (ev.getTarget() instanceof Player) return TriggerResult.keep();
			am.addCount(ev.getStacks());

			if (am.getCount() >= thres) {
				activateWeapon(data);
				return TriggerResult.remove();
			}
			int pct = am.getCount() / (thres / 10);
			icon.setAmount(Math.min(1, pct));
			return TriggerResult.keep();
		});
	}

	private void activateWeapon(PlayerFightData data) {
		Player p = data.getPlayer();
		Sounds.fire.play(p, p);
		pc.play(p, p);
		p.getInventory().setItemInOffHand(item);
		Util.msg(p, item.displayName().append(Component.text(" was activated")));
		data.addTrigger(id, Trigger.RIGHT_CLICK_HIT, (pdata2, inputs) -> {
			RightClickHitEvent ev = (RightClickHitEvent) inputs;
			if (ev.getTarget() instanceof Player)
				return TriggerResult.keep();
			weaponSwingAndDamage(p, data, ev.getTarget());
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.GOLDEN_AXE, "Can only be used after applying " + DescUtil.yellow(thres) + " negative status stacks to enemies. " +
		"Right click to basic attack.");
	}
}
