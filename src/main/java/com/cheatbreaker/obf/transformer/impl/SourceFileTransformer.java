package com.cheatbreaker.obf.transformer.impl;

import com.cheatbreaker.obf.transformer.Transformer;
import org.objectweb.asm.tree.ClassNode;

import java.util.concurrent.ThreadLocalRandom;

public class SourceFileTransformer extends Transformer {

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