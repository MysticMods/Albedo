function initializeCoreMod() {
    return {
        'coremodone': {
            'target': {
                'type': 'CLASS',
                'name': 'net.minecraft.profiler.Profiler'
            },
            'transformer': function(classNode) {
                var opcodes = Java.type('org.objectweb.asm.Opcodes')

                var MethodInsnNode = Java.type('org.objectweb.asm.tree.MethodInsnNode')
                var VarInsnNode = Java.type('org.objectweb.asm.tree.VarInsnNode')

                var api = Java.type('net.minecraftforge.coremod.api.ASMAPI');

                var methods = classNode.methods;

                for(m in methods) {
                    var method = methods[m];
                    if (method.name.compareTo("endStartSection")==0 && method.desc.compareTo("(Ljava/lang/String;)V") == 0) {
				        var code = method.instructions;
                        code.insertBefore(code.get(2), new MethodInsnNode(opcodes.INVOKESTATIC, "com/hrznstudio/albedo/event/ProfilerStartEvent", "postNewEvent", "(Ljava/lang/String;)V", false));
                        code.insertBefore(code.get(2), new VarInsnNode(opcodes.ALOAD, 1));
                    }
                }

                return classNode;
            }
        }
    }
}