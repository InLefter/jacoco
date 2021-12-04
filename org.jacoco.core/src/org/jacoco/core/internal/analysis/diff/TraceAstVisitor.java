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

import org.eclipse.jdt.core.dom.*;
import org.jacoco.core.internal.analysis.MethodInfo;
import sun.misc.BASE64Encoder;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.stream.Collectors;

public class TraceAstVisitor extends ASTVisitor {
    private int anonymousIndex = 1;

    private String currentClassName = null;

    private final Stack<String> className = new Stack<>();

    private final List<MethodInfo> methodInfo = new ArrayList<>();

    private MethodInfo currentMethod = null;

    private String packageName;

    private boolean isCurAnonymousClass = false;

    @Override
    public boolean visit(AnonymousClassDeclaration node) {
        className.push(className.peek() + "$" + anonymousIndex);
        isCurAnonymousClass = true;
        return super.visit(node);
    }

    @Override
    public void endVisit(AnonymousClassDeclaration node) {
        anonymousIndex++;
        isCurAnonymousClass = false;
        super.endVisit(node);
    }

    @Override
    public boolean visit(LambdaExpression node) {
        return super.visit(node);
    }

    @Override
    public boolean visit(MethodDeclaration node) {
        currentMethod = new MethodInfo();
        currentMethod.className = className.peek();
        currentMethod.methodName = node.getName().toString();
        List<SingleVariableDeclaration> parameters = node.parameters();
        currentMethod.paramList = parameters.stream().map(ASTNode::toString).collect(Collectors.toList());
        currentMethod.isAnonymousClass = isCurAnonymousClass;
        currentMethod.md5 = md5(node.toString());
        methodInfo.add(currentMethod);
        return super.visit(node);
    }

    @Override
    public void endVisit(MethodDeclaration node) {
        super.endVisit(node);
    }

    @Override
    public boolean visit(PackageDeclaration node) {
        packageName = node.getName().getFullyQualifiedName();
        return super.visit(node);
    }

    @Override
    public boolean visit(TypeDeclaration node) {
        if (currentClassName == null) {
            currentClassName = packageName + "." + node.getName().getIdentifier();
        } else {
            currentClassName = className.peek() + "$" + node.getName().getIdentifier();
        }
        className.push(currentClassName);
        return super.visit(node);
    }

    @Override
    public void endVisit(TypeDeclaration node) {
        super.endVisit(node);
    }

    @Override
    public String toString() {
        StringBuilder methodInfo = new StringBuilder();
        methodInfo.append("\n");
        for (MethodInfo method : this.methodInfo) {
            methodInfo.append("[\t").append(method).append(",\n");
        }
        methodInfo.append("]");
        return "TraceAstVisitor{" +
                ", className=" + className +
                ", methodInfo=" + methodInfo +
                ", packageName='" + packageName + '\'' +
                '}';
    }

    public String md5(String body) {
        String madeMD5String = "";
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            BASE64Encoder base64en = new BASE64Encoder();
            madeMD5String = base64en.encode(
                    md5.digest(body.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return madeMD5String;
    }

    public List<MethodInfo> getMethodInfo() {
        return methodInfo;
    }
}
