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
package org.apache.maven.model.building;

import javax.xml.stream.XMLStreamReader;

import java.io.IOException;
import java.nio.file.Path;

/**
 * The ModelSourceTransformer is a way to transform the local pom while streaming the input.
 *
 * The {@link #transform(XMLStreamReader, Path, TransformerContext)} method uses a Path on purpose, to ensure the
 * local pom is the original source.
 *
 * @since 4.0.0
 */
public interface ModelSourceTransformer {
    /**
     *
     * @param pomFile the pom file, cannot be null
     * @param context the context, cannot be null
     * @return the InputStream for the ModelReader
     * @throws IOException if an I/O error occurs
     * @throws TransformerException if the transformation fails
     */
    XMLStreamReader transform(XMLStreamReader parser, Path pomFile, TransformerContext context)
            throws IOException, TransformerException;
}
