package com.cheatbreaker.obf.transformer.impl;

import com.cheatbreaker.obf.Obf;
import com.cheatbreaker.obf.transformer.Transformer;
import org.objectweb.asm.tree.ClassNode;

/**
 * Removes the {@link java.beans.ConstructorProperties} from methods.
 */
public class BeansConstructorTransformer extends Transformer {

    @Override
    public void visit(ClassNode classNode) {
        classNode.methods.forEach(methodNode -> {
            if (methodNode.visibleAnnotations == null)
                return;

            methodNode.visibleAnnotations
                    .stream()
                    .filter(annotationNode -> annotationNode.desc.equals("Ljava/beans/ConstructorProperties;"))
                    .findFirst().ifPresent(a -> methodNode.visibleAnnotations.remove(a));
            ;
        });
    }
}
