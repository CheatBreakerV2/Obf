package com.cheatbreaker.obf.transformer;

import com.cheatbreaker.obf.Obf;
import org.objectweb.asm.tree.ClassNode;

/**
 * Removes toString() methods.
 */
public class ToStringTransformer extends Transformer {

    public ToStringTransformer(Obf obf) {
        super(obf);
    }

    @Override
    public void visit(ClassNode classNode) {
        classNode.methods.forEach(methodNode -> {
            if (methodNode.name.equals("toString"))
                classNode.methods.remove(methodNode);
        });
    }
}