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
     *
     * @param className class name with full package
     * @return diff lines
     */
    public static int[] getClassLines(String className) {
        return new int[]{3, 4, 5, 6, 7};
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
     * whether task with diff report
     *
     * @return is diff mode
     */
    public static boolean isDiffMode() {
        return DIFF_MODE;
    }

    public static void init() {
        System.out.println("==> begin to init");
        String baseBranch = System.getProperty("baseBranch");
        String curBranch = System.getProperty("curBranch");
        String projectDir = System.getProperty("projectDir");
        if (baseBranch.isEmpty() || curBranch.isEmpty() || projectDir.isEmpty()) {
            return;
        }
        GitTool.getDiffs(projectDir, curBranch, baseBranch).forEach(classInfo -> {
            DiffClassRegistry.putDiffMethodsOfClass(classInfo.className, classInfo.diffMethod);
        });
    }

    private static boolean isEmpty(String str) {
        return str == null || str.isEmpty();
    }

    public static void dump() {
        System.out.println(classMethods);
        System.out.println(classLineRanges);
    }
}
