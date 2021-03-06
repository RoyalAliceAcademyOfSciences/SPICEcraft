package simelectricity.essential.client.grid.pole;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import rikka.librikka.DirHorizontal8;
import simelectricity.essential.grid.TilePoleMetal35kV;

public class MetalPole35kVBottomTER extends TileEntityRenderer<TilePoleMetal35kV.Bottom>{
	public MetalPole35kVBottomTER(TileEntityRendererDispatcher rendererDispatcherIn) {
		super(rendererDispatcherIn);
	}

	@Override
	public void render(TilePoleMetal35kV.Bottom te, float partialTicks, MatrixStack matrixStack,
			IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn) {
		DirHorizontal8 facing8 = te.facing();
		int facing = (8-facing8.ordinal()) & 7;
		int part = te.getPartId();	

		BlockState blockState = te.getBlockState();
		if (blockState == null)
			return;
		IBakedModel bakedmodel = Minecraft.getInstance().getModelManager()
				.getBlockModelShapes().getModel(blockState);
		if (!(bakedmodel instanceof MetalPole35kVModel))
			return;
		MetalPole35kVModel model = (MetalPole35kVModel) bakedmodel;
		
        IVertexBuilder buffer = bufferIn.getBuffer(RenderType.getSolid());

        int i = 15728640;
		for (BakedQuad quad: model.bakedModelBasePart[facing][part]) {
			matrixStack.push();
			buffer.addQuad(matrixStack.getLast(), quad, 1, 1, 1, i, OverlayTexture.NO_OVERLAY);
			matrixStack.pop();
		}
	}

}
