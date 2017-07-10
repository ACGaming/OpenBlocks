package openblocks.client.gui;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import openblocks.common.LiquidXpUtils;
import openblocks.common.container.ContainerAutoEnchantmentTable;
import openblocks.common.tileentity.TileEntityAutoEnchantmentTable;
import openblocks.common.tileentity.TileEntityAutoEnchantmentTable.AutoSlots;
import openblocks.rpc.ILevelChanger;
import openmods.api.IValueReceiver;
import openmods.gui.GuiConfigurableSlots;
import openmods.gui.Icon;
import openmods.gui.component.BaseComponent;
import openmods.gui.component.BaseComposite;
import openmods.gui.component.GuiComponentLabel;
import openmods.gui.component.GuiComponentSlider;
import openmods.gui.component.GuiComponentTab;
import openmods.gui.component.GuiComponentTankLevel;
import openmods.gui.component.GuiComponentToggleButton;
import openmods.gui.listener.IMouseDownListener;
import openmods.gui.listener.IValueChangedListener;
import openmods.gui.logic.ValueCopyAction;
import openmods.utils.EnchantmentUtils;
import openmods.utils.MiscUtils;
import openmods.utils.TranslationUtils;
import openmods.utils.VanillaEnchantLogic;
import openmods.utils.VanillaEnchantLogic.Level;

public class GuiAutoEnchantmentTable extends GuiConfigurableSlots<TileEntityAutoEnchantmentTable, ContainerAutoEnchantmentTable, TileEntityAutoEnchantmentTable.AutoSlots> {

	public GuiAutoEnchantmentTable(ContainerAutoEnchantmentTable container) {
		super(container, 176, 175, "openblocks.gui.autoenchantmenttable");
	}

	@Override
	protected Iterable<AutoSlots> getSlots() {
		return ImmutableList.of(AutoSlots.toolInput, AutoSlots.lapisInput, AutoSlots.xp, AutoSlots.output);
	}

	private static final ResourceLocation VANILLA_TEXTURE = new ResourceLocation("textures/gui/container/enchanting_table.png");

	private static final Map<VanillaEnchantLogic.Level, Icon> icons = ImmutableMap.of(
			VanillaEnchantLogic.Level.L1, Icon.createSheetIcon(VANILLA_TEXTURE, 16 * 0, 223, 16, 16),
			VanillaEnchantLogic.Level.L2, Icon.createSheetIcon(VANILLA_TEXTURE, 16 * 1, 223, 16, 16),
			VanillaEnchantLogic.Level.L3, Icon.createSheetIcon(VANILLA_TEXTURE, 16 * 2, 223, 16, 16));

	@Override
	protected void addCustomizations(BaseComposite root) {
		final TileEntityAutoEnchantmentTable te = getContainer().getOwner();

		final ILevelChanger rpc = te.createClientRpcProxy(ILevelChanger.class);

		final GuiComponentSlider slider = new GuiComponentSlider(44, 39, 45, 1, 30, 1, true, TranslationUtils.translateToLocal("openblocks.gui.limit"));
		slider.setListener(new IValueChangedListener<Double>() {
			@Override
			public void valueChanged(Double value) {
				rpc.changePowerLimit(value.intValue());
			}
		});

		addSyncUpdateListener(ValueCopyAction.create(te.getLevelProvider(), slider, new Function<Integer, Double>() {
			@Override
			public Double apply(Integer input) {
				return input.doubleValue();
			}
		}));

		root.addComponent(slider);

		final GuiComponentLabel maxPower = new GuiComponentLabel(40, 25, "0");
		maxPower.setMaxWidth(100);
		addSyncUpdateListener(ValueCopyAction.create(te.getAvailablePowerProvider(), new IValueReceiver<Integer>() {
			@Override
			public void setValue(Integer value) {
				maxPower.setText(TranslationUtils.translateToLocalFormatted("openblocks.gui.available_power", value));
			}
		}));
		root.addComponent(maxPower);

		final GuiComponentTankLevel tankLevel = new GuiComponentTankLevel(140, 30, 17, 37, TileEntityAutoEnchantmentTable.MAX_STORED_LEVELS);
		addSyncUpdateListener(ValueCopyAction.create(te.getFluidProvider(), tankLevel.fluidReceiver(), new Function<FluidStack, FluidStack>() {
			@Override
			public FluidStack apply(FluidStack input) {
				if (input == null) return null;
				// display levels instead of actual xp fluid level
				final FluidStack result = input.copy();
				result.amount = EnchantmentUtils.getLevelForExperience(LiquidXpUtils.liquidToXpRatio(input.amount));
				return result;
			}
		}));
		root.addComponent(tankLevel);

		final GuiComponentToggleButton<VanillaEnchantLogic.Level> levelSelect = new GuiComponentToggleButton<VanillaEnchantLogic.Level>(16, 60, 0xFFFFFF, icons);
		levelSelect.setListener(new IMouseDownListener() {
			@Override
			public void componentMouseDown(BaseComponent component, int x, int y, int button) {
				final VanillaEnchantLogic.Level currentValue = te.getSelectedLevelProvider().getValue();
				final Level[] values = VanillaEnchantLogic.Level.values();
				final VanillaEnchantLogic.Level nextValue = values[(currentValue.ordinal() + 1) % values.length];
				rpc.changeLevel(nextValue);
			}
		});
		addSyncUpdateListener(ValueCopyAction.create(te.getSelectedLevelProvider(), levelSelect));
		root.addComponent(levelSelect);
	}

	@Override
	protected GuiComponentTab createTab(AutoSlots slot) {
		switch (slot) {
			case toolInput:
				return new GuiComponentTab(StandardPalette.blue.getColor(), new ItemStack(Items.DIAMOND_PICKAXE, 1), 100, 100);
			case lapisInput:
				return new GuiComponentTab(StandardPalette.blue.getColor(), new ItemStack(Items.DYE, 1, 4), 100, 100);
			case output: {
				ItemStack enchantedAxe = new ItemStack(Items.DIAMOND_PICKAXE, 1);
				enchantedAxe.addEnchantment(Enchantments.FORTUNE, 1);
				return new GuiComponentTab(StandardPalette.lightblue.getColor(), enchantedAxe, 100, 100);
			}
			case xp:
				return new GuiComponentTab(StandardPalette.green.getColor(), new ItemStack(Items.BUCKET, 1), 100, 100);
			default:
				throw MiscUtils.unhandledEnum(slot);
		}
	}

	@Override
	protected GuiComponentLabel createLabel(AutoSlots slot) {
		switch (slot) {
			case lapisInput:
			case toolInput:
				return new GuiComponentLabel(22, 82, TranslationUtils.translateToLocal("openblocks.gui.autoextract"));
			case output:
				return new GuiComponentLabel(22, 82, TranslationUtils.translateToLocal("openblocks.gui.autoeject"));
			case xp:
				return new GuiComponentLabel(22, 82, TranslationUtils.translateToLocal("openblocks.gui.autodrink"));
			default:
				throw MiscUtils.unhandledEnum(slot);

		}
	}

}
