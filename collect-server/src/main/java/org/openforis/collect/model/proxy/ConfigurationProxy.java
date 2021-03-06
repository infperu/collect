/**
 * 
 */
package org.openforis.collect.model.proxy;

import org.granite.messaging.amf.io.util.externalizer.annotation.ExternalizedProperty;
import org.openforis.collect.Proxy;
import org.openforis.collect.model.Configuration;

/**
 * @author ste
 *
 */
public class ConfigurationProxy implements Proxy {

	private transient Configuration configuration;
	private String defaultRecordFileUploadPath;
	private String defaultRecordIndexPath;

	public ConfigurationProxy(Configuration configuration, String defaultRecordFileUploadPath, String defaultRecordIndexPath) {
		super();
		this.configuration = configuration;
		this.defaultRecordFileUploadPath = defaultRecordFileUploadPath;
		this.defaultRecordIndexPath = defaultRecordIndexPath;
	}
	
	@ExternalizedProperty
	public String getUploadPath() {
		return configuration.getUploadPath();
	}

	@ExternalizedProperty
	public String getDefaultUploadPath() {
		return defaultRecordFileUploadPath;
	}

	@ExternalizedProperty
	public String getIndexPath() {
		return configuration.getIndexPath();
	}
	
	@ExternalizedProperty
	public String getDefaultIndexPath() {
		return defaultRecordIndexPath;
	}

	public String getDefaultRecordFileUploadPath() {
		return defaultRecordFileUploadPath;
	}
	
	public String getDefaultRecordIndexPath() {
		return defaultRecordIndexPath;
	}
	
}
