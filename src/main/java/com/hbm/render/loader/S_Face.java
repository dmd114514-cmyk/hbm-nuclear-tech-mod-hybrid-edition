package com.hbm.render.loader;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class S_Face {

	public int[] verticesID;
	public Vertex[] vertices;
	public Vertex[] vertexNormals;
	public Vertex faceNormal;
	public TextureCoordinate[] textureCoordinates;
	private boolean smoothing;

	public S_Face(boolean smoothing) {
		this.smoothing = smoothing;
	}

	public void addFaceForRender(BufferBuilder buffer) {
		addFaceForRender(buffer, 0.0F, 0.0F);
	}

	public void addFaceForRender(BufferBuilder buffer, float textureOffset) {
		addFaceForRender(buffer, textureOffset, 0.0F);
	}

	public void addFaceForRender(BufferBuilder buffer, float textureOffset, float vAnim) {

		if(this.faceNormal == null) {
			this.faceNormal = calculateFaceNormal();
		}

		boolean uvs = hasUVs();

		float averageU = 0F;
		float averageV = 0F;

		if(uvs) {
			for(int i = 0; i < this.textureCoordinates.length; i++) {
				averageU += this.textureCoordinates[i].u;
				averageV += this.textureCoordinates[i].v;
			}

			averageU /= this.textureCoordinates.length;
			averageV /= this.textureCoordinates.length;
		}

		for(int i = 0; i < this.vertices.length; i++) {

			// normals are a per-vertex attribute in 1.12.2 and must be written per vertex; smooth faces take vertex normals, hard edges take the face normal
			Vertex normal = this.faceNormal;
			if(this.smoothing && this.vertexNormals != null && i < this.vertexNormals.length && this.vertexNormals[i] != null) {
				normal = this.vertexNormals[i];
			}

			float u = 0F;
			float v = 0F;

			if(uvs) {
				// half-pixel compensation at texture seams, the old algorithm unchanged
				float offsetU = textureOffset;
				float offsetV = textureOffset;

				if(this.textureCoordinates[i].u > averageU) {
					offsetU = -offsetU;
				}
				if(this.textureCoordinates[i].v > averageV) {
					offsetV = -offsetV;
				}

				u = this.textureCoordinates[i].u + offsetU;
				v = this.textureCoordinates[i].v + offsetV + vAnim;
			}

			buffer.pos(this.vertices[i].x, this.vertices[i].y, this.vertices[i].z).tex(u, v).normal(normal.x, normal.y, normal.z).endVertex();
		}
	}

	public boolean hasUVs() {
		return this.textureCoordinates != null && this.textureCoordinates.length > 0;
	}

	public Vertex calculateFaceNormal() {

		Vec3d v1 = new Vec3d(vertices[1].x - vertices[0].x, vertices[1].y - vertices[0].y, vertices[1].z - vertices[0].z);
		Vec3d v2 = new Vec3d(vertices[2].x - vertices[0].x, vertices[2].y - vertices[0].y, vertices[2].z - vertices[0].z);
		Vec3d normalVector = v1.crossProduct(v2).normalize();

		return new Vertex((float) normalVector.x, (float) normalVector.y, (float) normalVector.z);
	}
}
