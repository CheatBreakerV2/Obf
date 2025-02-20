/**
 * The MIT License (MIT)
 *
 * Copyright (C) 2018 CheatBreaker, LLC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.cheatbreaker.obf.transformer.impl;

import com.cheatbreaker.obf.transformer.Transformer;
import com.cheatbreaker.obf.utils.AsmUtils;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class StringTransformer extends Transformer {

    private static final int PARTITION_BITS = 10;
    private static final int PARTITION_SIZE = 1 << PARTITION_BITS;
    private static final int PARTITION_MASK = PARTITION_SIZE - 1;
    private List<String> strings = new ArrayList<>();

    @Override
    public void visit(ClassNode classNode) {
        for (MethodNode method : classNode.methods) {
            for (Iterator<AbstractInsnNode> iter = method.instructions.iterator(); iter.hasNext(); ) {
                AbstractInsnNode insn = iter.next();
                if (insn.getOpcode() == Opcodes.LDC) {
                    LdcInsnNode ldc = (LdcInsnNode) insn;
                    if (ldc.cst instanceof String) {
                        String string = (String) ldc.cst;
                        int id = strings.indexOf(string);
                        if (id == -1) {
                            id = strings.size();
                            strings.add(string);
                        }
                        int index = id & PARTITION_MASK;
                        int classId = id >> PARTITION_BITS;
                        int mask = (short) random.nextInt();
                        int a = (short) random.nextInt() & mask | index;
                        int b = (short) random.nextInt() & ~mask | index;
                        method.instructions.insertBefore(insn, new FieldInsnNode(Opcodes.GETSTATIC, "generated/Strings" + classId, "strings", "[Ljava/lang/String;"));
                        method.instructions.insertBefore(insn, AsmUtils.pushInt(a));
                        method.instructions.insertBefore(insn, AsmUtils.pushInt(b));
                        method.instructions.insertBefore(insn, new InsnNode(Opcodes.IAND));
                        method.instructions.insertBefore(insn, new InsnNode(Opcodes.AALOAD));
                        iter.remove();
                    }
                }
            }
        }
    }

    @Override
    public void after() {
        for (int classId = 0; classId <= strings.size() >> PARTITION_BITS; classId++) {
            ClassNode classNode = new ClassNode();
            classNode.version = Opcodes.V1_8;
            classNode.access = Opcodes.ACC_PUBLIC;
            classNode.name = "generated/Strings" + classId;
            classNode.superName = "java/lang/Object";
            classNode.fields.add(new FieldNode(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "strings", "[Ljava/lang/String;", null, null));
            MethodNode clinit = new MethodNode(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "<clinit>", "()V", null, null);
            classNode.methods.add(clinit);
            int start = classId << PARTITION_BITS;
            int end = Math.min(start + PARTITION_SIZE, strings.size());
            clinit.instructions.add(AsmUtils.pushInt(end - start));
            clinit.instructions.add(new TypeInsnNode(Opcodes.ANEWARRAY, "Ljava/lang/String;"));
            for (int id = start; id < end; id++) {
                clinit.instructions.add(new InsnNode(Opcodes.DUP));
                clinit.instructions.add(AsmUtils.pushInt(id & PARTITION_MASK));
                clinit.instructions.add(new LdcInsnNode(strings.get(id)));
                clinit.instructions.add(new InsnNode(Opcodes.AASTORE));
            }
            clinit.instructions.add(new FieldInsnNode(Opcodes.PUTSTATIC, classNode.name, "strings", "[Ljava/lang/String;"));
            clinit.instructions.add(new InsnNode(Opcodes.RETURN));
            obf.addNewClass(classNode);
        }
    }
}
