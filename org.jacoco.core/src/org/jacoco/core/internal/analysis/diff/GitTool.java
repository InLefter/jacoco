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

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.diff.EditList;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.util.io.DisabledOutputStream;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class GitTool {
    public static void main(String[] args) throws IOException {
        String clientPath = "D:\\Dev\\jacoco\\.git";
        String currentBranch = "master";
        String baseBranch = "master_bak";
        System.out.println(getDiffs(clientPath, currentBranch, baseBranch));
    }

    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(10);

    /**
     * get diff between branch
     *
     * @param clientPath
     * @param currentBranch
     * @param baseBranch
     * @return
     */
    public static List<ClassInfo> getDiffs(String clientPath, String currentBranch, String baseBranch) {
        try (Repository repository = new FileRepository(clientPath); Git git = new Git(repository)) {

            CanonicalTreeParser newTreeParser = new CanonicalTreeParser();
            CanonicalTreeParser oldTreeParser = new CanonicalTreeParser();

            RevTree newTree = prepareTreeParser(repository, "refs/heads/" + currentBranch, newTreeParser);
            RevTree oldTree = prepareTreeParser(repository, "refs/heads/" + baseBranch, oldTreeParser);

            List<DiffEntry> diff =
                    git.diff().setOldTree(oldTreeParser).setNewTree(newTreeParser).call().stream()
                            .filter(diffEntry -> {
                                String newPath = diffEntry.getNewPath();
                                return newPath.endsWith(".java") && !newPath.contains("test/");
                            })
                            .filter(diffEntry -> diffEntry.getChangeType().equals(DiffEntry.ChangeType.ADD) ||
                                    diffEntry.getChangeType().equals(DiffEntry.ChangeType.MODIFY))
                            .collect(Collectors.toList());

            DiffFormatter diffFormatter = new DiffFormatter(DisabledOutputStream.INSTANCE);
            diffFormatter.setRepository(repository);
            diffFormatter.setContext(0);

            List<CompletableFuture<ClassInfo>> futures = new ArrayList<>();
            for (DiffEntry entry : diff) {
                String[] classFile = entry.getNewPath().split("src/(main/java)?");
                if (classFile.length < 2) {
                    continue;
                }
                String className = classFile[1].split("\\.java")[0];
                EditList edits;
                try {
                    edits = diffFormatter.toFileHeader(entry).toEditList();
                } catch (IOException e) {
                    e.printStackTrace();
                    continue;
                }
                if (edits == null || edits.isEmpty()) {
                    continue;
                }
                List<Edit> collect = edits.stream()
                        .filter(e -> Edit.Type.INSERT.equals(e.getType()) || Edit.Type.REPLACE.equals(e.getType()))
                        .collect(Collectors.toList());
                int[][] lineRange = new int[collect.size()][2];
                for (int i = 0; i < collect.size(); i++) {
                    lineRange[i][0] = collect.get(i).getBeginB();
                    lineRange[i][1] = collect.get(i).getEndB();
                }
                DiffClassRegistry.putDiffLinesOfClass(className, lineRange);

                CompletableFuture<ClassInfo> future = CompletableFuture.supplyAsync(() -> {
                    List<MethodInfo> diffMethods;
                    List<MethodInfo> newMethodInfo = getMethodInfoList(repository, newTree, entry.getNewPath());
                    if (newMethodInfo.isEmpty()) {
                        return null;
                    }
                    List<MethodInfo> oldMethodInfo = getMethodInfoList(repository, oldTree, entry.getOldPath());
                    if (oldMethodInfo.isEmpty()) {
                        // new class
                        diffMethods = newMethodInfo;
                    } else {
                        List<String> oldMethodMd5List =
                                oldMethodInfo.stream().map(methodInfo -> methodInfo.md5).collect(Collectors.toList());
                        diffMethods =
                                newMethodInfo.stream().filter(methodInfo -> !oldMethodMd5List.contains(methodInfo.md5))
                                        .collect(Collectors.toList());
                    }
                    return new ClassInfo(className, diffMethods);
                }, EXECUTOR);
                futures.add(future);
            }
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            return futures.stream().map(CompletableFuture::join).filter(Objects::nonNull).collect(Collectors.toList());
        } catch (IOException | GitAPIException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    private static List<MethodInfo> getMethodInfoList(Repository repository, RevTree newTree, String fileName){
        String javaContent;
        if (fileName.equals("/dev/null")) {
            return Collections.emptyList();
        }
        try {
            javaContent = getContent(repository, newTree, fileName);
            return AstGenerator.parseClassContent(javaContent);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

    private static RevTree prepareTreeParser(Repository repository, String ref, CanonicalTreeParser treeParser) throws IOException {
        Ref head = repository.exactRef(ref);
        try (RevWalk walk = new RevWalk(repository)) {
            RevCommit commit = walk.parseCommit(head.getObjectId());
            RevTree tree = walk.parseTree(commit.getTree().getId());

            try (ObjectReader reader = repository.newObjectReader()) {
                treeParser.reset(reader, tree.getId());
            }

            walk.dispose();
            return tree;
        }
    }

    private static String getContent(Repository repo, RevTree tree, String path) throws IOException {
        try (TreeWalk treeWalk = TreeWalk.forPath(repo, path, tree)) {
            ObjectId blobId = treeWalk.getObjectId(0);
            try (ObjectReader objectReader = repo.newObjectReader()) {
                ObjectLoader objectLoader = objectReader.open(blobId);
                byte[] bytes = objectLoader.getBytes();
                return new String(bytes, StandardCharsets.UTF_8);
            }
        }
    }
}
