/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.woonsanko.davstore.demo.bundlereader;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;

import javax.jcr.Binary;
import javax.jcr.PropertyType;

import org.apache.jackrabbit.api.ReferenceBinary;
import org.apache.jackrabbit.core.id.NodeId;
import org.apache.jackrabbit.core.persistence.util.BundleBinding;
import org.apache.jackrabbit.core.persistence.util.NodePropBundle;
import org.apache.jackrabbit.core.persistence.util.NodePropBundle.ChildNodeEntry;
import org.apache.jackrabbit.core.persistence.util.NodePropBundle.PropertyEntry;
import org.apache.jackrabbit.core.value.InternalValue;

public class JackrabbitBundleReaderUtils {

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.err.println("Usage: command <bundle_file_path> [<node_uuid>]");
            System.exit(1);
        }

        String buldeFilePath = args[0];
        String nodeUuid = (args.length > 1) ? args[1] : null;

        printlnBundle(buldeFilePath, nodeUuid);
    }

    public static void printlnBundle(String bundleFilePath, String nodeUuid) throws Exception {
        File bundleFile = new File(bundleFilePath);

        if (!bundleFile.exists()) {
            throw new IllegalArgumentException("File doesn't exist: " + bundleFilePath);
        }

        printlnBundle(bundleFile, nodeUuid);
    }

    public static void printlnBundle(File bundleFile, String nodeUuid) throws Exception {
        printlnBundle(bundleFile, nodeUuid, new PrintWriter(System.out));
    }

    public static void printlnBundle(File bundleFile, String nodeUuid, PrintWriter out) throws Exception {
        if (nodeUuid == null || nodeUuid.isEmpty()) {
            if (!bundleFile.getName().endsWith(".n")) {
                throw new IllegalArgumentException(
                        "No node uuid is provided, nor inferrable from the file path: " + bundleFile.getPath());
            }

            final String uuidWithoutHyphens = bundleFile.getParentFile().getParentFile().getName()
                    + bundleFile.getParentFile().getName()
                    + bundleFile.getName().substring(0, bundleFile.getName().length() - 2);

            if (uuidWithoutHyphens.length() != NodeId.UUID_FORMATTED_LENGTH - 4) {
                throw new IllegalArgumentException(
                        "Node ID cannot be inferred from the file path: " + bundleFile.getPath());
            }

            nodeUuid = new StringBuilder(NodeId.UUID_FORMATTED_LENGTH).append(uuidWithoutHyphens.substring(0, 8))
                    .append('-').append(uuidWithoutHyphens.substring(8, 12)).append('-')
                    .append(uuidWithoutHyphens.substring(12, 16)).append('-')
                    .append(uuidWithoutHyphens.substring(16, 20)).append('-')
                    .append(uuidWithoutHyphens.substring(20, NodeId.UUID_FORMATTED_LENGTH - 4)).toString();
        }

        try (InputStream bundleInput = new BufferedInputStream(new FileInputStream(bundleFile))) {
            printlnBundle(bundleInput, NodeId.valueOf(nodeUuid), out);
        }
    }

    public static void printlnBundle(InputStream bundleInput, NodeId nodeId, PrintWriter out) throws Exception {
        final BundleBinding binding = new BundleBinding(null, null, null, null, null);
        final NodePropBundle bundle = binding.readBundle(bundleInput, nodeId);
        printlnNodePropBundle(out, bundle);
        out.flush();
    }

    private static void printlnNodePropBundle(final PrintWriter out, final NodePropBundle bundle) throws Exception {
        out.println("ID: " + bundle.getId());
        out.println("Parent ID: " + bundle.getParentId());
        out.println("Node type: " + bundle.getNodeTypeName());
        out.println("Mixin types: " + bundle.getMixinTypeNames());
        out.println("Referenceable? " + bundle.isReferenceable());
        out.println("Shared set: " + bundle.getSharedSet());

        out.println("Properties:");

        for (PropertyEntry entry : bundle.getPropertyEntries()) {
            String typeAndMultiplicity;

            if (entry.isMultiValued()) {
                typeAndMultiplicity = PropertyType.nameFromValue(entry.getType()) + " multiple";
            } else {
                typeAndMultiplicity = PropertyType.nameFromValue(entry.getType());
            }

            out.print("  - " + entry.getName() + " (" + typeAndMultiplicity + "): ");
            out.println(stringifyInteralValues(entry.getValues()));
        }

        out.println("Children:");

        for (ChildNodeEntry entry : bundle.getChildNodeEntries()) {
            out.println("  - " + entry.getName() + " (" + entry.getId() + ")");
        }
    }

    private static List<String> stringifyInteralValues(InternalValue[] values) throws Exception {
        List<String> strValues = new LinkedList<>();

        for (InternalValue value : values) {
            final int type = value.getType();

            if (type == PropertyType.BINARY) {
                final Binary binary = value.getBinary();

                if (binary instanceof ReferenceBinary) {
                    strValues.add(binary.toString());
                } else {
                    strValues.add("EmbeddedBinarySize=" + binary.getSize());
                }
            } else {
                strValues.add(value.getString());
            }
        }

        return strValues;
    }
}
