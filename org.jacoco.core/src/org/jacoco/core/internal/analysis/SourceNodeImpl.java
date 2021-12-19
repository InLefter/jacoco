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
package org.jacoco.core.internal.analysis;

import org.jacoco.core.analysis.CoverageNodeImpl;
import org.jacoco.core.analysis.ICounter;
import org.jacoco.core.analysis.ILine;
import org.jacoco.core.analysis.ISourceNode;
import org.jacoco.core.internal.analysis.diff.DiffClassRegistry;

import java.util.Arrays;

/**
 * Implementation of {@link ISourceNode}.
 */
public class SourceNodeImpl extends CoverageNodeImpl implements ISourceNode {

	private LineImpl[] lines;

	private int[][] diffineRanges;

	/** first line number in {@link #lines} */
	private int offset;

	/**
	 * Create a new source node implementation instance.
	 *
	 * @param elementType
	 *            element type
	 * @param name
	 *            name of the element
	 */
	public SourceNodeImpl(final ElementType elementType, final String name, final String sourceFile) {
		this(elementType, name);
		if (elementType.equals(ElementType.METHOD)) {
			diffineRanges = DiffClassRegistry.getClassDiffLines(sourceFile);
		}
	}

	/**
	 * Create a new source node implementation instance.
	 *
	 * @param elementType
	 *            element type
	 * @param name
	 *            name of the element
	 */
	public SourceNodeImpl(final ElementType elementType, final String name) {
		super(elementType, name);
		lines = null;
		offset = UNKNOWN_LINE;
		if (elementType.equals(ElementType.CLASS) || elementType.equals(ElementType.SOURCEFILE)) {
			diffineRanges = DiffClassRegistry.getClassDiffLines(name);
		}
	}

	/**
	 * Make sure that the internal buffer can keep lines from first to last.
	 * While the buffer is also incremented automatically, this method allows
	 * optimization in case the total range is known in advance.
	 *
	 * @param first
	 *            first line number or {@link ISourceNode#UNKNOWN_LINE}
	 * @param last
	 *            last line number or {@link ISourceNode#UNKNOWN_LINE}
	 */
	public void ensureCapacity(final int first, final int last) {
		if (first == UNKNOWN_LINE || last == UNKNOWN_LINE) {
			return;
		}
		if (lines == null) {
			offset = first;
			lines = new LineImpl[last - first + 1];
		} else {
			final int newFirst = Math.min(getFirstLine(), first);
			final int newLast = Math.max(getLastLine(), last);
			final int newLength = newLast - newFirst + 1;
			if (newLength > lines.length) {
				final LineImpl[] newLines = new LineImpl[newLength];
				System.arraycopy(lines, 0, newLines, offset - newFirst,
						lines.length);
				offset = newFirst;
				lines = newLines;
			}
		}
	}

	/**
	 * Increments all counters by the values of the given child. When
	 * incrementing the line counter it is assumed that the child refers to the
	 * same source file.
	 *
	 * @param child
	 *            child node to add
	 */
	public void increment(final ISourceNode child) {
		instructionCounter = instructionCounter
				.increment(child.getInstructionCounter());
		branchCounter = branchCounter.increment(child.getBranchCounter());
		complexityCounter = complexityCounter
				.increment(child.getComplexityCounter());
		methodCounter = methodCounter.increment(child.getMethodCounter());
		classCounter = classCounter.increment(child.getClassCounter());
		diffMethodCounter = diffMethodCounter.increment(child.getDiffMethodCounter());
		diffClassCounter = diffClassCounter.increment(child.getDiffClassCounter());
		final int firstLine = child.getFirstLine();
		if (firstLine != UNKNOWN_LINE) {
			final int lastLine = child.getLastLine();
			ensureCapacity(firstLine, lastLine);
			for (int i = firstLine; i <= lastLine; i++) {
				final ILine line = child.getLine(i);
				incrementLine(line.getInstructionCounter(),
						line.getBranchCounter(), i);
			}
		}
	}

	/**
	 * Increments instructions and branches by the given counter values. If a
	 * optional line number is specified the instructions and branches are added
	 * to the given line. The line counter is incremented accordingly.
	 *
	 * @param instructions
	 *            instructions to add
	 * @param branches
	 *            branches to add
	 * @param line
	 *            optional line number or {@link ISourceNode#UNKNOWN_LINE}
	 */
	public void increment(final ICounter instructions, final ICounter branches,
			final int line) {
		if (line != UNKNOWN_LINE) {
			incrementLine(instructions, branches, line);
		}
		instructionCounter = instructionCounter.increment(instructions);
		branchCounter = branchCounter.increment(branches);
	}

	private static int binarySearchInRange(int[][] ranges, int targetRange) {
		int low = 0;
		int high = ranges.length - 1;

		while (low <= high) {
			int mid = (low + high) >>> 1;
			int[] range = ranges[mid];
			if (targetRange < range[0]) {
				high = mid - 1;
			} else if (targetRange >= range[1]) {
				low = mid + 1;
			} else {
				return mid;
			}
		}
		return -(low + 1);  // key not found.
	}

	private void incrementLine(final ICounter instructions,
			final ICounter branches, final int line) {
		ensureCapacity(line, line);
		final LineImpl l = getLine(line);
		final int oldTotal = l.getInstructionCounter().getTotalCount();
		final int oldCovered = l.getInstructionCounter().getCoveredCount();
		boolean isDiffLine;
		if (l == LineImpl.EMPTY) {
			// search target in diff range
			isDiffLine = diffineRanges != null && binarySearchInRange(diffineRanges, line) >= 0;
		} else {
			isDiffLine = l.isDiffLine();
		}
		lines[line - offset] = l.increment(instructions, branches, isDiffLine);

		if (isDiffLine) {
			diffBranchCounter = diffBranchCounter.increment(branches);
		}

		// Increment line counter:
		if (instructions.getTotalCount() > 0) {
			if (instructions.getCoveredCount() == 0) {
				if (oldTotal == 0) {
					lineCounter = lineCounter
							.increment(CounterImpl.COUNTER_1_0);
					if (isDiffLine) {
						diffLineCounter = diffLineCounter.increment(CounterImpl.COUNTER_1_0);
					}
				}
			} else {
				if (oldTotal == 0) {
					lineCounter = lineCounter
							.increment(CounterImpl.COUNTER_0_1);
					if (isDiffLine) {
						diffLineCounter = diffLineCounter.increment(CounterImpl.COUNTER_0_1);
					}
				} else {
					if (oldCovered == 0) {
						lineCounter = lineCounter.increment(-1, +1);
						if (isDiffLine) {
							diffLineCounter = diffLineCounter.increment(-1, +1);
						}
					}
				}
			}
		}
	}

	// === ISourceNode implementation ===

	public int getFirstLine() {
		return offset;
	}

	public int getLastLine() {
		return lines == null ? UNKNOWN_LINE : (offset + lines.length - 1);
	}

	public LineImpl getLine(final int nr) {
		if (lines == null || nr < getFirstLine() || nr > getLastLine()) {
			return LineImpl.EMPTY;
		}
		final LineImpl line = lines[nr - offset];
		return line == null ? LineImpl.EMPTY : line;
	}

}
