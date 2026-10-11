package com.hbm.render.loader;

import java.util.ArrayList;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class S_GroupObject {

	public String name;
	public ArrayList<S_Face> faces = new ArrayList<S_Face>();
	public int glDrawingMode;

	public S_GroupObject() {
		this("");
	}

	public S_GroupObject(String name) {
		this(name, -1);
	}

	public S_GroupObject(String name, int glDrawingMode) {
		this.name = name;
		this.glDrawingMode = glDrawingMode;
	}

	public void render() {
		render(0.0F);
	}

	public void render(float vAnim) {

		if(this.faces.size() <= 0 || this.glDrawingMode < 0) return;

		BufferBuilder buffer = Tessellator.getInstance().getBuffer();
		buffer.begin(this.glDrawingMode, DefaultVertexFormats.POSITION_TEX_NORMAL);
		tessellate(buffer, vAnim);
		Tessellator.getInstance().draw();
	}

	public void tessellate(BufferBuilder buffer) {
		tessellate(buffer, 0.0F);
	}

	public void tessellate(BufferBuilder buffer, float vAnim) {

		for(int i = 0; i < this.faces.size(); i++) {
			this.faces.get(i).addFaceForRender(buffer, 0.0F, vAnim);
		}
	}
}
