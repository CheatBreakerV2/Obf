package com.cheatbreaker.obf.transformer.impl;

import com.cheatbreaker.obf.Obf;
import com.cheatbreaker.obf.transformer.Transformer;
import org.objectweb.asm.tree.ClassNode;

/**
 * Removes toString() methods.
 */
public class ToStringTransformer extends Transformer {

    @Override
    public void visit(ClassNode classNode) {
        classNode.methods.removeIf(next -> next.name.equals("toString"));
    }
}