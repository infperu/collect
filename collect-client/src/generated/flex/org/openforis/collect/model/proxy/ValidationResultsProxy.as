/**
 * Generated by Gas3 v2.3.0 (Granite Data Services).
 *
 * NOTE: this file is only generated if it does not exist. You may safely put
 * your custom code here.
 */

package org.openforis.collect.model.proxy {
	import mx.collections.ArrayCollection;
	import mx.collections.IList;
	
	import org.openforis.collect.util.ArrayUtil;
	import org.openforis.collect.util.CollectionUtil;

    [Bindable]
    [RemoteClass(alias="org.openforis.collect.model.proxy.ValidationResultsProxy")]
    public class ValidationResultsProxy extends ValidationResultsProxyBase {
		
		public function get validationMessages():IList {
			var results:IList = null;
			if ( CollectionUtil.isNotEmpty(errors) ) {
				results = errors;
			} else if(CollectionUtil.isNotEmpty(warnings)) {
				results = warnings;
			}
			return results;
		}
    }
}