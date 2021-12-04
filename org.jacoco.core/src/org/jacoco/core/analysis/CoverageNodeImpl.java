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
package org.jacoco.core.analysis;

import java.util.Collection;

import org.jacoco.core.internal.analysis.CounterImpl;

/**
 * Base implementation for coverage data nodes.
 */
public class CoverageNodeImpl implements ICoverageNode {

	private final ElementType elementType;

	private final String name;

	/** Counter for branches. */
	protected CounterImpl branchCounter;

	/** Counter for diff branches. */
	protected CounterImpl diffBranchCounter;

	/** Counter for instructions. */
	protected CounterImpl instructionCounter;

	/** Counter for lines */
	protected CounterImpl lineCounter;

	/** Counter for diff lines */
	protected CounterImpl diffLineCounter;

	/** Counter for complexity. */
	protected CounterImpl complexityCounter;

	/** Counter for methods. */
	protected CounterImpl methodCounter;

	/** Counter for diff methods. */
	protected CounterImpl diffMethodCounter;

	/** Counter for classes. */
	protected CounterImpl classCounter;

	/** Counter for diff classes. */
	protected CounterImpl diffClassCounter;

	/**
	 * Creates a new coverage data node.
	 *
	 * @param elementType
	 *            type of the element represented by this instance
	 * @param name
	 *            name of this node
	 */
	public CoverageNodeImpl(final ElementType elementType, final String name) {
		this.elementType = elementType;
		this.name = name;
		this.branchCounter = CounterImpl.COUNTER_0_0;
		this.diffBranchCounter = CounterImpl.COUNTER_0_0;
		this.instructionCounter = CounterImpl.COUNTER_0_0;
		this.complexityCounter = CounterImpl.COUNTER_0_0;
		this.methodCounter = CounterImpl.COUNTER_0_0;
		this.diffMethodCounter = CounterImpl.COUNTER_0_0;
		this.classCounter = CounterImpl.COUNTER_0_0;
		this.diffClassCounter = CounterImpl.COUNTER_0_0;
		this.lineCounter = CounterImpl.COUNTER_0_0;
		this.diffLineCounter = CounterImpl.COUNTER_0_0;
	}

	/**
	 * Increments the counters by the values given by another element.
	 *
	 * @param child
	 *            counters to add
	 */
	public void increment(final ICoverageNode child) {
		instructionCounter = instructionCounter
				.increment(child.getInstructionCounter());
		branchCounter = branchCounter.increment(child.getBranchCounter());
		diffBranchCounter = diffBranchCounter.increment(child.getDiffBranchCounter());
		lineCounter = lineCounter.increment(child.getLineCounter());
		diffLineCounter = diffLineCounter.increment(child.getDiffLineCounter());
		complexityCounter = complexityCounter
				.increment(child.getComplexityCounter());
		methodCounter = methodCounter.increment(child.getMethodCounter());
		diffMethodCounter = diffMethodCounter.increment(child.getDiffMethodCounter());
		classCounter = classCounter.increment(child.getClassCounter());
		diffClassCounter = diffClassCounter.increment(child.getDiffClassCounter());
	}

	/**
	 * Increments the counters by the values given by the collection of
	 * elements.
	 *
	 * @param children
	 *            list of nodes, which counters will be added to this node
	 */
	public void increment(final Collection<? extends ICoverageNode> children) {
		for (final ICoverageNode child : children) {
			increment(child);
		}
	}

	// === ICoverageDataNode ===

	public ElementType getElementType() {
		return elementType;
	}

	public String getName() {
		return name;
	}

	public ICounter getInstructionCounter() {
		return instructionCounter;
	}

	public ICounter getBranchCounter() {
		return branchCounter;
	}

	public CounterImpl getDiffBranchCounter() {
		return diffBranchCounter;
	}

	public ICounter getLineCounter() {
		return lineCounter;
	}

	public CounterImpl getDiffLineCounter() {
		return diffLineCounter;
	}

	public ICounter getComplexityCounter() {
		return complexityCounter;
	}

	public ICounter getMethodCounter() {
		return methodCounter;
	}

	public CounterImpl getDiffMethodCounter() {
		return diffMethodCounter;
	}

	public ICounter getClassCounter() {
		return classCounter;
	}

	public CounterImpl getDiffClassCounter() {
		return diffClassCounter;
	}

	public ICounter getCounter(final CounterEntity entity) {
		switch (entity) {
		case INSTRUCTION:
			return getInstructionCounter();
		case BRANCH:
			return getBranchCounter();
		case DIFF_BRANCH:
			return getDiffBranchCounter();
		case LINE:
			return getLineCounter();
		case DIFF_LINE:
			return getDiffLineCounter();
		case COMPLEXITY:
			return getComplexityCounter();
		case METHOD:
			return getMethodCounter();
		case DIFF_METHOD:
			return getDiffMethodCounter();
		case CLASS:
			return getClassCounter();
		case DIFF_CLASS:
			return getDiffClassCounter();
		}
		throw new AssertionError(entity);
	}

	public boolean containsCode() {
		return getInstructionCounter().getTotalCount() != 0;
	}

	public ICoverageNode getPlainCopy() {
		final CoverageNodeImpl copy = new CoverageNodeImpl(elementType, name);
		copy.instructionCounter = CounterImpl.getInstance(instructionCounter);
		copy.branchCounter = CounterImpl.getInstance(branchCounter);
		copy.diffBranchCounter = CounterImpl.getInstance(diffBranchCounter);
		copy.lineCounter = CounterImpl.getInstance(lineCounter);
		copy.diffLineCounter = CounterImpl.getInstance(diffLineCounter);
		copy.complexityCounter = CounterImpl.getInstance(complexityCounter);
		copy.methodCounter = CounterImpl.getInstance(methodCounter);
		copy.diffMethodCounter = CounterImpl.getInstance(diffMethodCounter);
		copy.classCounter = CounterImpl.getInstance(classCounter);
		copy.diffClassCounter = CounterImpl.getInstance(diffClassCounter);
		return copy;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append(name).append(" [").append(elementType).append("]");
		return sb.toString();
	}

}
