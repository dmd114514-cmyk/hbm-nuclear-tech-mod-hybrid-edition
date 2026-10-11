package com.hbm.render.loader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.lwjgl.opengl.GL11;

import com.hbm.lib.RefStrings;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.resources.IResource;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Home-grown OBJ parsing and immediate-mode rendering. 1.12.2's Tessellator no longer has a global buffered singleton,
 * so rendering moved to BufferBuilder and got rewritten wholesale; parsing and the data format are untouched.
 */
@SideOnly(Side.CLIENT)
public class HFRWavefrontObject implements IModelCustomNamed {

	/** Re-parses per this list on resource reload */
	public static LinkedHashSet<HFRWavefrontObject> allModels = new LinkedHashSet<HFRWavefrontObject>();

	private static Pattern vertexPattern = Pattern.compile("(v( (\\-){0,1}\\d+(\\.\\d+)?){3,4} *\\n)|(v( (\\-){0,1}\\d+(\\.\\d+)?){3,4} *$)");
	private static Pattern vertexNormalPattern = Pattern.compile("(vn( (\\-){0,1}\\d+(\\.\\d+)?){3,4} *\\n)|(vn( (\\-){0,1}\\d+(\\.\\d+)?){3,4} *$)");
	private static Pattern textureCoordinatePattern = Pattern.compile("(vt( (\\-){0,1}\\d+\\.\\d+){2,3} *\\n)|(vt( (\\-){0,1}\\d+(\\.\\d+)?){2,3} *$)");
	private static Pattern face_V_VT_VN_Pattern = Pattern.compile("(f( \\d+/\\d+/\\d+){3,4} *\\n)|(f( \\d+/\\d+/\\d+){3,4} *$)");
	private static Pattern face_V_VT_Pattern = Pattern.compile("(f( \\d+/\\d+){3,4} *\\n)|(f( \\d+/\\d+){3,4} *$)");
	private static Pattern face_V_VN_Pattern = Pattern.compile("(f( \\d+//\\d+){3,4} *\\n)|(f( \\d+//\\d+){3,4} *$)");
	private static Pattern face_V_Pattern = Pattern.compile("(f( \\d+){3,4} *\\n)|(f( \\d+){3,4} *$)");
	private static Pattern groupObjectPattern = Pattern.compile("([go]( [\\w\\d\\.]+) *\\n)|([go]( [\\w\\d\\.]+) *$)");

	private static Matcher vertexMatcher, vertexNormalMatcher, textureCoordinateMatcher;
	private static Matcher face_V_VT_VN_Matcher, face_V_VT_Matcher, face_V_VN_Matcher, face_V_Matcher;
	private static Matcher groupObjectMatcher;

	public ArrayList<Vertex> vertices = new ArrayList<Vertex>();
	public ArrayList<Vertex> vertexNormals = new ArrayList<Vertex>();
	public ArrayList<TextureCoordinate> textureCoordinates = new ArrayList<TextureCoordinate>();
	public ArrayList<S_GroupObject> groupObjects = new ArrayList<S_GroupObject>();
	private S_GroupObject currentGroupObject;
	public ResourceLocation resource;
	private String fileName;
	private boolean smoothing = true;
	private boolean allowMixedMode = false;

	public HFRWavefrontObject(String name) throws ModelFormatException {
		this(new ResourceLocation(RefStrings.MODID, name), false);
	}

	public HFRWavefrontObject(String name, boolean mixedMode) throws ModelFormatException {
		this(new ResourceLocation(RefStrings.MODID, name), mixedMode);
	}

	public HFRWavefrontObject noSmooth() {
		this.smoothing = false;
		return this;
	}

	/** Lets one model mix triangles and quads; such a model can't be rendered whole, only fed to hand-vertex code like ISBRH */
	public void mixedMode() {
		this.allowMixedMode = true;
	}

	public HFRWavefrontObject(ResourceLocation resource) throws ModelFormatException {
		this(resource, false);
	}

	// For subclasses constructing from arbitrary streams (the HMF scrolling-texture variant), bypassing the resource manager
	protected HFRWavefrontObject(String fileName, InputStream inputStream) throws ModelFormatException {
		this.fileName = fileName;
		loadObjModel(inputStream);
	}

	/** Texture V-scroll amount; plain OBJ is always 0, HMF subclasses override by time */
	protected float uvAnim() {
		return 0.0F;
	}

	public HFRWavefrontObject(ResourceLocation resource, boolean mixedMode) throws ModelFormatException {

		if(mixedMode) this.mixedMode();

		this.resource = resource;
		this.fileName = resource.toString();

		try {
			IResource res = Minecraft.getMinecraft().getResourceManager().getResource(resource);
			loadObjModel(res.getInputStream());
		} catch(IOException e) {
			throw new ModelFormatException("IO Exception reading model format", e);
		}

		allModels.add(this);
	}

	// Immediate mode for now: correctness first, GPU buffers come with the performance pass
	public IModelCustomNamed asVBO() {
		return this;
	}

	public void destroy() {
		vertices.clear();
		vertexNormals.clear();
		textureCoordinates.clear();
		groupObjects.clear();
		currentGroupObject = null;
	}

	public void loadObjModel(InputStream inputStream) throws ModelFormatException {

		BufferedReader reader = null;
		String currentLine = null;
		int lineCount = 0;

		try {
			reader = new BufferedReader(new InputStreamReader(inputStream));

			while((currentLine = reader.readLine()) != null) {
				lineCount++;
				currentLine = currentLine.replaceAll("\\s+", " ").trim();

				if(currentLine.startsWith("#") || currentLine.length() == 0) {
					continue;
				} else if(currentLine.startsWith("v ")) {
					Vertex vertex = parseVertex(currentLine, lineCount);
					if(vertex != null) vertices.add(vertex);
				} else if(currentLine.startsWith("vn ")) {
					Vertex vertex = parseVertexNormal(currentLine, lineCount);
					if(vertex != null) vertexNormals.add(vertex);
				} else if(currentLine.startsWith("vt ")) {
					TextureCoordinate textureCoordinate = parseTextureCoordinate(currentLine, lineCount);
					if(textureCoordinate != null) textureCoordinates.add(textureCoordinate);
				} else if(currentLine.startsWith("f ")) {

					if(currentGroupObject == null) {
						currentGroupObject = new S_GroupObject("Default");
					}

					S_Face face = parseFace(currentLine, lineCount);
					if(face != null) currentGroupObject.faces.add(face);

				} else if(currentLine.startsWith("g ") | currentLine.startsWith("o ")) {

					S_GroupObject group = parseGroupObject(currentLine, lineCount);
					if(group != null && currentGroupObject != null) groupObjects.add(currentGroupObject);
					currentGroupObject = group;
				}
			}

			groupObjects.add(currentGroupObject);

		} catch(IOException e) {
			throw new ModelFormatException("IO Exception reading model format", e);
		} finally {
			try {
				if(reader != null) reader.close();
			} catch(IOException e) {
				// failing to close the stream is nothing to worry about
			}
			try {
				inputStream.close();
			} catch(IOException e) {
				// same as above
			}
		}
	}

	@Override
	public List<String> getPartNames() {
		List<String> names = new ArrayList<String>();
		for(int i = 0; i < groupObjects.size(); i++) names.add(groupObjects.get(i).name);
		return names;
	}

	@Override
	public void renderAll() {
		if(allowMixedMode) throw new UnsupportedOperationException("Rendering of mixed-mode model " + this.fileName + " is not supported!");

		// batched by GL mode: groups sharing a mode share one draw call, no per-group open/close
		BufferBuilder buffer = Tessellator.getInstance().getBuffer();
		float anim = uvAnim();
		int mode = -1;

		for(int i = 0; i < groupObjects.size(); i++) {
			S_GroupObject group = groupObjects.get(i);

			if(group.glDrawingMode < 0 || group.faces.isEmpty()) continue;

			if(group.glDrawingMode != mode) {
				if(mode != -1) Tessellator.getInstance().draw();
				buffer.begin(group.glDrawingMode, FORMAT);
				mode = group.glDrawingMode;
			}

			group.tessellate(buffer, anim);
		}

		if(mode != -1) Tessellator.getInstance().draw();
	}

	// OBJ faces always carry uv (0-filled when missing); one vertex format globally, no per-group branching
	private static final VertexFormat FORMAT = net.minecraft.client.renderer.vertex.DefaultVertexFormats.POSITION_TEX_NORMAL;

	public void tessellateAll(BufferBuilder buffer) {
		float anim = uvAnim();
		for(int i = 0; i < groupObjects.size(); i++) groupObjects.get(i).tessellate(buffer, anim);
	}

	@Override
	public void renderOnly(String... groupNames) {
		if(allowMixedMode) throw new UnsupportedOperationException("Rendering of mixed-mode model " + this.fileName + " is not supported!");

		float anim = uvAnim();

		for(int i = 0; i < groupObjects.size(); i++) {
			S_GroupObject groupObject = groupObjects.get(i);
			for(int j = 0; j < groupNames.length; j++) {
				if(groupNames[j].equalsIgnoreCase(groupObject.name)) groupObject.render(anim);
			}
		}
	}

	public void tessellateOnly(BufferBuilder buffer, String... groupNames) {
		float anim = uvAnim();
		for(int i = 0; i < groupObjects.size(); i++) {
			S_GroupObject groupObject = groupObjects.get(i);
			for(int j = 0; j < groupNames.length; j++) {
				if(groupNames[j].equalsIgnoreCase(groupObject.name)) groupObject.tessellate(buffer, anim);
			}
		}
	}

	@Override
	public void renderPart(String partName) {
		if(allowMixedMode) throw new UnsupportedOperationException("Rendering of mixed-mode model " + this.fileName + " is not supported!");

		float anim = uvAnim();

		for(int i = 0; i < groupObjects.size(); i++) {
			S_GroupObject groupObject = groupObjects.get(i);
			if(partName.equalsIgnoreCase(groupObject.name)) groupObject.render(anim);
		}
	}

	public void tessellatePart(BufferBuilder buffer, String partName) {
		float anim = uvAnim();
		for(int i = 0; i < groupObjects.size(); i++) {
			S_GroupObject groupObject = groupObjects.get(i);
			if(partName.equalsIgnoreCase(groupObject.name)) groupObject.tessellate(buffer, anim);
		}
	}

	@Override
	public void renderAllExcept(String... excludedGroupNames) {
		if(allowMixedMode) throw new UnsupportedOperationException("Rendering of mixed-mode model " + this.fileName + " is not supported!");

		float anim = uvAnim();

		for(int i = 0; i < groupObjects.size(); i++) {
			S_GroupObject groupObject = groupObjects.get(i);
			boolean skipPart = false;
			for(int j = 0; j < excludedGroupNames.length; j++) {
				if(excludedGroupNames[j].equalsIgnoreCase(groupObject.name)) skipPart = true;
			}
			if(!skipPart) groupObject.render(anim);
		}
	}

	public void tessellateAllExcept(BufferBuilder buffer, String... excludedGroupNames) {
		float anim = uvAnim();
		for(int i = 0; i < groupObjects.size(); i++) {
			S_GroupObject groupObject = groupObjects.get(i);
			boolean exclude = false;
			for(int j = 0; j < excludedGroupNames.length; j++) {
				if(excludedGroupNames[j].equalsIgnoreCase(groupObject.name)) exclude = true;
			}
			if(!exclude) groupObject.tessellate(buffer, anim);
		}
	}

	private Vertex parseVertex(String line, int lineCount) throws ModelFormatException {

		if(isValidVertexLine(line)) {
			line = line.substring(line.indexOf(" ") + 1);
			String[] tokens = line.split(" ");

			try {
				if(tokens.length == 2) {
					return new Vertex(Float.parseFloat(tokens[0]), Float.parseFloat(tokens[1]));
				} else if(tokens.length == 3) {
					return new Vertex(Float.parseFloat(tokens[0]), Float.parseFloat(tokens[1]), Float.parseFloat(tokens[2]));
				}
			} catch(NumberFormatException e) {
				throw new ModelFormatException(String.format("Number formatting error at line %d", lineCount), e);
			}
		} else {
			throw new ModelFormatException("Error parsing entry ('" + line + "'" + ", line " + lineCount + ") in file '" + fileName + "' - Incorrect format");
		}

		return null;
	}

	private Vertex parseVertexNormal(String line, int lineCount) throws ModelFormatException {

		if(isValidVertexNormalLine(line)) {
			line = line.substring(line.indexOf(" ") + 1);
			String[] tokens = line.split(" ");

			try {
				if(tokens.length == 3)
					return new Vertex(Float.parseFloat(tokens[0]), Float.parseFloat(tokens[1]), Float.parseFloat(tokens[2]));
			} catch(NumberFormatException e) {
				throw new ModelFormatException(String.format("Number formatting error at line %d", lineCount), e);
			}
		} else {
			throw new ModelFormatException("Error parsing entry ('" + line + "'" + ", line " + lineCount + ") in file '" + fileName + "' - Incorrect format");
		}

		return null;
	}

	private TextureCoordinate parseTextureCoordinate(String line, int lineCount) throws ModelFormatException {

		if(isValidTextureCoordinateLine(line)) {
			line = line.substring(line.indexOf(" ") + 1);
			String[] tokens = line.split(" ");

			try {
				if(tokens.length == 2)
					return new TextureCoordinate(Float.parseFloat(tokens[0]), 1 - Float.parseFloat(tokens[1]));
				else if(tokens.length == 3)
					return new TextureCoordinate(Float.parseFloat(tokens[0]), 1 - Float.parseFloat(tokens[1]), Float.parseFloat(tokens[2]));
			} catch(NumberFormatException e) {
				throw new ModelFormatException(String.format("Number formatting error at line %d", lineCount), e);
			}
		} else {
			throw new ModelFormatException("Error parsing entry ('" + line + "'" + ", line " + lineCount + ") in file '" + fileName + "' - Incorrect format");
		}

		return null;
	}

	private S_Face parseFace(String line, int lineCount) throws ModelFormatException {

		if(!isValidFaceLine(line)) {
			throw new ModelFormatException("Error parsing entry ('" + line + "'" + ", line " + lineCount + ") in file '" + fileName + "' - Incorrect format");
		}

		S_Face face = new S_Face(this.smoothing);

		String trimmedLine = line.substring(line.indexOf(" ") + 1);
		String[] tokens = trimmedLine.split(" ");
		String[] subTokens = null;

		if(!this.allowMixedMode) {
			if(tokens.length == 3) {
				if(currentGroupObject.glDrawingMode == -1) {
					currentGroupObject.glDrawingMode = GL11.GL_TRIANGLES;
				} else if(currentGroupObject.glDrawingMode != GL11.GL_TRIANGLES) {
					throw new ModelFormatException("Error parsing entry ('" + line + "'" + ", line " + lineCount + ") in file '" + fileName
							+ "' - Invalid number of points for face (expected 4, found " + tokens.length + ")");
				}
			} else if(tokens.length == 4) {
				if(currentGroupObject.glDrawingMode == -1) {
					currentGroupObject.glDrawingMode = GL11.GL_QUADS;
				} else if(currentGroupObject.glDrawingMode != GL11.GL_QUADS) {
					throw new ModelFormatException("Error parsing entry ('" + line + "'" + ", line " + lineCount + ") in file '" + fileName
							+ "' - Invalid number of points for face (expected 3, found " + tokens.length + ")");
				}
			}
		}

		// f v1/vt1/vn1 v2/vt2/vn2 v3/vt3/vn3 ...
		if(isValidFace_V_VT_VN_Line(line)) {
			face.vertices = new Vertex[tokens.length];
			face.textureCoordinates = new TextureCoordinate[tokens.length];
			face.vertexNormals = new Vertex[tokens.length];

			for(int i = 0; i < tokens.length; ++i) {
				subTokens = tokens[i].split("/");
				face.vertices[i] = vertices.get(Integer.parseInt(subTokens[0]) - 1);
				face.textureCoordinates[i] = textureCoordinates.get(Integer.parseInt(subTokens[1]) - 1);
				face.vertexNormals[i] = vertexNormals.get(Integer.parseInt(subTokens[2]) - 1);
			}

			face.faceNormal = face.calculateFaceNormal();
		}
		// f v1/vt1 v2/vt2 v3/vt3 ...
		else if(isValidFace_V_VT_Line(line)) {
			face.vertices = new Vertex[tokens.length];
			face.textureCoordinates = new TextureCoordinate[tokens.length];

			for(int i = 0; i < tokens.length; ++i) {
				subTokens = tokens[i].split("/");
				face.vertices[i] = vertices.get(Integer.parseInt(subTokens[0]) - 1);
				face.textureCoordinates[i] = textureCoordinates.get(Integer.parseInt(subTokens[1]) - 1);
			}

			face.faceNormal = face.calculateFaceNormal();
		}
		// f v1//vn1 v2//vn2 v3//vn3 ...
		else if(isValidFace_V_VN_Line(line)) {
			face.vertices = new Vertex[tokens.length];
			face.vertexNormals = new Vertex[tokens.length];

			for(int i = 0; i < tokens.length; ++i) {
				subTokens = tokens[i].split("//");
				face.vertices[i] = vertices.get(Integer.parseInt(subTokens[0]) - 1);
				face.vertexNormals[i] = vertexNormals.get(Integer.parseInt(subTokens[1]) - 1);
			}

			face.faceNormal = face.calculateFaceNormal();
		}
		// f v1 v2 v3 ...
		else if(isValidFace_V_Line(line)) {
			face.vertices = new Vertex[tokens.length];

			for(int i = 0; i < tokens.length; ++i) {
				face.vertices[i] = vertices.get(Integer.parseInt(tokens[i]) - 1);
			}

			face.faceNormal = face.calculateFaceNormal();
		} else {
			throw new ModelFormatException("Error parsing entry ('" + line + "'" + ", line " + lineCount + ") in file '" + fileName + "' - Incorrect format");
		}

		return face;
	}

	private S_GroupObject parseGroupObject(String line, int lineCount) throws ModelFormatException {

		if(isValidGroupObjectLine(line)) {
			String trimmedLine = line.substring(line.indexOf(" ") + 1);
			if(trimmedLine.length() > 0) return new S_GroupObject(trimmedLine);
		} else {
			throw new ModelFormatException("Error parsing entry ('" + line + "'" + ", line " + lineCount + ") in file '" + fileName + "' - Incorrect format");
		}

		return null;
	}

	private static boolean isValidVertexLine(String line) {

		if(vertexMatcher != null) vertexMatcher.reset();
		vertexMatcher = vertexPattern.matcher(line);
		return vertexMatcher.matches();
	}

	private static boolean isValidVertexNormalLine(String line) {

		if(vertexNormalMatcher != null) vertexNormalMatcher.reset();
		vertexNormalMatcher = vertexNormalPattern.matcher(line);
		return vertexNormalMatcher.matches();
	}

	private static boolean isValidTextureCoordinateLine(String line) {

		if(textureCoordinateMatcher != null) textureCoordinateMatcher.reset();
		textureCoordinateMatcher = textureCoordinatePattern.matcher(line);
		return textureCoordinateMatcher.matches();
	}

	private static boolean isValidFace_V_VT_VN_Line(String line) {

		if(face_V_VT_VN_Matcher != null) face_V_VT_VN_Matcher.reset();
		face_V_VT_VN_Matcher = face_V_VT_VN_Pattern.matcher(line);
		return face_V_VT_VN_Matcher.matches();
	}

	private static boolean isValidFace_V_VT_Line(String line) {

		if(face_V_VT_Matcher != null) face_V_VT_Matcher.reset();
		face_V_VT_Matcher = face_V_VT_Pattern.matcher(line);
		return face_V_VT_Matcher.matches();
	}

	private static boolean isValidFace_V_VN_Line(String line) {

		if(face_V_VN_Matcher != null) face_V_VN_Matcher.reset();
		face_V_VN_Matcher = face_V_VN_Pattern.matcher(line);
		return face_V_VN_Matcher.matches();
	}

	private static boolean isValidFace_V_Line(String line) {

		if(face_V_Matcher != null) face_V_Matcher.reset();
		face_V_Matcher = face_V_Pattern.matcher(line);
		return face_V_Matcher.matches();
	}

	private static boolean isValidGroupObjectLine(String line) {

		if(groupObjectMatcher != null) groupObjectMatcher.reset();
		groupObjectMatcher = groupObjectPattern.matcher(line);
		return groupObjectMatcher.matches();
	}

	private boolean isValidFaceLine(String line) {
		return isValidFace_V_VT_VN_Line(line) || isValidFace_V_VT_Line(line) || isValidFace_V_VN_Line(line) || isValidFace_V_Line(line);
	}
}
