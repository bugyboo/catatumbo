/*
 * Copyright 2016 Yasser Al-Harbi.
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

import java.util.HashMap;
import java.util.Map;

import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.EntityValue;
import com.google.cloud.datastore.FullEntity;
import com.google.cloud.datastore.IncompleteKey;
import com.google.cloud.datastore.StringValue;
import com.google.cloud.datastore.Value;
import com.google.cloud.datastore.ValueBuilder;

public class MapConverter extends AbstractConverter {
	
	/**
	 * Singleton instance
	 */
	private static final MapConverter INSTANCE = new MapConverter();
	
	/**
	 * Creates a new instance of <code>MapConverter</code>.
	 */	
	private MapConverter() {
		// Hide the constructor
	}

	@Override
	public ValueBuilder<?, ?, ?> toValueBuilder(Object obj) {

		Map<String,String> map = (Map<String,String>) obj;
		
		FullEntity.Builder<IncompleteKey> builder = Entity.builder();		
		for (String property : map.keySet()) {
			builder.set(property, StringValue.builder(map.get(property)).build()).build();
		}
		FullEntity<IncompleteKey> embedded = builder.build();
		
		return EntityValue.builder(embedded);
	}

	@Override
	public Object toObject(Value<?> input) {
		EntityValue entityValue = (EntityValue) input;
		FullEntity<?> embedded = entityValue.get();		
		Map<String, String> map = new HashMap<>(); 
		for ( String property : embedded.names() ) {
			map.put(property, embedded.getString(property));
		}
		
		return map;
	}
	
	/**
	 * Returns the singleton instance of <code>ListConverter</code>.
	 * 
	 * @return the singleton instance of <code>ListConverter</code>.
	 */
	public static MapConverter getInstance() {
		return INSTANCE;
	}	

}
