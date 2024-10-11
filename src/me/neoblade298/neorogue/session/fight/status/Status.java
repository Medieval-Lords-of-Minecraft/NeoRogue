package me.neoblade298.neorogue.session.fight.status;

import java.util.Comparator;

import org.bukkit.Bukkit;

import me.neoblade298.neocore.shared.util.SharedUtil;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.TickAction;
import net.kyori.adventure.text.Component;

public abstract class Status {
	public static final Status EMPTY = new BasicStatus("EMPTY", null, StatusClass.NONE);
	protected String id;
	protected int stacks;
	protected TickAction action;
	protected FightData data;
	protected StatusSliceHolder slices = new StatusSliceHolder();
	protected int ticks;
	protected boolean hidden;
	protected StatusClass sc;
	
	public static final Comparator<Status> comp = new Comparator<Status>() {
		@Override
		public int compare(Status o1, Status o2) {
			int comp = Integer.compare(o1.stacks, o2.stacks);
			if (comp != 0) return comp;
			return o1.getId().compareTo(o2.getId());
		}
	};
	
	public Status(String id, FightData data, StatusClass sc) {
		this.id = id;
		this.data = data;
		this.sc = sc;
	}
	
	public Status(String id, FightData data, StatusClass sc, boolean hidden) {
		this.id = id;
		this.data = data;
		this.hidden = hidden;
		this.sc = sc;
	}
	
	// Setting stacks or status to 0 means they will be untouched
	public abstract void apply(FightData applier, int stacks, int seconds);
	
	public static Status createByType(StatusType id, FightData target) {
		switch (id) {
		case POISON: return new PoisonStatus(target);
		case BLEED: return new BleedStatus(target);
		case REND: return new RendStatus(target);
		case BURN: return new DecrementStackStatus(id.name(), target, StatusClass.NEGATIVE);
		case FROST: return new FrostStatus(target);
		case ELECTRIFIED: return new ElectrifiedStatus(target);
		case CONCUSSED: return new ConcussedStatus(target);
		case INSANITY: return new InsanityStatus(target);
		case SANCTIFIED: return new DecrementStackStatus(id.name(), target, StatusClass.NEGATIVE);
		case THORNS: return new BasicStatus(id.name(), target, StatusClass.POSITIVE);
		case REFLECT: return new BasicStatus(id.name(), target, StatusClass.POSITIVE);
		case BERSERK: return new BasicStatus(id.name(), target, StatusClass.POSITIVE);
		case STRENGTH: return new StrengthStatus(target);
		case INTELLECT: return new IntellectStatus(target);
		case PROTECT: return new ProtectStatus(target);
		case SHELL: return new ShellStatus(target);
		case STEALTH: return new DurationStatus(id.name(), target, StatusClass.POSITIVE);
		case EVADE: return new BasicStatus(id.name(), target, StatusClass.POSITIVE);
		case FOCUS: return new BasicStatus(id.name(), target, StatusClass.POSITIVE);
		case INJURY: return new DecrementStackStatus(id.name(), target, StatusClass.NEGATIVE);
		case SILENCED: return new BasicStatus(id.name(), target, StatusClass.NEGATIVE, true);
		case CHANNELING: return new BasicStatus(id.name(), target, StatusClass.NONE, true);
		}
		Bukkit.getLogger().warning("[NeoRogue] Failed to create status type " + id);
		return new BasicStatus(id.name(), target, StatusClass.NONE);
	}
	
	public static Status createByGenericType(GenericStatusType type, String id, FightData target) {
		return createByGenericType(type, id, target, false);
	}
	
	public static Status createByGenericType(GenericStatusType type, String id, FightData target, boolean hidden) {
		switch (type) {
		case DECREMENT_STACK: return new DecrementStackStatus(id, target, StatusClass.NONE, hidden);
		case BASIC: return new BasicStatus(id, target, StatusClass.NONE, hidden);
		case DURATION: return new DurationStatus(id, target, StatusClass.NONE, hidden);
		default: return null;
		}
	}
	
	public String getDisplay() {
		try {
			return StatusType.valueOf(id).boardLine + "§7: §f" + stacks + (ticks > 0 ? " [" + (ticks / 20) + "s]" : "");
		}
		catch (IllegalArgumentException ex) {
			return "§f" + id + "§7: §f" + stacks + (ticks > 0 ? " [" + (ticks / 20) + "s]" : "");
		}
	}
	
	public void cleanup() {
		if (action != null) action.setCancelled(true);
	}
	
	public String getId() {
		return id;
	}
	
	public StatusSliceHolder getSlices() {
		return slices;
	}
	
	public int getStacks() {
		return stacks;
	}
	
	public boolean isHidden() {
		return hidden;
	}
	
	public StatusClass getStatusClass() {
		return sc;
	}
	
	public enum StatusType {
		POISON("<dark_green>Poison</dark_green>", "&2Poison"),
		REND("<red>Rend</red>", "&cRend"),
		BLEED("<red>Bleed</red>", "&cBleed"),
		BURN("<gold>Burn</gold>", "&6Burn"),
		FROST("<blue>Frost</blue>", "&9Frost"),
		ELECTRIFIED("<yellow>Electrified</yellow>", "&eElectrified"),
		CONCUSSED("<dark_green>Concussed</dark_green>", "&2Concussed"),
		INSANITY("<dark_purple>Insanity</dark_purple>", "&5Insanity"),
		SANCTIFIED("<white>Sanctified</white>", "&fSanctified"),
		THORNS("<gold>Thorns</gold>", "&6Thorns"),
		REFLECT("<light_purple>Reflect</light_purple>", "&dReflect"),
		BERSERK("<dark_red>Berserk</dark_red>", "&4Berserk"),
		STRENGTH("<red>Strength</red>", "&cStrength"),
		INTELLECT("<blue>Intellect</blue>", "&9Intellect"),
		PROTECT("<green>Protect</green>", "&aProtect"),
		SHELL("<aqua>Shell</aqua>", "&3Shell"),
		STEALTH("<dark_purple>Stealth</dark_purple>", "&5Stealth"),
		EVADE("<aqua>Evade</aqua>", "&3Evade"),
		FOCUS("<aqua>Focus</aqua>", "&3Focus"),
		INJURY("<dark_red>Injury</dark_red>", "&4Injury"),
		SILENCED("<dark_gray>Silenced</dark_gray>", "&8Silenced"), // Hidden
		CHANNELING("<dark_red>Channeling</dark_red>", "&4Channeling"); // Hidden
		public String tag;
		public Component ctag;
		public String boardLine;
		private StatusType(String tag, String boardLine) {
			this.tag = tag;
			this.ctag = SharedUtil.color(tag);
			this.boardLine = boardLine.replaceAll("&", "§");
		}
	}
	public enum GenericStatusType {
		DECREMENT_STACK, BASIC, DURATION;
	}
	
	public enum StatusClass {
		POSITIVE, NEGATIVE, NONE;
	}
}
