package to.itsme.itsmyconfig;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;
import to.itsme.itsmyconfig.command.CommandManager;
import to.itsme.itsmyconfig.config.DynamicPlaceHolder;
import to.itsme.itsmyconfig.config.placeholder.PlaceholderData;
import to.itsme.itsmyconfig.config.placeholder.PlaceholderType;
import to.itsme.itsmyconfig.config.placeholder.type.AnimatedPlaceholderData;
import to.itsme.itsmyconfig.config.placeholder.type.ColorPlaceholderData;
import to.itsme.itsmyconfig.config.placeholder.type.RandomPlaceholderData;
import to.itsme.itsmyconfig.config.placeholder.type.StringPlaceholderData;
import to.itsme.itsmyconfig.progress.ProgressBar;
import to.itsme.itsmyconfig.progress.ProgressBarBucket;
import to.itsme.itsmyconfig.requirement.RequirementManager;

public final class ItsMyConfig extends JavaPlugin {

    private static ItsMyConfig instance;
    private final ProgressBarBucket progressBarBucket = new ProgressBarBucket();
    private DynamicPlaceHolder dynamicPlaceHolder;
    private String symbolPrefix;
    private RequirementManager requirementManager;

    private BukkitAudiences adventure;

    public static ItsMyConfig getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;
        this.dynamicPlaceHolder = new DynamicPlaceHolder(this, progressBarBucket);
        this.dynamicPlaceHolder.register();

        new CommandManager(this);

        this.requirementManager = new RequirementManager();
        this.adventure = BukkitAudiences.create(this);

        loadConfig();

        final ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
        protocolManager.addPacketListener(new PacketChatListener(this, PacketType.Play.Server.DISGUISED_CHAT, PacketType.Play.Server.SYSTEM_CHAT));
        protocolManager.addPacketListener(new PacketChatListener(this, PacketType.Play.Server.CHAT));
    }

    public void loadConfig() {
        progressBarBucket.clearProgressBar();
        this.saveDefaultConfig();
        this.reloadConfig();

        this.symbolPrefix = this.getConfig().getString("symbol-prefix");

        final ConfigurationSection placeholdersSec = this.getConfig().getConfigurationSection("custom-placeholder");
        for (final String identifier : placeholdersSec.getKeys(false)) {
            final PlaceholderType type = PlaceholderType.find(placeholdersSec.getString(identifier + ".type"));
            final PlaceholderData data;
            switch (type) {
                case RANDOM:
                    data = new RandomPlaceholderData(placeholdersSec.getStringList(identifier + ".values"));
                    break;
                case ANIMATED:
                    data = new AnimatedPlaceholderData(placeholdersSec.getStringList(identifier + ".values"));
                    break;
                case COLOR:
                    data = new ColorPlaceholderData(placeholdersSec.getString(identifier + ".value"));
                    break;
                default:
                case STRING:
                    data = new StringPlaceholderData(placeholdersSec.getString(identifier + ".value"));
                    break;
            }

            this.dynamicPlaceHolder.registerIdentifier(identifier, data);
            final ConfigurationSection requirementSec = placeholdersSec.getConfigurationSection(identifier + ".requirements");
            if (requirementSec != null) {
                for (final String requirement : requirementSec.getKeys(false)) {
                    data.registerRequirement(requirementSec.getConfigurationSection(requirement));
                }
            }

            this.getLogger().info(String.format("Registered placeholder %s", identifier));
        }

        final ConfigurationSection customProgressConfigurationSection = this.getConfig().getConfigurationSection("custom-progress");
        for (String identifier : customProgressConfigurationSection.getKeys(false)) {
            ConfigurationSection configurationSection = customProgressConfigurationSection.getConfigurationSection(identifier);
            String symbol = configurationSection.getString("symbol");
            String completedColor = configurationSection.getString("completed-color");
            String progressColor = configurationSection.getString("progress-color");
            String remainingColor = configurationSection.getString("remaining-color");
            ProgressBar progressBar = new ProgressBar(identifier, symbol, completedColor, progressColor, remainingColor);
            progressBarBucket.registerProgressBar(progressBar);
        }

    }

    public BukkitAudiences adventure() {
        if (this.adventure == null) {
            throw new IllegalStateException("Tried to access Adventure when the plugin was disabled!");
        }
        return this.adventure;
    }

    public String getSymbolPrefix() {
        return symbolPrefix;
    }

    public RequirementManager getRequirementManager() {
        return requirementManager;
    }

}
