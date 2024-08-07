package to.itsme.itsmyconfig.command;

import revxrsal.commands.bukkit.BukkitCommandHandler;
import to.itsme.itsmyconfig.ItsMyConfig;
import to.itsme.itsmyconfig.command.handler.ExceptionHandler;
import to.itsme.itsmyconfig.command.handler.PlaceholderException;
import to.itsme.itsmyconfig.command.impl.ItsMyConfigCommand;
import to.itsme.itsmyconfig.placeholder.Placeholder;

import java.util.stream.Collectors;

public final class CommandManager {

    private final ItsMyConfig plugin;
    private final BukkitCommandHandler handler;

    public CommandManager(final ItsMyConfig plugin) {
        this.plugin = plugin;
        this.handler = BukkitCommandHandler.create(plugin);

        // set the help-writer format
        this.handler.setHelpWriter((cmd, actor) ->
                String.format(
                        "  <gray>• <white>/%s <gold>%s",
                        cmd.getPath().toRealString(),
                        cmd.getUsage().isEmpty() ? "" : cmd.getUsage() + " "
                )
        );

        this.handler.registerValueResolver(
                Placeholder.class, context -> {
                    final String name = context.pop();
                    final Placeholder placeholder = plugin.getPlaceholderManager().get(name);
                    if (placeholder != null) {
                        return placeholder;
                    }

                    throw new PlaceholderException(name);
                }
        );

        this.handler.getAutoCompleter().registerSuggestion(
                "placeholders", (args, sender, command) -> plugin.getPlaceholderManager().getPlaceholdersMap().keySet()
        );

        this.handler.getAutoCompleter().registerParameterSuggestions(
                Placeholder.class, "placeholders"
        );

        this.handler.getAutoCompleter().registerSuggestion("singleValuePlaceholder", (args, sender, command) ->
                plugin.getPlaceholderManager().getPlaceholdersMap().keySet().stream().filter(name -> {
                    final Placeholder data = plugin.getPlaceholderManager().get(name);
                    return data.getConfigurationSection().contains("value");
                }).collect(Collectors.toList())
        );

        this.handler.setExceptionHandler(new ExceptionHandler());
        this.handler.getAutoCompleter();
        this.registerCommands();
        this.handler.registerBrigadier();
        this.handler.enableAdventure();
    }

    public void registerCommands() {
        this.handler.register(new ItsMyConfigCommand(this.plugin));
    }

}
