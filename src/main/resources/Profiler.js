function initializeCoreMod() {
    return {
        'coremodone': {
            'target': {
                'type': 'CLASS',
                'name': 'net.minecraft.profiler.Profiler'
            },
            'transformer': function(classNode) {
                var opcodes = Java.type('org.objectweb.asm.Opcodes')
                print("Transforming Profiler!")

                var MethodInsnNode = Java.type('org.objectweb.asm.tree.MethodInsnNode')
                var VarInsnNode = Java.type('org.objectweb.asm.tree.VarInsnNode')

                var api = Java.type('net.minecraftforge.coremod.api.ASMAPI');

                var methods = classNode.methods;

                for(m in methods) {
                    var method = methods[m];
                    print(method.name)
                    if ((method.name.equals("endStartSection")||method.name.equals("func_76318_c")) && method.desc.equals("(Ljava/lang/String;)V")) {
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