/*******************************************************************************
 * Copyright (c) 2009, 2021 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.internal.analysis.diff;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.*;
import org.jacoco.core.internal.analysis.MethodInfo;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class AstGenerator {
    public static List<MethodInfo> parseClassFile(String classFilePath){
        byte[] input = null;
        try (BufferedInputStream bufferedInputStream = new BufferedInputStream(
                new FileInputStream(classFilePath));) {
            input = new byte[bufferedInputStream.available()];
            bufferedInputStream.read(input);
        } catch (final IOException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }

        ASTParser astParser = ASTParser.newParser(AST.JLS8);
        Map<String, String> options = JavaCore.getOptions();

        JavaCore.setComplianceOptions(JavaCore.VERSION_1_8, options);
        astParser.setCompilerOptions(options);
        astParser.setKind(ASTParser.K_COMPILATION_UNIT);
        astParser.setResolveBindings(true);
        astParser.setBindingsRecovery(true);
        astParser.setStatementsRecovery(true);
        astParser.setSource(new String(input).toCharArray());

        CompilationUnit compilationUnit = (CompilationUnit) astParser.createAST(null);
        TraceAstVisitor astVisitor = new TraceAstVisitor();
        compilationUnit.accept(astVisitor);

        System.out.println(astVisitor);
        return astVisitor.getMethodInfo();
    }

    public static void main(String[] args) throws IOException {
        System.out.println("==>begin");
        parseClassFile("D:\\Dev\\jacoco\\org.jacoco.core\\src\\org\\jacoco\\core\\internal\\analysis\\diff\\AstGenerator.java");
    }

    public class Test {
        Action prepare() {
            return () -> System.out.println("444");
        }

        Action prepareX() {
            return new Action() {
                @Override
                public void doAct() {
                    System.out.println("555");
                }

                public void test() {
                    System.out.println("666");
                }
            };
        }

        public class TestB {
            Action bVersion() {
                return () -> System.out.println("777");
            }
        }
    }

    public interface Action {
        void doAct();
    }
}
