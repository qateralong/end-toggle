package com.qateralong.endtoggle;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EndToggle implements ModInitializer {
    public static final String MOD_ID = "endtoggle";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("endtoggle.json");
    private static final Map<UUID, Long> NOTIFIED = new java.util.HashMap<>();
    private static Config config = new Config();

    public static class Config {
        public boolean enabled = true;
        public String closedMessage = "&5Энд закрыт";
    }

    @Override
    public void onInitialize() {
        load();
        CommandRegistrationCallback.EVENT.register((dispatcher, access, environment) -> register(dispatcher));
    }

    public static boolean blocks(Level level, Entity entity) {
        if (config.enabled || level.isClientSide || level.dimension() == Level.END) {
            return false;
        }
        if (entity instanceof ServerPlayer player && !config.closedMessage.isEmpty()) {
            long now = System.currentTimeMillis();
            Long last = NOTIFIED.get(player.getUUID());
            if (last == null || now - last > 3000L) {
                NOTIFIED.put(player.getUUID(), now);
                player.displayClientMessage(Component.literal(config.closedMessage.replace('&', '§')), true);
            }
        }
        return true;
    }

    private static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("end")
                .requires(source -> source.hasPermission(2))
                .executes(EndToggle::status)
                .then(Commands.literal("on").executes(context -> set(context, true)))
                .then(Commands.literal("off").executes(context -> set(context, false)))
                .then(Commands.literal("status").executes(EndToggle::status))
                .then(Commands.literal("reload").executes(context -> {
                    load();
                    return status(context);
                })));
    }

    private static int set(CommandContext<CommandSourceStack> context, boolean enabled) {
        config.enabled = enabled;
        save();
        context.getSource().sendSuccess(() -> Component.literal(enabled ? "Энд открыт: порталы работают" : "Энд закрыт: сквозь портал можно пройти")
                .withStyle(enabled ? ChatFormatting.GREEN : ChatFormatting.RED), true);
        return 1;
    }

    private static int status(CommandContext<CommandSourceStack> context) {
        context.getSource().sendSuccess(() -> Component.literal("Энд " + (config.enabled ? "открыт" : "закрыт")), false);
        return config.enabled ? 1 : 0;
    }

    private static void load() {
        if (Files.exists(FILE)) {
            try (Reader reader = Files.newBufferedReader(FILE)) {
                Config loaded = GSON.fromJson(reader, Config.class);
                if (loaded != null) {
                    config = loaded;
                }
            } catch (Exception e) {
                LOGGER.error("Failed to read {}", FILE, e);
            }
        }
        save();
    }

    private static void save() {
        try (Writer writer = Files.newBufferedWriter(FILE)) {
            GSON.toJson(config, writer);
        } catch (IOException e) {
            LOGGER.error("Failed to write {}", FILE, e);
        }
    }
}
