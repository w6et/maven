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

import java.util.List;

import org.apache.maven.model.transform.stax.NodeBufferingParser;

/**
 * Remove all modules, this is just buildtime information
 *
 * @since 4.0.0
 */
class ModulesXMLFilter extends NodeBufferingParser {
    ModulesXMLFilter(XMLStreamReader delegate) {
        super(delegate, "modules");
    }

    protected void process(List<Event> buffer) {
        // Do nothing, as we want to delete those nodes completely
    }
}
