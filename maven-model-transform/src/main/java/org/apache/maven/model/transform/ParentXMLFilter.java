/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.maven.model.transform;

import javax.xml.stream.XMLStreamReader;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Pattern;

import org.apache.maven.model.transform.stax.NodeBufferingParser;

/**
 * <p>
 * Transforms relativePath to version.
 * We could decide to simply allow {@code <parent/>}, but let's require the GA for now for checking
 * This filter does NOT remove the relativePath (which is done by {@link RelativePathXMLFilter}, it will only
 * optionally include the version based on the path
 * </p>
 *
 * @since 4.0.0
 */
class ParentXMLFilter extends NodeBufferingParser {

    private final Function<Path, Optional<RelativeProject>> relativePathMapper;

    private final Function<Path, Path> modelLocator;

    private final Path projectPath;

    private static final Pattern S_FILTER = Pattern.compile("\\s+");

    /**
     * @param relativePathMapper
     */
    ParentXMLFilter(
            XMLStreamReader delegate,
            Function<Path, Optional<RelativeProject>> relativePathMapper,
            Function<Path, Path> modelLocator,
            Path projectPath) {
        super(delegate, "parent");
        this.relativePathMapper = relativePathMapper;
        this.modelLocator = modelLocator;
        this.projectPath = projectPath;
    }

    protected void process(List<Event> buffer) {
        String tagName = null;
        String groupId = null;
        String artifactId = null;
        String version = null;
        String relativePath = null;
        String whitespaceAfterParentStart = "";
        boolean hasVersion = false;
        boolean hasRelativePath = false;
        for (int i = 0; i < buffer.size(); i++) {
            Event event = buffer.get(i);
            if (event.event == START_ELEMENT) {
                tagName = event.name;
                hasVersion |= "version".equals(tagName);
                hasRelativePath |= "relativePath".equals(tagName);
            } else if (event.event == CHARACTERS) {
                if (S_FILTER.matcher(event.text).matches()) {
                    if (whitespaceAfterParentStart.isEmpty()) {
                        whitespaceAfterParentStart = event.text;
                    }
                } else if ("groupId".equals(tagName)) {
                    groupId = nullSafeAppend(groupId, event.text);
                } else if ("artifactId".equals(tagName)) {
                    artifactId = nullSafeAppend(artifactId, event.text);
                } else if ("relativePath".equals(tagName)) {
                    relativePath = nullSafeAppend(relativePath, event.text);
                } else if ("version".equals(tagName)) {
                    version = nullSafeAppend(version, event.text);
                }
            } else if (event.event == END_ELEMENT && "parent".equals(event.name)) {
                Optional<RelativeProject> resolvedParent;
                if (!hasVersion && (!hasRelativePath || relativePath != null)) {
                    Path relPath = Paths.get(Objects.toString(relativePath, ".."));
                    resolvedParent = resolveRelativePath(relPath, groupId, artifactId);
                } else {
                    resolvedParent = Optional.empty();
                }
                if (!hasVersion && resolvedParent.isPresent()) {
                    int pos = buffer.get(i - 1).event == CHARACTERS ? i - 1 : i;
                    Event e = new Event();
                    e.event = CHARACTERS;
                    e.text = whitespaceAfterParentStart;
                    buffer.add(pos++, e);
                    e = new Event();
                    e.event = START_ELEMENT;
                    e.namespace = buffer.get(0).namespace;
                    e.prefix = buffer.get(0).prefix;
                    e.name = "version";
                    buffer.add(pos++, e);
                    e = new Event();
                    e.event = CHARACTERS;
                    e.text = resolvedParent.get().getVersion();
                    buffer.add(pos++, e);
                    e = new Event();
                    e.event = END_ELEMENT;
                    e.name = "version";
                    e.namespace = buffer.get(0).namespace;
                    e.prefix = buffer.get(0).prefix;
                    buffer.add(pos++, e);
                }
                break;
            }
        }
        buffer.forEach(this::pushEvent);
    }

    protected Optional<RelativeProject> resolveRelativePath(Path relativePath, String groupId, String artifactId) {
        Path pomPath = projectPath.resolve(relativePath).normalize();
        if (Files.isDirectory(pomPath) && modelLocator != null) {
            pomPath = modelLocator.apply(pomPath);
        }

        if (pomPath == null || !Files.isRegularFile(pomPath)) {
            return Optional.empty();
        }

        Optional<RelativeProject> mappedProject = relativePathMapper.apply(pomPath.normalize());

        if (mappedProject.isPresent()) {
            RelativeProject project = mappedProject.get();

            if (Objects.equals(groupId, project.getGroupId()) && Objects.equals(artifactId, project.getArtifactId())) {
                return mappedProject;
            }
        }
        return Optional.empty();
    }
}
