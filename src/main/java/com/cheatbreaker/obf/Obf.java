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

package com.cheatbreaker.obf;

import com.cheatbreaker.obf.transformer.*;
import com.cheatbreaker.obf.transformer.impl.*;
import com.cheatbreaker.obf.utils.StreamUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Random;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

public class Obf {

    private final Random random;
    private final List<ClassNode> classes = new ArrayList<>();
    private final List<Transformer> transformers = new ArrayList<>();
    private final List<ClassNode> newClasses = new ArrayList<>();

    private static Obf instance;

    public Obf(File inputFile, File outputFile) throws IOException {
        instance = this;
        random = new Random();

        transformers.add(new ConstantTransformer());
        transformers.add(new StringTransformer());
        transformers.add(new JunkFieldTransformer());
        transformers.add(new AccessTransformer());
        transformers.add(new ShuffleTransformer());
        transformers.add(new ToStringTransformer());
        transformers.add(new SourceFileTransformer());
        transformers.add(new BeansConstructorTransformer());

        JarFile inputJar = new JarFile(inputFile);

        try (JarOutputStream out = new JarOutputStream(new FileOutputStream(outputFile))) {

            // read all classes into this.classes and copy all resources to output jar
            System.out.println("Reading jar...");
            for (Enumeration<JarEntry> iter = inputJar.entries(); iter.hasMoreElements(); ) {
                JarEntry entry = iter.nextElement();
                try (InputStream in = inputJar.getInputStream(entry)) {
                    if (entry.getName().endsWith(".class")) {
                        ClassReader reader = new ClassReader(in);
                        ClassNode classNode = new ClassNode();
                        reader.accept(classNode, 0);
                        classes.add(classNode);
                    } else {
                        out.putNextEntry(new JarEntry(entry.getName()));
                        StreamUtils.copy(in, out);
                    }
                }
            }

            // shuffle the entries in case the order in the output jar gives away information
            Collections.shuffle(classes, random);

            System.out.println("Transforming classes...");
            for (Transformer transformer : transformers) {
                System.out.println("Running " + transformer.getClass().getSimpleName() + "...");
                classes.forEach(transformer::visit);
            }
            for (Transformer transformer : transformers) {
                transformer.after();
            }

            System.out.println("Writing classes...");
            for (ClassNode classNode : classes) {
                ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
                classNode.accept(writer);
                out.putNextEntry(new JarEntry(classNode.name + ".class"));
                out.write(writer.toByteArray());
            }

            System.out.println("Writing generated classes...");
            for (ClassNode classNode : newClasses) {
                ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
                classNode.accept(writer);
                out.putNextEntry(new JarEntry(classNode.name + ".class"));
                out.write(writer.toByteArray());
            }
        }
    }

    public static Obf getInstance() {
        return instance;
    }

    public Random getRandom() {
        return random;
    }

    public List<ClassNode> getClasses() {
        return classes;
    }

    public void addNewClass(ClassNode classNode) {
        newClasses.add(classNode);
    }
}
