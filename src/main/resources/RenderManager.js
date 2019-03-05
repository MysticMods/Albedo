function initializeCoreMod() {
    return {
        'coremodone': {
            'target': {
                'type': 'CLASS',
                'name': 'net.minecraft.client.renderer.entity.RenderManager'
            },
            'transformer': function(classNode) {
                print("Cheese ", classNode.toString());
                var opcodes = Java.type('org.objectweb.asm.Opcodes')

                var MethodInsnNode = Java.type('org.objectweb.asm.tree.MethodInsnNode')
                var VarInsnNode = Java.type('org.objectweb.asm.tree.VarInsnNode')

                var api = Java.type('net.minecraftforge.coremod.api.ASMAPI');

                var methods = classNode.methods;

                for(m in methods) {
                    var method = methods[m];
                    if (method.name.compareTo("renderEntity")==0) {
				        var code = method.instructions;
				        var instr=code.toArray();
                        for(t in instr) {
                            var instruction = instr[t];
                            if(instruction.getOpcode() == opcodes.RETURN) {
                                code.insertBefore(code.get(2), new MethodInsnNode(opcodes.INVOKESTATIC, "com/hrznstudio/albedo/event/RenderEntityEvent", "postNewEvent", "(Lnet/minecraft/entity/Entity;)V", false))
                                code.insertBefore(code.get(2), new VarInsnNode(opcodes.ALOAD, 1))
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