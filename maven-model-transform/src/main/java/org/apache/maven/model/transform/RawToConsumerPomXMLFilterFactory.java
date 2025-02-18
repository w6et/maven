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

import java.nio.file.Path;

/**
 * @since 4.0.0
 */
public class RawToConsumerPomXMLFilterFactory {
    private BuildToRawPomXMLFilterFactory buildPomXMLFilterFactory;

    public RawToConsumerPomXMLFilterFactory(BuildToRawPomXMLFilterFactory buildPomXMLFilterFactory) {
        this.buildPomXMLFilterFactory = buildPomXMLFilterFactory;
    }

    public final XMLStreamReader get(XMLStreamReader orgParser, Path projectPath) {
        // Ensure that xs:any elements aren't touched by next filters
        XMLStreamReader parser = orgParser instanceof FastForwardFilter ? orgParser : new FastForwardFilter(orgParser);

        parser = buildPomXMLFilterFactory.get(parser, projectPath);

        // Remove root model attribute
        parser = new RootXMLFilter(parser);
        // Strip modules
        parser = new ModulesXMLFilter(parser);
        // Adjust relativePath
        parser = new RelativePathXMLFilter(parser);

        return parser;
    }
}
