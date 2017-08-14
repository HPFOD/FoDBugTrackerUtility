/*******************************************************************************
 * (c) Copyright 2017 Hewlett Packard Enterprise Development LP
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the Software"),
 * to deal in the Software without restriction, including without limitation 
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, 
 * and/or sell copies of the Software, and to permit persons to whom the 
 * Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included 
 * in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY
 * KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE 
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR 
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE 
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, 
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF 
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN 
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS 
 * IN THE SOFTWARE.
 ******************************************************************************/
package com.fortify.processrunner.context;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

/**
 * <p>This abstract base class combines {@link IContextGenerator} and {@link IContextUpdater}
 * to allow implementations to generate default values for the configured {@link #contextPropertyName},
 * and add mapped context properties.</p>
 *  
 * @author Ruud Senden
 *
 */
public abstract class AbstractContextGeneratorAndUpdater implements IContextGenerator, IContextUpdater {
	private final String isContextGeneratedFromDefaultValuesKey = "isContextGeneratedFromDefaultValues-"+this.getClass().getName();
	private String contextPropertyName;
	private boolean useForDefaultValueGeneration = false;
	
	/**
	 * Indicate whether this instance is enabled
	 */
	public boolean isContextGeneratorEnabled() {
		return isUseForDefaultValueGeneration();
	}
	
	/**
	 * Generate a collection of {@link Context} instances, based on the given initialContext.
	 * If the initial context already contains a value for the configured context property
	 * name ({@link #getContextPropertyName()}, this method will just add any mapped context
	 * properties using the {@link #addMappedContextProperties(Context, Object)} method.
	 * If the initial context does not yet contain a value for the configured context property,
	 * this method will call {@link #getDefaultValuesWithMappedContextProperties(Context)} to
	 * retrieve possible values for the configured context property, together with all mapped
	 * context properties, and use these to generate zero or more {@link Context} instances. 
	 */
	public final Collection<Context> generateContexts(Context initialContext) {
		Collection<Context> result = new ArrayList<Context>();
		Object value = initialContext.get(getContextPropertyName());
		if ( value!=null && !(value instanceof String && StringUtils.isBlank((String)value)) ) {
			addMappedContextProperties(initialContext, value);
			result.add(initialContext);
		} else {
			for ( Map.Entry<Object, Context> entry : getDefaultValuesWithMappedContextProperties(initialContext).entrySet() ) {
				Context newContext = new Context(initialContext);
				newContext.putAll(entry.getValue());
				newContext.put(getContextPropertyName(), entry.getKey());
				newContext.put(isContextGeneratedFromDefaultValuesKey, true);
				result.add(newContext);
			}
		}
		return result;
	}
	
	/**
	 * If the given {@link Context} was not generated by ourselves (indicated by the
	 * {@link #isContextGeneratedFromDefaultValuesKey} context property), add any
	 * mapped context properties.
	 */
	public void updateContext(Context initialContext) {
		if ( !initialContext.containsKey(isContextGeneratedFromDefaultValuesKey) ) {
			addMappedContextProperties(initialContext, initialContext.get(getContextPropertyName()));
		}
	}
	
	/**
	 * Get the 'useForDefaultValueGeneration' flag, indicating whether this instance should
	 * be used for generating default context property values 
	 * @return
	 */
	public boolean isUseForDefaultValueGeneration() {
		return useForDefaultValueGeneration;
	}

	/**
	 * Set the 'useForDefaultValueGeneration' flag, indicating whether this instance should
	 * be used for generating default context property values 
	 * @param useForDefaultValueGeneration
	 */
	public void setUseForDefaultValueGeneration(boolean useForDefaultValueGeneration) {
		this.useForDefaultValueGeneration = useForDefaultValueGeneration;
	}

	/**
	 * Get the context property name for which this instance can generate default
	 * values and mapped context properties.
	 * @return
	 */
	public String getContextPropertyName() {
		return contextPropertyName;
	}

	/**
	 * Set the context property name for which this instance can generate default
	 * values and mapped context properties.
	 * @param contextPropertyName
	 */
	public void setContextPropertyName(String contextPropertyName) {
		this.contextPropertyName = contextPropertyName;
	}
	
	/**
	 * p>Get the default values for the configured context property name,
	 * together with all mapped context properties for each default value.</p>
	 * 
	 * <p>The {@link #addMappedContextProperties(Context, Object)} will not
	 * be called for contexts returned by this method. Implementations are 
	 * responsible for generating a new {@link Context} instance for every
	 * returned default value, containing any mapped context properties.<p>
	 * 
	 * <p>This allows implementations to load all necessary data for both
	 * generating default values and the corresponding mapped context 
	 * properties as a single operation, instead of first generating all
	 * default values, and then later on iterating over the list of default
	 * values and adding mapped context properties for each of them.</p>
	 * 
	 * @param initialContext
	 * @return
	 */
	protected abstract Map<Object, Context> getDefaultValuesWithMappedContextProperties(Context initialContext);

	/**
	 * Add mapped context properties for the given value for the configured context
	 * property name to the given context.
	 * 
	 * @param context
	 * @param contextPropertyValue
	 */
	protected abstract void addMappedContextProperties(Context context, Object contextPropertyValue);
}
