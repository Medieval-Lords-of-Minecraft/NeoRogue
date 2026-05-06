package me.neoblade298.neorogue.equipment.artifacts;

import org.bukkit.Material;

import me.neoblade298.neorogue.equipment.Artifact;
import me.neoblade298.neorogue.equipment.ArtifactInstance;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.BuffStatTracker;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.WeaponSwingEvent;

public class TempestSigil extends Artifact {
	private static final String ID = "TempestSigil";
	private static final long WINDOW = 2000; // 2 seconds in ms
	private static final long COOLDOWN = 3000; // 3 seconds in ms

	public TempestSigil() {
		super(ID, "Tempest Sigil", Rarity.RARE, EquipmentClass.CLASSLESS);
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, ArtifactInstance ai) {
		long[] lastCast = { 0 };
		long[] buffUntil = { 0 };
		long[] nextUse = { 0 };

		data.addTrigger(id, Trigger.CAST_USABLE, (pdata, in) -> {
			lastCast[0] = System.currentTimeMillis();
			return TriggerResult.keep();
		});

		data.addTrigger(id, Trigger.WEAPON_SWING, (pdata, in) -> {
			long now = System.currentTimeMillis();
			if (now >= nextUse[0] && now - lastCast[0] <= WINDOW) {
				buffUntil[0] = now + COOLDOWN;
				nextUse[0] = now + COOLDOWN;
			}
			if (now < buffUntil[0]) {
				WeaponSwingEvent ev = (WeaponSwingEvent) in;
				ev.getAttackSpeedBuffList().add(new Buff(pdata, 0, 0.2, BuffStatTracker.ignored(TempestSigil.this)));
			}
			return TriggerResult.keep();
		});
	}

	@Override
	public void onAcquire(PlayerSessionData data, int amount) { }

	@Override
	public void onInitializeSession(PlayerSessionData data) { }

	@Override
	public void setupItem() {
		item = createItem(Material.ECHO_SHARD,
				"If you use your weapon within <white>2s</white> of casting an ability, gain <white>20%</white> attack speed for <white>3s</white>. Does not stack.");
	}
}
