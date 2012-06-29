/**
 * Copyright (C) 2012 FuseSource Corp. All rights reserved.
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
import org.fusesource.amqp.generator.jaxb.Choice;
import org.fusesource.amqp.generator.jaxb.Type;
import org.fusesource.hawtbuf.AsciiBuffer;
import org.fusesource.hawtbuf.Buffer;

import static org.fusesource.amqp.generator.Utilities.toJavaClassName;
import static org.fusesource.amqp.generator.Utilities.toStaticName;
import static com.sun.codemodel.JExpr.*;

/**
 *
 */
public class RestrictedType extends AmqpDefinedType {

    JClass basePrimitiveType;

    public RestrictedType(Generator generator, String className, Type type) throws JClassAlreadyExistsException {
        super(generator, className, type);
    }

    @Override
    protected void init() {
        if ( type.getProvides() != null ) {
            cls()._implements(cm.ref(generator.getInterfaces() + ".AMQP" + toJavaClassName(type.getProvides())));
        } else {
            cls()._implements(cm.ref(generator.getAmqpBaseType()));
        }

        String source = generator.getPrimitiveJavaClass().get(type.getSource());
        if ( source == null ) {
            source = generator.getTypes() + "." + toJavaClassName(type.getSource());
        }

        String key = generator.getRestrictedMapping().get(type.getName());
        basePrimitiveType = cm.ref(generator.getMapping().get(key));

        cls()._extends(cm.ref(source));

        cls().constructor(JMod.PUBLIC).body().block();


        JMethod setter = cls().constructor(JMod.PUBLIC);
        setter.param(basePrimitiveType, "value");
        setter.body().block().assign(JExpr._this().ref("value"), JExpr.ref("value"));

        generateConstants();
        generateValueOf();
    }

    @Override
    protected void createGetArrayConstructor() {

    }

    private void generateConstants() {

        for ( Object obj : type.getEncodingOrDescriptorOrFieldOrChoiceOrDoc() ) {
            if ( obj instanceof Choice ) {
                Choice constant = (Choice) obj;
                int mods = JMod.PUBLIC | JMod.STATIC | JMod.FINAL;
                String name = toStaticName(constant.getName());
                Class<?> clazz = Buffer.class;
                if (isBase(clazz)) {
                    cls().field(mods, cls(), name, JExpr
                            ._new(cls()).arg(JExpr
                                    ._new(cm.ref(AsciiBuffer.class)).arg(JExpr.lit(constant.getValue()))));
                } else if (isBase(Boolean.class)) {
                    if ( Boolean.parseBoolean(constant.getValue()) ) {
                        cls().field(mods, cls(), name, JExpr._new(cls()).arg(cm.ref("java.lang.Boolean").staticRef("TRUE")));
                    } else {
                        cls().field(mods, cls(), name, JExpr._new(cls()).arg(cm.ref("java.lang.Boolean").staticRef("FALSE")));
                    }
                } else if (isBase(Short.class)) {
                    cls().field(mods, cls(), name, JExpr._new(cls()).arg(JExpr._new(cm.ref("java.lang.Short")).arg(JExpr.cast(cm.ref("short"), JExpr.lit(Short.parseShort(constant.getValue()))))));
                } else if (isBase(Long.class)) {
                    cls().field(mods, cls(), name, JExpr._new(cls()).arg(JExpr._new(cm.ref("java.lang.Long")).arg(JExpr.lit(Long.parseLong(constant.getValue())))));
                } else {
                    Log.warn("Not generating constant %s with type %s for restricted type %s!", constant.getName(), basePrimitiveType.name(), type.getName());
                }
            }
        }
    }

    private boolean isBase(Class<?> clazz) {
        return basePrimitiveType.equals(cm.ref(clazz));
    }

    private void generateValueOf() {


        Class<?> t = Buffer.class;
        if ( isBase(t) || isBase(Boolean.class)) {
            JMethod valueOf = cls().method(JMod.PUBLIC | JMod.STATIC, cls(), "valueOf");
            valueOf.param(basePrimitiveType, "value");
            valueOf.body()._if(ref("value").eq(_null()))._then()._return(_null());
            for ( Object obj : type.getEncodingOrDescriptorOrFieldOrChoiceOrDoc() ) {
                if ( obj instanceof Choice ) {
                    Choice constant = (Choice) obj;
                    JInvocation value = ref(toStaticName(constant.getName())).invoke("getValue");
                    valueOf.body()._if(ref("value").eq(value)).
                        _then()._return(ref(toStaticName(constant.getName())));
                }
            }
            valueOf.body()._throw(_new(cm.ref("java.lang.IllegalArgumentException")).arg(lit("invalid "+toJavaClassName(type.getName())+" value: ").plus(ref("value"))));
        } else if ( isBase(Short.class) || isBase(Integer.class) || isBase(Long.class)) {
            JMethod valueOf = cls().method(JMod.PUBLIC | JMod.STATIC, cls(), "valueOf");
            valueOf.param(basePrimitiveType, "value");
            valueOf.body()._if(ref("value").eq(_null()))._then()._return(_null());
            JSwitch _switch = null;
            if (isBase(Short.class)) {
                _switch = valueOf.body()._switch(ref("value").invoke("shortValue"));
            } else if (isBase(Integer.class)) {
                _switch = valueOf.body()._switch(ref("value").invoke("intValue"));
            } else if (isBase(Long.class)) {
                _switch = valueOf.body()._switch(ref("value").invoke("intValue"));
            }
            for ( Object obj : type.getEncodingOrDescriptorOrFieldOrChoiceOrDoc() ) {
                if ( obj instanceof Choice ) {
                    Choice constant = (Choice) obj;
                    JCase _case = null;
                    if (isBase(Short.class)) {
                        _case = _switch._case(JExpr.lit(Short.parseShort(constant.getValue())));
                    } else if (isBase(Integer.class)) {
                        _case = _switch._case(JExpr.lit(Integer.parseInt(constant.getValue())));
                    } else if (isBase(Long.class)) {
                        _case = _switch._case(JExpr.lit((int)Long.parseLong(constant.getValue())));
                    }
                    if( _case !=null ) {
                        _case.body()._return(ref(toStaticName(constant.getName())));
                    }
                }
            }              
            valueOf.body()._throw(_new(cm.ref("java.lang.IllegalArgumentException")).arg(lit("invalid "+toJavaClassName(type.getName())+" value: ").plus(ref("value"))));
        }
    }


    @Override
    protected void createStaticBlock() {

    }

    @Override
    protected void createInitialFields() {

    }
}
