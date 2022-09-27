package cofh.core;

import cofh.core.capability.CapabilityArchery;
import cofh.core.capability.CapabilityAreaEffect;
import cofh.core.capability.CapabilityEnchantableItem;
import cofh.core.capability.CapabilityShieldItem;
import cofh.core.client.gui.FluidFilterScreen;
import cofh.core.client.gui.ItemFilterScreen;
import cofh.core.command.CoFHCommand;
import cofh.core.compat.curios.CuriosProxy;
import cofh.core.compat.quark.QuarkFlags;
import cofh.core.config.CoreClientConfig;
import cofh.core.config.CoreCommandConfig;
import cofh.core.config.CoreEnchantConfig;
import cofh.core.config.CoreServerConfig;
import cofh.core.event.ArmorEvents;
import cofh.core.init.*;
import cofh.core.network.packet.PacketIDs;
import cofh.core.network.packet.client.*;
import cofh.core.network.packet.server.*;
import cofh.core.util.Proxy;
import cofh.core.util.ProxyClient;
import cofh.core.util.helpers.FluidHelper;
import cofh.lib.client.renderer.entity.ElectricArcRenderer;
import cofh.lib.client.renderer.entity.KnifeRenderer;
import cofh.lib.client.renderer.entity.NothingRenderer;
import cofh.lib.client.renderer.entity.model.ArmorFullSuitModel;
import cofh.lib.config.ConfigManager;
import cofh.lib.loot.TileNBTSync;
import cofh.lib.network.PacketHandler;
import cofh.lib.util.DeferredRegisterCoFH;
import cofh.lib.util.Utils;
import cofh.lib.util.constants.ModIds;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.common.loot.GlobalLootModifierSerializer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static cofh.core.util.references.CoreReferences.*;
import static cofh.lib.client.renderer.entity.model.ArmorFullSuitModel.ARMOR_FULL_SUIT_LAYER;

@Mod (ModIds.ID_COFH_CORE)
public class CoFHCore {

    public static final PacketHandler PACKET_HANDLER = new PacketHandler(new ResourceLocation(ModIds.ID_COFH_CORE, "general"));
    public static final Logger LOG = LogManager.getLogger(ModIds.ID_COFH_CORE);
    public static final Proxy PROXY = DistExecutor.unsafeRunForDist(() -> ProxyClient::new, () -> Proxy::new);
    public static final ConfigManager CONFIG_MANAGER = new ConfigManager();

    public static final DeferredRegisterCoFH<Block> BLOCKS = DeferredRegisterCoFH.create(ForgeRegistries.BLOCKS, ModIds.ID_COFH_CORE);
    public static final DeferredRegisterCoFH<Fluid> FLUIDS = DeferredRegisterCoFH.create(ForgeRegistries.FLUIDS, ModIds.ID_COFH_CORE);
    public static final DeferredRegisterCoFH<Item> ITEMS = DeferredRegisterCoFH.create(ForgeRegistries.ITEMS, ModIds.ID_COFH_CORE);
    public static final DeferredRegisterCoFH<EntityType<?>> ENTITIES = DeferredRegisterCoFH.create(ForgeRegistries.ENTITIES, ModIds.ID_COFH_CORE);

    public static final DeferredRegisterCoFH<MenuType<?>> CONTAINERS = DeferredRegisterCoFH.create(ForgeRegistries.CONTAINERS, ModIds.ID_COFH_CORE);
    public static final DeferredRegisterCoFH<MobEffect> EFFECTS = DeferredRegisterCoFH.create(ForgeRegistries.MOB_EFFECTS, ModIds.ID_COFH_CORE);
    public static final DeferredRegisterCoFH<Enchantment> ENCHANTMENTS = DeferredRegisterCoFH.create(ForgeRegistries.ENCHANTMENTS, ModIds.ID_COFH_CORE);
    public static final DeferredRegisterCoFH<ParticleType<?>> PARTICLES = DeferredRegisterCoFH.create(ForgeRegistries.PARTICLE_TYPES, ModIds.ID_COFH_CORE);
    public static final DeferredRegisterCoFH<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegisterCoFH.create(ForgeRegistries.RECIPE_SERIALIZERS, ModIds.ID_COFH_CORE);
    public static final DeferredRegisterCoFH<SoundEvent> SOUND_EVENTS = DeferredRegisterCoFH.create(ForgeRegistries.SOUND_EVENTS, ModIds.ID_COFH_CORE);
    public static final DeferredRegisterCoFH<BlockEntityType<?>> TILE_ENTITIES = DeferredRegisterCoFH.create(ForgeRegistries.BLOCK_ENTITIES, ModIds.ID_COFH_CORE);

    public static boolean curiosLoaded = false;

    public CoFHCore() {

        curiosLoaded = Utils.isModLoaded(ModIds.ID_CURIOS);

        registerPackets();

        final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        modEventBus.addListener(this::entityLayerSetup);
        modEventBus.addListener(this::entityRendererSetup);
        modEventBus.addListener(this::capSetup);
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::clientSetup);
        modEventBus.addGenericListener(GlobalLootModifierSerializer.class, this::registerLootData);

        MinecraftForge.EVENT_BUS.addListener(this::registerCommands);

        BLOCKS.register(modEventBus);
        FLUIDS.register(modEventBus);
        ITEMS.register(modEventBus);
        ENTITIES.register(modEventBus);

        CONTAINERS.register(modEventBus);
        EFFECTS.register(modEventBus);
        ENCHANTMENTS.register(modEventBus);
        PARTICLES.register(modEventBus);
        RECIPE_SERIALIZERS.register(modEventBus);
        SOUND_EVENTS.register(modEventBus);
        TILE_ENTITIES.register(modEventBus);

        CONFIG_MANAGER.register(modEventBus)
                .addClientConfig(new CoreClientConfig())
                .addServerConfig(new CoreServerConfig())
                .addServerConfig(new CoreCommandConfig())
                .addServerConfig(new CoreEnchantConfig());
        CONFIG_MANAGER.setupClient();
        CONFIG_MANAGER.setupServer();

        CoreBlocks.register();
        CoreFluids.register();
        CoreItems.register();
        CoreEntities.register();

        CoreParticles.register();
        CoreContainers.register();
        CoreMobEffects.register();
        CoreEnchantments.register();
        CoreRecipeSerializers.register();
        CoreSounds.register();

        CuriosProxy.register();
    }

    private void registerPackets() {

        PACKET_HANDLER.registerPacket(PacketIDs.PACKET_CONTROL, TileControlPacket::new);
        PACKET_HANDLER.registerPacket(PacketIDs.PACKET_GUI, TileGuiPacket::new);
        PACKET_HANDLER.registerPacket(PacketIDs.PACKET_REDSTONE, TileRedstonePacket::new);
        PACKET_HANDLER.registerPacket(PacketIDs.PACKET_STATE, TileStatePacket::new);
        PACKET_HANDLER.registerPacket(PacketIDs.PACKET_RENDER, TileRenderPacket::new);

        PACKET_HANDLER.registerPacket(PacketIDs.PACKET_MODEL_UPDATE, ModelUpdatePacket::new);

        PACKET_HANDLER.registerPacket(PacketIDs.PACKET_CHAT, IndexedChatPacket::new);
        PACKET_HANDLER.registerPacket(PacketIDs.PACKET_MOTION, PlayerMotionPacket::new);

        PACKET_HANDLER.registerPacket(PacketIDs.PACKET_FILTERABLE_GUI_OPEN, FilterGuiOpenPacket::new);

        PACKET_HANDLER.registerPacket(PacketIDs.PACKET_CONTAINER_CONFIG, ContainerConfigPacket::new);
        PACKET_HANDLER.registerPacket(PacketIDs.PACKET_CONTAINER_GUI, ContainerGuiPacket::new);

        PACKET_HANDLER.registerPacket(PacketIDs.PACKET_SECURITY, SecurityPacket::new);

        PACKET_HANDLER.registerPacket(PacketIDs.PACKET_CONFIG, TileConfigPacket::new);
        PACKET_HANDLER.registerPacket(PacketIDs.PACKET_SECURITY_CONTROL, SecurityControlPacket::new);
        PACKET_HANDLER.registerPacket(PacketIDs.PACKET_REDSTONE_CONTROL, RedstoneControlPacket::new);
        PACKET_HANDLER.registerPacket(PacketIDs.PACKET_TRANSFER_CONTROL, TransferControlPacket::new);
        PACKET_HANDLER.registerPacket(PacketIDs.PACKET_SIDE_CONFIG, SideConfigPacket::new);
        PACKET_HANDLER.registerPacket(PacketIDs.PACKET_STORAGE_CLEAR, StorageClearPacket::new);
        PACKET_HANDLER.registerPacket(PacketIDs.PACKET_CLAIM_XP, ClaimXPPacket::new);

        PACKET_HANDLER.registerPacket(PacketIDs.PACKET_ITEM_MODE_CHANGE, ItemModeChangePacket::new);
        PACKET_HANDLER.registerPacket(PacketIDs.PACKET_ITEM_LEFT_CLICK, ItemLeftClickPacket::new);

        PACKET_HANDLER.registerPacket(PacketIDs.PACKET_EFFECT_ADD, EffectAddedPacket::new);
        PACKET_HANDLER.registerPacket(PacketIDs.PACKET_EFFECT_REMOVE, EffectRemovedPacket::new);
    }

    // region INITIALIZATION
    private void registerLootData(final RegistryEvent.Register<GlobalLootModifierSerializer<?>> event) {

        CoreFlags.manager().setup();
        QuarkFlags.setup();
    }

    private void entityLayerSetup(final EntityRenderersEvent.RegisterLayerDefinitions event) {

        event.registerLayerDefinition(ARMOR_FULL_SUIT_LAYER, ArmorFullSuitModel::createBodyLayer);
    }

    private void entityRendererSetup(final EntityRenderersEvent.RegisterRenderers event) {

        event.registerEntityRenderer(ELECTRIC_ARC_ENTITY, ElectricArcRenderer::new);
        event.registerEntityRenderer(ELECTRIC_FIELD_ENTITY, NothingRenderer::new);

        event.registerEntityRenderer(KNIFE_ENTITY, KnifeRenderer::new);
    }

    private void capSetup(RegisterCapabilitiesEvent event) {

        CapabilityArchery.register(event);
        CapabilityAreaEffect.register(event);
        CapabilityEnchantableItem.register(event);
        CapabilityShieldItem.register(event);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {

        event.enqueueWork(TileNBTSync::setup);
        event.enqueueWork(ArmorEvents::setup);
        event.enqueueWork(FluidHelper::setup);
    }

    private void clientSetup(final FMLClientSetupEvent event) {

        event.enqueueWork(() -> {
            MenuScreens.register(FLUID_FILTER_CONTAINER, FluidFilterScreen::new);
            MenuScreens.register(ITEM_FILTER_CONTAINER, ItemFilterScreen::new);
        });
        event.enqueueWork(CoreKeys::register);
        event.enqueueWork(ProxyClient::registerItemModelProperties);
    }

    private void registerCommands(final RegisterCommandsEvent event) {

        CoFHCommand.initialize(event.getDispatcher());
    }
    // endregion
}
