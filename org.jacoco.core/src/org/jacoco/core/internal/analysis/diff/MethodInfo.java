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

import java.util.ArrayList;
import java.util.List;

public class MethodInfo {
    public String className;
    public String md5;
    public String methodName;
    public List<String> paramList = new ArrayList<>();
    public String methodBody;
    public boolean isAnonymousClass;

    @Override
    public String toString() {
        return "MethodInfo{" +
                "className='" + className + '\'' +
                ", md5='" + md5 + '\'' +
                ", methodName='" + methodName + '\'' +
                ", paramList=" + paramList +
                ", methodBody='" + methodBody + '\'' +
                ", isAnonymousClass=" + isAnonymousClass +
                '}';
    }
}
