package simelectricity.essential.grid.render;

import org.lwjgl.opengl.GL11;

import simelectricity.essential.grid.ISETransmissionTower;
import net.minecraft.tileentity.TileEntity;

public class TileRenderTransmissionTower extends TileRenderTranmissionTowerBase{
    @Override
    public void renderTileEntityAt(TileEntity tileEntity, double x, double y, double z, float f) {
    	ISETransmissionTower tw = ((ISETransmissionTower) tileEntity);
    	super.renderTileEntityAt(tileEntity, x, y, z, f);
    	
    	int meta = tileEntity.getBlockMetadata();
    	
    	if ((meta&8) == 0 && render1 && render2){       
        	double[] 	from = new double[9], to = new double[9];
    		
    		for (int i=0; i<9; i++){
    			from[i] = fixedfrom1[i];
    			to[i] = fixedfrom2[i];
    		}
    		
    		TransmissionTowerRenderHelper.swapIfIntersect(from,to);
            GL11.glPushMatrix();
            GL11.glTranslated(x-tileEntity.xCoord, y-tileEntity.yCoord, z-tileEntity.zCoord);
            GL11.glTranslated(from[0], from[1],from[2]);
            CableGLRender.renderParabolicCable(from[0], from[1],from[2], to[0], to[1],to[2], 0.075, 2, this, 1);
            GL11.glPopMatrix();
            
            GL11.glPushMatrix();
            GL11.glTranslated(x-tileEntity.xCoord, y-tileEntity.yCoord, z-tileEntity.zCoord);
            GL11.glTranslated(from[6], from[7],from[8]);
            CableGLRender.renderParabolicCable(from[6], from[7],from[8], to[6], to[7],to[8], 0.0755, 2, this, 1);
            GL11.glPopMatrix();
            
            
            GL11.glPushMatrix();
            GL11.glTranslated(x-tileEntity.xCoord, y-tileEntity.yCoord, z-tileEntity.zCoord);
            GL11.glTranslated(fixedfrom1[3], fixedfrom1[4],fixedfrom1[5]);
            CableGLRender.renderParabolicCable(fixedfrom1[3], fixedfrom1[4],fixedfrom1[5],
            		3.95 * Math.sin(rotation/180*Math.PI) + 0.5F + tileEntity.xCoord,
            		tileEntity.yCoord + 23 -18,
            		3.95 * Math.cos(rotation/180*Math.PI) + 0.5F + tileEntity.zCoord,
            		0.075, 2, this, 1);
            GL11.glPopMatrix();

            GL11.glPushMatrix();
            GL11.glTranslated(x-tileEntity.xCoord, y-tileEntity.yCoord, z-tileEntity.zCoord);
            GL11.glTranslated(fixedfrom2[3], fixedfrom2[4],fixedfrom2[5]);
            CableGLRender.renderParabolicCable(fixedfrom2[3], fixedfrom2[4],fixedfrom2[5],
            		3.95 * Math.sin(rotation/180*Math.PI) + 0.5F + tileEntity.xCoord,
            		tileEntity.yCoord + 23 -18,
            		3.95 * Math.cos(rotation/180*Math.PI) + 0.5F + tileEntity.zCoord,
            		0.075, 2, this, 1);
            GL11.glPopMatrix();
    	}
    }
}
