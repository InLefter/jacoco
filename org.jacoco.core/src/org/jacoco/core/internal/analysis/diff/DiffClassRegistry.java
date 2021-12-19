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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DiffClassRegistry {
    private static final String JACOCO_DIFF_MODE = "jacoco.diff.mode";

    private static final String JACOCO_MERGE_ID = "merge.id";

    private static final boolean DIFF_MODE = Boolean.parseBoolean(System.getProperty(JACOCO_DIFF_MODE));

    private static final String MERGE_REQ_ID = System.getProperty(JACOCO_MERGE_ID);

    private static boolean DIFF_INIT_FLAG = false;

    /**
     * Diff class with method info
     */
    private static final Map<String, List<MethodInfo>> classMethods = new ConcurrentHashMap<>();

    /**
     * Diff class with method info
     * diff line range: [[begin, end), [begin, end) ... ]
     */
    private static final Map<String, int[][]> classLineRanges = new ConcurrentHashMap<>();

    /**
     * get diff methods' info by a diff class name with full package.
     *
     * @param className class name with full package
     * @return methods' info
     */
    public static List<MethodInfo> getDiffMethodsOfClass(String className) {
        return classMethods.get(className);
    }

    /**
     * update class's diff methods
     *
     * @param className class name with full package
     * @param methodInfo methods' info
     */
    public static void putDiffMethodsOfClass(String className, List<MethodInfo> methodInfo) {
        classMethods.put(className, methodInfo);
    }

    /**
     * get diff lines by a diff class name with full package.
     * diff line range with format in [begin, end)
     *
     * @param className class name with full package
     * @return diff lines
     */
    public static int[][] getClassDiffLines(String className) {
        return classLineRanges.get(className);
    }

    /**
     * update class's diff lines
     *
     * @param className class name with full package
     * @param diffLineRange class diff line range
     */
    public static void putDiffLinesOfClass(String className, int[][] diffLineRange) {
        classLineRanges.put(className, diffLineRange);
    }

    /**
     * parse diff class info include diff lines and diff methods.
     *
     * @param classInfo
     */
    public static void parseDiffClassInfo(List<ClassInfo> classInfo) {
        for (ClassInfo info : classInfo) {
            classLineRanges.put(info.className, info.classLineRanges);
            classLineRanges.put(info.sourceFile, info.classLineRanges);

            classMethods.put(info.className, info.diffMethod);
        }
    }

    /**
     * whether task with diff report
     *
     * @return is diff mode
     */
    public static boolean isDiffMode() {
        return DIFF_MODE;
    }

    public static void init() {
        if (DIFF_INIT_FLAG) {
            return;
        }
        DIFF_INIT_FLAG = true;
        System.out.println("==> begin to init");
        String baseBranch = System.getProperty("baseBranch");
        String curBranch = System.getProperty("curBranch");
        String projectDir = System.getProperty("projectDir");
        if (isEmpty(baseBranch) || isEmpty(projectDir)) {
            return;
        }
        DiffClassRegistry.parseDiffClassInfo(GitTool.getDiffs(projectDir, curBranch, baseBranch));
    }

    private static boolean isEmpty(String str) {
        return str == null || str.isEmpty();
    }

    public static void dump() {
        System.out.println(classMethods);
        System.out.println(classLineRanges);
    }
}
