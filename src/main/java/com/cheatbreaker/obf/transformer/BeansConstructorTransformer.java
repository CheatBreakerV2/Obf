package com.cheatbreaker.obf.transformer;

import com.cheatbreaker.obf.Obf;
import org.objectweb.asm.tree.ClassNode;

/**
 * Removes the {@link java.beans.ConstructorProperties} from methods.
 */
public class BeansConstructorTransformer extends Transformer {

    public BeansConstructorTransformer(Obf obf) {
        super(obf);
    }

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
