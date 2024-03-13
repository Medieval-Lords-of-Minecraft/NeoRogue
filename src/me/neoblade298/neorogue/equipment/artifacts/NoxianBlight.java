package me.neoblade298.neorogue.equipment.artifacts;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neorogue.equipment.Artifact;
import me.neoblade298.neorogue.equipment.ArtifactInstance;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.CastUsableEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class NoxianBlight extends Artifact {
	private static final String ID = "noxianBlight";
	private static final int stamina = 25, mana = 25;

	public NoxianBlight() {
		super(ID, "Noxian Blight", Rarity.RARE, EquipmentClass.WARRIOR);
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, ArtifactInstance ai) {
		data.addTrigger(id, Trigger.CAST_USABLE, (pdata, in) -> {
			CastUsableEvent ev = (CastUsableEvent) in;
			if (!ev.getInstance().canTrigger(data.getPlayer(), data)) return TriggerResult.keep();
			boolean activated = false;
			if (ev.getInstance().getManaCost() >= 25) {
				activated = true;
				data.applyStatus(StatusType.INTELLECT, p.getUniqueId(), 1, -1);
			}
			if (ev.getInstance().getStaminaCost() >= 25) {
				activated = true;
				data.applyStatus(StatusType.STRENGTH, p.getUniqueId(), 1, -1);
			}
			
			if (activated) {
				p.playSound(p, Sound.ENTITY_ARROW_HIT_PLAYER, 1F, 1F);
				Util.msg(p, this.display.append(Component.text(" was activated", NamedTextColor.GRAY)));
			}
			return TriggerResult.keep();
		});
	}

	@Override
	public void onAcquire(PlayerSessionData data) {
		
	}

	@Override
	public void onInitializeSession(PlayerSessionData data) {
		
	}

	@Override
	public void setupItem() {
		item = createItem(Material.MAGMA_CREAM, 
				"For every skill you cast that at base costs over <white>" + stamina + "</white> stamina, gain 1 " + GlossaryTag.STRENGTH.tag(this) +
				". For every skill you cast that at base costs over"
				+ " <white>" + mana + "</white> mana, gain 1 " + GlossaryTag.INTELLECT.tag(this) + ".");
	}
}
