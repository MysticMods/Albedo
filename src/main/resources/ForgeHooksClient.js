function initializeCoreMod() {
    return {
        'coremodone': {
            'target': {
                'type': 'CLASS',
                'name': 'net.minecraftforge.client.ForgeHooksClient'
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
                    if (method.name.compareTo("handleCameraTransforms")==0) {
				        var code = method.instructions;
				        var instr=code.toArray();
                        code.insertBefore(code.get(2), new MethodInsnNode(opcodes.INVOKESTATIC, "com/hrznstudio/albedo/util/RenderUtil", "setTransform", "(Lnet/minecraft/client/renderer/model/ItemCameraTransforms$TransformType;)V", false))
                        code.insertBefore(code.get(2), new VarInsnNode(opcodes.ALOAD, 1))
                        break;

                    }
                }

                return classNode;
            }
        }
    }
}