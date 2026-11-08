package me.cortex.voxy.client.core.gl.shader;


import net.minecraft.resources.ResourceLocation;

import org.apache.commons.io.IOUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

public class ShaderLoader {
    private static boolean legacyGemRenderWarningIssued;

    /**
     * Selectively avoid the legacy GemRender 0.1.20 source rewrite for the
     * modern opaque/translucent 0.5.0 terrain pair. Its Voxy vertex rewrite
     * requires source anchors which 0.5.0 removed, causing an exception during
     * world renderer creation. The two shaders must be treated as a pair:
     * patching only the fragment stage or only the vertex stage would leave
     * unpaired varying declarations and produce an OpenGL link failure.
     *
     * Other shaders continue through parse(String) and remain extensible.
     * This fallback sacrifices only GemRender's optional *distant LOD lighting*
     * injection; GemRender and Golem Core continue to render normally.
     */
    public static String parseForCompiler(String id) {
        boolean modernTerrainPair = "voxy:lod/gl46/quads3.vert".equals(id)
                || "voxy:lod/gl46/quads.frag".equals(id);
        if (modernTerrainPair && net.neoforged.fml.ModList.get().isLoaded("gemrender")) {
            if (!legacyGemRenderWarningIssued) {
                legacyGemRenderWarningIssued = true;
                me.cortex.voxy.common.Logger.warn("GemRender 0.1.20's legacy Voxy LOD source patch " +
                        "is incompatible with the 0.5.0 terrain shaders. Using native Voxy " +
                        "terrain shaders instead; GemRender's other rendering remains active.");
            }
            return parseRaw(id);
        }
        return parse(id);
    }

    /**
     * Public hook used by shader-source integrations such as GemRender.
     * Keep this entry point intact for mods whose source transformations are
     * compatible with the specific shader revision being loaded.
     */
    public static String parse(String id) {
        return parseRaw(id);
    }

    /**
     * Load the actual bundled Voxy GLSL without third-party mixins targeting
     * parse(String). GemRender 0.1.20's Voxy patch assumes old exact statements
     * (including "vec4 tinting = getLighting(lighting);" and
     * "attributes.z = addin|(face<<8);") that no longer exist in this fork's
     * gl46 terrain shaders. Its replaceOne(...) throws when an anchor is absent.
     *
     * Use this path only for those two incompatible shaders when GemRender is
     * present. Do not globally suppress GemRender or Golem Core; their other
     * renderers and Voxy's normal shader loader stay untouched.
     */
    public static String parseRaw(String id) {
        var src = "#version 460 core\n";
        src += String.join("\n", ShaderLoadingParser.parseRoot(ResourceLocation.parse(id)));
        return src;
    }


    //Use our own loader

    private static final class ShaderLoadingParser {
        private static final Pattern IMPORT_PATTERN = Pattern.compile("#import <(?<namespace>.*):(?<path>.*)>");
        public static List<String> parseRoot(ResourceLocation id) {
            List<String> out = new ArrayList<>();
            for (var line : toLines(loadShaderAsset(id))) {
                if (line.startsWith("#version")) {
                    continue;
                } else if (line.startsWith("#import")) {
                    var match = IMPORT_PATTERN.matcher(line);
                    if (!match.matches()) throw new IllegalArgumentException("Unknown import: " + line);
                    var iid = ResourceLocation.fromNamespaceAndPath(match.group("namespace"), match.group("path"));
                    out.addAll(parseRoot(iid));
                } else {
                    out.add(line);
                }
            }
            return out;
        }

        private static List<String> toLines(String src) {
            return new BufferedReader(new StringReader(src)).lines().toList();
        }
        private static String loadShaderAsset(ResourceLocation id) {
            String path = String.format("/assets/%s/shaders/%s", id.getNamespace(), id.getPath());
            try (InputStream in = ShaderLoadingParser.class.getResourceAsStream(path)) {
                if (in == null) {
                    throw new RuntimeException("Shader not found: " + path);
                } else {
                    return IOUtils.toString(in, StandardCharsets.UTF_8);
                }
            } catch (IOException e) {
                throw new RuntimeException("Failed to read shader source for " + path, e);
            }
        }
    }
}
