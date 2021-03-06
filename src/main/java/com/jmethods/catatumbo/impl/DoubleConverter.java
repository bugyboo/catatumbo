/*
 * Copyright 2016 Sai Pullabhotla.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jmethods.catatumbo.impl;

import com.google.cloud.datastore.DoubleValue;
import com.google.cloud.datastore.Value;
import com.google.cloud.datastore.ValueBuilder;

/**
 * An implementation of the {@link PropertyConverter} interface for dealing with
 * primitive double and wrapper Double types.
 * 
 * @author Sai Pullabhotla
 *
 */
public class DoubleConverter extends AbstractConverter {

	/**
	 * Singleton instance
	 */
	private static final DoubleConverter INSTANCE = new DoubleConverter();

	/**
	 * Creates a new instance of <code>DoubleConverter</code>.
	 */
	private DoubleConverter() {
		// Do nothing
	}

	@Override
	public ValueBuilder<?, ?, ?> toValueBuilder(Object input) {
		return DoubleValue.builder((double) input);
	}

	@Override
	public Object toObject(Value<?> input) {
		return input.get();
	}

	/**
	 * Returns the singleton instance of this class.
	 * 
	 * @return the singleton instance of this class.
	 */
	public static DoubleConverter getInstance() {
		return INSTANCE;
	}

}
