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

import java.io.File;
import java.net.URI;

import org.apache.maven.building.FileSource;
import org.apache.maven.model.locator.ModelLocator;

/**
 * Wraps an ordinary {@link File} as a model source.
 *
 */
public class FileModelSource extends FileSource implements ModelSource3 {

    /**
     * Creates a new model source backed by the specified file.
     *
     * @param pomFile The POM file, must not be {@code null}.
     */
    public FileModelSource(File pomFile) {
        super(pomFile);
    }

    /**
     *
     * @return the file of this source
     *
     * @deprecated instead use {@link #getFile()}
     */
    @Deprecated
    public File getPomFile() {
        return getFile();
    }

    @Override
    public ModelSource3 getRelatedSource(ModelLocator locator, String relPath) {
        relPath = relPath.replace('\\', File.separatorChar).replace('/', File.separatorChar);

        File relatedPom = new File(getFile().getParentFile(), relPath);

        if (relatedPom.isDirectory() && locator != null) {
            relatedPom = locator.locatePom(relatedPom);
        }

        if (relatedPom.isFile() && relatedPom.canRead()) {
            return new FileModelSource(relatedPom.toPath().normalize().toFile());
        }

        return null;
    }

    @Override
    public URI getLocationURI() {
        return getFile().toURI();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null) {
            return false;
        }

        if (!FileModelSource.class.equals(obj.getClass())) {
            return false;
        }
        FileModelSource other = (FileModelSource) obj;
        return getFile().equals(other.getFile());
    }

    @Override
    public int hashCode() {
        return getFile().hashCode();
    }
}
