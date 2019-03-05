function initializeCoreMod() {
    return {
        'coremodone': {
            'target': {
                'type': 'CLASS',
                'name': 'net.minecraft.client.renderer.GlStateManager'
            },
            'transformer': function(classNode) {
                var opcodes = Java.type('org.objectweb.asm.Opcodes')

                var MethodInsnNode = Java.type('org.objectweb.asm.tree.MethodInsnNode')
                var VarInsnNode = Java.type('org.objectweb.asm.tree.VarInsnNode')

                var api = Java.type('net.minecraftforge.coremod.api.ASMAPI');

                var methods = classNode.methods;

                for(m in methods) {
                    var method = methods[m];
                    if (method.name.equals("enableLighting")||method.name.equals("func_179145_e")) {
				        var code = method.instructions;
                        code.insertBefore(code.get(2), new MethodInsnNode(opcodes.INVOKESTATIC, "com/hrznstudio/albedo/util/RenderUtil", "enableLightingUniforms", "()V", false));
                    }
                    if (method.name.equals("disableLighting")||method.name.equals("func_179140_f")) {
				        var code = method.instructions;
                        code.insertBefore(code.get(2), new MethodInsnNode(opcodes.INVOKESTATIC, "com/hrznstudio/albedo/util/RenderUtil", "disableLightingUniforms", "()V", false));
                    }
                }

                return classNode;
            }
        }
    }
}