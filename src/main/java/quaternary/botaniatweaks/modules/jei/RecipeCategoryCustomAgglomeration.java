package quaternary.botaniatweaks.modules.jei;

import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiFluidStackGroup;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeCategory;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fluids.FluidStack;
import org.apache.commons.lang3.tuple.Pair;
import quaternary.botaniatweaks.BotaniaTweaks;
import quaternary.botaniatweaks.modules.shared.helper.ModCompatUtil;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class RecipeCategoryCustomAgglomeration implements IRecipeCategory<RecipeWrapperAgglomeration> {
	
	public static final String UID = "botaniatweaks.agglomeration";
	public static final Pair<ItemStack, FluidStack> EMPTY_STACK = Pair.of(ItemStack.EMPTY, null);

	static final int WIDTH = 170;
	static final int HEIGHT = 130;
	
	final String localizedName;
	final IDrawable background;
	
	public RecipeCategoryCustomAgglomeration(IGuiHelper guiHelper) {
		localizedName = I18n.format("botania_tweaks.jei.agglomeration.category");
		background = guiHelper.createDrawable(new ResourceLocation(BotaniaTweaks.MODID, "textures/ui/terrasteeloverlay.png"), 0, 0, WIDTH, HEIGHT);
	}
	
	@Override
	public String getUid() {
		return UID;
	}
	
	@Override
	public String getTitle() {
		return localizedName;
	}
	
	@Override
	public String getModName() {
		return BotaniaTweaks.NAME;
	}
	
	@Override
	public IDrawable getBackground() {
		return background;
	}
	
	static final int ITEM_WIDTH = 16;
	static final int ITEM_HEIGHT = 16;
	static final int ITEM_BUFFER = 4;
	
	@Override
	public void setRecipe(IRecipeLayout layout, RecipeWrapperAgglomeration wrapper, IIngredients ings) {
		IGuiItemStackGroup stacks = layout.getItemStacks();
		IGuiFluidStackGroup fluidStacks = layout.getFluidStacks();
		List<List<ItemStack>> ins = ings.getInputs(VanillaTypes.ITEM);
		List<List<ItemStack>> itemInputs = ins.subList(0, ins.size() - 3);
		List<List<ItemStack>> outs = ings.getOutputs(VanillaTypes.ITEM);
		Pair<AtomicInteger, AtomicInteger> indexes = Pair.of(new AtomicInteger(), new AtomicInteger());
		
		//Set centered row of inputs
		
		int inputCount = itemInputs.size();
		
		int totalInputWidth = ITEM_WIDTH * inputCount + (ITEM_BUFFER * (inputCount - 1));
		
		int posX = WIDTH / 2 - totalInputWidth / 2;
		int posY = 0;
		for(List<ItemStack> in : itemInputs) {
			stacks.init(getItemIndex(indexes), true, posX, posY);
			stacks.set(getItemIndex(indexes), in);
			increaseItemIndex(indexes);
			posX += ITEM_WIDTH + ITEM_BUFFER;
		}
		
		posY += ITEM_HEIGHT * 2 + ITEM_BUFFER;
		
		//Set output item
		List<ItemStack> outItem = outs.get(0);
		stacks.init(getItemIndex(indexes), false, WIDTH / 2 - ITEM_WIDTH / 2, posY);
		stacks.set(getItemIndex(indexes), outItem);
		increaseItemIndex(indexes);
		
		posY += ITEM_HEIGHT * 4.5 - 5;
		
		//Set multiblock under plate
		
		Pair<ItemStack, FluidStack> centerReplace = wrapper.multiblockReplaceCenterStack;
		Pair<ItemStack, FluidStack> edgeReplace = wrapper.multiblockReplaceEdgeStack;
		Pair<ItemStack, FluidStack> cornerReplace = wrapper.multiblockReplaceCornerStack;
		
		boolean isCenterReplaced = true;
		boolean isEdgeReplaced = true;
		boolean isCornerReplaced = true;
		
		if(centerReplace == null) {
			isCenterReplaced = false;
			centerReplace = EMPTY_STACK;
		}
		
		if(edgeReplace == null) {
			isEdgeReplaced = false;
			edgeReplace = EMPTY_STACK;
		}
		
		if(cornerReplace == null) {
			isCornerReplaced = false;
			cornerReplace = EMPTY_STACK;
		}
		
		if(!isCenterReplaced && !isEdgeReplaced && !isCornerReplaced) {
			setMultiblock(indexes, stacks, fluidStacks, wrapper.multiblockCenterStack, wrapper.multiblockEdgeStack, wrapper.multiblockCornerStack, WIDTH / 2, posY, false, false, false);
		} else {
			setMultiblock(indexes, stacks, fluidStacks, wrapper.multiblockCenterStack, wrapper.multiblockEdgeStack, wrapper.multiblockCornerStack, WIDTH / 2 - ITEM_WIDTH * 3 - 1, posY, false, false, false);
			
			Pair<ItemStack, FluidStack> drawCenter = isCenterReplaced ? centerReplace : wrapper.multiblockCenterStack;
			Pair<ItemStack, FluidStack> drawEdge = isEdgeReplaced ? edgeReplace : wrapper.multiblockEdgeStack;
			Pair<ItemStack, FluidStack> drawCorner = isCornerReplaced ? cornerReplace : wrapper.multiblockCornerStack;
			
			setMultiblock(indexes, stacks, fluidStacks, drawCenter, drawEdge, drawCorner, WIDTH / 2 + ITEM_WIDTH * 3 - 1, posY, isCenterReplaced, isEdgeReplaced, isCornerReplaced);
		}
	}

	static int getItemIndex(Pair<AtomicInteger, AtomicInteger> indexes) {
		return indexes.getLeft().get();
	}

	static int getFluidIndex(Pair<AtomicInteger, AtomicInteger> indexes) {
		return indexes.getRight().get();
	}

	static void increaseItemIndex(Pair<AtomicInteger, AtomicInteger> indexes) {
		indexes.getLeft().incrementAndGet();
	}

	static void increaseFluidIndex(Pair<AtomicInteger, AtomicInteger> indexes) {
		indexes.getRight().incrementAndGet();
	}
	
	void setMultiblock(Pair<AtomicInteger, AtomicInteger> indexes, IGuiItemStackGroup stacks, IGuiFluidStackGroup fluidStacks, Pair<ItemStack, FluidStack> center, Pair<ItemStack, FluidStack> edges, Pair<ItemStack, FluidStack> corners, int posX, int posY, boolean centerOutput, boolean edgeOutput, boolean cornerOutput) {
		stacks.init(getItemIndex(indexes), false, posX - ITEM_WIDTH / 2, posY - MathHelper.floor(ITEM_HEIGHT * 2.5));
		stacks.set(getItemIndex(indexes), ModCompatUtil.getStackFor(new ResourceLocation("botania", "terraplate")));
		increaseItemIndex(indexes);

		setElement(indexes, stacks, fluidStacks, center, posX - ITEM_WIDTH / 2, posY - ITEM_HEIGHT / 2, centerOutput);
		setElement(indexes, stacks, fluidStacks, edges, posX - MathHelper.floor(ITEM_WIDTH * 1.5), posY - ITEM_HEIGHT, edgeOutput);
		setElement(indexes, stacks, fluidStacks, edges, posX + ITEM_WIDTH / 2, posY - ITEM_HEIGHT, edgeOutput);
		setElement(indexes, stacks, fluidStacks, edges, posX - MathHelper.floor(ITEM_WIDTH * 1.5), posY, edgeOutput);
		setElement(indexes, stacks, fluidStacks, edges, posX + ITEM_WIDTH / 2, posY, edgeOutput);
		setElement(indexes, stacks, fluidStacks, corners, posX - MathHelper.floor(ITEM_WIDTH * 2.5), posY - ITEM_HEIGHT / 2, cornerOutput);
		setElement(indexes, stacks, fluidStacks, corners, posX + MathHelper.floor(ITEM_WIDTH * 1.5), posY - ITEM_HEIGHT / 2, cornerOutput);
		setElement(indexes, stacks, fluidStacks, corners, posX - ITEM_WIDTH / 2, posY - MathHelper.floor(ITEM_HEIGHT * 1.5), cornerOutput);
		setElement(indexes, stacks, fluidStacks, corners, posX - ITEM_WIDTH / 2, posY + ITEM_HEIGHT / 2, cornerOutput);
	}

	void setElement(Pair<AtomicInteger, AtomicInteger> indexes, IGuiItemStackGroup stacks, IGuiFluidStackGroup fluidStacks, Pair<ItemStack, FluidStack> element, int x, int y, boolean output) {
		if (!element.equals(EMPTY_STACK)) {
			FluidStack fluid = element.getRight();
			ItemStack item = element.getLeft();
			if (fluid != null) {
				fluidStacks.init(getFluidIndex(indexes), output, x, y);
				fluidStacks.set(getFluidIndex(indexes), fluid);
				increaseFluidIndex(indexes);
			} else if (!item.isEmpty()) {
				stacks.init(getItemIndex(indexes), output, x, y);
				stacks.set(getItemIndex(indexes), item);
				increaseItemIndex(indexes);
			}
		}
	}
}
