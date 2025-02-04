package krasa.grepconsole.plugin;

import com.intellij.openapi.diagnostic.Logger;
import krasa.grepconsole.Cloner;
import krasa.grepconsole.grep.GrepCompositeModel;
import krasa.grepconsole.integration.ThemeColors;
import krasa.grepconsole.model.DomainObject;
import krasa.grepconsole.model.Profile;
import krasa.grepconsole.model.StreamBufferSettings;
import krasa.grepconsole.model.TailSettings;
import krasa.grepconsole.utils.Utils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class PluginState extends DomainObject implements Cloneable {
	private static final Logger LOG = Logger.getInstance(PluginState.class);

	/**
	 * MUST BE CLASSLOADED BEFORE THE STATE
	 */
	@SuppressWarnings("InstantiationOfUtilityClass")
	private static final ThemeColors THEME_COLORS = new ThemeColors();

	private List<Profile> profiles = new ArrayList<>();
	private TailSettings tailSettings;
	private StreamBufferSettings streamBufferSettings;
	private boolean allowRunConfigurationChanges = true;
	private int version;
	private List<GrepCompositeModel> grepHistory = new ArrayList<>();
	private boolean autoReloadGrepModel = false;
	private boolean autoClearChildConsoles = false;
	private boolean autoApplyGrepModel = true;

	public static PluginState getInstance() {
		return GrepConsoleApplicationComponent.getInstance().getState();
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public Profile getDefaultProfile() {
		Profile result = null;
		for (Profile profile : profiles) {
			if (profile.isDefaultProfile()) {
				result = profile;
			}
		}

		if (result == null) {
			if (profiles.isEmpty()) {
				profiles.add(DefaultState.getDefaultProfile());
			}
			Profile profile = profiles.get(0);
			profile.setDefaultProfile(true);
			result = profile;
		}
		return result;
	}

	@NotNull
	public Profile getProfile(long selectedProfileId) {
		Profile result = null;
		for (Profile profile : profiles) {
			if (profile.getId() == selectedProfileId) {
				result = profile;
			}
		}
		if (result == null) {
			result = getDefaultProfile();
		}
		return result;
	}

	public TailSettings getTailSettings() {
		if (tailSettings == null) {
			tailSettings = new TailSettings();
		}
		return tailSettings;
	}

	public StreamBufferSettings getStreamBufferSettings() {
		if (streamBufferSettings == null) {
			streamBufferSettings = new StreamBufferSettings();
		}
		return streamBufferSettings;
	}

	public void setStreamBufferSettings(StreamBufferSettings streamBufferSettings) {
		this.streamBufferSettings = streamBufferSettings;
	}

	public void setTailSettings(TailSettings tailSettings) {
		this.tailSettings = tailSettings;
	}

	public void setProfiles(List<Profile> profiles) {
		this.profiles = profiles;
	}

	public List<Profile> getProfiles() {
		return profiles;
	}

	@Override
	public PluginState clone() {
		return Cloner.deepClone(this);
	}

	public boolean isAllowRunConfigurationChanges() {
		return allowRunConfigurationChanges;
	}

	public void setAllowRunConfigurationChanges(final boolean allowRunConfigurationChanges) {
		this.allowRunConfigurationChanges = allowRunConfigurationChanges;
	}

	public Profile createProfile() {
		Profile profile = DefaultState.getDefaultProfile();
		profile.setDefaultProfile(false);
		profile.setName(Utils.generateName(profiles, "new"));
		profiles.add(profile);
		return profile;
	}

	public void setDefault(Profile selectedProfile) {
		for (Profile profile : profiles) {
			profile.setDefaultProfile(false);
		}
		selectedProfile.setDefaultProfile(true);
	}

	public Profile copyProfile(Profile selectedProfile) {
		Profile profile = selectedProfile.clone();
		profile.setName(Utils.generateName(profiles, profile.getPresentableName()));
		profile.setDefaultProfile(false);
		profile.setId(System.currentTimeMillis());
		profiles.add(profile);
		return profile;
	}


	public Profile delete(Profile selectedProfile) {
		int index = profiles.indexOf(selectedProfile);
		profiles.remove(selectedProfile);

		if (selectedProfile.isDefaultProfile()) {
			if (profiles.isEmpty()) {
				profiles.add(DefaultState.getDefaultProfile());
			} else {
				profiles.get(0).setDefaultProfile(true);
			}
			return profiles.get(0);
		} else {
			if (profiles.size() > index) {
				return profiles.get(index);
			}
			if (profiles.size() > index - 1) {
				return profiles.get(index - 1);
			}
			return getDefaultProfile();
		}
	}

	@Override
	public String toString() {
		return "PluginState{" +
				"profiles=" + profiles +
				", tailSettings=" + tailSettings +
				", streamBufferSettings=" + streamBufferSettings +
				", allowRunConfigurationChanges=" + allowRunConfigurationChanges +
				"} " + super.toString();
	}

	private static final int MAX_RECENT_SIZE = 30;

	public List<GrepCompositeModel> getGrepHistory() {
		return grepHistory;
	}

	public void setGrepHistory(List<GrepCompositeModel> grepHistory) {
		this.grepHistory = grepHistory;
	}

	public void addToHistory(GrepCompositeModel grepModel) {
		grepHistory.removeIf(m -> m.equals(grepModel));
		grepHistory.add(grepModel);
		while (grepHistory.size() > MAX_RECENT_SIZE) {
			grepHistory.remove(0);
		}
	}

	public void removeFromHistory(GrepCompositeModel currentModel) {
		if (currentModel != null) {
			grepHistory.remove(currentModel);
		}
	}

	public boolean isAutoReloadGrepModel() {
		return autoReloadGrepModel;
	}

	public void setAutoReloadGrepModel(boolean autoReloadGrepModel) {
		this.autoReloadGrepModel = autoReloadGrepModel;
	}

	public boolean isAutoApplyGrepModel() {
		return autoApplyGrepModel;
	}

	public void setAutoApplyGrepModel(boolean autoApplyGrepModel) {
		this.autoApplyGrepModel = autoApplyGrepModel;
	}

	public boolean isAutoClearChildConsoles() {
		return autoClearChildConsoles;
	}

	public void setAutoClearChildConsoles(boolean autoClearChildConsoles) {
		this.autoClearChildConsoles = autoClearChildConsoles;
	}
}
