/**
 * Generated by Gas3 v2.3.0 (Granite Data Services).
 *
 * WARNING: DO NOT CHANGE THIS FILE. IT MAY BE OVERWRITTEN EACH TIME YOU USE
 * THE GENERATOR.
 */

package org.openforis.collect.metamodel.proxy {

    import org.granite.util.Enum;

    [Bindable]
    [RemoteClass(alias="org.openforis.collect.metamodel.proxy.NumberAttributeDefinitionProxy$Type")]
    public class NumberAttributeDefinitionProxy$Type extends Enum {

        public static const INTEGER:NumberAttributeDefinitionProxy$Type = new NumberAttributeDefinitionProxy$Type("INTEGER", _);
        public static const REAL:NumberAttributeDefinitionProxy$Type = new NumberAttributeDefinitionProxy$Type("REAL", _);

        function NumberAttributeDefinitionProxy$Type(value:String = null, restrictor:* = null) {
            super((value || INTEGER.name), restrictor);
        }

        protected override function getConstants():Array {
            return constants;
        }

        public static function get constants():Array {
            return [INTEGER, REAL];
        }

        public static function valueOf(name:String):NumberAttributeDefinitionProxy$Type {
            return NumberAttributeDefinitionProxy$Type(INTEGER.constantOf(name));
        }
    }
}