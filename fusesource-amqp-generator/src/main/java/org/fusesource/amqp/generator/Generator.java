/**
 * Copyright (C) FuseSource, Inc.
 * http://fusesource.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.fusesource.amqp.generator;

import com.sun.codemodel.*;
import org.fusesource.amqp.generator.jaxb.Definition;
import org.fusesource.amqp.generator.jaxb.Encoding;
import org.fusesource.amqp.generator.jaxb.Type;
import org.fusesource.hawtbuf.Buffer;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

import static org.fusesource.amqp.generator.Utilities.toJavaClassName;
import static org.fusesource.amqp.generator.Utilities.toStaticName;

/**
 *
 */
public class Generator {

    private File[] inputFiles;
    private File outputDirectory;
    private File sourceDirectory;
    private String packagePrefix;

    private HashSet<Definition> definitions = new HashSet<Definition>();

    private TreeMap<String, Type> primitives = new TreeMap<String, Type>();
    private TreeMap<String, Type> composites = new TreeMap<String, Type>();
    private TreeMap<String, Type> restricted = new TreeMap<String, Type>();
    private TreeMap<String, Type> described = new TreeMap<String, Type>();
    private TreeMap<String, Type> enums = new TreeMap<String, Type>();

    private TreeSet<String> provides = new TreeSet<String>();
    private TreeSet<String> requires = new TreeSet<String>();
    private TreeMap<String, String> requiresMapping = new TreeMap<String, String>();

    private TreeMap<String, String> restrictedMapping = new TreeMap<String, String>();

    private TreeMap<String, String> describedJavaClass = new TreeMap<String, String>();
    private TreeMap<String, String> primitiveJavaClass = new TreeMap<String, String>();

    private TreeSet<String> classes = new TreeSet<String>();

    private TreeMap<String, String> sections = new TreeMap<String, String>();
    private TreeMap<String, String> mapping = new TreeMap<String, String>();

    JCodeModel cm = new JCodeModel();

    private String interfaces = "interfaces";
    private String types = "types";
    private String marshaller = "marshaller";

    private String primitiveEncoder;

    private TypeRegistry registry;
    private EncodingPicker picker;
    private Sizer sizer;

    private final XmlDefinitionParser xmlDefinitionParser = new XmlDefinitionParser(this);
    private final InterfaceGenerator interfaceGenerator = new InterfaceGenerator(this);

    public Generator() {
        mapping.put("null", null);
        mapping.put("boolean", Boolean.class.getName());
        mapping.put("ubyte", Short.class.getName());
        mapping.put("ushort", Integer.class.getName());
        mapping.put("uint", Long.class.getName());
        mapping.put("ulong", BigInteger.class.getName());
        mapping.put("byte", Byte.class.getName());
        mapping.put("short", Short.class.getName());
        mapping.put("int", Integer.class.getName());
        mapping.put("long", Long.class.getName());
        mapping.put("float", Float.class.getName());
        mapping.put("double", Double.class.getName());
        mapping.put("decimal32", BigDecimal.class.getName());
        mapping.put("decimal64", BigDecimal.class.getName());
        mapping.put("decimal128", BigDecimal.class.getName());
        mapping.put("char", Character.class.getName());
        mapping.put("timestamp", Date.class.getName());
        mapping.put("uuid", UUID.class.getName());
        mapping.put("binary", Buffer.class.getName());
        mapping.put("string", String.class.getName());
        mapping.put("symbol", Buffer.class.getName());

        mapping.put("list", List.class.getName());
        mapping.put("map", "org.fusesource.fabric.apollo.amqp.codec.types.MapEntries");
        mapping.put("array", Object[].class.getName());
    }

    public File getSourceDirectory() {
        return sourceDirectory;
    }

    public TreeMap<String, String> getRequiresMapping() {
        return requiresMapping;
    }

    public TreeMap<String, String> getDescribedJavaClass() {
        return describedJavaClass;
    }

    public void generate() throws Exception {

        primitiveEncoder = getInterfaces() + "." + "PrimitiveEncoder";
        String typeRegistry = getMarshaller() + "." + "TypeRegistry";
        String encodingPicker = getInterfaces() + "." + "EncodingPicker";
        String encodingSizer = getInterfaces() + "." + "Sizer";

        xmlDefinitionParser.parseXML();

        buildRestrictedTypeMapping();

        generatePrimitiveEncoderDecoder();

        registry = new TypeRegistry(cm, typeRegistry);
        picker = new EncodingPicker(this, encodingPicker);
        sizer = new Sizer(this, encodingSizer);

        registry.cls().field(JMod.PROTECTED | JMod.FINAL | JMod.STATIC, cm._getClass(primitiveEncoder), "ENCODER", JExpr.direct("Encoder.instance()"));
        JMethod singletonAccessor = registry.cls().method(JMod.PUBLIC, cm._getClass(primitiveEncoder), "encoder");
        singletonAccessor.body()._return(JExpr.ref("ENCODER"));

        Log.info("\n%s", this);

        outputDirectory.mkdirs();

        try {

            List<PrimitiveType> primitiveTypes = new ArrayList<PrimitiveType>();

            List<String> filter = new ArrayList<String>();
            filter.add("*");
            filter.add("null");

            for ( String key : getPrimitives().keySet() ) {
                if ( filter.contains(key) ) {
                    continue;
                }
                Type type = getPrimitives().get(key);
                String className = getTypes() + "." + "AMQP" + toJavaClassName(key);
                getPrimitiveJavaClass().put(key, className);
                primitiveTypes.add(new PrimitiveType(this, className, type));
            }

            interfaceGenerator.generateAbstractBases();

            List<DescribedType> describedTypes = new ArrayList<DescribedType>();
            for ( String key : getDescribed().keySet() ) {
                Type type = getDescribed().get(key);
                String className = getTypes() + "." + toJavaClassName(key);
                getDescribedJavaClass().put(key, className);
                describedTypes.add(new DescribedType(this, className, type));
            }

            for ( DescribedType type : describedTypes ) {
                type.generateDescribedFields();
            }

            List<RestrictedType> restrictedTypes = new ArrayList<RestrictedType>();
            for ( String key : getEnums().keySet() ) {
                Type type = getRestricted().get(key);
                String className = getTypes() + "." + toJavaClassName(key);
                restrictedTypes.add(new RestrictedType(this, className, type));
            }

            for ( String key : getRestricted().keySet() ) {
                Type type = getRestricted().get(key);
                if ( type.getProvides() != null && !getEnums().containsKey(key) && !getDescribed().containsKey(key) ) {
                    String className = getTypes() + "." + toJavaClassName(key);
                    restrictedTypes.add(new RestrictedType(this, className, type));
                }
            }


            //describedTypeGenerator.createDescribedClasses(this);

            //generateEnumTypes();

            //describedTypeGenerator.generateDescribedTypes();

            generateDefinitions();

            cm.build(outputDirectory);
        } catch (Exception e) {
            Log.error("Error generating code : %s", e);
            for ( StackTraceElement s : e.getStackTrace() ) {
                Log.error("\tat %s.%s(%s:%s)", s.getClassName(), s.getMethodName(), s.getFileName(), s.getLineNumber());
            }
            throw e;
        }
    }

    private void generatePrimitiveEncoderDecoder() throws JClassAlreadyExistsException {
        JDefinedClass enc = cm._class(primitiveEncoder, ClassType.INTERFACE);

        int mods = JMod.PUBLIC;

        for ( String key : primitives.keySet() ) {
            Log.info("Adding encoder methods for type %s", key);
            Type type = primitives.get(key);

            for ( Object obj : type.getEncodingOrDescriptorOrFieldOrChoiceOrDoc() ) {
                if ( obj instanceof Encoding ) {
                    Encoding encoding = (Encoding) obj;

                    String methodName = type.getName();
                    if ( encoding.getName() != null ) {
                        methodName += "_" + encoding.getName();
                    }

                    methodName = toJavaClassName(methodName);
                    Log.info("Writing encode/decode methods for type %s encoding %s using method name %s", type.getName(), encoding.getName(), methodName);

                    createEncodeDecodeMethods(enc, mods, type, methodName);
                }
            }
        }
    }

    private void createEncodeDecodeMethods(JDefinedClass clazz, int mods, Type type, String methodName) {
        JMethod readMethod;

        String m = mapping.get(type.getName());

        if ( m == null ) {
            readMethod = clazz.method(mods, cm.ref("java.lang.Object"), "read" + methodName);
        } else {
            readMethod = clazz.method(mods, cm.ref(m), "read" + methodName);
        }
        readMethod._throws(java.lang.Exception.class);
        readMethod.param(DataInput.class, "in");

        JMethod writeMethod = clazz.method(mods, cm.VOID, "write" + methodName);
        writeMethod._throws(java.lang.Exception.class);
        if ( m != null ) {
            writeMethod.param(cm.ref(m), "value");
        }
        writeMethod.param(DataOutput.class, "out");

        /*
        JMethod encMethod = clazz.method(mods, cm.VOID, "encode" + methodName);
        encMethod._throws(java.lang.Exception.class);
        if (m != null) {
            encMethod.param(m, "value");
        }
        encMethod.param(Buffer.class, "buffer");
        encMethod.param(cm.INT, "offset");

        JMethod decMethod;
        if (m == null) {
            decMethod = clazz.method(mods, cm.ref("java.lang.Object"), "decode" + methodName);
        } else {
            decMethod = clazz.method(mods, m, "decode" + methodName);
        }
        decMethod._throws(java.lang.Exception.class);
        decMethod.param(Buffer.class, "buffer");
        decMethod.param(cm.INT, "offset");
        */
    }

    private void buildRestrictedTypeMapping() {
        for ( String key : restricted.keySet() ) {
            Type type = restricted.get(key);

            String source = type.getSource();
            while (!mapping.containsKey(source)) {
                Type t = restricted.get(source);
                if ( t == null ) {
                    Log.info("Skipping restricted type %s with source %s, no primitive type found", type.getName(), source);
                    source = "*";
                    break;
                }
                source = t.getSource();
            }
            restrictedMapping.put(type.getName(), source);
        }
    }

    public String toString() {
        StringBuilder rc = new StringBuilder();

        rc.append(String.format("Type classes : %s", classes));
        rc.append("\n");
        rc.append("\n");
        rc.append(String.format("Primitive types : %s", names(primitives)));
        rc.append("\n");
        rc.append("\n");
        rc.append(String.format("Restricted types : %s", names(restricted)));
        rc.append("\n");
        rc.append("\n");
        rc.append("Restricted mapping : \n");
        for ( String key : restrictedMapping.keySet() ) {
            String value = restrictedMapping.get(key);
            rc.append(String.format("%s = %s\n", key, value));
        }
        rc.append("\n");
        rc.append("\n");
        rc.append(String.format("Composite types : %s", names(composites)));
        rc.append("\n");
        rc.append("\n");
        rc.append(String.format("Described types : %s", names(described)));
        rc.append("\n");
        rc.append("\n");
        rc.append(String.format("Provides : %s", provides));
        rc.append("\n");
        rc.append("\n");
        rc.append(String.format("Requires : %s", requires));
        rc.append("\n");
        return rc.toString();
    }

    private String names(Map<String, Type> set) {
        return set.keySet().toString();
    }

    private void generateDefinitions() throws Exception {
        JDefinedClass defs = cm._class(packagePrefix + ".Definitions", ClassType.INTERFACE);
        Log.info("Creating %s", defs.binaryName());

        for ( Definition def : definitions ) {
            Log.info("Adding field %s with value %s", def.getName(), def.getValue());
            JFieldVar field = defs.field(JMod.PUBLIC | JMod.STATIC, java.lang.String.class, toStaticName(def.getName()), JExpr.lit(def.getValue()));
            field.javadoc().add(def.getLabel());
        }
    }

    public JClass getBitUtils() {
        return cm.ref(getPackagePrefix() + ".BitUtils");
    }

    public File[] getInputFiles() {
        return inputFiles;
    }

    public void setInputFiles(File... inputFiles) {
        this.inputFiles = inputFiles;
    }

    public File getOutputDirectory() {
        return outputDirectory;
    }

    public void setOutputDirectory(File outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    public void setSourceDirectory(File sourceDirectory) {
        this.sourceDirectory = sourceDirectory;
    }

    public String getPackagePrefix() {
        return packagePrefix;
    }

    public void setPackagePrefix(String packagePrefix) {
        this.packagePrefix = packagePrefix;
    }

    public JCodeModel getCm() {
        return cm;
    }

    public String getInterfaces() {
        return getPackagePrefix() + "." + interfaces;
    }

    public String getTypes() {
        return getPackagePrefix() + "." + types;
    }

    public Map<String, String> getRestrictedMapping() {
        return restrictedMapping;
    }

    public Map<String, Type> getEnums() {
        return enums;
    }

    public Map<String, Type> getDescribed() {
        return described;
    }

    public Set<String> getRequires() {
        return requires;
    }

    public Set<Definition> getDefinitions() {
        return definitions;
    }

    public Map<String, Type> getPrimitives() {
        return primitives;
    }

    public Map<String, Type> getComposites() {
        return composites;
    }

    public Map<String, Type> getRestricted() {
        return restricted;
    }

    public Set<String> getProvides() {
        return provides;
    }

    public Set<String> getClasses() {
        return classes;
    }

    public Map<String, String> getSections() {
        return sections;
    }

    public Map<String, String> getMapping() {
        return mapping;
    }

    public String getMarshaller() {
        return getPackagePrefix() + "." + marshaller;
    }

    public TypeRegistry registry() {
        return registry;
    }

    public EncodingPicker picker() {
        return picker;
    }

    public String getAmqpBaseType() {
        return getInterfaces() + "." + "AMQPType";
    }

    public Map<String, String> getPrimitiveJavaClass() {
        return primitiveJavaClass;
    }

    public Sizer sizer() {
        return sizer;
    }
}
