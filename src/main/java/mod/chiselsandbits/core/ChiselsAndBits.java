package mod.chiselsandbits.core;

import mod.chiselsandbits.api.IChiselAndBitsAPI;
import mod.chiselsandbits.config.ModConfig;
import mod.chiselsandbits.core.api.ChiselAndBitsAPI;
import mod.chiselsandbits.core.api.IMCHandler;
import mod.chiselsandbits.crafting.ChiselCrafting;
import mod.chiselsandbits.crafting.MirrorTransferCrafting;
import mod.chiselsandbits.crafting.NegativeInversionCrafting;
import mod.chiselsandbits.crafting.StackableCrafting;
import mod.chiselsandbits.events.EventBreakSpeed;
import mod.chiselsandbits.events.EventPlayerInteract;
import mod.chiselsandbits.gui.ModGuiRouter;
import mod.chiselsandbits.integration.Integration;
import mod.chiselsandbits.network.NetworkRouter;
import mod.chiselsandbits.registry.ModBlocks;
import mod.chiselsandbits.registry.ModItems;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLInterModComms;
import net.minecraftforge.fml.common.event.FMLModIdMappingEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.oredict.RecipeSorter;
import net.minecraftforge.oredict.RecipeSorter.Category;

@Mod(
		name = ChiselsAndBits.MODNAME,
		modid = ChiselsAndBits.MODID,
		version = ChiselsAndBits.VERSION,
		acceptedMinecraftVersions = "[1.8.8,1.8.9]",
		dependencies = ChiselsAndBits.DEPENDENCIES,
		guiFactory = "mod.chiselsandbits.gui.ModConfigGuiFactory" )
public class ChiselsAndBits
{
	public static final String MODNAME = "Chisels & Bits";
	public static final String MODID = "chiselsandbits";
	public static final String VERSION = "@VERSION@";

	public static final String DEPENDENCIES = "required-after:Forge@[" // forge.
			+ net.minecraftforge.common.ForgeVersion.majorVersion + '.' // majorVersion
			+ net.minecraftforge.common.ForgeVersion.minorVersion + '.' // minorVersion
			+ net.minecraftforge.common.ForgeVersion.revisionVersion + '.' // revisionVersion
			+ net.minecraftforge.common.ForgeVersion.buildVersion + ",);after:mcmultipart@[1.0.4,);after:jei@[11.15.0.1697,)"; // buildVersion

	// create creative tab...
	private static ChiselsAndBits instance;
	private ModConfig config;
	private ModItems items;
	private ModBlocks blocks;
	private final Integration integration = new Integration();
	private final IChiselAndBitsAPI api = new ChiselAndBitsAPI();

	public ChiselsAndBits()
	{
		instance = this;
	}

	public static ChiselsAndBits getInstance()
	{
		return instance;
	}

	public static ModBlocks getBlocks()
	{
		return instance.blocks;
	}

	public static ModItems getItems()
	{
		return instance.items;
	}

	public static ModConfig getConfig()
	{
		return instance.config;
	}

	public static IChiselAndBitsAPI getApi()
	{
		return instance.api;
	}

	@EventHandler
	private void handleIMCEvent(
			final FMLInterModComms.IMCEvent event )
	{
		final IMCHandler imcHandler = new IMCHandler();
		imcHandler.handleIMCEvent( event );
	}

	@EventHandler
	public void preinit(
			final FMLPreInitializationEvent event )
	{
		// load config...
		config = new ModConfig( event.getSuggestedConfigurationFile() );
		items = new ModItems( getConfig() );
		blocks = new ModBlocks( getConfig(), event.getSide() );

		integration.preinit( event );

		// loader must be added here to prevent missing models, the rest of the
		// model/textures must be configured later.
		if ( event.getSide() == Side.CLIENT )
		{
			ClientSide.instance.preinit( this );
		}
	}

	@EventHandler
	public void init(
			final FMLInitializationEvent event )
	{
		if ( event.getSide() == Side.CLIENT )
		{
			ClientSide.instance.init( this );
		}

		integration.init();

		registerWithBus( new EventBreakSpeed() );
		registerWithBus( new EventPlayerInteract() );

		// add recipes to game...
		getItems().addRecipes();

		final String craftingOrder = "after:minecraft:shapeless";

		// add special recipes...
		if ( getConfig().enablePositivePrintCrafting )
		{
			GameRegistry.addRecipe( new ChiselCrafting() );
			RecipeSorter.register( MODID + ":chiselcrafting", ChiselCrafting.class, Category.UNKNOWN, craftingOrder );
		}

		if ( getConfig().enableStackableCrafting )
		{
			GameRegistry.addRecipe( new StackableCrafting() );
			RecipeSorter.register( MODID + ":stackablecrafting", StackableCrafting.class, Category.UNKNOWN, craftingOrder );
		}

		if ( getConfig().enableNegativePrintInversionCrafting )
		{
			GameRegistry.addRecipe( new NegativeInversionCrafting() );
			RecipeSorter.register( MODID + ":negativepatterncrafting", NegativeInversionCrafting.class, Category.UNKNOWN, craftingOrder );
		}

		if ( getConfig().enableMirrorPrint )
		{
			GameRegistry.addRecipe( new MirrorTransferCrafting() );
			RecipeSorter.register( MODID + ":mirrorpatterncrafting", MirrorTransferCrafting.class, Category.UNKNOWN, craftingOrder );
		}
	}

	@EventHandler
	public void postinit(
			final FMLPostInitializationEvent event )
	{
		if ( event.getSide() == Side.CLIENT )
		{
			ClientSide.instance.postinit( this );
		}

		integration.postinit();

		NetworkRouter.instance = new NetworkRouter();
		NetworkRegistry.INSTANCE.registerGuiHandler( this, new ModGuiRouter() );
	}

	@EventHandler
	public void idsMapped(
			final FMLModIdMappingEvent event )
	{
		getItems().itemBlockBit.clearCache();
	}

	public static void registerWithBus(
			final Object obj )
	{
		MinecraftForge.EVENT_BUS.register( obj );
	}

}