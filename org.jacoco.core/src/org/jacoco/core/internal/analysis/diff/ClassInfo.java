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

    public List<MethodInfo> diffMethod;

    public ClassInfo(String className, List<MethodInfo> diffMethod) {
        this.className = className;
        this.diffMethod = diffMethod;
    }

    @Override
    public String toString() {
        return "ClassInfo{" +
                "className='" + className + '\'' +
                ", diffMethod=" + diffMethod +
                '}';
    }
}
