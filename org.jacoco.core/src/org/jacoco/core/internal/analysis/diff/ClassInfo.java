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

import java.util.List;

public class ClassInfo {
    public String className;

    public String sourceFile;

    public List<MethodInfo> diffMethod;

    public int[][] classLineRanges;

    public ClassInfo(String className, String sourceFile, List<MethodInfo> diffMethod, int[][] classLineRanges) {
        this.className = className;
        this.sourceFile = sourceFile;
        this.diffMethod = diffMethod;
        this.classLineRanges = classLineRanges;
    }

    @Override
    public String toString() {
        return "ClassInfo{" +
                "className='" + className + '\'' +
                ", sourceFile='" + sourceFile + '\'' +
                ", diffMethod=" + diffMethod +
                '}';
    }
}
