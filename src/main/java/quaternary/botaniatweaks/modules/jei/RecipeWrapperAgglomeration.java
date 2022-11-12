package quaternary.botaniatweaks.modules.jei;

import com.google.common.collect.ImmutableList;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.oredict.OreDictionary;
import org.apache.commons.lang3.tuple.Pair;
import quaternary.botaniatweaks.modules.botania.recipe.AgglomerationRecipe;
import quaternary.botaniatweaks.modules.shared.helper.MiscHelpers;
import vazkii.botania.client.core.handler.HUDHandler;
import vazkii.botania.common.block.tile.mana.TilePool;

import javax.annotation.Nullable;
import java.util.List;

public class RecipeWrapperAgglomeration implements IRecipeWrapper {
	AgglomerationRecipe recipe;
	
	List<List<ItemStack>> inputs;
	List<ItemStack> outputs;
	List<FluidStack> fluidInputs;
	List<FluidStack> fluidOutputs;
	
	Pair<ItemStack, FluidStack> multiblockCenterStack;
	Pair<ItemStack, FluidStack> multiblockEdgeStack;
	Pair<ItemStack, FluidStack> multiblockCornerStack;
	
	//yeah i know it's stupid
	
	@Nullable
	Pair<ItemStack, FluidStack> multiblockReplaceCenterStack;
	@Nullable
	Pair<ItemStack, FluidStack> multiblockReplaceEdgeStack;
	@Nullable
	Pair<ItemStack, FluidStack> multiblockReplaceCornerStack;
	
	int manaCost;
	
	public RecipeWrapperAgglomeration(AgglomerationRecipe recipe) {
		this.recipe = recipe;
		ImmutableList.Builder<List<ItemStack>> inputs_ = ImmutableList.builder();
		ImmutableList.Builder<FluidStack> fluidInputs_ = ImmutableList.builder();
		
		//Itemstack inputs
		for(ItemStack stack : recipe.getRecipeStacks()) {
			inputs_.add(ImmutableList.of(stack));
		}
		
		//Ore key inputs
		for(String key : recipe.getRecipeOreKeys()) {
			inputs_.add(ImmutableList.copyOf(OreDictionary.getOres(key)));
		}
		
		//The three multiblock pieces
		toInput(inputs_, fluidInputs_, recipe.multiblockCenter);
		toInput(inputs_, fluidInputs_, recipe.multiblockEdge);
		toInput(inputs_, fluidInputs_, recipe.multiblockCorner);
		multiblockCenterStack = MiscHelpers.stackFromStateOrFluid(recipe.multiblockCenter);
		multiblockEdgeStack = MiscHelpers.stackFromStateOrFluid(recipe.multiblockEdge);
		multiblockCornerStack = MiscHelpers.stackFromStateOrFluid(recipe.multiblockCorner);
		
		ImmutableList.Builder<ItemStack> outputs_ = ImmutableList.builder();
		ImmutableList.Builder<FluidStack> fluidOutputs_ = ImmutableList.builder();
		
		//Recipe output
		outputs_.add(recipe.getRecipeOutputCopy());
		
		//The multiblock replacements
		if(recipe.multiblockCenterReplace != null) {
			multiblockReplaceCenterStack = MiscHelpers.stackFromStateOrFluid(recipe.multiblockCenterReplace);
		}
		
		if(recipe.multiblockEdgeReplace != null) {
			multiblockReplaceEdgeStack = MiscHelpers.stackFromStateOrFluid(recipe.multiblockEdgeReplace);
		}
		
		if(recipe.multiblockCornerReplace != null) {
			multiblockReplaceCornerStack = MiscHelpers.stackFromStateOrFluid(recipe.multiblockCornerReplace);
		}
		toOutput(outputs_, fluidOutputs_, recipe.multiblockCenterReplace);
		toOutput(outputs_, fluidOutputs_, recipe.multiblockEdgeReplace);
		toOutput(outputs_, fluidOutputs_, recipe.multiblockCornerReplace);
		
		inputs = inputs_.build();
		outputs = outputs_.build();
		fluidInputs = fluidInputs_.build();
		fluidOutputs = fluidOutputs_.build();
		
		manaCost = recipe.manaCost;
	}
	
	@Override
	public void getIngredients(IIngredients ing) {
		ing.setInputLists(VanillaTypes.ITEM, inputs);
		ing.setOutputs(VanillaTypes.ITEM, outputs);
		ing.setInputs(VanillaTypes.FLUID, fluidInputs);
		ing.setOutputs(VanillaTypes.FLUID, fluidOutputs);
	}
	
	@Override
	public void drawInfo(Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
		GlStateManager.enableAlpha();
		HUDHandler.renderManaBar(35, 60, 0x0000FF, 0.75f, manaCost, TilePool.MAX_MANA);
		
		if(manaCost > 1_000_000) {
			int roughPoolCount = (250_000 * Math.round(manaCost / 250_000f)) / 1_000_000;
			Minecraft.getMinecraft().fontRenderer.drawString("x" + roughPoolCount, 140, 58, 0x000000);
		}
		
		GlStateManager.disableAlpha();
	}

	private void toInput(ImmutableList.Builder<List<ItemStack>> itemBuilder, ImmutableList.Builder<FluidStack> fluidBuilder, @Nullable IBlockState block) {
		if (block == null) return;
		Pair<ItemStack, FluidStack> result = MiscHelpers.stackFromStateOrFluid(block);
		if (result.getRight() != null) {
			fluidBuilder.add(result.getRight());
		} else {
			itemBuilder.add(ImmutableList.of(result.getLeft()));
		}
	}

	private void toOutput(ImmutableList.Builder<ItemStack> itemBuilder, ImmutableList.Builder<FluidStack> fluidBuilder, @Nullable IBlockState block) {
		if (block == null) return;
		Pair<ItemStack, FluidStack> result = MiscHelpers.stackFromStateOrFluid(block);
		if (result.getRight() != null) {
			fluidBuilder.add(result.getRight());
		} else if (!result.getLeft().isEmpty()) {
			itemBuilder.add(result.getLeft());
		}
	}
}
