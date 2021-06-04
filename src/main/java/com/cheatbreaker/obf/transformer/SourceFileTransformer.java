package com.cheatbreaker.obf.transformer;

import com.cheatbreaker.obf.Obf;
import org.objectweb.asm.tree.ClassNode;

import java.util.concurrent.ThreadLocalRandom;

public class SourceFileTransformer extends Transformer {

    public SourceFileTransformer(Obf obf) {
        super(obf);
    }

    @Override
    public void visit(ClassNode classNode) {
        ThreadLocalRandom threadLocalRandom = ThreadLocalRandom.current();
        int randomPicked = threadLocalRandom.nextInt(15, 20);
        StringBuilder stringBuilder = new StringBuilder();

        for (int i = 0; i < randomPicked; i++)
            stringBuilder.append(threadLocalRandom.nextBoolean() ? "I" : "l");

        classNode.sourceFile = stringBuilder.toString();
    }
}