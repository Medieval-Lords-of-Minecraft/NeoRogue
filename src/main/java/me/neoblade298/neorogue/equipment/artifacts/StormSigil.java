package me.neoblade298.neorogue.equipment.artifacts;

import java.util.UUID;

import org.bukkit.Material;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.Ammunition;
import me.neoblade298.neorogue.equipment.AmmunitionInstance;
import me.neoblade298.neorogue.equipment.Artifact;
import me.neoblade298.neorogue.equipment.ArtifactInstance;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.buff.StatTracker;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.ChangedAmmunitionEvent;
import me.neoblade298.neorogue.session.fight.trigger.event.PreLaunchProjectileGroupEvent;

public class StormSigil extends Artifact {
	private static final String ID = "StormSigil";
	private static final int damage = 15, secs = 5;

	public StormSigil() {
		super(ID, "Storm Sigil", Rarity.UNCOMMON, EquipmentClass.ARCHER);
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, ArtifactInstance ai) {
		String buffId = UUID.randomUUID().toString();
		Ammunition[] pendingAmmo = new Ammunition[1];
		data.addTrigger(id, Trigger.CHANGE_AMMUNITION, (pdata, in) -> {
			ChangedAmmunitionEvent ev = (ChangedAmmunitionEvent) in;
			AmmunitionInstance oldAmmo = ev.getOldAmmo();
			AmmunitionInstance currentAmmo = ev.getCurrentAmmo();
			if (oldAmmo != null && oldAmmo.getAmmo().getId().equals(currentAmmo.getAmmo().getId())) {
				return TriggerResult.keep();
			}
			pendingAmmo[0] = currentAmmo.getAmmo();
			return TriggerResult.keep();
		});

		data.addTrigger(id + "-attack", Trigger.PRE_LAUNCH_PROJECTILE_GROUP, (pdata, in) -> {
			if (pendingAmmo[0] == null) return TriggerResult.keep();
			PreLaunchProjectileGroupEvent ev = (PreLaunchProjectileGroupEvent) in;
			if (!ev.isBasicAttack()) return TriggerResult.keep();
			AmmunitionInstance currentAmmo = data.getAmmoInstance();
			if (currentAmmo == null || !currentAmmo.getAmmo().getId().equals(pendingAmmo[0].getId())) {
				return TriggerResult.keep();
			}

			data.addDamageBuff(DamageBuffType.of(DamageCategory.DIRECT), 
				Buff.increase(data, damage, StatTracker.damageBuffAlly(buffId, this).shouldCombine(false)), secs * 20);
			pendingAmmo[0] = null;
			return TriggerResult.keep();
		});
	}

	@Override
	public void onAcquire(PlayerSessionData data, int amount) {
		
	}

	@Override
	public void onInitializeSession(PlayerSessionData data) {
		
	}

	@Override
	public void setupItem() {
		item = createItem(Material.AMETHYST_SHARD, 
				"After changing to a different ammunition, fire a basic attack with it to increase your damage by "
				+ DescUtil.val(damage) + " for " + DescUtil.duration(secs) + ". Does not stack; repeated activations refresh the duration.");
	}
}
