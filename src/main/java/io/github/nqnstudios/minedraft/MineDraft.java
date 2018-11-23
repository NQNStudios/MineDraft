package io.github.nqnstudios.minedraft;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.logging.log4j.Logger;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod(modid = MineDraft.MODID, name = MineDraft.NAME, version = MineDraft.VERSION, clientSideOnly = true)
public class MineDraft
{
    public static final String MODID = "minedraft";
    public static final String NAME = "MineDraft";
    public static final String VERSION = "0.0.1";

    protected static Logger logger;
    private static boolean draftMode = false;
    private static DraftCore core = new DraftCore();

    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        logger = event.getModLog();
    }

    @EventHandler
    public void init(FMLInitializationEvent event)
    {
        MinecraftForge.EVENT_BUS.register(this);
        ClientCommandHandler.instance.registerCommand(new DraftCommand());
    }

    @SubscribeEvent
    public static void onTick(TickEvent.PlayerTickEvent evt)
    {
        if (evt.side == Side.CLIENT && evt.phase == TickEvent.Phase.END)
        {
            // Update the DraftCore
            core.tick();

            // TODO check for messages out of the DraftCore
            String output = core.takeOutput();
            if (output.length() > 0) {
                evt.player.sendMessage(new TextComponentString(output));
            }

        }
    }

    @SubscribeEvent
    public void onPlayerAttemptChat(ClientChatEvent event)
    {
        if (draftMode)
        {
            event.setCanceled(true);
            core.process(event.getMessage());
        }
    }

    private class DraftCommand extends CommandBase {
        @Override
        public String getName()
        {
            return "draft";
        }

        @Override
        public String getUsage(ICommandSender sender)
        {
            return "draft";
        }

        @Override
        public boolean checkPermission(MinecraftServer server, ICommandSender sender)
        {
            return true;
        }

        @Override
        public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos pos)
        {
            return Collections.emptyList();
        }

        @Override
        public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
        {
            if (draftMode){
                draftMode = false;
                core.clear();
                 sender.sendMessage(new TextComponentString("Draft mode disabled!"));
            }
             else {
                 draftMode = true;
                 sender.sendMessage(new TextComponentString("Draft mode enabled!"));
             }
        }
    }
}
