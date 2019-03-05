function initializeCoreMod() {
    return {
        'coremodone': {
            'target': {
                'type': 'CLASS',
                'name': 'net.minecraft.client.renderer.ChunkRenderContainer'
            },
            'transformer': function(classNode) {
                var opcodes = Java.type('org.objectweb.asm.Opcodes')

                var MethodInsnNode = Java.type('org.objectweb.asm.tree.MethodInsnNode')
                var VarInsnNode = Java.type('org.objectweb.asm.tree.VarInsnNode')

                var api = Java.type('net.minecraftforge.coremod.api.ASMAPI');

                var methods = classNode.methods;

                for(m in methods) {
                    var method = methods[m];
                    if (method.name.equals("preRenderChunk")||method.name.equals("func_178003_a ")) {
				        var code = method.instructions;
				        var instr=code.toArray();
                        for(t in instr) {
                            var instruction = instr[t];
                            if(instruction.getOpcode() == opcodes.RETURN) {
                                code.insertBefore(instruction, new VarInsnNode(opcodes.ALOAD, 1))
                                code.insertBefore(instruction, new MethodInsnNode(opcodes.INVOKESTATIC, "com/hrznstudio/albedo/util/RenderUtil", "renderChunkUniforms", "(Lnet/minecraft/client/renderer/chunk/RenderChunk;)V", false))
                                break;
                            }
                        }

                    }
                }

                return classNode;
            }
        }
    }
}