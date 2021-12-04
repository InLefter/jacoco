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

import org.jacoco.core.internal.analysis.MethodInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DiffClassRegistry {
    /**
     * Diff class with method info
     */
    private static final Map<String, List<MethodInfo>> classMethods = new HashMap<>();

    /**
     * get all methods' info by a diff class name with full package.
     *
     * @param className class name with full package
     * @return methods' info
     */
    public static List<MethodInfo> getClassMethods(String className) {
        return classMethods.get(className);
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
}
