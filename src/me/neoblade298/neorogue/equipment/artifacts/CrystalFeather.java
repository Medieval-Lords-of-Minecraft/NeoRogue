package me.neoblade298.neorogue.equipment.artifacts;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Artifact;
import me.neoblade298.neorogue.equipment.ArtifactInstance;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class CrystalFeather extends Artifact {
	private static final String ID = "crystalFeather";
	private static final int dur = 3;

	public CrystalFeather() {
		super(ID, "Crystal Feather", Rarity.UNCOMMON, EquipmentClass.CLASSLESS);
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, ArtifactInstance ai) {
		data.addTrigger(id, Trigger.RECEIVE_HEALTH_DAMAGE, (pdata, in) -> {
			data.applyStatus(StatusType.INVINCIBLE, data, 1, dur * 20);
			p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, dur * 20, 1));
			Sounds.glass.play(p, p);
			p.sendMessage(display.append(Component.text(" was activated", NamedTextColor.GRAY)));
			return TriggerResult.remove();
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
		item = createItem(Material.FEATHER, 
				"Once every fight upon receiving health damage, gain " + DescUtil.potion("Speed", 1, dur) +
				" and invincibility for that duration.");
	}
}
